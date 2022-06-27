package com.meinc.store.processor;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.CreditCard;
import com.braintreegateway.Customer;
import com.braintreegateway.CustomerRequest;
import com.braintreegateway.Environment;
import com.braintreegateway.PaymentMethod;
import com.braintreegateway.PaymentMethodRequest;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.braintreegateway.ValidationError;
import com.braintreegateway.exceptions.NotFoundException;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.store.dao.StoreServiceDaoSqlMap;
import com.meinc.store.domain.CreditCardInfo;
import com.meinc.store.domain.Item;
import com.meinc.store.domain.Receipt;
import com.meinc.store.domain.Receipt.ReceiptType;
import com.meinc.store.exception.InvalidJsonException;
import com.meinc.store.exception.NoSuchCreditCardException;
import com.meinc.store.exception.StoreException;

import tv.shout.util.StringUtil;

//https://developers.braintreepayments.com/guides/payment-methods/java
//https://developers.braintreepayments.com/guides/customers/java
//https://developers.braintreepayments.com/guides/transactions/java
public class BraintreeProcessor2
extends PaymentProcessor
{
    private static final Log _logger = LogFactory.getLog(BraintreeProcessor2.class);
    private BraintreeGateway _gateway;
    private StoreServiceDaoSqlMap _dao;
    private IIdentityService _identityService;

    @Override
    public void setDao(StoreServiceDaoSqlMap dao)
    {
        _dao = dao;
    }

    public void setIdentityService(IIdentityService service)
    {
        _identityService = service;
    }

    @Override
    List<ReceiptType> getTypes()
    {
        return Arrays.asList(ReceiptType.BRAINTREE_CREDIT_CARD);
    }

    public void start()
    {
        String environment              = ServerPropertyHolder.getProperty("store.processor.braintree2.environment");
        String productionMerchantId     = ServerPropertyHolder.getProperty("store.processor.braintree2.production.merchant.id");
        String productionPublicKey      = ServerPropertyHolder.getProperty("store.processor.braintree2.production.public.key");
        String productionPrivateKey     = ServerPropertyHolder.getProperty("store.processor.braintree2.production.private.key");

        if ("production".equals(environment)) {
            _logger.info("Using Braintree production environment");
            if (productionMerchantId == null || productionMerchantId.trim().isEmpty())
                _logger.warn("Missing production merchant key, falling back to sandbox environment");
            else if (productionPublicKey == null || productionPublicKey.trim().isEmpty())
                _logger.warn("Missing production public key, falling back to sandbox environment");
            else if (productionPrivateKey == null || productionPrivateKey.trim().isEmpty())
                _logger.warn("Missing production private key, falling back to sandbox environment");
            else {
                _gateway = new BraintreeGateway(Environment.PRODUCTION,
                                productionMerchantId,
                                productionPublicKey,
                                productionPrivateKey);
            }
        }
        if (_gateway == null) {
            _logger.info("Using Braintree sandbox environment");
            //this is for the bxgrant shout sandbox
            _gateway = new BraintreeGateway(
                    Environment.SANDBOX,
                    "9kgbhrrvk7rgytpr",
                    "cvc74qps2qfpz8pb",
                    "0f93cd573ce824d3fccf0036001e084e"
                  );
        }
    }

    public void stop()
    {
        _gateway = null;
    }

    @Override
    protected String getClientToken(long subscriberId)
    {
        return _gateway.clientToken().generate();
    }

    @Override
    protected String addPaymentMethodToCustomerProfile(long subscriberId, String nonce, boolean makeDefault, String firstname, String lastname)
    throws StoreException
    {
        if (nonce == null) throw new IllegalArgumentException("nonce is null");

        Subscriber subscriber = _identityService.getSubscriberById(subscriberId);
        if (subscriber == null) throw new IllegalArgumentException("subscriber is null");

        //see if the customer exists (in both our system and theirs)
        String customerId = _dao.getBraintreeCustomerId(subscriber.getSubscriberId());
        if (customerId != null) {
            Customer customer = findCustomer(customerId);
            if (customer == null) {
                //our database is out of date. remove the old data
                _dao.removeBraintreeCustomerId(subscriber.getSubscriberId());
                customerId = null;
            }
        }

        if (customerId == null) {
            //create the customer
            createCustomer(subscriber, firstname, lastname);
            customerId = _dao.getBraintreeCustomerId(subscriber.getSubscriberId());
        }

        //add the payment method
        PaymentMethodRequest request = new PaymentMethodRequest()
            .customerId(customerId)
            .paymentMethodNonce(nonce);
        Result<? extends PaymentMethod> result = _gateway.paymentMethod().create(request);

        String paymentMethodToken = null;
        if (result.isSuccess()) {
            paymentMethodToken = result.getTarget().getToken();
            _logger.info(MessageFormat.format("payment method{0} added for subscriber {1,number,#} ", paymentMethodToken, subscriberId));

            if (makeDefault) {
                PaymentMethodRequest updateRequest = new PaymentMethodRequest().options().makeDefault(true).done();
                Result<? extends PaymentMethod> updateResult = _gateway.paymentMethod().update(paymentMethodToken, updateRequest);

                if (updateResult.isSuccess()) {
                    _logger.info(MessageFormat.format("payment method {0} for subscriber {1,number,#} has been set to default", paymentMethodToken, subscriberId));
                } else {
                    throwExceptionFromResult(updateResult);
                }
            }

        } else {
            throwExceptionFromResult(result);
        }

        return paymentMethodToken;
    }

    @Override
    protected List<CreditCardInfo> getPaymentMethodsForCustomerProfile(long subscriberId)
    throws StoreException
    {
        Subscriber subscriber = _identityService.getSubscriberById(subscriberId);
        if (subscriber == null) throw new IllegalArgumentException("subscriber is null");

        List<CreditCardInfo> ccInfoList = new ArrayList<>();

        String customerId = _dao.getBraintreeCustomerId(subscriber.getSubscriberId());
        if (customerId == null) return null; //not an error. simply means they haven't set up anything in braintree
        Customer customer = findCustomer(customerId);
        if (customer == null) throw new IllegalArgumentException("customer not found");

        List<? extends PaymentMethod> paymentMethods = customer.getPaymentMethods();
        for (PaymentMethod paymentMethod : paymentMethods) {
            if (paymentMethod instanceof CreditCard) {
                CreditCard card = (CreditCard) paymentMethod;

                //convert from their format into ours
                CreditCardInfo ccInfo = new CreditCardInfo();
                ccInfo.setCardType(card.getCardType());
                ccInfo.setExpDate(card.getExpirationMonth() + "/" + card.getExpirationYear());
                ccInfo.setNumber(card.getLast4());
                ccInfo.setExternalRefId(card.getToken());
                ccInfo.setDefaultCard(card.isDefault());

                ccInfoList.add(ccInfo);

            } else {
                throw new StoreException("unsupported payment method type: " + paymentMethod.getClass().toString());
            }
        }

        return ccInfoList;
    }

    @Override
    protected void deletePaymentMethod(String paymentMethodToken)
    throws StoreException
    {
        Result<? extends PaymentMethod> result = _gateway.paymentMethod().delete(paymentMethodToken);

        if (result.isSuccess()) {
            _logger.info(MessageFormat.format("deleted payment method: {0}", paymentMethodToken));
        } else {
            throwExceptionFromResult(result);
        }
    }

    //purchase item via nonce
    @Override
    protected Receipt purchaseItem(long subscriberId, String x, Item item, String nonce, String firstname, String lastname)
    throws StoreException, NoSuchCreditCardException
    {
        Subscriber subscriber = _identityService.getSubscriberById(subscriberId);
        if (subscriber == null) throw new IllegalArgumentException("subscriber is null");

        String customerId = _dao.getBraintreeCustomerId(subscriber.getSubscriberId());
        if (customerId == null) {
            //create the customer first
            createCustomer(subscriber, firstname, lastname);
            customerId = _dao.getBraintreeCustomerId(subscriber.getSubscriberId());
        }

        Customer customer = findCustomer(customerId);
        if (customer == null) throw new StoreException("customer not found");

        if (item == null) throw new IllegalArgumentException("item is null");
        if (nonce == null) throw new IllegalArgumentException("none is null");

        double price = item.getItemPrice().stream()
                .filter(ip -> ip.getCurrencyCode().equals("USD"))
                .mapToDouble(i -> i.getPrice())
                .findFirst()
                .orElseThrow(() -> new StoreException("item contains no price"));

        Receipt receipt = new Receipt();

        TransactionRequest request = new TransactionRequest()
            .amount(new BigDecimal(price))
            .paymentMethodNonce(nonce)
            .customerId(customer.getId())
            .orderId(receipt.getUuid())
            .options()
                .submitForSettlement(true)
                .done();

        Result<Transaction> result = _gateway.transaction().sale(request);
        if (result.isSuccess()) {
            finishPurchase(subscriber, customer.getId(), item, result, receipt);
        } else {
            throwExceptionFromResult(result);
        }

        return receipt;
    }

    //purchase item via payment method (piggy backing on the other method since it has the same signature)
    @Override
    protected Receipt purchaseViaCustomerProfile(long subscriberId, Item item, String paymentMethodToken)
    throws StoreException
    {
        Subscriber subscriber = _identityService.getSubscriberById(subscriberId);
        if (subscriber == null) throw new IllegalArgumentException("subscriber is null");

        Customer customer = findCustomer(_dao.getBraintreeCustomerId(subscriber.getSubscriberId()));
        if (customer == null) throw new StoreException("customer not found");

        if (item == null) throw new IllegalArgumentException("item is null");
        if (paymentMethodToken == null) throw new IllegalArgumentException("paymentMethodToken is null");

        double price = item.getItemPrice().stream()
                .filter(ip -> ip.getCurrencyCode().equals("USD"))
                .mapToDouble(i -> i.getPrice())
                .findFirst()
                .orElseThrow(() -> new StoreException("item contains no price"));

        Receipt receipt = new Receipt();

        TransactionRequest request = new TransactionRequest()
            .amount(new BigDecimal(price))
            .paymentMethodToken(paymentMethodToken)
            .customerId(customer.getId())
            .orderId(receipt.getUuid())
            .options()
                .submitForSettlement(true)
                .done();

        Result<Transaction> result = _gateway.transaction().sale(request);
        if (result.isSuccess()) {
            finishPurchase(subscriber, customer.getId(), item, result, receipt);
        } else {
            throwExceptionFromResult(result);
        }

        return receipt;
    }

    private void finishPurchase(Subscriber subscriber, String customerId, Item item, Result<Transaction> result, Receipt receipt)
    throws StoreException
    {
        Transaction transaction = result.getTarget();

_logger.info(MessageFormat.format(">>> transaction.id: {0}, transaction.orderId: {1}", transaction.getId(), transaction.getOrderId()));
        String transactionResultMessage = result.getMessage();

        //fill out the receipt
        String payload;
        try {
            payload = Receipt.createCreditCardPayload(customerId,
                transaction.getId(),
                null,
                transaction.getStatus().name(),
                transaction.getCreatedAt().getTime(),
                transaction.getUpdatedAt().getTime());
        } catch (InvalidJsonException e) {
            throw new StoreException("purchase successful but unable to create receipt", e);
        }

        receipt.setSubscriberId(subscriber.getSubscriberId());
        receipt.setType(ReceiptType.BRAINTREE_CREDIT_CARD);
        receipt.setItemUuid(item.getUuid());
        receipt.setPayload(payload.getBytes());
        receipt.setStoreUid(transaction.getId());

        //verify the new transaction status
        Transaction.Status transactionStatus = (transaction == null || transaction.getStatus() == null) ? Transaction.Status.FAILED : transaction.getStatus();
        switch (transactionStatus)
        {
            //should never see these when using submitForSettlement(true)
            case AUTHORIZING:
            case AUTHORIZED:
                _logger.warn(MessageFormat.format("Transaction {0} returned with status {1}", transaction.getId(), transactionStatus.name()));
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
                    throw new StoreException("credit card error");
                else
                    throw new StoreException(transactionResultMessage);
        }
    }

    private Customer findCustomer(String customerId)
    {
        if (customerId == null) throw new IllegalArgumentException("customerId is null");

        Customer customer;
        try {
            customer = _gateway.customer().find(customerId);
        } catch (NotFoundException e) {
            return null;
        }
        return customer;
    }

    private void createCustomer(Subscriber subscriber, String firstname, String lastname)
    throws StoreException
    {
        if (subscriber == null) throw new IllegalArgumentException("subscriber is null");

        if (StringUtil.isEmpty(subscriber.getEmail())) {
            throw new StoreException("missing email");
        } else if (StringUtil.isEmpty(firstname)) {
            throw new StoreException("missing firstname");
        } else if (StringUtil.isEmpty(lastname)) {
            throw new StoreException("missing lastname");
        }

        CustomerRequest request = new CustomerRequest()
            .firstName(firstname)
            .lastName(lastname)
            .email(subscriber.getEmail());
        Result<Customer> result = _gateway.customer().create(request);

        if (result.isSuccess()) {
            _logger.info(MessageFormat.format("created customer: {0}", result.getTarget().getId()));
            _dao.setBraintreeCustomerId(subscriber.getSubscriberId(), result.getTarget().getId());
        } else {
            throwExceptionFromResult(result);
        }
    }

    //https://developers.braintreepayments.com/reference/general/validation-errors/overview/java
    //https://developers.braintreepayments.com/reference/general/validation-errors/all/java#verification
    private void throwExceptionFromResult(Result<?> result)
    throws StoreException
    {
        Map<String, Object> errorMap = new HashMap<>();
        for (ValidationError error : result.getErrors().getAllDeepValidationErrors()) {
            String code = error.getCode().code;
            String msg = error.getMessage();
            errorMap.put(code, msg);
        }
        throw new StoreException(errorMap);

//        StringBuilder errorMessage = new StringBuilder();
//        ValidationErrors errors = result.getErrors();
//        if (errors != null) {
//            List<ValidationError> errorList = errors.getAllDeepValidationErrors();
//            Iterator<ValidationError> errorListIt = errorList.iterator();
//            while (errorListIt.hasNext()) {
//                errorMessage.append(errorListIt.next().getMessage());
//                if (errorListIt.hasNext()) {
//                    errorMessage.append("; ");
//                }
//            }
//        }
//
//        if (errorMessage.length() == 0) {
//            String resultMessage = result.getMessage();
//            if (resultMessage != null && !resultMessage.trim().isEmpty())
//                throw new StoreException(resultMessage);
//            else
//                throw new StoreException("unknown Braintree exception");
//        }
//        throw new StoreException(errorMessage.toString());
    }
}
