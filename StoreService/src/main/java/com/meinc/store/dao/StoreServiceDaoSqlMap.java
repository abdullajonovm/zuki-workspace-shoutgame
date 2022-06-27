package com.meinc.store.dao;

import java.util.Date;
import java.util.List;

import com.meinc.store.domain.Entitlement;
import com.meinc.store.domain.Item;
import com.meinc.store.domain.Receipt;
import com.meinc.store.domain.ReceiptItem;
import com.meinc.store.domain.SubscriberEntitlement;

public class StoreServiceDaoSqlMap
{
    private IStoreServiceDaoMapper _mapper;

    public IStoreServiceDaoMapper getMapper()
    {
        return _mapper;
    }

    public void setMapper(IStoreServiceDaoMapper mapper)
    {
        _mapper = mapper;
    }

    public List<Item> getAllActiveItems()
    {
        return _mapper.getAllActiveItems();
    }

    public void addReceipt(Receipt receipt)
    {
        _mapper.addReceipt(receipt);
    }

    public List<SubscriberEntitlement> getCurrentEntitlementsForSubscriber(long subscriberId)
    {
        return _mapper.getCurrentEntitlementsForSubscriber(subscriberId);
    }

    public List<SubscriberEntitlement> getUnreservedEntitlementsForSubscriber(long subscriberId) {
        return _mapper.getUnreservedEntitlementsForSubscriber(subscriberId);
    }

    public List<SubscriberEntitlement> getUnreservedEntitlementsForSubscribers(List<Long> subscriberIds) {
        return _mapper.getUnreservedEntitlementsForSubscribers(subscriberIds);
    }

    public List<SubscriberEntitlement> getExpiredEntitlementsForSubscriber(long subscriberId)
    {
        return _mapper.getExpiredEntitlementsForSubscriber(subscriberId);
    }

    public List<SubscriberEntitlement> getSubscriberEntitlementConsumed(long subscriberId, String entitlementUuid)
    {
    	return _mapper.getSubscriberEntitlementConsumed(subscriberId, entitlementUuid);
    }

    public List<Entitlement> getEntitlements()
    {
        return _mapper.getEntitlements();
    }

    public Entitlement getEntitlement(String entitlementUuid)
    {
        return _mapper.getEntitlement(entitlementUuid);
    }

    public Entitlement getEntitlementById(int entitlementId) {
        return _mapper.getEntitlementById(entitlementId);
    }

    public List<Entitlement> getEntitlementsForItem(int itemId)
    {
        return _mapper.getEntitlementsForItem(itemId);
    }

    public List<Item> getItemsForEntitlement(int entitlementId) {
        return _mapper.getItemsForEntitlement(entitlementId);
    }

    public void addSubscriberEntitlement(SubscriberEntitlement entitlement)
    {
        _mapper.addSubscriberEntitlement(entitlement);
    }

    public void addSubscriberEntitlementWithContext(SubscriberEntitlement se) {
        _mapper.addSubscriberEntitlementWithContext(se);
    }

    public void addSubscriberEntitlementsWithContext(List<SubscriberEntitlement> ses) {
        _mapper.addSubscriberEntitlementsWithContext(ses);
    }

    public void removeSubscriberEntitlement(int subscriberEntitlementId)
    {
        _mapper.removeSubscriberEntitlement(subscriberEntitlementId);
    }

    public Item getItemById(int itemId)
    {
        return _mapper.getItemById(itemId);
    }

    public Item getItemByUuid(String itemUuid) {
        return _mapper.getItemByUuid(itemUuid);
    }

    public List<String> getEntitlementUuids() {
        return _mapper.getEntitlementUuids();
    }

    public List<String> getItemUuids() {
        return _mapper.getItemUuids();
    }

    public List<Integer> getItemIds() {
        return _mapper.getItemIds();
    }


//    public List<String> getMatchingCouponCodes(HashSet<String> newCodes) {
//        return _mapper.getMatchingCouponCodes(newCodes);
//    }

//    public void addCouponCodesForItem(String itemUuid, Set<String> newCodes)
//    {
//        _mapper.addCouponCodesForItem(itemUuid, newCodes);
//    }

    public List<SubscriberEntitlement> getExpiredSubscriberEntitlements() {
        return _mapper.getExpiredSubscriberEntitlements();
    }

    public void updateReceipt(Receipt receipt) {
        _mapper.updateReceipt(receipt);
    }

//    public CouponCode getCouponByCode(String couponCode) {
//        return _mapper.getCouponByCode(couponCode);
//    }

//    public void markCouponRedeemed(int couponId) {
//        _mapper.markCouponRedeemed(couponId);
//    }

    public List<Receipt> getReceiptsBySubscriberId(long subscriberId) {
        return _mapper.getReceiptsBySubscriberId(subscriberId);
    }

    public Receipt getReceiptByStoreUid(String receiptType, String storeUid) {
        return _mapper.getReceiptByStoreUid(receiptType, storeUid);
    }

    public Receipt getReceiptByUuid(String receiptUuid) {
        return _mapper.getReceiptByUuid(receiptUuid);
    }

    public SubscriberEntitlement getSubscriberEntitlement(String subscriberEntitlementUuid)
    {
        return _mapper.getSubscriberEntitlement(subscriberEntitlementUuid);
    }

    public void reserveSubscriberEntitlement(int subscriberEntitlementId)
    {
        _mapper.reserveSubscriberEntitlement(subscriberEntitlementId);
    }

    public void unreserveSubscriberEntitlement(String subscriberEntitlementUuid)
    {
        _mapper.unreserveSubscriberEntitlement(subscriberEntitlementUuid);
    }

    public void consumeSubscriberEntitlement(int subscriberEntitlementId)
    {
        _mapper.consumeSubscriberEntitlement(subscriberEntitlementId);
    }

    public void restoreSubscriberEntitlement(String subscriberEntitlementUuid) {
        _mapper.restoreSubscriberEntitlement(subscriberEntitlementUuid);
    }

    public List<ReceiptItem> getReceiptsBySubscriberIdFromDate(long subscriberId, Date fromDate) {
        return _mapper.getReceiptsBySubscriberIdFromDate(subscriberId, fromDate);
    }

    public ReceiptItem getReceiptItemForReceiptId(int receiptId){
        return _mapper.getReceiptItemForReceiptId(receiptId);
    }

    //external customer profile mapping
    public void insertCustomerProfileMapping(long subscriberId, String customerProfileId)
    {
        _mapper.insertCustomerProfileMapping(subscriberId, customerProfileId);
    }
    public String getCustomerProfileMapping(long subscriberId)
    {
        return _mapper.getCustomerProfileMapping(subscriberId);
    }
    public void deleteCustomerProfileMapping(long subscriberId)
    {
        _mapper.deleteCustomerProfileMapping(subscriberId);
    }

    public void setBraintreeCustomerId(long subscriberId, String customerId)
    {
        _mapper.setBraintreeCustomerId(subscriberId, customerId);
    }

    public String getBraintreeCustomerId(long subscriberId)
    {
        return _mapper.getBraintreeCustomerId(subscriberId);
    }

    public void removeBraintreeCustomerId(long subscriberId)
    {
        _mapper.removeBraintreeCustomerId(subscriberId);
    }

}
