package com.meinc.store.processor;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meinc.store.dao.StoreServiceDaoSqlMap;
import com.meinc.store.domain.CouponCode;
import com.meinc.store.domain.CreditCardInfo;
import com.meinc.store.domain.Item;
import com.meinc.store.domain.Receipt;
import com.meinc.store.domain.Receipt.ReceiptType;
import com.meinc.store.domain.ReceiptResult;
import com.meinc.store.exception.StoreException;

public class CouponProcessor extends PaymentProcessor {
    private static final Log logger = LogFactory.getLog(CouponProcessor.class);

    @Override
    protected List<ReceiptType> getTypes() {
        return Arrays.asList(new ReceiptType[] {ReceiptType.COUPON});
    }
    
    @Override
    protected Receipt purchaseItem(int subscriberId, String customerId, Item item, CreditCardInfo ccInfo) {
        throw new UnsupportedOperationException();
    }
    
    public ReceiptResult verifyReceipt(Receipt receipt) {
        ReceiptResult result = new ReceiptResult();
        
        byte[] couponCodeBytes = receipt.getPayload();
        if (couponCodeBytes == null || couponCodeBytes.length == 0) {
            result.setNoRetryError("coupon receipt missing coupon code in payload");
            return result;
        }
        String couponCode = new String(couponCodeBytes);
        
        CouponCode coupon = _dao.getCouponByCode(couponCode);
        if (coupon == null || coupon.getItemUuid() == null) {
            logger.warn("no such coupon code: " + couponCode);
            result.setNoRetryError("no such coupon code");
            return result;
        }
        if (coupon.getRedeemDate() != null) {
            logger.warn("coupon code already redeemed: " + couponCode);
            result.setNoRetryError("coupon code already redeemed");
            return result;
        }
        if (coupon.getCancelDate() != null) {
            logger.warn("coupon code is canceled: " + couponCode);
            result.setNoRetryError("coupon code is canceled");
            return result;
        }
        
        String itemUuid = coupon.getItemUuid();
        Item item = _dao.getItemByUuid(itemUuid);
        if (item == null) {
            logger.warn("no item for coupon code: " + couponCode);
            result.setNoRetryError("no item for coupon code");
            return result;
        }

        receipt.setItemUuid(itemUuid);
        if (subscriberOwnsReceiptItem(receipt)) {
            result.setDuplicateItemError(true);
            return result;
        }
        
        _dao.markCouponRedeemed(coupon.getCouponId());
        
        Receipt storeReceipt = receipt.clone();
        
        storeReceipt.setItemUuid(itemUuid);
        
        Integer itemDurationQuantity = item.getDurationQuantity();
        if (itemDurationQuantity == null) {
            storeReceipt.setExpirationDate(null);
        } else {
            Calendar now = Calendar.getInstance();
            now.add(item.getDurationUnit().toCalendarUnit(), itemDurationQuantity);
            storeReceipt.setExpirationDate(now.getTime());
        }
        
        logReceiptVerified(storeReceipt, couponCode);

        result.setStoreReceipt(storeReceipt);
        return result;
    }
    
    @Override
    public void updateSubscriptionPayment(Receipt receipt, CreditCardInfo ccInfo) throws StoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void cancelSubscription(Receipt receipt) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected CreditCardInfo getReceiptPaymentInfo(Receipt receipt) throws StoreException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected CreditCardInfo getSubscriptionPaymentInfo(Receipt receipt) throws StoreException {
        throw new UnsupportedOperationException();
    }
}
