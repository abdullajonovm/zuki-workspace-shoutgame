package com.meinc.store.service;

import java.util.Date;
import java.util.List;

import com.meinc.store.domain.CreditCardInfo;
import com.meinc.store.domain.CustomerProfile;
import com.meinc.store.domain.Entitlement;
import com.meinc.store.domain.Item;
import com.meinc.store.domain.Receipt;
import com.meinc.store.domain.ReceiptItem;
import com.meinc.store.domain.ReceiptResult;
import com.meinc.store.domain.SubscriberEntitlement;
import com.meinc.store.exception.AlreadyConsumedException;
import com.meinc.store.exception.AlreadyReservedException;
import com.meinc.store.exception.InvalidItemException;
import com.meinc.store.exception.InvalidReceiptException;
import com.meinc.store.exception.NoSuchCreditCardException;
import com.meinc.store.exception.StoreException;

public interface IStoreService
{
    void start();
    void stop();

    Receipt purchaseItemViaCreditCard(long subscriberId, String customerId, String itemUuid, CreditCardInfo ccInfo)
    throws InvalidItemException, StoreException, NoSuchCreditCardException;

    Receipt purchaseItemViaNonce(long subscriberId, String customerId, String itemUuid, String nonce, String firstname, String lastname)
    throws InvalidItemException, StoreException, NoSuchCreditCardException;

    Receipt purchaseViaCustomerProfile(long subscriberId, String itemUuid, String customerProfileCreditCardInfoExternalRefId)
    throws InvalidItemException, StoreException;

    CreditCardInfo getDefaultPaymentInfo(long subscriberId)
    throws StoreException, NoSuchCreditCardException;

    boolean auditReceipts(long subscriberId, List<Receipt> receipts)
    throws InvalidItemException;

    ReceiptResult addReceipt(Receipt receipt)
    throws InvalidItemException/*, InvalidReceiptException, StoreException*/;

    void updateSubscriptionPayment(long subscriberId, String receiptUuid, CreditCardInfo ccInfo)
    throws StoreException, InvalidReceiptException;

    void updateSubscriptionPaymentViaNonce(int subscriberId, String receiptUuid, String nonce)
    throws StoreException, InvalidReceiptException;

    void cancelSubscription(long subscriberId, String receiptUuid)
    throws InvalidReceiptException, StoreException;

    List<Item> getAllActiveItems();

    List<Entitlement> getEntitlements();

    Entitlement getEntitlement(String entitlementUuid);

    List<Entitlement> getEntitlementsForItem(int itemId);

    List<Entitlement> getEntitlementsForItemRaw(int itemId);

    List<Item> getItemsForEntitlements(String entitlementUuid);

    List<Item> getItemsForEntitlement(int entitlementId);

    List<SubscriberEntitlement> getCurrentEntitlementsForSubscriber(long subscriberId);

    List<SubscriberEntitlement> getUnreservedEntitlementsForSubscriber(long subscriberId);

    List<SubscriberEntitlement> getUnreservedEntitlementsForSubscribers(List<Long> subscriberIds);

	List<SubscriberEntitlement> getSubscriberEntitlementConsumed(long subscriberIds, String entitlementUuid);

    SubscriberEntitlement getSubscriberEntitlement(String subscriberEntitlementUuid);

    void addSubscriberEntitlements(List<SubscriberEntitlement> entitlements);

    void addSubscriberEntitlementsBatch(List<SubscriberEntitlement> entitlements);

    List<SubscriberEntitlement> getExpiredEntitlementsForSubscriber(long subscriberId);

    void removeSubscriberEntitlement(int subscriberEntitlementId);

    void reserveSubscriberEntitlement(String subscriberEntitlementUuid, int contextId)
    throws InvalidItemException, AlreadyReservedException, AlreadyConsumedException;

    /** rollback a reserved subscriber entitlement in case an exception happens after reservation */
    void unreserveSubscriberEntitlement(String subscriberEntitlementUuid);

    void consumeSubscriberEntitlement(String subscriberEntitlementUuid, int contextId)
    throws InvalidItemException, AlreadyConsumedException;

    void restoreSubscriberEntitlement(String subscriberEntitlementUuid);

    Item getItemById(int itemId);

    Item getItemByUuid(String itemUuid);

    Item getItemByUuidRaw(String itemUuid);

//    /**
//     * Generate a number of coupon codes that can be redeemed for a given itemUuid.
//     *
//     * @param itemUuid the uuid of the item this code will be good for
//     * @param count how many coupon codes to generate
//     * @return
//     * @throws InvalidItemException
//     */
//    List<CouponCode> generateCouponCodesForItem(String itemUuid, int count) throws InvalidItemException;

//    CouponCode getCoupon(String couponCode);

    List<Receipt> getReceiptsBySubscriberId(long subscriberId);

    void removeSubscriberEntitlements(long subscriberId);
    Date expireSubscriberEntitlements(long subscriberId, boolean remove);
    Date changeExpirationOfSubscriberEntitlements(long subscriberId, int changeExpirationHours);

    CreditCardInfo getReceiptPaymentInfo(long subscriberId, String receiptUuid)
    throws InvalidReceiptException, StoreException;

    CreditCardInfo getSubscriptionPaymentInfo(long subscriberId, String receiptUuid)
    throws InvalidReceiptException, StoreException;

    // Added 6/16/2016 -- to handle Braintree "nonce" style of payment processing
    String getClientToken(long subscriberId);

    List<ReceiptItem> getReceiptsBySubscriberIdFromDate(long subscriberId, Date fromDate);

    ReceiptItem getReceiptItemForReceiptId(int receiptId);

    String createCustomerProfile(long subscriberId, String nonce)
    throws StoreException;

    CustomerProfile getCustomerProfile(long subscriberId)
    throws StoreException;

    String addPaymentMethodToCustomerProfile(long subscriberId, String nonce, boolean makeDefault, String firstname, String lastname) throws StoreException;
    List<CreditCardInfo> getPaymentMethodsForCustomerProfile(long subscriberId) throws StoreException;
    void deletePaymentMethod(String paymentMethodToken) throws StoreException;

    void deleteCustomerProfile(long subscriberId)
    throws StoreException;

}
