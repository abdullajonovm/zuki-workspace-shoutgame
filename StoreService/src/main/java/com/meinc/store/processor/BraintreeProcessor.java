package com.meinc.store.processor;

import static com.braintreegateway.Subscription.Status.ACTIVE;
import static com.braintreegateway.Transaction.Status.FAILED;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.braintreegateway.Address;
import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.CreditCard;
import com.braintreegateway.CreditCardRequest;
import com.braintreegateway.Customer;
import com.braintreegateway.CustomerRequest;
import com.braintreegateway.Environment;
import com.braintreegateway.PayPalDetails;
import com.braintreegateway.PaymentMethod;
import com.braintreegateway.PaymentMethodRequest;
import com.braintreegateway.Result;
import com.braintreegateway.Subscription;
import com.braintreegateway.SubscriptionRequest;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.braintreegateway.ValidationError;
import com.braintreegateway.ValidationErrors;
import com.braintreegateway.exceptions.NotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.store.dao.StoreServiceDaoSqlMap;
import com.meinc.store.domain.CreditCardInfo;
import com.meinc.store.domain.DefaultCreditCardInfo;
import com.meinc.store.domain.Item;
import com.meinc.store.domain.Receipt;
import com.meinc.store.domain.Receipt.ReceiptType;
import com.meinc.store.domain.Receipt.SubscriptionState;
import com.meinc.store.domain.ReceiptResult;
import com.meinc.store.domain.SubscriberEntitlement;
import com.meinc.store.exception.InvalidJsonException;
import com.meinc.store.exception.NoSuchCreditCardException;
import com.meinc.store.exception.StoreException;

public class BraintreeProcessor extends PaymentProcessor {
    private static final Log logger = LogFactory.getLog(BraintreeProcessor.class);

    private BraintreeGateway gateway;
    private StoreServiceDaoSqlMap dao;
    private ObjectMapper jsonMapper = new ObjectMapper();

    public void start() {
        String environment              = ServerPropertyHolder.getProperty("store.processor.braintree.environment");
        String productionMerchantId     = ServerPropertyHolder.getProperty("store.processor.braintree.production.merchant.id");
        String productionPublicKey      = ServerPropertyHolder.getProperty("store.processor.braintree.production.public.key");
        String productionPrivateKeyPath = ServerPropertyHolder.getProperty("store.processor.braintree.production.private.key.path");

        if ("production".equals(environment)) {
            logger.info("Using Braintree production environment");
            if (productionMerchantId == null || productionMerchantId.trim().isEmpty())
                logger.warn("Missing production merchant key, falling back to sandbox environment");
            else if (productionPublicKey == null || productionPublicKey.trim().isEmpty())
                logger.warn("Missing production public key, falling back to sandbox environment");
            else if (productionPrivateKeyPath == null || productionPrivateKeyPath.trim().isEmpty())
                logger.warn("Missing production private key, falling back to sandbox environment");
            else {
                File privateKeyFile = new File(productionPrivateKeyPath);

                if (!privateKeyFile.exists() || !privateKeyFile.canRead() || !privateKeyFile.isFile())
                    logger.warn("Cannot access production private key, falling back to sandbox environment-");
                else {
                    String privateKey = null;
                    try {
                        privateKey = FileUtils.readFileToString(privateKeyFile);
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }

                    if (privateKey == null || privateKey.isEmpty())
                        logger.warn("Cannot access production private key, falling back to sandbox environment");
                    else
                        gateway = new BraintreeGateway(Environment.PRODUCTION,
                                        productionMerchantId,
                                        productionPublicKey,
                                        privateKey);
                }
            }
        }
        if (gateway == null) {
            logger.info("Using Braintree sandbox environment");
            //this is for the bxgrant shout sandbox
            gateway = new BraintreeGateway(
                    Environment.SANDBOX,
                    "9kgbhrrvk7rgytpr",
                    "cvc74qps2qfpz8pb",
                    "0f93cd573ce824d3fccf0036001e084e"
                  );
        }
    }

    public void start_old() {
        String environment              = ServerPropertyHolder.getProperty("store.processor.braintree.environment", "SANDBOX");
        String productionMerchantId     = ServerPropertyHolder.getProperty("store.processor.braintree.production.merchant.id");
        String productionPublicKey      = ServerPropertyHolder.getProperty("store.processor.braintree.production.public.key");
        String productionPrivateKeyPath = ServerPropertyHolder.getProperty("store.processor.braintree.production.private.key.path");

        if (productionMerchantId == null || productionMerchantId.trim().isEmpty())
            logger.warn("Missing production merchant key, falling back to sandbox environment");
        else if (productionPublicKey == null || productionPublicKey.trim().isEmpty())
            logger.warn("Missing production public key, falling back to sandbox environment");
        else if (productionPrivateKeyPath == null || productionPrivateKeyPath.trim().isEmpty())
            logger.warn("Missing production private key, falling back to sandbox environment");
        else {
            File privateKeyFile = new File(productionPrivateKeyPath);

            if (!privateKeyFile.exists() || !privateKeyFile.canRead() || !privateKeyFile.isFile())
                logger.warn("Cannot access production private key, falling back to sandbox environment-");
            else {
                String privateKey = null;
                try {
                    privateKey = FileUtils.readFileToString(privateKeyFile);
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
                if (privateKey == null || privateKey.isEmpty())
                    logger.warn("Cannot access production private key, falling back to sandbox environment");
                else {
                    Environment env = ("production".equals(environment.trim().toLowerCase())) ? Environment.PRODUCTION : Environment.SANDBOX;
                    logger.info("Using Braintree " + ((env == Environment.PRODUCTION) ? "production" : "sandbox") + " environment");
                    //logger.info("Using Braintree Merchant ID " + productionMerchantId);
                    //logger.info("Using Braintree Public Key " + productionPublicKey);
                    //logger.info("Using Braintree Private Key " + privateKey.trim().substring(0, 4) + "----------------------------");
                    gateway = new BraintreeGateway(
                                    env,
                                    productionMerchantId,
                                    productionPublicKey,
                                    privateKey.trim());
                }
            }
        }
    }

    public void stop() {
        gateway = null;
    }

    @Override
    List<ReceiptType> getTypes() {
        return Arrays.asList(new ReceiptType[] {ReceiptType.CREDIT_CARD});
    }

    private void throwPurchaseException(Result<?> result)
    throws StoreException {
        StringBuilder errorMessage = new StringBuilder();

        ValidationErrors errors = result.getErrors();
        if (errors != null) {
            List<ValidationError> errorList = errors.getAllDeepValidationErrors();
            Iterator<ValidationError> errorListIt = errorList.iterator();
            while (errorListIt.hasNext()) {
                errorMessage.append(errorListIt.next().getMessage());
                if (errorListIt.hasNext())
                    errorMessage.append("; ");
            }
        }

        if (errorMessage.length() == 0) {
            String resultMessage = result.getMessage();
            if (resultMessage != null && !resultMessage.trim().isEmpty())
                throw new StoreException(resultMessage);
            else
                throw new StoreException("Credit card error");
        }
        throw new StoreException(errorMessage.toString());
    }

    private String getCustomerId(long subscriberId, String customerId) {
        return (customerId != null && !customerId.trim().isEmpty()) ? customerId : String.format("sub_%d", subscriberId);
    }

    private void cleanCustomerAddresses(Customer customer) {
        /** Remove any old addresses on file - keeps things tidy */
        List<Address> customerAddresses = customer.getAddresses();
        if (customerAddresses != null) {
            for (Address address : customerAddresses) {
                try {
                    gateway.address().delete(customer.getId(), address.getId());
                } catch (Exception e) {
                    logger.error(String.format("Error deleting customer %s address %s: %s",
                                                customer.getId(), address.getId(), e.getMessage()), e);
                }
            }
        }
    }

    @Override
    protected Receipt purchaseItem(long subscriberId, String customerId, Item item, CreditCardInfo ccInfo)
    throws StoreException, NoSuchCreditCardException {
        if (ccInfo instanceof DefaultCreditCardInfo)
            return purchaseItem(subscriberId, customerId, item);

        try {
            customerId = getCustomerId(subscriberId, customerId);

            Customer customer = null;
            try {
                customer = gateway.customer().find(customerId);
            } catch (NotFoundException e) { }

            if (customer == null) {
                /** New customer */
                CustomerRequest customerRequest = new CustomerRequest()
                    .id(customerId);

                Result<Customer> customerResult = gateway.customer().create(customerRequest);
                customer = customerResult.getTarget();
                if (!customerResult.isSuccess() || customer == null)
                    throwPurchaseException(customerResult);
            }

            cleanCustomerAddresses(customer);

            /** Add the provided credit card and billing address */
            CreditCardRequest creditCardRequest = new CreditCardRequest()
                .customerId(customerId)
                .cardholderName(ccInfo.getName())
                .number(ccInfo.getNumber())
                .expirationDate(ccInfo.getExpDate())
                .cvv(ccInfo.getCvv())
                .billingAddress()
                    .streetAddress(ccInfo.getStreetAddress())
                    .extendedAddress(ccInfo.getExtAddress())
                    .locality(ccInfo.getLocality())
                    .region(ccInfo.getRegion())
                    .postalCode(ccInfo.getPostalCode())
                    .countryCodeAlpha2(ccInfo.getCountry())
                    .done()
                .options()
                    .makeDefault(true)
                    .done();

            Result<CreditCard> creditCardResult = gateway.creditCard().create(creditCardRequest);
            CreditCard customerCreditCard = creditCardResult.getTarget();
            if (!creditCardResult.isSuccess() || customerCreditCard == null)
                throwPurchaseException(creditCardResult);

            return createTransaction(subscriberId, customerId, item, customerCreditCard);

        } catch (Exception e) {
            String msg = "Error purchasing item " + item.getUuid();
            logger.error(msg, e);
            if (e instanceof StoreException)
                throw (StoreException)e;
            throw new StoreException(msg, e);
        }
    }

    public CreditCard findDefaultCreditCard(Long subscriberId, String customerId)
    throws StoreException, NoSuchCreditCardException {
        customerId = getCustomerId(subscriberId, customerId);

        Customer customer = null;
        try {
            customer = gateway.customer().find(customerId);
        } catch (NotFoundException e) {
            throw new NoSuchCreditCardException();
        }

        /** Find the default credit card to use for purchase */
        CreditCard customerCreditCard = null;

        List<CreditCard> creditCards = customer.getCreditCards();
        if (creditCards == null || creditCards.isEmpty())
            throw new NoSuchCreditCardException();

        if (creditCards.size() == 1) {
            customerCreditCard = creditCards.get(0);
            if (!customerCreditCard.isDefault()) {
                CreditCardRequest creditCardRequest = new CreditCardRequest()
                    .options()
                        .makeDefault(true)
                        .done();
                Result<CreditCard> creditCardResult = gateway.creditCard().update(customerCreditCard.getToken(), creditCardRequest);
                if (!creditCardResult.isSuccess())
                    throwPurchaseException(creditCardResult);
            }
        } else for (CreditCard creditCard : creditCards) {
            if (creditCard.isDefault()) {
                customerCreditCard = creditCard;
                break;
            }
        }

        if (customerCreditCard == null)
            throw new NoSuchCreditCardException();

        return customerCreditCard;
    }

    private Receipt purchaseItem(long subscriberId, String customerId, Item item)
    throws StoreException, NoSuchCreditCardException {
        try {
            CreditCard customerCreditCard = findDefaultCreditCard(subscriberId, customerId);
            return createTransaction(subscriberId, customerId, item, customerCreditCard);
        } catch (Exception e) {
            String msg = "Error purchasing item " + item.getUuid();
            logger.error(msg, e);
            if (e instanceof StoreException)
                throw (StoreException)e;
            if (e instanceof NoSuchCreditCardException)
                throw (NoSuchCreditCardException)e;
            throw new StoreException(msg, e);
        }
    }

    private Receipt createTransaction(Long subscriberId, String customerId, Item item, CreditCard customerCreditCard)
    throws StoreException, InvalidJsonException {
        /** Create a new transaction to charge the subscriber for their purchase */
        Transaction transaction = null;
        String transactionResultMessage = null;

        Receipt receipt = new Receipt();
        receipt.setSubscriberId(subscriberId);
        receipt.setType(ReceiptType.CREDIT_CARD);
        receipt.setItemUuid(item.getUuid());

        if (item.getDurationQuantity() != null && item.getDurationUnit() != null) {
            /** This is a subscription purchase */
            /** Remove any existing subscriptions */
            Set<String> subscriptionIds = new HashSet<String>();
            List<SubscriberEntitlement> subscriberEntitlements = dao.getCurrentEntitlementsForSubscriber(subscriberId);
            for (SubscriberEntitlement entitlement : subscriberEntitlements) {
                if (entitlement.getReceipt().getType() == ReceiptType.CREDIT_CARD && entitlement.getReceipt().getExpirationDate() != null) {
                    Map<String,String> payloadJson;
                    try {
                        payloadJson = jsonMapper.readValue(entitlement.getReceipt().getPayload(), new TypeReference<Map<String,String>>() { });
                    } catch (Exception e) {
                        throw new StoreException("Error parsing credit card receipt: " + e.getMessage(), e);
                    }
                    String subscriptionId = payloadJson.get("subscriptionId");
                    if (subscriptionId == null || subscriptionId.trim().isEmpty())
                        throw new StoreException(String.format("Credit card receipt %d is missing subscriptionId: %s",
                                                               entitlement.getReceipt().getReceiptId(), entitlement.getReceipt()));
                    subscriptionIds.add(subscriptionId);
                }
            }
            for (String subscriptionId : subscriptionIds) {
                try {
                    Subscription subscription = gateway.subscription().find(subscriptionId);
                    if (subscription != null && subscription.getStatus() == ACTIVE)
                        gateway.subscription().delete(customerId, subscriptionId);
                } catch (Exception e) {
                    logger.error(String.format("Error deleting subscriber %d subscription %s: %s", subscriberId, subscriptionId, e.getMessage()), e);
                }
                // To be consistent with other processors, we don't
                // remove old entitlements. It is up to the client to
                // figure out which subscriber entitlements matter.
                //dao.removeSubscriberEntitlement(entitlement.getSubscriberEntitlementId());
            }

            /** Create the new subscription */
            SubscriptionRequest subscriptionRequest = new SubscriptionRequest()
            .planId(item.getUuid())
            .paymentMethodToken(customerCreditCard.getToken());

            Result<Subscription> subscriptionResult = gateway.subscription().create(subscriptionRequest);
            Subscription subscription = subscriptionResult.getTarget();
            if (!subscriptionResult.isSuccess() || subscription == null)
                throwPurchaseException(subscriptionResult);

            /** Obtain the new transaction from the subscription */
            List<Transaction> transactions = subscription.getTransactions();
            if (transactions == null || transactions.isEmpty())
                throw new StoreException(String.format("Credit card gateway missing subscription %s transaction for subscriber %d",
                                                       subscription.getId(), subscriberId));
            transaction = transactions.get(0);

            receipt.setExpirationDate(subscription.getNextBillingDate().getTime());
            String payload = Receipt.createCreditCardPayload(customerId,
                                                             transaction.getId(),
                                                             subscription.getId(),
                                                             subscription.getStatus().name(),
                                                             subscription.getFirstBillingDate().getTime(),
                                                             new Date());
            receipt.setPayload(payload.getBytes());
            receipt.setSubscriptionState(SubscriptionState.ACTIVE);

        } else {
            /** This is a one-time purchase */
            /** Create a standalone transaction */
            TransactionRequest transactionRequest = new TransactionRequest()
                .amount(new BigDecimal(item.getPrice()))
                .orderId(receipt.getUuid())
                .customerId(customerId)
                .paymentMethodToken(customerCreditCard.getToken())
                .options()
                    .submitForSettlement(true)
                    .storeInVaultOnSuccess(true)
                    .done();

            Result<Transaction> transactionResult = gateway.transaction().sale(transactionRequest);
            transaction = transactionResult.getTarget();
            if (!transactionResult.isSuccess() || transaction == null)
                throwPurchaseException(transactionResult);

            transactionResultMessage = transactionResult.getMessage();

            String payload = Receipt.createCreditCardPayload(customerId,
                                                             transaction.getId(),
                                                             null,
                                                             transaction.getStatus().name(),
                                                             transaction.getCreatedAt().getTime(),
                                                             transaction.getUpdatedAt().getTime());
            receipt.setPayload(payload.getBytes());
        }

        /** Verify the new transaction status */
        Transaction.Status transactionStatus = (transaction == null || transaction.getStatus() == null) ? FAILED : transaction.getStatus();
        switch (transactionStatus) {
        case AUTHORIZING:
        case AUTHORIZED:
            //We shouldn't ever see these when using submitForSettlement(true)
            logger.warn(String.format("Transaction %s returned with status %s", transaction.getId(), transactionStatus.name()));
            break;

        case SUBMITTED_FOR_SETTLEMENT:
        case SETTLING:
        case SETTLED:
            //Success
            break;

        case GATEWAY_REJECTED:
            //errorMessage.append(transaction.getGatewayRejectionReason());
            //break;
        case PROCESSOR_DECLINED:
            //errorMessage.append(transaction.getProcessorResponseText());
            //break;

        case AUTHORIZATION_EXPIRED:
        case FAILED:
        case UNRECOGNIZED:
        case VOIDED:
        default:
            if (transactionResultMessage == null || transactionResultMessage.trim().isEmpty())
                throw new StoreException("Credit card error");
            else
                throw new StoreException(transactionResultMessage);
        }

        return receipt;
    }

    @Override
    protected CreditCardInfo getDefaultPaymentInfo(long subscriberId, String customerId)
    throws StoreException, NoSuchCreditCardException {
        try {
            CreditCard customerCreditCard = findDefaultCreditCard(subscriberId, customerId);
            return creditCardToCreditCardInfo(customerCreditCard);
        } catch (Exception e) {
            String msg = "Error getting default payment info for customer " + customerId;
            logger.error(msg, e);
            if (e instanceof StoreException)
                throw (StoreException)e;
            if (e instanceof NoSuchCreditCardException)
                throw (NoSuchCreditCardException)e;
            throw new StoreException(msg, e);
        }
    }

    private CreditCardInfo creditCardToCreditCardInfo(CreditCard creditCard) {
        CreditCardInfo creditCardInfo = new CreditCardInfo();
        creditCardInfo.setName         ( creditCard.getCardholderName()                        );
        creditCardInfo.setNumber       ( creditCard.getMaskedNumber()                          );
        creditCardInfo.setExpDate      ( creditCard.getExpirationDate()                        );
        creditCardInfo.setStreetAddress( creditCard.getBillingAddress().getStreetAddress()     );
        creditCardInfo.setExtAddress   ( creditCard.getBillingAddress().getExtendedAddress()   );
        creditCardInfo.setLocality     ( creditCard.getBillingAddress().getLocality()          );
        creditCardInfo.setRegion       ( creditCard.getBillingAddress().getRegion()            );
        creditCardInfo.setPostalCode   ( creditCard.getBillingAddress().getPostalCode()        );
        creditCardInfo.setCountry      ( creditCard.getBillingAddress().getCountryCodeAlpha2() );
        return creditCardInfo;
    }

    @Override
    protected ReceiptResult verifyReceipt(Receipt receipt, String storeBundleId) {
        ReceiptResult result = new ReceiptResult();
        Receipt storeReceipt = receipt.clone();

        Map<String,String> payloadJson;
        try {
            payloadJson = jsonMapper.readValue(receipt.getPayload(), new TypeReference<Map<String,String>>() { });
        } catch (Exception e) {
            logger.warn("Error parsing credit card receipt: " + e.getMessage(), e);
            result.setNoRetryError("Error parsing receipt payload");
            return result;
        }

        String customerId = payloadJson.get("customerId");
        if (customerId == null || customerId.trim().isEmpty()) {
            logger.warn(String.format("Credit card receipt %d is missing customerId: %s",
                                        receipt.getReceiptId(), receipt));
            result.setNoRetryError("Error parsing receipt payload");
            return result;
        }

        if (receipt.getExpirationDate() != null) {
            /** This is a subscription purchase */
            String subscriptionId = payloadJson.get("subscriptionId");
            if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
                logger.warn(String.format("Credit card receipt %d is missing subscriptionId: %s",
                                            receipt.getReceiptId(), receipt));
                result.setNoRetryError("Error parsing receipt payload");
                return result;
            }

            Subscription subscription = null;
            try {
                subscription = gateway.subscription().find(subscriptionId);
            } catch (Exception e) {
                logger.error(String.format("Error finding subscriber %d subscription %s: %s", receipt.getSubscriberId(), subscriptionId, e.getMessage()), e);
            }
            if (subscription == null) {
                logger.warn(String.format("Could not find subscription %s from receipt: %s", subscriptionId, receipt));
                result.setNoRetryError("Error finding subscription");
                return result;
            }
            Subscription.Status subscriptionStatus = subscription.getStatus();
            switch (subscriptionStatus) {
            case UNRECOGNIZED:
            case CANCELED:
            case EXPIRED:
            default:
                if (logger.isDebugEnabled())
                    logger.debug(String.format("Subscription %s is %s and no longer valid", subscriptionId, subscriptionStatus));
                result.setNoRetryError("Invalid/expired subscription");
                return result;
            case PAST_DUE:
                //-send email
                break;
            case PENDING:
            case ACTIVE:
                break;
            }

            storeReceipt.setExpirationDate(subscription.getNextBillingDate().getTime());

            List<Transaction> transactions = subscription.getTransactions();
            if (transactions == null || transactions.isEmpty()) {
                logger.warn(String.format("Credit card gateway missing subscription %s transaction for subscriber %d",
                                          subscription.getId(), receipt.getSubscriberId()));
                result.setNoRetryError("No transaction in subscription");
                return result;
            }
            Transaction transaction = transactions.get(0);

            String payload;
            try {
                payload = Receipt.createCreditCardPayload(customerId,
                                                             transaction.getId(),
                                                             subscription.getId(),
                                                             subscription.getStatus().name(),
                                                             subscription.getFirstBillingDate().getTime(),
                                                             new Date());
            } catch (InvalidJsonException e) {
                logger.error(String.format("Error updating receipt from subscription %s: %s", subscriptionId, receipt));
                result.setNoRetryError("Error generating receipt payload");
                return result;
            }
            storeReceipt.setPayload(payload.getBytes());

        } else {
            /** This is a one-time purchase */
            String transactionId = payloadJson.get("transactionId");
            if (transactionId == null || transactionId.trim().isEmpty()) {
                logger.warn(String.format("Credit card receipt %d is missing transactionId: %s",
                                            receipt.getReceiptId(), receipt));
                result.setNoRetryError("Error parsing receipt payload");
                return result;
            }

            Transaction transaction;
            try {
                transaction = gateway.transaction().find(transactionId);
            } catch (Exception e) {
                logger.error(String.format("Error finding transaction %d: %s", receipt.getSubscriberId(), transactionId, e.getMessage()), e);
                result.setNoRetryError("Error finding transaction");
                return result;
            }
            if (transaction == null) {
                logger.warn(String.format("Could not find transaction %s from receipt: %s", transactionId, receipt));
                result.setNoRetryError("Error finding transaction");
            }

            Transaction.Status transactionStatus = transaction.getStatus();
            switch (transactionStatus) {
            case AUTHORIZING:
            case AUTHORIZED:
                //We shouldn't ever see these when using submitForSettlement(true)
                logger.warn(String.format("Transaction %s found with status %s", transaction.getId(), transactionStatus.name()));
                break;

            case SUBMITTED_FOR_SETTLEMENT:
            case SETTLING:
            case SETTLED:
                //Success
                break;

            case GATEWAY_REJECTED:
                //errorMessage.append(transaction.getGatewayRejectionReason());
                //break;
            case PROCESSOR_DECLINED:
                //errorMessage.append(transaction.getProcessorResponseText());
                //break;

            case AUTHORIZATION_EXPIRED:
            case FAILED:
            case UNRECOGNIZED:
            case VOIDED:
            default:
                if (logger.isDebugEnabled())
                    logger.debug(String.format("Transaction %s is %s and no longer valid", transactionId, transactionStatus));
                result.setNoRetryError("Invalid/missing transaction");
                return result;
            }

            String payload;
            try {
                payload = Receipt.createCreditCardPayload(customerId,
                                                             transaction.getId(),
                                                             null,
                                                             transaction.getStatus().name(),
                                                             transaction.getCreatedAt().getTime(),
                                                             transaction.getUpdatedAt().getTime());
            } catch (InvalidJsonException e) {
                logger.error(String.format("Error updating receipt from transaction %s: %s", transactionId, receipt));
                result.setNoRetryError("Error generating receipt payload");
                return result;
            }
            storeReceipt.setPayload(payload.getBytes());
        }

        result.setStoreReceipt(storeReceipt);
        return result;
    }

    @Override
    protected void updateSubscriptionPayment(Receipt receipt, CreditCardInfo ccInfo)
    throws StoreException {
        if (ccInfo instanceof DefaultCreditCardInfo)
            throw new StoreException("Cannot update credit card info with default credit card");

        Map<String,String> payloadJson;
        try {
            payloadJson = jsonMapper.readValue(receipt.getPayload(), new TypeReference<Map<String,String>>() { });
        } catch (Exception e) {
            String msg = "Error parsing credit card receipt: " + e.getMessage();
            logger.error(msg, e);
            throw new StoreException(msg);
        }

        String customerId = getCustomerId(receipt.getSubscriberId(), payloadJson.get("customerId"));

        String subscriptionId = payloadJson.get("subscriptionId");
        if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
            String msg = String.format("Receipt %s has no associated subscripion", receipt.getUuid());
            logger.warn(msg + ": " + receipt);
            throw new StoreException(msg);
        }

        Customer customer;
        try {
            customer = gateway.customer().find(customerId);
        } catch (Exception e) {
            String msg = String.format("Error fetching customer %s: %s", customerId, e.getMessage());
            logger.error(msg, e);
            throw new StoreException(msg);
        }

        Subscription subscription;
        try {
            subscription = gateway.subscription().find(subscriptionId);
        } catch (Exception e) {
            String msg = String.format("Error finding subscription %d: %s", subscriptionId, e.getMessage());
            logger.error(msg, e);
            throw new StoreException(msg);
        }

        /** Add the provided credit card and billing address */
        CreditCardRequest creditCardRequest = new CreditCardRequest()
            .customerId(customerId)
            .cardholderName(ccInfo.getName())
            .number(ccInfo.getNumber())
            .expirationDate(ccInfo.getExpDate())
            .cvv(ccInfo.getCvv())
            .billingAddress()
                .streetAddress(ccInfo.getStreetAddress())
                .extendedAddress(ccInfo.getExtAddress())
                .locality(ccInfo.getLocality())
                .region(ccInfo.getRegion())
                .postalCode(ccInfo.getPostalCode())
                .countryCodeAlpha2(ccInfo.getCountry())
                .done();

        Result<CreditCard> creditCardResult;
        try {
            creditCardResult = gateway.creditCard().create(creditCardRequest);
        } catch (Exception e) {
            String msg = String.format("Error adding subscription %s payment method: %s", subscriptionId, e.getMessage());
            logger.error(msg, e);
            throw new StoreException(msg);
        }
        CreditCard customerCreditCard = creditCardResult.getTarget();
        if (!creditCardResult.isSuccess() || customerCreditCard == null)
            throwPurchaseException(creditCardResult);

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest()
                                                        .paymentMethodToken(customerCreditCard.getToken());

        Result<Subscription> subscriptionResult;
        try {
            subscriptionResult = gateway.subscription().update(subscriptionId, subscriptionRequest);
        } catch (Exception e) {
            String msg = String.format("Error updating subscription %s to credit card %s: %s",
                                        subscriptionId, customerCreditCard.getToken(), e.getMessage());
            logger.error(msg, e);
            throw new StoreException(msg);
        }
        subscription = subscriptionResult.getTarget();
        if (!subscriptionResult.isSuccess() || subscription == null)
            throwPurchaseException(subscriptionResult);

        ///** Remove any old credit cards on file. As a side-effect this also removes any subscriptions related to each removed credit card */
        /*List<CreditCard> customerCreditCards = customer.getCreditCards();
        if (customerCreditCards != null) {
            for (CreditCard creditCard : customerCreditCards) {
                if (!customerCreditCard.getToken().equals(creditCard.getToken())) {
                    try {
                        gateway.creditCard().delete(creditCard.getToken());
                    } catch (Exception e) {
                        logger.error(String.format("Error deleting subscriber %d card %s: %s", subscriberId, creditCard.getToken(), e.getMessage()), e);
                    }
                }
            }
        }*/

        cleanCustomerAddresses(customer);
    }

    @Override
    protected void updateSubscriptionPayment(Receipt receipt, String nonce)
    throws StoreException
    {
        Map<String,String> payloadJson;
        try {
            payloadJson = jsonMapper.readValue(receipt.getPayload(), new TypeReference<Map<String,String>>() { });
        } catch (Exception e) {
            String msg = "Error parsing credit card receipt: " + e.getMessage();
            logger.error(msg, e);
            throw new StoreException(msg);
        }

        String customerId = payloadJson.get("customerId");
        if (customerId == null || customerId.trim().isEmpty())
            customerId = getCustomerId(receipt.getSubscriberId(), null);

        String subscriptionId = payloadJson.get("subscriptionId");
        if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
            String msg = String.format("Receipt %s has no associated subscripion", receipt.getUuid());
            logger.warn(msg + ": " + receipt);
            throw new StoreException(msg);
        }

        Customer customer;
        try {
            customer = gateway.customer().find(customerId);
        } catch (Exception e) {
            String msg = String.format("Error fetching customer %s: %s", customerId, e.getMessage());
            logger.error(msg, e);
            throw new StoreException(msg);
        }

        Subscription subscription;
        try {
            subscription = gateway.subscription().find(subscriptionId);
        } catch (Exception e) {
            String msg = String.format("Error finding subscription %d: %s", subscriptionId, e.getMessage());
            logger.error(msg, e);
            throw new StoreException(msg);
        }

        PaymentMethodRequest paymentMethodRequest = new PaymentMethodRequest()
              .customerId(customerId)
              .paymentMethodNonce(nonce);
        Result<? extends PaymentMethod> paymentMethodResult = gateway.paymentMethod().create(paymentMethodRequest);
        if (!paymentMethodResult.isSuccess()) {
            throw new StoreException(paymentMethodResult.getMessage());
        }

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest()
            .paymentMethodToken(paymentMethodResult.getTarget().getToken())
        ;

        Result<Subscription> subscriptionResult;
        try {
            subscriptionResult = gateway.subscription().update(subscriptionId, subscriptionRequest);
        } catch (Exception e) {
            String msg = String.format("Error updating subscription %s: %s", subscriptionId, e.getMessage());
            logger.error(msg, e);
            throw new StoreException(msg);
        }
        subscription = subscriptionResult.getTarget();
        if (!subscriptionResult.isSuccess() || subscription == null)
            throwPurchaseException(subscriptionResult);

        cleanCustomerAddresses(customer);

        //TODO: also delete the old payment method
        //in order to do this, we'll need to save all these tokens somewhere
        //Result<? extends PaymentMethod> result = gateway.paymentMethod().delete("the_token");
    }

    @Override
    protected void cancelSubscription(Receipt receipt)
    throws StoreException {
        Map<String,String> payloadJson;
        try {
            payloadJson = jsonMapper.readValue(receipt.getPayload(), new TypeReference<Map<String,String>>() { });
        } catch (Exception e) {
            String msg = "Error parsing credit card receipt: " + e.getMessage();
            logger.error(msg, e);
            throw new StoreException(msg);
        }

        String subscriptionId = payloadJson.get("subscriptionId");
        if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
            String msg = String.format("Receipt %s has no associated subscripion", receipt.getUuid());
            logger.warn(msg + ": " + receipt);
            throw new StoreException(msg);
        }

        Result<Subscription> subscriptionResult;
        Subscription subscription;
        try {
            subscriptionResult = gateway.subscription().cancel(subscriptionId);
        } catch (Exception e) {
            String msg = String.format("Error canceling subscription %s: %s", subscriptionId, e.getMessage());
            logger.error(msg, e);
            throw new StoreException(msg);
        }
        subscription = subscriptionResult.getTarget();
        if (!subscriptionResult.isSuccess() || subscription == null)
            throwPurchaseException(subscriptionResult);
        if (subscription.getStatus() != Subscription.Status.CANCELED) {
            String msg = String.format("Subscription %s status did not change to CANCELED", subscriptionId);
            logger.warn(msg + ": " + receipt);
            throw new StoreException(msg);
        }
    }

    @Override
    protected CreditCardInfo getReceiptPaymentInfo(Receipt receipt)
    throws StoreException {
        Map<String,String> payloadJson;
        try {
            payloadJson = jsonMapper.readValue(receipt.getPayload(), new TypeReference<Map<String,String>>() { });
        } catch (Exception e) {
            String msg = "Error parsing credit card receipt: " + e.getMessage();
            logger.error(msg, e);
            throw new StoreException(msg);
        }

        String transactionId = payloadJson.get("transactionId");
        if (transactionId == null || transactionId.trim().isEmpty()) {
            String msg = String.format("Receipt %s has no associated transaction", receipt.getUuid());
            logger.warn(msg + ": " + receipt);
            throw new StoreException(msg);
        }

        Transaction transaction;
        try {
            transaction = gateway.transaction().find(transactionId);
        } catch (Exception e) {
            String msg = String.format("Error getting payment info for receipt %s for transaction %s: %s",
                                       receipt.getUuid(), transactionId, e.getMessage());
            logger.error(msg, e);
            throw new StoreException(msg);
        }

        CreditCardInfo ccInfo = new CreditCardInfo();
        ccInfo.setAmount(transaction.getAmount().toPlainString());
        ccInfo.setCountry(transaction.getBillingAddress().getCountryCodeAlpha2());
        ccInfo.setExtAddress(transaction.getBillingAddress().getExtendedAddress());
        ccInfo.setLocality(transaction.getBillingAddress().getLocality());
        ccInfo.setRegion(transaction.getBillingAddress().getRegion());
        ccInfo.setName(transaction.getCreditCard().getCardholderName());
        ccInfo.setNumber(transaction.getCreditCard().getLast4());
        ccInfo.setPostalCode(transaction.getBillingAddress().getPostalCode());
        ccInfo.setStreetAddress(transaction.getBillingAddress().getStreetAddress());

        return ccInfo;
    }

    @Override
    protected CreditCardInfo getSubscriptionPaymentInfo(Receipt receipt)
    throws StoreException {
        Map<String,String> payloadJson;
        try {
            payloadJson = jsonMapper.readValue(receipt.getPayload(), new TypeReference<Map<String,String>>() { });
        } catch (Exception e) {
            String msg = "Error parsing credit card receipt: " + e.getMessage();
            logger.error(msg, e);
            throw new StoreException(msg);
        }

        String subscriptionId = payloadJson.get("subscriptionId");
        if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
            String msg = String.format("Receipt %s has no associated subscription", receipt.getUuid());
            logger.warn(msg + ": " + receipt);
            throw new StoreException(msg);
        }

        Subscription subscription;
        try {
            subscription = gateway.subscription().find(subscriptionId);
        } catch (Exception e) {
            String msg = String.format("Error getting payment info for receipt %s for subscription %s: %s",
                                       receipt.getUuid(), subscriptionId, e.getMessage());
            logger.error(msg, e);
            throw new StoreException(msg);
        }
        String creditCardToken = subscription.getPaymentMethodToken();
        if (creditCardToken == null || creditCardToken.trim().isEmpty()) {
            String msg = String.format("Receipt %s for subscription %s has no associated credit card", receipt.getUuid(), subscriptionId);
            logger.warn(msg + ": " + receipt);
            throw new StoreException(msg);
        }

        CreditCard subscriptionCreditCard;
        try {
            subscriptionCreditCard = gateway.creditCard().find(creditCardToken);
        } catch (Exception e) {
            String msg = String.format("Error getting credit card info for receipt %s for subscription %s: %s",
                                       receipt.getUuid(), subscriptionId, e.getMessage());
            logger.error(msg, e);
            throw new StoreException(msg);
        }

        return creditCardToCreditCardInfo(subscriptionCreditCard);
    }

    @Override
    public void setDao(StoreServiceDaoSqlMap dao) {
        this.dao = dao;
    }

    @Override
    protected String getClientToken(long subscriberId){
        // TODO: use subscriberId to create a customerId that will be used to return the prior vault stored payment types
        //ClientTokenRequest clientTokenRequest = new ClientTokenRequest().customerId(aCustomerId);
        //String clientToken = gateway.clientToken().generate(clientTokenRequest);
        //See: https://developers.braintreepayments.com/reference/request/client-token/generate/java
        return gateway.clientToken().generate();
    }

    @Override
    protected Receipt purchaseItem(long subscriberId, String customerId, Item item, String nonce, String xFirstname, String xLastname)
    throws StoreException, NoSuchCreditCardException {
        logger.debug(MessageFormat.format("purchaseItem - sub:{0}, cust:{1}, item:{2}", subscriberId, customerId, item.getUuid()));

        try {
            //
            // Hit Braintree here to process the nonce
            //
            TransactionRequest request = new TransactionRequest()
                .amount(new BigDecimal(item.getPrice()))
                .paymentMethodNonce(nonce)
                .options()
                    .submitForSettlement(true)
                    .done();
            Result<Transaction> result = gateway.transaction().sale(request);

            //
            // Transaction successfully submitted for settlement
            // https://developers.braintreepayments.com/reference/response/transaction/java#result-object
            //
            if (result.isSuccess()) {
                Transaction transaction = result.getTarget();
                customerId = getCustomerId(subscriberId, customerId);
                Receipt receipt = extractReceiptFromTransaction(subscriberId, customerId, item, transaction);
                return receipt;
            } else {
                //
                // Failed payment processing...
                //
                ValidationErrors validationErrors = result.getErrors();
                String errorMessage = "purchase item failed. Reasons:";
                if (validationErrors != null){
                    List<ValidationError> errors = validationErrors.getAllValidationErrors();
                    for(ValidationError ve : errors)
                        errorMessage += ve.getMessage() + ", ";
                }else
                {
                    errorMessage += "gateway.transaction().sale(request) returned null ValidationErrors.";
                }
                throw new StoreException(errorMessage);
            }

        } catch (Exception e) {
            String msg = "Error purchasing item " + item.getUuid();
            logger.error(msg, e);
            if (e instanceof StoreException)
                throw (StoreException)e;
            throw new StoreException(msg, e);
        }
    }

    private Receipt extractReceiptFromTransaction(Long subscriberId, String customerId, Item item, Transaction transaction)
    throws StoreException, InvalidJsonException {
        /** Create a new transaction to charge the subscriber for their purchase */

        Receipt receipt = new Receipt();
        receipt.setSubscriberId(subscriberId);
        receipt.setItemUuid(item.getUuid());
        receipt.setStoreUid(transaction.getId());

        //
        // Credit Card OR PayPal
        //
        String paymentType = transaction.getPaymentInstrumentType();
        if ("credit_card".equals(paymentType)){
            receipt.setType(ReceiptType.CREDIT_CARD);
            String payload = Receipt.createCreditCardPayload(
                customerId,
                transaction.getId(),
                null,
                transaction.getStatus().name(),
                transaction.getCreatedAt().getTime(),
                transaction.getUpdatedAt().getTime());
            receipt.setPayload(payload.getBytes());
        }else if ("paypal_account".equals(paymentType)){
            receipt.setType(ReceiptType.PAYPAL);
            PayPalDetails paypalDetails = transaction.getPayPalDetails();
            String payload = Receipt.createPayPalPayload(
                    transaction.getId(),
                    paypalDetails.getPaymentId(),
                    paypalDetails.getPayerEmail());
            receipt.setPayload(payload.getBytes());

            //paypalDetails.getAuthorizationId();
            //paypalDetails.getCaptureId();
            //paypalDetails.getCustomField();
            //paypalDetails.getImageUrl();
            //paypalDetails.getPayerEmail();
            //paypalDetails.getPayerFirstName();
            //paypalDetails.getPayerId();
            //paypalDetails.getPayerLastName();
            //paypalDetails.getPaymentId();
            //paypalDetails.getRefundId();
            //paypalDetails.getSellerProtectionStatus();
            //paypalDetails.getToken();
            //paypalDetails.getTransactionFeeAmount();
            //paypalDetails.getTransactionFeeCurrencyIsoCode();
        }else{
            String transactionResultMessage = MessageFormat.format("purchase item failed: Unknown paymentType: {0}", paymentType);
            throw new StoreException(transactionResultMessage);
        }

        /** Verify the new transaction status */
        Transaction.Status transactionStatus = (transaction == null || transaction.getStatus() == null) ? FAILED : transaction.getStatus();
        switch (transactionStatus) {
        case AUTHORIZING:
        case AUTHORIZED:
            //We shouldn't ever see these when using submitForSettlement(true)
            logger.warn(String.format("Transaction %s returned with status %s", transaction.getId(), transactionStatus.name()));
            break;
        case SUBMITTED_FOR_SETTLEMENT:
        case SETTLING:
        case SETTLED:
            //Success
            break;
        case GATEWAY_REJECTED:
            //errorMessage.append(transaction.getGatewayRejectionReason());
            //break;
        case PROCESSOR_DECLINED:
            //errorMessage.append(transaction.getProcessorResponseText());
            //break;

        case AUTHORIZATION_EXPIRED:
        case FAILED:
        case UNRECOGNIZED:
        case VOIDED:
        default:
            String reason1 = transaction.getStatus().toString();
            String reason2 = transaction.getProcessorResponseText();
            String transactionResultMessage = MessageFormat.format("purchase item failed:{0} {1}", reason1, reason2);
            throw new StoreException(transactionResultMessage);
        }

        return receipt;
    }
}
