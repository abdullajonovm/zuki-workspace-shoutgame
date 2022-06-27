package com.meinc.store.processor;

import static com.meinc.store.domain.Receipt.ReceiptType.CREDIT_CARD;
import static com.meinc.store.domain.Receipt.ReceiptType.GPLAY_RECURRING;
import static org.springframework.transaction.annotation.Propagation.NESTED;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.meinc.mrsoa.distdata.visor.DistributedVisor;
import com.meinc.store.dao.StoreServiceDaoSqlMap;
import com.meinc.store.domain.Item;
import com.meinc.store.domain.Receipt;
import com.meinc.store.domain.Receipt.ReceiptType;
import com.meinc.store.domain.ReceiptResult;
import com.meinc.store.domain.SubscriberEntitlement;

public class EntitlementVerifyDaemon implements Runnable {
//    private static final Log _log = LogFactory.getLog(EntitlementVerifyDaemon.class);

    /**
     * There are two styles of subscription receipts, renewable and
     * non-renewable. Renewable receipts may have their expiration dates
     * changed as future payments occur, whereas non-renewable receipts may
     * not.
     */
    private static final Set<ReceiptType> _renewableReceiptTypes = new HashSet<ReceiptType>(
                    Arrays.asList(new ReceiptType[] { CREDIT_CARD, GPLAY_RECURRING }) );

    private StoreServiceDaoSqlMap _dao;

    public static void doConfig() {
    }

    @Override
    @Transactional(propagation=NESTED)
    public void run() {
        if (!DistributedVisor.tryClusterLock("EntitlementVerifyDaemonLock"))
            return;
        try {
            List<SubscriberEntitlement> expiredList = _dao.getExpiredSubscriberEntitlements();
            Map<Integer,Receipt> storeReceiptCache = new HashMap<Integer,Receipt>();
            for (SubscriberEntitlement entitlement : expiredList) {
                Receipt receipt = entitlement.getReceipt();

                if (receipt.isSkipVerify()) {
                    //_log.warn("Receipt is marked as skip_verify - skipping: " + receipt.isSkipVerify());
                    continue;
                }

                Item item = _dao.getItemByUuid(receipt.getItemUuid());

                if (!_renewableReceiptTypes.contains(receipt.getType())) {
                    _dao.removeSubscriberEntitlement(entitlement.getSubscriberEntitlementId());
                } else {
                    Receipt storeReceipt;
                    int receiptId = receipt.getReceiptId();
                    if (storeReceiptCache.containsKey(receiptId))
                        storeReceipt = storeReceiptCache.get(receiptId);
                    else {
                        ReceiptResult verifyResult = PaymentProcessor.globalVerifyReceipt(receipt, item.getStoreBundleId());
                        storeReceipt = verifyResult.getStoreReceipt();
                        if (storeReceipt == null)
                            continue;
                        else {
                            storeReceipt.setReceiptId(0);
                            storeReceipt.setUuid(UUID.randomUUID().toString());
                            _dao.addReceipt(storeReceipt);
                            storeReceiptCache.put(receiptId, storeReceipt);
                        }
                    }

                    if (storeReceipt != null) {
                        if (storeReceipt.getExpirationDate() == null ||
                                receipt.getExpirationDate().compareTo(storeReceipt.getExpirationDate()) != 0) {
                            SubscriberEntitlement newEntitlement = entitlement.clone();
                            newEntitlement.setSubscriberEntitlementId(0);
                            newEntitlement.setReceipt(storeReceipt);
                            _dao.removeSubscriberEntitlement(entitlement.getSubscriberEntitlementId());
                            _dao.addSubscriberEntitlement(newEntitlement);
                        }
                    } else
                        _dao.removeSubscriberEntitlement(entitlement.getSubscriberEntitlementId());
                }
            }
        } finally {
            DistributedVisor.releaseClusterLock("EntitlementVerifyDaemonLock");
        }
    }

    public void setDao(StoreServiceDaoSqlMap dao) {
        _dao = dao;
    }
}
