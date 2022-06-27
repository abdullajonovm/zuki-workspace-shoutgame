package com.meinc.store.service;

import static org.springframework.transaction.annotation.Propagation.NESTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.mrsoa.distdata.simple.DistributedMap;
import com.meinc.mrsoa.service.annotation.OnStart;
import com.meinc.mrsoa.service.annotation.OnStop;
import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;
import com.meinc.store.dao.StoreServiceDaoSqlMap;
import com.meinc.store.domain.CreditCardInfo;
import com.meinc.store.domain.CustomerProfile;
import com.meinc.store.domain.Entitlement;
import com.meinc.store.domain.Item;
import com.meinc.store.domain.Receipt;
import com.meinc.store.domain.Receipt.ReceiptType;
import com.meinc.store.domain.Receipt.SubscriptionState;
import com.meinc.store.domain.ReceiptItem;
import com.meinc.store.domain.ReceiptResult;
import com.meinc.store.domain.SubscriberEntitlement;
import com.meinc.store.exception.AlreadyConsumedException;
import com.meinc.store.exception.AlreadyReservedException;
import com.meinc.store.exception.InvalidItemException;
import com.meinc.store.exception.InvalidReceiptException;
import com.meinc.store.exception.NoSuchCreditCardException;
import com.meinc.store.exception.StoreException;
import com.meinc.store.processor.AuthorizeDotNetProcessor;
import com.meinc.store.processor.BraintreeProcessor;
import com.meinc.store.processor.BraintreeProcessor2;
import com.meinc.store.processor.EntitlementVerifyDaemon;
import com.meinc.store.processor.PaymentProcessor;

@Service(namespace = StoreService.MEINC_NAMESPACE, name = StoreService.SERVICE_NAME, interfaces = StoreService.NOTIFICATION_INTERFACE, version = StoreService.SERVICE_VERSION, exposeAs = IStoreService.class)
public class StoreService implements IStoreService
{
    public static final String MEINC_NAMESPACE = "meinc-service";
    public static final String SERVICE_NAME = "StoreService";
    public static final String NOTIFICATION_INTERFACE = "IStoreService";
    public static final String SERVICE_VERSION = "1.0";

//    private static final int COUPON_STRING_LENGTH = 8;

    private static Logger _logger = Logger.getLogger(StoreService.class);
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static Pattern purchaseInfoPattern = Pattern.compile("'purchase-info'/s*=/s*'([^']+)'".replaceAll("'", "\"").replaceAll("/", "\\\\"));
    private static Pattern transactionIdPattern = Pattern.compile("'transaction-id'/s*=/s*'([^']+)'".replaceAll("'", "\"").replaceAll("/", "\\\\"));
//    private static Pattern certificateIdPattern = Pattern.compile("CertificateId='([^']+)'".replaceAll("'", "\""));

    private ReceiptType _ccProcessorType;
    private StoreServiceDaoSqlMap _dao;
    private BraintreeProcessor _braintreeProcessor;
    private BraintreeProcessor2 _braintreeProcessor2;
    private AuthorizeDotNetProcessor _authorizeDotNetProcessor;
    private PlatformTransactionManager _transactionManager;
//    private EntitlementMapStore _entitlementMapStore;
//    private ItemMapStore _itemMapStore;
//    private ItemToEntitlementMapStore _itemToEntitlementMapStore;
//    private EntitlementToItemMapStore _entitlementToItemMapStore;

    private DistributedMap<String, Item> _itemByUuid;
    private DistributedMap<Integer, Item> _itemByItemId;
    private DistributedMap<String, Entitlement> _entitlementByUuid;

    private final byte[] COUPON_VALID_BYTES;

    public StoreService()
    {
        // Valid characters include numbers and lower case letters avoiding '0', 'o', 'l', and '1'
        byte[][] ranges = { {'2', '9'},
                            {'a', 'k'},
                            {'m', 'n'},
                            {'p', 'z'} };
        int length = 0;
        for (int i = 0; i < ranges.length; i++)
            length += ranges[i][1] - ranges[i][0] + 1;

        COUPON_VALID_BYTES = new byte[length];
        int couponCharsIndex = 0;
        for (int i = 0; i < ranges.length; i++)
            for (byte b = ranges[i][0]; b <= ranges[i][1]; b++)
                COUPON_VALID_BYTES[couponCharsIndex++] = b;
    }

    public void setCcProcessor(String ccProcessor)
    {
        switch (ccProcessor)
        {
            case "CREDIT_CARD":
                _ccProcessorType = Receipt.ReceiptType.CREDIT_CARD;
                break;

            case "AUTHNET_CREDIT_CARD":
                _ccProcessorType = Receipt.ReceiptType.AUTHNET_CREDIT_CARD;
                break;

            case "BRAINTREE_CREDIT_CARD":
                _ccProcessorType = Receipt.ReceiptType.BRAINTREE_CREDIT_CARD;
                break;

            default:
                throw new IllegalArgumentException("unsupported value for store.cc.processor. must be CREDIT_CARD, AUTHNET_CREDIT_CARD, or BRAINTREE_CREDIT_CARD");
        }
    }

    public void setDao(StoreServiceDaoSqlMap dao) {
        _dao = dao;
    }

    public void setBraintreeProcessor(BraintreeProcessor braintreeProcessor) {
        _braintreeProcessor = braintreeProcessor;
    }

    public void setBraintreeProcessor2(BraintreeProcessor2 braintreeProcessor2) {
        _braintreeProcessor2 = braintreeProcessor2;
    }

    public void setAuthorizeDotNetProcessor(AuthorizeDotNetProcessor processor)
    {
        _authorizeDotNetProcessor = processor;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        _transactionManager = transactionManager;
    }

//    public void setEntitlementMapStore(EntitlementMapStore entitlementMapStore) {
//        _entitlementMapStore = entitlementMapStore;
//    }
//
//    public void setItemMapStore(ItemMapStore itemMapStore) {
//        _itemMapStore = itemMapStore;
//    }
//
//    public void setItemToEntitlementMapStore(ItemToEntitlementMapStore itemToEntitlementMapStore) {
//        _itemToEntitlementMapStore = itemToEntitlementMapStore;
//    }
//
//    public void setEntitlementToItemMapStore(EntitlementToItemMapStore entitlementToItemMapStore) {
//        _entitlementToItemMapStore = entitlementToItemMapStore;
//    }
//
//    private <K,V> void createMapConfig(Map<String,MapConfig> configs, String name, MapStore<K,V> storeObject) {
//        MapConfig mapConfig = configs.get(name);
//        if (mapConfig == null) {
//            mapConfig = new MapConfig();
//            configs.put(name, mapConfig);
//        }
//        MapStoreConfig storeConfig = new MapStoreConfig();
//        storeConfig.setEnabled(true);
//        storeConfig.setImplementation(storeObject);
//        storeConfig.setWriteDelaySeconds(0);
//        mapConfig.setMapStoreConfig(storeConfig);
//    }

    @Override
    @OnStart
    @ServiceMethod
    @Transactional(propagation=NESTED, readOnly=false)
    public void start() {
        _logger.info("StoreService loading");

        EntitlementVerifyDaemon.doConfig();

        _entitlementByUuid = DistributedMap.getMap("entitlementByUuid");

        _itemByUuid = DistributedMap.getMap("storeItemByUuid");
        _itemByItemId = DistributedMap.getMap("storeItemByItemId");

        resetItemByUuidMap();
        resetEntitlementByUuidMap();

        _braintreeProcessor.start();
        _authorizeDotNetProcessor.start();
        _braintreeProcessor2.start();

        _logger.info("StoreService loading finished");
    }

    private void resetItemByUuidMap() {
        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txDef.setReadOnly(false);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        List<String> ids;
        try {
            ids = _dao.getItemUuids();
            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null)
                _transactionManager.rollback(txStatus);
        }
        for (String itemUuid : ids) {
            Item item = _dao.getItemByUuid(itemUuid);
            if (item != null) {
                _itemByUuid.put(itemUuid, item);
                _itemByItemId.put(item.getItemId(), item);
            }
        }
    }

    private void resetEntitlementByUuidMap() {
        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txDef.setReadOnly(false);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        List<String> ids;
        try {
            ids = _dao.getEntitlementUuids();
            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null)
                _transactionManager.rollback(txStatus);
        }
        for (String entitlementUuid : ids) {
            Entitlement entitlement = _dao.getEntitlement(entitlementUuid);
            if (entitlement != null) {
                _entitlementByUuid.put(entitlementUuid, entitlement);
            }
        }
    }

    @Override
    @OnStop
    @ServiceMethod
    public void stop() {
        _braintreeProcessor.stop();
        _braintreeProcessor2.stop();
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=REQUIRES_NEW)  //once a purchase is recorded there's no taking it back
    public Receipt purchaseItemViaCreditCard(long subscriberId, String customerId, String itemUuid, CreditCardInfo ccInfo)
    throws InvalidItemException, StoreException, NoSuchCreditCardException {
        _logger.debug(String.format("Subscriber %d is purchasing item %s", subscriberId, itemUuid));

        Item item = _dao.getItemByUuid(itemUuid);
        if (item == null)
            throw new InvalidItemException();

        if (!ccInfo.isComplete())
            throw new StoreException("incomplete credit card information");

        Receipt receipt = PaymentProcessor.globalPurchaseItem(_ccProcessorType, subscriberId, customerId, item, ccInfo);
        if (receipt == null) {
            String msg = String.format("Purchase for subscriber %d failed for item %s: unknown error", subscriberId, itemUuid);
            _logger.warn(msg);
            throw new StoreException("unknown error");
        }

        _dao.addReceipt(receipt);

        List<Entitlement> entitlements = _dao.getEntitlementsForItem(item.getItemId());
        for (Entitlement e : entitlements) {
            SubscriberEntitlement entitlement =
                            new SubscriberEntitlement(UUID.randomUUID().toString(), subscriberId, e.getEntitlementId(), e.getUuid(), 0);
            entitlement.setReceipt(receipt);
            _dao.addSubscriberEntitlement(entitlement);
        }

        return receipt;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=REQUIRES_NEW)
    public Receipt purchaseItemViaNonce(long subscriberId, String customerId, String itemUuid, String nonce, String firstname, String lastname)
    throws InvalidItemException, StoreException, NoSuchCreditCardException{
        _logger.debug(String.format("Subscriber %d is purchasing item %s", subscriberId, itemUuid));

        Item item = _dao.getItemByUuid(itemUuid);
        if (item == null)
            throw new InvalidItemException();

        if (nonce == null || nonce.trim().length() == 0)
            throw new StoreException("nonce is empty");

        Receipt receipt = PaymentProcessor.globalPurchaseItem(_ccProcessorType, subscriberId, customerId, item, nonce, firstname, lastname);
        if (receipt == null) {
            String msg = String.format("Purchase for subscriber %d failed for item %s: unknown error", subscriberId, itemUuid);
            _logger.warn(msg);
            throw new StoreException("unknown error");
        }

        _dao.addReceipt(receipt);

        List<Entitlement> entitlements = _dao.getEntitlementsForItem(item.getItemId());
        for (Entitlement e : entitlements) {
            SubscriberEntitlement entitlement =
                            new SubscriberEntitlement(UUID.randomUUID().toString(), subscriberId, e.getEntitlementId(), e.getUuid(), 0);
            entitlement.setReceipt(receipt);
            _dao.addSubscriberEntitlement(entitlement);
        }

        return receipt;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=REQUIRES_NEW)
    public Receipt purchaseViaCustomerProfile(long subscriberId, String itemUuid, String customerProfileCreditCardInfoExternalRefId)
    throws InvalidItemException, StoreException
    {
        _logger.debug(String.format("Subscriber %d is purchasing item %s", subscriberId, itemUuid));

        Item item = _dao.getItemByUuid(itemUuid);
        if (item == null) {
            throw new InvalidItemException();
        }

        Receipt receipt = PaymentProcessor.globalPurchaseItemViaCustomerProfile(_ccProcessorType, subscriberId, item, customerProfileCreditCardInfoExternalRefId);
        if (receipt == null) {
            String msg = String.format("Purchase for subscriber %d failed for item %s: unknown error", subscriberId, itemUuid);
            _logger.warn(msg);
            throw new StoreException("unknown error");
        }

        _dao.addReceipt(receipt);

        List<Entitlement> entitlements = _dao.getEntitlementsForItem(item.getItemId());
        for (Entitlement e : entitlements) {
            SubscriberEntitlement entitlement =
                            new SubscriberEntitlement(UUID.randomUUID().toString(), subscriberId, e.getEntitlementId(), e.getUuid(), 0);
            entitlement.setReceipt(receipt);
            _dao.addSubscriberEntitlement(entitlement);
        }

        return receipt;
    }

    @Override
    @ServiceMethod
    public CreditCardInfo getDefaultPaymentInfo(long subscriberId)
    throws StoreException, NoSuchCreditCardException {
        return PaymentProcessor.globalGetDefaultPaymentInfo(_ccProcessorType, subscriberId, null);
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public boolean auditReceipts(long subscriberId, List<Receipt> receipts)
    throws InvalidItemException {
        //_logger.info("*** store::auditReceipts");
        boolean success = true;
        for (Receipt receipt : receipts) {
            Receipt existingReceipt = null;
            String storeUid = getStoreUidFromReceipt(receipt);
            if (storeUid != null && storeUid.trim().length() > 0) {
                existingReceipt = _dao.getReceiptByStoreUid(receipt.getType().name(), storeUid);
            }
            else {
                _logger.info(String.format("Receipt type is unauditable: %s", receipt));
                continue;
            }

            if (existingReceipt == null) {
                _logger.info(String.format("Adding missing audit receipt for subscriber %d: %s", subscriberId, receipt));
                ReceiptResult result = addReceipt(receipt);
                if (result.getStoreReceipt() == null) {
                    _logger.warn(String.format("Error adding receipt during audit '%s'/'%s' for receipt: %s", result.getNoRetryError(), result.getRetryError(), receipt));
                    success = false;
                    continue;
                }
            }
        }
        return success;
    }

    static String getStoreUidFromReceipt(Receipt receipt) {
        try {
            Map<String,Object> payloadJson;
            try {
                payloadJson = jsonMapper.readValue(receipt.getPayload(), new TypeReference<Map<String,Object>>() { });
            } catch (Exception e) {
                _logger.warn("Error parsing receipt: " + e.getMessage(), e);
                return null;
            }

            ReceiptType receiptType = receipt.getType();
            switch (receiptType) {
            case GPLAY_ONETIME:
                @SuppressWarnings("unchecked")
                Map<String,String> receiptJson = (Map<String,String>) payloadJson.get("receipt");
                if (receiptJson == null) {
                    _logger.warn("Error parsing google client receipt - missing 'receipt' value: " + new String(receipt.getPayload()));
                    return null;
                }
                String orderId = receiptJson.get("orderId");
                if (orderId == null) {
                    _logger.warn("Error parsing google client receipt - missing 'orderId' value: " + new String(receipt.getPayload()));
                    return null;
                }
                return orderId;
            case GPLAY_RECURRING:
                String subscriptionId = (String) payloadJson.get("token");
                //_logger.info("*** parsing gplay_recurring, subscriptionId: " + subscriptionId);
                if (subscriptionId == null) {
                    _logger.warn("Error parsing google client receipt - missing 'subscriptionId' value: " + new String(receipt.getPayload()));
                    return null;
                }
                return subscriptionId;
            case ITUNES:
                String encodedPurchaseReceipt = (String) payloadJson.get("receipt");
                if (encodedPurchaseReceipt == null) {
                    _logger.warn("Error parsing iTunes receipt - missing 'receipt' value: " + new String(receipt.getPayload()));
                    return null;
                }
                byte[] purchaseReceiptBytes = Base64.decodeBase64(encodedPurchaseReceipt);
                if (purchaseReceiptBytes == null) {
                    _logger.warn("Error parsing iTunes receipt - invalid base-64 encoding: " + new String(receipt.getPayload()));
                    return null;
                }
                String purchaseReceipt = new String(purchaseReceiptBytes);
                Matcher m = purchaseInfoPattern.matcher(purchaseReceipt);
                if (!m.find()) {
                    _logger.warn("Error parsing iTunes receipt - missing 'purchase-info' value: " + new String(receipt.getPayload()));
                    return null;
                }
                String encodedPurchaseInfo = m.group(1);
                byte[] purchaseInfoBytes = Base64.decodeBase64(encodedPurchaseInfo);
                if (purchaseInfoBytes == null) {
                    _logger.warn("Error parsing iTunes receipt - invalid 'purchase-info' base-64 encoding: " + new String(receipt.getPayload()));
                    return null;
                }
                String purchaseInfo = new String(purchaseInfoBytes);
                Matcher m2 = transactionIdPattern.matcher(purchaseInfo);
                if (!m2.find()) {
                    _logger.warn("Error parsing iTunes receipt - missing 'transaction-id' value: " + new String(receipt.getPayload()));
                    return null;
                }
                String transactionId = m2.group(1);
                if (transactionId == null) {
                    _logger.warn("Error parsing iTunes receipt - missing 'transaction-id' value: " + new String(receipt.getPayload()));
                    return null;
                }
                return transactionId;
            case WIN_STORE:
                /*encodedPurchaseReceipt = (String) payloadJson.get("receipt");
                if (encodedPurchaseReceipt == null) {
                    _logger.warn("Error parsing Windows Store receipt - missing 'receipt' value: " + new String(receipt.getPayload()));
                    return null;
                }
                purchaseReceiptBytes = Base64.decodeBase64(encodedPurchaseReceipt);
                if (purchaseReceiptBytes == null) {
                    _logger.warn("Error parsing Windows Store receipt - invalid base-64 encoding: " + new String(receipt.getPayload()));
                    return null;
                }
                purchaseReceipt = new String(purchaseReceiptBytes);
                m = certificateIdPattern.matcher(purchaseReceipt);
                if (!m.find()) {
                    _logger.warn("Error parsing Windows Store receipt - missing 'CertificateId' value: " + new String(receipt.getPayload()));
                    return null;
                }
                String certificateId = m.group(1);
                return certificateId;*/
                return null;
            default:
                _logger.info(String.format("Receipt type does not have a unique storeUid: %s", receipt));
                return null;
            }
        }
        catch (RuntimeException t) {
            _logger.warn(String.format("exception while trying to get store UID of receipt %s", receipt.toString()), t);
            throw t;
        }
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public ReceiptResult addReceipt(Receipt receipt)
    throws InvalidItemException/*, InvalidReceiptException, StoreException*/
    {
        if (receipt.getStoreUid() == null) {
            receipt.setStoreUid(getStoreUidFromReceipt(receipt));
        }
        _logger.info("Adding receipt: " + receipt);

        Item item = _dao.getItemByUuid(receipt.getItemUuid());
        if (item == null) {
            throw new InvalidItemException(String.format("invalid store item '%s'", receipt.getItemUuid()));
        }

        // check for existing receipt first
        String storeUid = receipt.getStoreUid();
        ReceiptResult verifyResult;
        if (storeUid != null && storeUid.trim().length() > 0) {
            Receipt existing = _dao.getReceiptByStoreUid(receipt.getType().name(), storeUid);
            if (existing != null) {
                verifyResult = new ReceiptResult();
                verifyResult.setStoreReceipt(existing);
                verifyResult.setDuplicateReceiptError(true);
                _logger.warn(String.format("Receipt %s is a duplicate receipt of %s", receipt.toString(), existing.toString()));
                return verifyResult;
            }
            else {
                _logger.info(String.format("no receipt found for storeUid %s", storeUid));
            }
        }
        else {
            _logger.info(String.format("no storeUid found in receipt %s", receipt.toString()));
        }

        if (receipt.isSkipVerify()) {
            verifyResult = new ReceiptResult();
            verifyResult.setStoreReceipt(receipt.clone());
        } else {
            verifyResult = PaymentProcessor.globalVerifyReceipt(receipt, item.getStoreBundleId());
            if (verifyResult.getStoreReceipt() == null) {
                String msg = "Receipt failed verification: " + verifyResult.toString();
                _logger.warn(msg);
                return verifyResult;
            }
        }

        Receipt storeReceipt = verifyResult.getStoreReceipt();

        // Get the item for the receipt.
        item = _dao.getItemByUuid(storeReceipt.getItemUuid());
        if (item == null)
            throw new InvalidItemException(String.format("invalid store item '%s'", storeReceipt.getItemUuid()));

        if (receipt.isSkipVerify() && item.getDurationQuantity() != null && item.getDurationUnit() != null) {
            Calendar nowCalendar = Calendar.getInstance();
            nowCalendar.add(item.getDurationUnit().toCalendarUnit(), item.getDurationQuantity());
            storeReceipt.setExpirationDate(nowCalendar.getTime());
        }

        if (item.getDurationQuantity() == null ^ storeReceipt.getExpirationDate() == null) {
            String msg = String.format("Receipt expiration mismatch: the item has a relative expiration of %d %s, but the store receipt has an expiration of %s",
                                        item.getDurationQuantity(), item.getDurationUnit().name(), storeReceipt.getExpirationDate());
            _logger.warn(msg);
            verifyResult.setStoreReceipt(null);
            verifyResult.setNoRetryError(msg);
            return verifyResult;
        }

        _dao.addReceipt(storeReceipt);
        List<Entitlement> entitlements = _dao.getEntitlementsForItem(item.getItemId());
        List<SubscriberEntitlement> subEntitlements = new ArrayList<SubscriberEntitlement>(entitlements.size());
        for (Entitlement e : entitlements)
        {
            for (int i=0; i<e.getQuantity(); i++) {
                SubscriberEntitlement subEntitlement =
                                new SubscriberEntitlement(UUID.randomUUID().toString(), storeReceipt.getSubscriberId(), e.getEntitlementId(), e.getUuid(), 0);
                subEntitlement.setReceipt(storeReceipt);
                _dao.addSubscriberEntitlement(subEntitlement);
                subEntitlements.add(subEntitlement);
            }
        }

        verifyResult.setSubscriberEntitlements(subEntitlements);
        return verifyResult;
    }

    @Override
    @ServiceMethod
    public List<Item> getAllActiveItems()
    {
        _logger.debug("Getting all active items");
        if (_itemByUuid.isEmpty())
            resetItemByUuidMap();
        return new ArrayList<Item>(_itemByUuid.values());
    }

    @Override
    @ServiceMethod
    public List<Entitlement> getEntitlements()
    {
        _logger.debug("Getting all entitlements");
        if (_entitlementByUuid.isEmpty())
            resetEntitlementByUuidMap();
        return new ArrayList<Entitlement>(_entitlementByUuid.values());
    }

    @Override
    @ServiceMethod
    public Entitlement getEntitlement(String entitlementUuid)
    {
        Entitlement value = _entitlementByUuid.get(entitlementUuid);
        if (value != null)
            return value;
        else {
            resetEntitlementByUuidMap();
            value = _entitlementByUuid.get(entitlementUuid);
            return (value == null) ? null : value;
        }
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=false)
    public List<Entitlement> getEntitlementsForItem(int itemId)
    {
        _logger.debug("Getting entitlements for item: " + itemId);
        return _dao.getEntitlementsForItem(itemId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=false)
    public List<Entitlement> getEntitlementsForItemRaw(int itemId) {
        return _dao.getEntitlementsForItem(itemId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=false)
    public List<Item> getItemsForEntitlements(String entitlementUuid) {
        Entitlement e = _dao.getEntitlement(entitlementUuid);
        if (e != null) {
            return _dao.getItemsForEntitlement(e.getEntitlementId());
        }
        return null;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=false)
    public List<Item> getItemsForEntitlement(int entitlementId) {
        return _dao.getItemsForEntitlement(entitlementId);
    }

    @Override
    @Transactional(propagation=NESTED,readOnly=false)
    @ServiceMethod
    public List<SubscriberEntitlement> getCurrentEntitlementsForSubscriber(long subscriberId)
    {
        //_logger.debug("Getting current entilements for: " + subscriberId);
        return _dao.getCurrentEntitlementsForSubscriber(subscriberId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public List<SubscriberEntitlement> getUnreservedEntitlementsForSubscriber(long subscriberId) {
        return _dao.getUnreservedEntitlementsForSubscriber(subscriberId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public List<SubscriberEntitlement> getUnreservedEntitlementsForSubscribers(List<Long> subscriberIds) {
        return _dao.getUnreservedEntitlementsForSubscribers(subscriberIds);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public List<SubscriberEntitlement> getSubscriberEntitlementConsumed(long subscriberIds, String entitlementUuid) {
        return _dao.getSubscriberEntitlementConsumed(subscriberIds, entitlementUuid);
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public SubscriberEntitlement getSubscriberEntitlement(String subscriberEntitlementUuid)
    {
        return _dao.getSubscriberEntitlement(subscriberEntitlementUuid);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void addSubscriberEntitlements(List<SubscriberEntitlement> entitlements) {
        for (SubscriberEntitlement se : entitlements) {
            _dao.addSubscriberEntitlementWithContext(se);
        }
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void addSubscriberEntitlementsBatch(List<SubscriberEntitlement> entitlements) {
        _dao.addSubscriberEntitlementsWithContext(entitlements);
    }

    @Override
    @Transactional(propagation=NESTED,readOnly=false)
    @ServiceMethod
    public List<SubscriberEntitlement> getExpiredEntitlementsForSubscriber(long subscriberId)
    {
        //_logger.debug("Getting expired entilements for: " + subscriberId);
        return _dao.getExpiredEntitlementsForSubscriber(subscriberId);
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public void removeSubscriberEntitlement(int subscriberEntitlementId)
    {
        _logger.debug("Removing subscriber entitlements for: " + subscriberEntitlementId);
        _dao.removeSubscriberEntitlement(subscriberEntitlementId);
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public void reserveSubscriberEntitlement(String subscriberEntitlementUuid, int contextId)
    throws InvalidItemException, AlreadyReservedException, AlreadyConsumedException
    {
        if (subscriberEntitlementUuid == null)
            throw new InvalidItemException();

        SubscriberEntitlement se = _dao.getSubscriberEntitlement(subscriberEntitlementUuid);
        if (se == null || se.getDeleteDate() != null) {
            throw new InvalidItemException();
        } else if (se.getReservedDate() != null) {
            throw new AlreadyReservedException();
        } else if (se.getConsumedDate() != null) {
            throw new AlreadyConsumedException();
        }

        _dao.reserveSubscriberEntitlement(se.getSubscriberEntitlementId());
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public void unreserveSubscriberEntitlement(String subscriberEntitlementUuid)
    {
        _dao.unreserveSubscriberEntitlement(subscriberEntitlementUuid);
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public void consumeSubscriberEntitlement(String subscriberEntitlementUuid, int contextId)
    throws InvalidItemException, AlreadyConsumedException
    {
        if (subscriberEntitlementUuid == null)
            throw new InvalidItemException();

        SubscriberEntitlement se = _dao.getSubscriberEntitlement(subscriberEntitlementUuid);
        if (se == null || se.getDeleteDate() != null) {
            throw new InvalidItemException();
        } else if (se.getConsumedDate() != null)
            throw new AlreadyConsumedException();

        _dao.consumeSubscriberEntitlement(se.getSubscriberEntitlementId());
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public void restoreSubscriberEntitlement(String subscriberEntitlementUuid) {
        _dao.restoreSubscriberEntitlement(subscriberEntitlementUuid);
    }

    @Override
    @ServiceMethod
    public Item getItemById(int itemId)
    {
        Item result = _itemByItemId.get(itemId);
        if (result != null)
            return result;
        else {
            resetItemByUuidMap();
            result = _itemByItemId.get(itemId);
            return (result == null) ? null : result;
        }
    }

    @Override
    @ServiceMethod
    public Item getItemByUuid(String itemUuid) {
        Item item = _itemByUuid.get(itemUuid);
        if (item == null) {
            resetItemByUuidMap();
            item = _itemByUuid.get(itemUuid);
        }
        return item;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public Item getItemByUuidRaw(String itemUuid) {
        return _dao.getItemByUuid(itemUuid);
    }

/*
    //    private String generateRandomCouponString(Random random) {
    //        while (true) {
    //            byte[] couponCodeBytes = new byte[COUPON_STRING_LENGTH];
    //            for (int i = 0; i < couponCodeBytes.length; i++)
    //                couponCodeBytes[i] = COUPON_VALID_BYTES[random.nextInt(COUPON_VALID_BYTES.length)];
    //
    //            String couponCodeString = new String(couponCodeBytes);
    //
    //            for (int i = 0; i < Hidden.ILLEGAL_CODE_SUBSTRINGS.length; i++)
    //                if (couponCodeString.contains(Hidden.ILLEGAL_CODE_SUBSTRINGS[i]))
    //                    continue;
    //
    //            return couponCodeString;
    //        }
    //    }

    //    @Override
    //    @Transactional(propagation=NESTED)
    //    @ServiceMethod
    //    public List<CouponCode> generateCouponCodesForItem(String itemUuid, int count)
    //    throws InvalidItemException
    //    {
    //        if (itemUuid == null)
    //            throw new IllegalArgumentException("itemUuid may not be null");
    //        if (count < 1)
    //            throw new IllegalArgumentException("count must be >= 1");
    //        if (count > 5000)
    //            throw new IllegalArgumentException("count may not be more than 5000");
    //
    //        Item item = _dao.getItemByUuid(itemUuid);
    //        if (item == null)
    //            throw new InvalidItemException();
    //
    //        Random random = new Random((int) System.currentTimeMillis());
    //
    //        HashSet<String> newCodes = new HashSet<String>();
    //        while (newCodes.size() < count) {
    //            while (newCodes.size() < count)
    //                newCodes.add(generateRandomCouponString(random));
    //
    //            List<String> badCodes = _dao.getMatchingCouponCodes(newCodes);
    //            newCodes.removeAll(badCodes);
    //        }
    //
    //        List<CouponCode> coupons = new ArrayList<CouponCode>();
    //        for (String newCode : newCodes) {
    //            CouponCode couponCode = new CouponCode();
    //            couponCode.setItemUuid(itemUuid);
    //            couponCode.setCode(newCode);
    //            coupons.add(couponCode);
    //        }
    //
    //        //bulk insert into the database
    //        _dao.addCouponCodesForItem(itemUuid, newCodes);
    //
    //        return coupons;
    //    }
    */

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public void removeSubscriberEntitlements(long subscriberId) {
        _logger.info("Removing all entitlements for subscriber " + subscriberId);
        List<SubscriberEntitlement> entitlements = _dao.getCurrentEntitlementsForSubscriber(subscriberId);
        for (SubscriberEntitlement entitlement : entitlements)
            _dao.removeSubscriberEntitlement(entitlement.getSubscriberEntitlementId());
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public Date expireSubscriberEntitlements(long subscriberId, boolean remove) {
        _logger.info("Expiring all entitlements for subscriber " + subscriberId);
        List<SubscriberEntitlement> entitlements = _dao.getCurrentEntitlementsForSubscriber(subscriberId);
        Date now = new Date();
        for (SubscriberEntitlement entitlement : entitlements) {
            Receipt receipt = entitlement.getReceipt();
            if (receipt.getExpirationDate() == null)
                continue;
            if (!now.after(receipt.getExpirationDate())) {
                receipt.setExpirationDate(now);
                _dao.updateReceipt(receipt);
            }
            if (remove)
                _dao.removeSubscriberEntitlement(entitlement.getSubscriberEntitlementId());
        }
        return now;
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public Date changeExpirationOfSubscriberEntitlements(long subscriberId, int changeExpirationHours) {
        _logger.info(String.format("Setting expiration of all entitlements for subscriber %d to %s",
                                   subscriberId, changeExpirationHours));
        List<SubscriberEntitlement> entitlements = _dao.getCurrentEntitlementsForSubscriber(subscriberId);
        Calendar expiration = Calendar.getInstance();
        expiration.add(Calendar.HOUR_OF_DAY, changeExpirationHours);
        Date expires = expiration.getTime();
        for (SubscriberEntitlement entitlement : entitlements) {
            Receipt receipt = entitlement.getReceipt();
            if (receipt.getExpirationDate() == null)
                continue;
            receipt.setExpirationDate(expires);
            _dao.updateReceipt(receipt);
        }
        return expires;
    }

/*
    //    @Override
    //    @Transactional(propagation=NESTED,readOnly=false)
    //    @ServiceMethod
    //    public CouponCode getCoupon(String couponCode) {
    //        return _dao.getCouponByCode(couponCode);
    //    }
*/

    @Override
    @Transactional(propagation=NESTED,readOnly=false)
    @ServiceMethod
    public List<Receipt> getReceiptsBySubscriberId(long subscriberId) {
        return _dao.getReceiptsBySubscriberId(subscriberId);
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public void updateSubscriptionPayment(long subscriberId, String receiptUuid, CreditCardInfo ccInfo)
    throws StoreException, InvalidReceiptException {
        if (_logger.isDebugEnabled())
            _logger.debug("Updating subscription payment for receipt " + receiptUuid);

        Receipt receipt = _dao.getReceiptByUuid(receiptUuid);
        if (receipt == null)
            throw new InvalidReceiptException();
        if (subscriberId != receipt.getSubscriberId())
            throw new InvalidReceiptException("Receipt subscriber does not match");

        PaymentProcessor.globalUpdateSubscriptionPayment(receipt, ccInfo);
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public void updateSubscriptionPaymentViaNonce(int subscriberId, String receiptUuid, String nonce)
    throws StoreException, InvalidReceiptException
    {
        if (_logger.isDebugEnabled()) {
            _logger.debug("Updating subscription payment for receipt " + receiptUuid);
        }
        if (nonce == null || nonce.trim().length() == 0)
            throw new StoreException("nonce is empty");

        Receipt receipt = _dao.getReceiptByUuid(receiptUuid);
        if (receipt == null)
            throw new InvalidReceiptException();
        if (subscriberId != receipt.getSubscriberId())
            throw new InvalidReceiptException("Receipt subscriber does not match");

        PaymentProcessor.globalUpdateSubscriptionPayment(receipt, nonce);
    }

    @Override
    @Transactional(propagation=REQUIRES_NEW)  //once canceled there's no taking it back
    @ServiceMethod
    public void cancelSubscription(long subscriberId, String receiptUuid)
    throws InvalidReceiptException, StoreException {
        if (_logger.isDebugEnabled())
            _logger.debug("Cancelling subscription for receipt " + receiptUuid);

        Receipt receipt = _dao.getReceiptByUuid(receiptUuid);
        if (receipt == null)
            throw new InvalidReceiptException();
        if (subscriberId != receipt.getSubscriberId())
            throw new InvalidReceiptException("Receipt subscriber does not match");

        PaymentProcessor.globalCancelSubscription(receipt);

        receipt.setSubscriptionState(SubscriptionState.CANCELED);
        _dao.updateReceipt(receipt);
    }

    @Override
    @Transactional(propagation=NESTED,readOnly=false)
    @ServiceMethod
    public CreditCardInfo getReceiptPaymentInfo(long subscriberId, String receiptUuid)
    throws InvalidReceiptException, StoreException {
        Receipt receipt = _dao.getReceiptByUuid(receiptUuid);
        if (receipt == null)
            throw new InvalidReceiptException();
        if (subscriberId != receipt.getSubscriberId())
            throw new InvalidReceiptException("Receipt subscriber does not match");

        return PaymentProcessor.globalGetReceiptPaymentInfo(receipt);
    }

    @Override
    @Transactional(propagation=NESTED,readOnly=false)
    @ServiceMethod
    public CreditCardInfo getSubscriptionPaymentInfo(long subscriberId, String receiptUuid)
    throws InvalidReceiptException, StoreException {
        Receipt receipt = _dao.getReceiptByUuid(receiptUuid);
        if (receipt == null)
            throw new InvalidReceiptException();
        if (subscriberId != receipt.getSubscriberId())
            throw new InvalidReceiptException("Receipt subscriber does not match");

        return PaymentProcessor.globalGetSubscriptionPaymentInfo(receipt);
    }

    // Added 6/16/2016 -- to handle Braintree "nonce" style of payment processing
    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED, readOnly=false)
    public String getClientToken(long subscriberId){
        String clientToken = PaymentProcessor.globalGetClientToken(_ccProcessorType, subscriberId);
        return clientToken;
    }

    @Override
    @Transactional(propagation=NESTED,readOnly=false)
    @ServiceMethod
    public List<ReceiptItem> getReceiptsBySubscriberIdFromDate(long subscriberId, Date fromDate){
        return _dao.getReceiptsBySubscriberIdFromDate(subscriberId, fromDate);
    }

    @Override
    @Transactional(propagation=NESTED,readOnly=false)
    @ServiceMethod
    public ReceiptItem getReceiptItemForReceiptId(int receiptId){
        return _dao.getReceiptItemForReceiptId(receiptId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public String createCustomerProfile(long subscriberId, String nonce)
    throws StoreException
    {
        return PaymentProcessor.globalCreateCustomerProfile(_ccProcessorType, subscriberId, nonce);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public CustomerProfile getCustomerProfile(long subscriberId)
    throws StoreException
    {
        return PaymentProcessor.globalGetCustomerProfile(_ccProcessorType, subscriberId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public String addPaymentMethodToCustomerProfile(long subscriberId, String nonce, boolean makeDefault, String firstname, String lastname)
    throws StoreException
    {
        return PaymentProcessor.globalAddPaymentMethodToCustomerProfile(_ccProcessorType, subscriberId, nonce, makeDefault, firstname, lastname);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<CreditCardInfo> getPaymentMethodsForCustomerProfile(long subscriberId) throws StoreException
    {
        return PaymentProcessor.globalGetPaymentMethodsForCustomerProfile(_ccProcessorType, subscriberId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void deletePaymentMethod(String paymentMethodToken) throws StoreException
    {
        PaymentProcessor.globalDeletePaymentMethod(_ccProcessorType, paymentMethodToken);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void deleteCustomerProfile(long subscriberId) throws StoreException
    {
        PaymentProcessor.globalDeleteCustomerProfile(_ccProcessorType, subscriberId);
    }
}












class Hidden {
    static final String[] ILLEGAL_CODE_SUBSTRINGS = {"fuck", "feck", "faack", "faaack", "frak", "shit", "shiit", "shiiit", "urine", "poop", "pooop", "ass", "arse", "pissant", "twat", "donga", "damn", "drat", "crikey", "phwoar", "nigger", "spic", "nigga", "niggga", "sex", "breed", "cunt", "pussy", "pusssy", "slut", "sluut", "cock", "piss", "tits", "titts", "wank", "suck", "bitch", "dyke", "fagg", "jesus", "budda", "muhamm", "whore", "bastard", "blood", "bollock", "bolock"};
}
