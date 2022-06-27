package com.meinc.store.processor;

import static java.lang.String.format;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.meinc.store.dao.StoreServiceDaoSqlMap;
import com.meinc.store.domain.CreditCardInfo;
import com.meinc.store.domain.CustomerProfile;
import com.meinc.store.domain.Item;
import com.meinc.store.domain.Receipt;
import com.meinc.store.domain.Receipt.ReceiptType;
import com.meinc.store.domain.ReceiptResult;
import com.meinc.store.exception.InvalidReceiptException;
import com.meinc.store.exception.NoSuchCreditCardException;
import com.meinc.store.exception.StoreException;

public abstract class PaymentProcessor {
//    private static final Log logger = LogFactory.getLog(PaymentProcessor.class);
    private static final Log receiptLogger = LogFactory.getLog("payment");
    private static final Map<ReceiptType,PaymentProcessor> processors = new HashMap<ReceiptType,PaymentProcessor>();

    private static final ISO8601DateFormat dateFormatter = new ISO8601DateFormat();
    static {
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    protected StoreServiceDaoSqlMap _dao;

    PaymentProcessor() {
        for (ReceiptType type : getTypes())
            processors.put(type, this);
    }

    public void setDao(StoreServiceDaoSqlMap dao) {
        _dao = dao;
    }

    public static ReceiptResult globalVerifyReceipt(Receipt receipt, String storeBundleId)
    /*throws InvalidReceiptException, StoreException*/ {
        PaymentProcessor processor = processors.get(receipt.getType());
        if (processor == null) {
            ReceiptResult verifyResult = new ReceiptResult();
            verifyResult.setNoRetryError("Unsupported receipt type: " + receipt.getType());
            return verifyResult;
        }
        return processor.verifyReceipt(receipt, storeBundleId);
    }

    public static Receipt globalPurchaseItem(Receipt.ReceiptType processorType, long subscriberId, String customerId, Item item, CreditCardInfo ccInfo)
    throws StoreException, NoSuchCreditCardException
    {
        PaymentProcessor creditCardProcessor = processors.get(processorType);
        return (creditCardProcessor == null) ? null : creditCardProcessor.purchaseItem(subscriberId, customerId, item, ccInfo);
    }

    public static Receipt globalPurchaseItem(Receipt.ReceiptType processorType, long subscriberId, String customerId, Item item, String nonce, String firstname, String lastname)
    throws StoreException, NoSuchCreditCardException
    {
        PaymentProcessor processor = processors.get(processorType);
        return (processor == null) ? null : processor.purchaseItem(subscriberId, customerId, item, nonce, firstname, lastname);
    }

    public static Receipt globalPurchaseItemViaCustomerProfile(Receipt.ReceiptType processorType, long subscriberId, Item item, String customerProfileCreditCardInfoExternalRefId)
    throws StoreException
    {
        PaymentProcessor processor = processors.get(processorType);
        return (processor == null) ? null : processor.purchaseViaCustomerProfile(subscriberId, item, customerProfileCreditCardInfoExternalRefId);
    }

    public static CreditCardInfo globalGetDefaultPaymentInfo(Receipt.ReceiptType processorType, long subscriberId, String customerId)
    throws StoreException, NoSuchCreditCardException
    {
        PaymentProcessor creditCardProcessor = processors.get(processorType);
        return (creditCardProcessor == null) ? null : creditCardProcessor.getDefaultPaymentInfo(subscriberId, customerId);
    }

    public static void globalUpdateSubscriptionPayment(Receipt receipt, CreditCardInfo ccInfo)
    throws StoreException, InvalidReceiptException {
        PaymentProcessor processor = processors.get(receipt.getType());
        if (processor == null)
            throw new InvalidReceiptException("Unsupported receipt type: " + receipt.getType());
        processor.updateSubscriptionPayment(receipt, ccInfo);
    }

    public static void globalUpdateSubscriptionPayment(Receipt receipt, String nonce)
    throws StoreException, InvalidReceiptException {
        PaymentProcessor processor = processors.get(receipt.getType());
        if (processor == null)
            throw new InvalidReceiptException("Unsupported receipt type: " + receipt.getType());
        processor.updateSubscriptionPayment(receipt, nonce);
    }

    public static void globalCancelSubscription(Receipt receipt)
    throws InvalidReceiptException, StoreException {
        PaymentProcessor processor = processors.get(receipt.getType());
        if (processor == null)
            throw new InvalidReceiptException("Unsupported receipt type: " + receipt.getType());
        processor.cancelSubscription(receipt);
    }

    public static CreditCardInfo globalGetReceiptPaymentInfo(Receipt receipt)
    throws InvalidReceiptException, StoreException {
        PaymentProcessor processor = processors.get(receipt.getType());
        if (processor == null)
            throw new InvalidReceiptException("Unsupported receipt type: " + receipt.getType());
        return processor.getReceiptPaymentInfo(receipt);
    }

    public static CreditCardInfo globalGetSubscriptionPaymentInfo(Receipt receipt)
    throws InvalidReceiptException, StoreException {
        PaymentProcessor processor = processors.get(receipt.getType());
        if (processor == null)
            throw new InvalidReceiptException("Unsupported receipt type: " + receipt.getType());
        return processor.getSubscriptionPaymentInfo(receipt);
    }

    // Added 6/16/2016 to support Braintree "nonce" payment methods
    public static String globalGetClientToken(Receipt.ReceiptType processorType, long subscriberId)
    {
        PaymentProcessor creditCardProcessor = processors.get(processorType);
        return (creditCardProcessor == null) ? null : creditCardProcessor.getClientToken(subscriberId);
    }

    public static String globalCreateCustomerProfile(Receipt.ReceiptType processorType, long subscriberId, String nonce)
    throws StoreException
    {
        PaymentProcessor processor = processors.get(processorType);
        return (processor == null) ? null : processor.createCustomerProfile(subscriberId, nonce);
    }

    public static CustomerProfile globalGetCustomerProfile(Receipt.ReceiptType processorType, long subscriberId)
    throws StoreException
    {
        PaymentProcessor processor = processors.get(processorType);
        return (processor == null) ? null : processor.getCustomerProfile(subscriberId);
    }

    public static String globalAddPaymentMethodToCustomerProfile(
            Receipt.ReceiptType processorType, long subscriberId, String nonce, boolean makeDefault, String firstname, String lastname)
    throws StoreException
    {
        PaymentProcessor processor = processors.get(processorType);
        if (processor != null) {
            return processor.addPaymentMethodToCustomerProfile(subscriberId, nonce, makeDefault, firstname, lastname);
        } else {
            return null;
        }
    }

    public static List<CreditCardInfo> globalGetPaymentMethodsForCustomerProfile(Receipt.ReceiptType processorType, long subscriberId)
    throws StoreException
    {
        PaymentProcessor processor = processors.get(processorType);
        if (processor != null) {
            return processor.getPaymentMethodsForCustomerProfile(subscriberId);
        } else {
            return null;
        }
    }

    public static void globalDeletePaymentMethod(Receipt.ReceiptType processorType, String paymentMethodToken)
    throws StoreException
    {
        PaymentProcessor processor = processors.get(processorType);
        if (processor != null) {
            processor.deletePaymentMethod(paymentMethodToken);
        }
    }

//TODO: call from service
    public static void globalDeleteCustomerProfile(Receipt.ReceiptType processorType, long subscriberId)
    throws StoreException
    {
        PaymentProcessor processor = processors.get(processorType);
        if (processor != null) {
            processor.deleteCustomerProfile(subscriberId);
        }
    }

    static void logReceiptVerified(Receipt receipt, String storeTransactionId, String...desc) {
        StringBuffer descString = new StringBuffer();
        for (String d : desc)
            descString.append(d);
        Date receiptExpiration = receipt.getExpirationDate();
        String expirationString = (receiptExpiration == null) ? "NEVER" : dateFormatter.format(receiptExpiration);
        receiptLogger.info(format("{'action':'RECEIPT_VERIFIED', 'store':'%s', 'subscriber_id':'%s', 'id':'%s', 'expires':'%s', 'desc':'%s'}".replaceAll("'","\""),
                                    receipt.getType().name(), receipt.getSubscriberId(),
                                    storeTransactionId, expirationString, descString.toString()));
    }

    //the following methods must be overridden by the specific PaymentProcessor implementations

    abstract List<ReceiptType> getTypes();

    //the following methods may be overridden by the specific PaymentProcessor implementations if the method is applicable to them
    //(not all processors support all operations)

    /**
     * Purchase a store item using new credit card information for a subscriber.
     *
     * @param subscriberId
     *            the purchasing subscriber
     * @param customerId
     *            identifies the payee account; if null, <tt>subscriberId</tt> is converted into a customerId.
     * @param item
     *            the item to purchase
     * @param ccInfo
     *            the credit card information to use for purchase
     * @return a receipt representing the status of the purchase attempt
     * @throws StoreException
     * @throws NoSuchCreditCardException .
     */
    protected Receipt purchaseItem(long subscriberId, String customerId, Item item, CreditCardInfo ccInfo)
    throws StoreException, NoSuchCreditCardException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Purchase an item via the provided 3rd party nonce (i.e. no credit card information is sent to our servers).
     *
     * @param subscriberId
     * @param customerId
     * @param item
     * @param nonce
     * @param firstname
     * @param lastname
     * @return
     * @throws StoreException
     * @throws NoSuchCreditCardException
     */
    protected Receipt purchaseItem(long subscriberId, String customerId, Item item, String nonce, String firstname, String lastname)
    throws StoreException, NoSuchCreditCardException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Purchase an item using payment information stored on a 3rd party server.
     *
     * @param subscriberId
     * @param item
     * @param customerProfileCreditCardInfoExternalRefId
     * @return
     * @throws StoreException
     */
    protected Receipt purchaseViaCustomerProfile(long subscriberId, Item item, String customerProfileCreditCardInfoExternalRefId)
    throws StoreException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Used to call the 3rd party provider to create a one time use nonce from existing stored credit card information on their servers.
     * @param subscriberId
     * @return
     */
    protected String getClientToken(long subscriberId)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieve credit card information used for the subscriber's last successful purchase
     *
     * @param subscriberId
     *            the purchasing subscriber
     * @param customerId
     *            identifies the payee account; if null, <tt>subscriberId</tt> is converted into a customerId.
     * @return as much information possible about subscriber's credit card without compromising user security/privacy.
     * @throws StoreException
     */
    protected CreditCardInfo getDefaultPaymentInfo(long subscriberId, String customerId)
    throws StoreException, NoSuchCreditCardException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * The intent of this method is that when a client does a purchase directly from a 3rd party server (such as purchasing via the iOS or Android build-in stores), and then notifies
     * our server of the receipt, our code can, on the back-end verify that what the client told us was true.
     * <br/><br/>
     * If, however, the purchase was made from our backend server to a 3rd party backend server, we trust it and no receipt verification is necessary.
     *
     * @param receipt
     * @param storeBundleId
     * @return
     */
    protected ReceiptResult verifyReceipt(Receipt receipt, String storeBundleId)
    {
        throw new UnsupportedOperationException();
    }

    protected void updateSubscriptionPayment(Receipt receipt, CreditCardInfo ccInfo)
    throws StoreException
    {
        throw new UnsupportedOperationException();
    }

    protected void updateSubscriptionPayment(Receipt receipt, String nonce)
    throws StoreException
    {
        throw new UnsupportedOperationException();
    }

    protected void cancelSubscription(Receipt receipt)
    throws StoreException
    {
        throw new UnsupportedOperationException();
    }

    protected CreditCardInfo getReceiptPaymentInfo(Receipt receipt)
    throws StoreException
    {
        throw new UnsupportedOperationException();
    }

    protected CreditCardInfo getSubscriptionPaymentInfo(Receipt receipt)
    throws StoreException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Create a customer profile with payment information stored on a 3rd party server. The nonce will reference credit card information already
     * send to the 3rd party server waiting to be tied to the subscriber.
     *
     * @param subscriberId
     * @param nonce
     * @return
     */
    protected String createCustomerProfile(long subscriberId, String nonce)
    throws StoreException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieve an existing customer profile from a 3rd party server. It will contain the customerId and any payment methods (minus the sensitive data).
     *
     * @param subscriberId
     * @return
     * @throws StoreException
     */
    protected CustomerProfile getCustomerProfile(long subscriberId)
    throws StoreException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Add a payment method to an existing 3rd party customer profile. the nonce must have already been retrieved from the 3rd party server and associated
     * with payment information.
     * @param subscriberId
     * @param nonce
     * @param makeDefault if true, this payment method will be marked as the default payment method
     * @return the payment method token/id from the 3rd party server
     * @throws StoreException
     */
    protected String addPaymentMethodToCustomerProfile(long subscriberId, String nonce, boolean makeDefault, String firstname, String lastname)
    throws StoreException
    {
        throw new UnsupportedOperationException();
    }

    protected List<CreditCardInfo> getPaymentMethodsForCustomerProfile(long subscriberId)
    throws StoreException
    {
        throw new UnsupportedOperationException();
    }

    protected void deletePaymentMethod(String paymentMethodToken)
    throws StoreException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Remove a 3rd party customer profile (and all associated payment information).
     *
     * @param subscriberId
     * @throws StoreException
     */
    protected void deleteCustomerProfile(long subscriberId)
    throws StoreException
    {
        throw new UnsupportedOperationException();
    }

}
