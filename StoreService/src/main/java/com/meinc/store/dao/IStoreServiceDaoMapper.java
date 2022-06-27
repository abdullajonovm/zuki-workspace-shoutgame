package com.meinc.store.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.meinc.store.domain.Entitlement;
import com.meinc.store.domain.Item;
import com.meinc.store.domain.Receipt;
import com.meinc.store.domain.ReceiptItem;
import com.meinc.store.domain.SubscriberEntitlement;

public interface IStoreServiceDaoMapper
{
    public List<Receipt> getReceiptsBySubscriberId(long subscriberId);

    public Receipt getReceiptByStoreUid(@Param("receiptType") String receiptType, @Param("storeUid") String storeUid);

    public Receipt getReceiptByUuid(String receiptUuid);

    public void addReceipt(Receipt receipt);

    public void updateReceipt(Receipt receipt);

    public void addSubscriberEntitlement(SubscriberEntitlement entitlement);

    public void addSubscriberEntitlementWithContext(SubscriberEntitlement entitlement);

    public void addSubscriberEntitlementsWithContext(List<SubscriberEntitlement> entitlements);

    public Item getItemById(int itemId);

    public Item getItemByUuid(String itemUuid);

    public List<Item> getAllActiveItems();

    public List<Entitlement> getEntitlements();

    public List<String> getEntitlementUuids();

    public List<String> getItemUuids();

    public List<Integer> getItemIds();

    public Entitlement getEntitlement(String entitlementUuid);

    public Entitlement getEntitlementById(int entitlementId);

    public List<Entitlement> getEntitlementsForItem(int itemId);

    public List<Item> getItemsForEntitlement(int entitlementId);

    public List<SubscriberEntitlement> getCurrentEntitlementsForSubscriber(long subscriberId);

    public List<SubscriberEntitlement> getUnreservedEntitlementsForSubscriber(long subscriberId);

    public List<SubscriberEntitlement> getUnreservedEntitlementsForSubscribers(List<Long> subscriberIds);

    public List<SubscriberEntitlement> getExpiredEntitlementsForSubscriber(long subscriberId);

    public List<SubscriberEntitlement> getSubscriberEntitlementConsumed(@Param("subscriberId") long subscriberId, @Param("entitlementUuid") String entitlementUuid);

    public void removeSubscriberEntitlement(int subscriberEntitlementId);

    public List<SubscriberEntitlement> getExpiredSubscriberEntitlements();

//    public List<String> getMatchingCouponCodes(@Param("codes") Set<String> codes);

//    public void addCouponCodesForItem(@Param("itemUuid") String itemUuid, @Param("codeUuids") Set<String> newCodes);

//    public CouponCode getCouponByCode(String couponCode);

//    public void markCouponRedeemed(int couponId);

    public SubscriberEntitlement getSubscriberEntitlement(String subscriberEntitlementUuid);

    public void reserveSubscriberEntitlement(int subscriberEntitlementId);

    public void unreserveSubscriberEntitlement(String subscriberEntitlementUuid);

    public void consumeSubscriberEntitlement(int subscriberEntitlementId);

    public void restoreSubscriberEntitlement(String subscriberEntitlementUuid);

    public List<ReceiptItem> getReceiptsBySubscriberIdFromDate(@Param("subscriberId") long subscriberId, @Param("fromDate") Date fromDate);

    public ReceiptItem getReceiptItemForReceiptId(@Param("receiptId") int receiptId);

    //external customer profile mapping
    void insertCustomerProfileMapping(@Param("subscriberId") long subscriberId, @Param("customerProfileId") String customerProfileId);
    String getCustomerProfileMapping(long subscriberId);
    void deleteCustomerProfileMapping(long subscriberId);

    public void setBraintreeCustomerId(@Param("subscriberId") long subscriberId, @Param("customerId") String customerId);

    public String getBraintreeCustomerId(long subscriberId);

    public void removeBraintreeCustomerId(long subscriberId);
}
