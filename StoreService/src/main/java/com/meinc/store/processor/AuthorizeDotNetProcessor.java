package com.meinc.store.processor;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.store.domain.CreditCardInfo;
import com.meinc.store.domain.CustomerProfile;
import com.meinc.store.domain.Item;
import com.meinc.store.domain.Receipt;
import com.meinc.store.domain.Receipt.ReceiptType;
import com.meinc.store.exception.InvalidJsonException;
import com.meinc.store.exception.NoSuchCreditCardException;
import com.meinc.store.exception.StoreException;

import net.authorize.Environment;
import net.authorize.api.contract.v1.CreateCustomerPaymentProfileRequest;
import net.authorize.api.contract.v1.CreateCustomerPaymentProfileResponse;
import net.authorize.api.contract.v1.CreateCustomerProfileRequest;
import net.authorize.api.contract.v1.CreateCustomerProfileResponse;
import net.authorize.api.contract.v1.CreateTransactionRequest;
import net.authorize.api.contract.v1.CreateTransactionResponse;
import net.authorize.api.contract.v1.CreditCardMaskedType;
import net.authorize.api.contract.v1.CustomerPaymentProfileType;
import net.authorize.api.contract.v1.CustomerProfilePaymentType;
import net.authorize.api.contract.v1.CustomerProfileType;
import net.authorize.api.contract.v1.CustomerTypeEnum;
import net.authorize.api.contract.v1.DeleteCustomerProfileRequest;
import net.authorize.api.contract.v1.DeleteCustomerProfileResponse;
import net.authorize.api.contract.v1.GetCustomerProfileRequest;
import net.authorize.api.contract.v1.GetCustomerProfileResponse;
import net.authorize.api.contract.v1.MerchantAuthenticationType;
import net.authorize.api.contract.v1.MessageTypeEnum;
import net.authorize.api.contract.v1.OpaqueDataType;
import net.authorize.api.contract.v1.PaymentProfile;
import net.authorize.api.contract.v1.PaymentType;
import net.authorize.api.contract.v1.TransactionRequestType;
import net.authorize.api.contract.v1.TransactionResponse;
import net.authorize.api.contract.v1.TransactionTypeEnum;
import net.authorize.api.contract.v1.ValidationModeEnum;
import net.authorize.api.controller.CreateCustomerPaymentProfileController;
import net.authorize.api.controller.CreateCustomerProfileController;
import net.authorize.api.controller.CreateTransactionController;
import net.authorize.api.controller.DeleteCustomerProfileController;
import net.authorize.api.controller.GetCustomerProfileController;
import net.authorize.api.controller.base.ApiOperationBase;

public class AuthorizeDotNetProcessor
extends PaymentProcessor
{
    private static final Log _logger = LogFactory.getLog(AuthorizeDotNetProcessor.class);

    private IIdentityService _identityService;
    private String _apiLoginId;
    private String _transactionKey;
    private String _environment;

    public void start()
    {
        ServerPropertyHolder.addPropertyChangeListener(
            "authorize\\.net\\.",
            (properties) -> {
                properties.forEach(change -> {
                    if ("authorize.net.environment".equals(change.key)) {
                        if (_logger.isDebugEnabled()) {
                            _logger.debug(MessageFormat.format("authorize.net.environment changed from {0} to {1}", change.oldValue, change.newValue));
                        }
                        setEnvironment(change.newValue);

                    } else if ("authorize.net.apiloginid".equals(change.key)) {
                        if (_logger.isDebugEnabled()) {
                            _logger.debug(MessageFormat.format("authorize.net.apiloginid changed from {0} to {1}", change.oldValue, change.newValue));
                        }
                        setApiLoginId(change.newValue);

                    } else if ("authorize.net.transactionkey".equals(change.key)) {
                        if (_logger.isDebugEnabled()) {
                            _logger.debug(MessageFormat.format("authorize.net.transactionkey changed from {0} to {1}", change.oldValue, change.newValue));
                        }
                        setTransactionKey(change.newValue);
                    }
                });
            }
        );
    }

    public void setIdentityService(IIdentityService service)
    {
        _identityService = service;
    }

    public void setEnvironment(String environment)
    {
        _environment = environment;
        ApiOperationBase.setEnvironment(Environment.valueOf(environment));
    }

    public void setApiLoginId(String apiLoginId)
    {
        _apiLoginId = apiLoginId;
    }

    public void setTransactionKey(String transactionKey)
    {
        _transactionKey = transactionKey;
    }

    @Override
    List<ReceiptType> getTypes()
    {
        return Arrays.asList(Receipt.ReceiptType.AUTHNET_CREDIT_CARD);
    }

    @Override
    protected Receipt purchaseItem(long subscriberId, String customerId, Item item, String nonce, String xfirstname, String xlastname)
    throws StoreException, NoSuchCreditCardException
    {
        if (_logger.isDebugEnabled()) {
            _logger.debug(MessageFormat.format("purchaseItemViaNonce -> subscriberId: {0,number,#}, itemUuid: {1}, nonce: {2}", subscriberId, item.getUuid(), nonce));
        }

        //merchant information
        MerchantAuthenticationType merchantAuthenticationType  = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(_apiLoginId);
        merchantAuthenticationType.setTransactionKey(_transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        //payment information (using a nonce in place of credit card data)
        OpaqueDataType op = new OpaqueDataType();
        op.setDataDescriptor("COMMON.ACCEPT.INAPP.PAYMENT");
        op.setDataValue(nonce);
        PaymentType paymentOne = new PaymentType();
        paymentOne.setOpaqueData(op);

        //payment transaction
        TransactionRequestType transactionRequest = new TransactionRequestType();
        transactionRequest.setAmount(new BigDecimal(item.getPrice()));
        transactionRequest.setTransactionType(TransactionTypeEnum.AUTH_CAPTURE_TRANSACTION.value());
        transactionRequest.setPayment(paymentOne);

        //make the request
        CreateTransactionRequest apiRequest = new CreateTransactionRequest();
        apiRequest.setTransactionRequest(transactionRequest);
        CreateTransactionController controller = new CreateTransactionController(apiRequest);
        controller.execute();

        //read the response
        CreateTransactionResponse response = controller.getApiResponse();
        return getReceiptFromCreateTransactionResponse(response, subscriberId, item);
    }

    @Override
    protected Receipt purchaseViaCustomerProfile(long subscriberId, Item item, String customerProfileCreditCardInfoExternalRefId)
    throws StoreException
    {
        if (customerProfileCreditCardInfoExternalRefId == null) {
            throw new StoreException("customerProfileCreditCardInfoExternalRefId required");
        }

        //make sure the externalRefId belongs to this subscriber (i.e. don't let someone else purchase on their behalf)
        CustomerProfile cp = getCustomerProfile(subscriberId);
        if (cp == null) {
            throw new StoreException("customer profile not found for subscriberId: " + subscriberId);
        }
        boolean foundMatch = false;
        if (cp.getCreditCardsOnFile() != null) {
            for (CreditCardInfo cci : cp.getCreditCardsOnFile()) {
                if (cci.getExternalRefId() != null && cci.getExternalRefId().equals(customerProfileCreditCardInfoExternalRefId)) {
                    foundMatch = true;
                    break;
                }
            }
        }
        if (!foundMatch) {
            throw new StoreException(
                MessageFormat.format(
                "subscriberId {0,number,#} mismatch for customer profile payment method ref {1} (possible fraud attempt)!",
                subscriberId, customerProfileCreditCardInfoExternalRefId));
        }

        _logger.info(MessageFormat.format(
                "purchaseViaCustomerProfile -> subscriberId: {0,number,#}, itemUuid: {1}, customerProfileCreditCardInfoExternalRefId: {2}",
                subscriberId, item.getUuid(), customerProfileCreditCardInfoExternalRefId));

        //merchant information
        MerchantAuthenticationType merchantAuthenticationType  = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(_apiLoginId);
        merchantAuthenticationType.setTransactionKey(_transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        //set the profile ID to charge
        CustomerProfilePaymentType profileToCharge = new CustomerProfilePaymentType();
        profileToCharge.setCustomerProfileId(cp.getCustomerId());
        PaymentProfile paymentProfile = new PaymentProfile();
        paymentProfile.setPaymentProfileId(customerProfileCreditCardInfoExternalRefId);
        profileToCharge.setPaymentProfile(paymentProfile);

        //create the payment transaction request
        TransactionRequestType txnRequest = new TransactionRequestType();
        txnRequest.setTransactionType(TransactionTypeEnum.AUTH_CAPTURE_TRANSACTION.value());
        txnRequest.setProfile(profileToCharge);
        txnRequest.setAmount(new BigDecimal(item.getPrice()));

        //make the request
        CreateTransactionRequest apiRequest = new CreateTransactionRequest();
        apiRequest.setTransactionRequest(txnRequest);
        CreateTransactionController controller = new CreateTransactionController(apiRequest);
        controller.execute();

        //read the response
        CreateTransactionResponse response = controller.getApiResponse();
        return getReceiptFromCreateTransactionResponse(response, subscriberId, item);
    }

    private Receipt getReceiptFromCreateTransactionResponse(CreateTransactionResponse response, long subscriberId, Item item)
    throws StoreException
    {
        if (response == null) {
            throw new StoreException("null response from Authorize.Net request");
        }

        if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
            TransactionResponse result = response.getTransactionResponse();
            if (result.getMessages() != null) {
                String transactionId = result.getTransId();
                String responseCode = result.getResponseCode();
                String messageCode = result.getMessages().getMessage().get(0).getCode();
                String description = result.getMessages().getMessage().get(0).getDescription();
                String authCode = result.getAuthCode();

                if (_logger.isDebugEnabled()) {
                String msg = MessageFormat.format(
                    "Authorize.Net transaction successful. transactionId: {0}, responseCode: {1}, messageCode: {2}, description: {3}, authCode: {4}",
                    transactionId, responseCode, messageCode, description, authCode);
                    _logger.debug(msg);
                }

                //convert response to a receipt
                Receipt receipt = new Receipt();
                receipt.setSubscriberId(subscriberId);
                receipt.setItemUuid(item.getUuid());
                receipt.setStoreUid(transactionId);
                receipt.setType(ReceiptType.AUTHNET_CREDIT_CARD);
                try {
                    receipt.setPayload(Receipt.createAuthorizeDotNetCreditardPayload(transactionId, responseCode, messageCode, description, authCode).getBytes());
                } catch (InvalidJsonException e) {
                    throw new StoreException("unable to create receipt payload", e);
                }
                receipt.setCreatedDate(new Date());

                return receipt;

            } else {
                String errorCode, errorMessage;

                if (response.getTransactionResponse().getErrors() != null) {
                    errorCode = response.getTransactionResponse().getErrors().getError().get(0).getErrorCode();
                    errorMessage = response.getTransactionResponse().getErrors().getError().get(0).getErrorText();

                } else {
                    errorCode = "UNKNOWN";
                    errorMessage = "UNKNOWN";
                }

                String errorMsg = MessageFormat.format("error response from Authorize.net. errorCode: {0}, errorMessage: {1}", errorCode, errorMessage);
                throw new StoreException(errorMsg);
            }

        } else {
            String errorCode, errorMessage;

            if (response.getTransactionResponse() != null && response.getTransactionResponse().getErrors() != null) {
                errorCode = response.getTransactionResponse().getErrors().getError().get(0).getErrorCode();
                errorMessage = response.getTransactionResponse().getErrors().getError().get(0).getErrorText();

            } else {
                errorCode = response.getMessages().getMessage().get(0).getCode();
                errorMessage = response.getMessages().getMessage().get(0).getText();
            }

            String errorMsg = MessageFormat.format("error response from Authorize.net. errorCode: {0}, errorMessage: {1}", errorCode, errorMessage);
            throw new StoreException(errorMsg);
        }
    }

    @Override
    protected String createCustomerProfile(long subscriberId, String nonce)
    throws StoreException
    {
        _logger.debug("creating customer profile for subscriberId: " + subscriberId);

        //grab the subscriber
        Subscriber subscriber = _identityService.getSubscriberById(subscriberId);

        //merchant information
        MerchantAuthenticationType merchantAuthenticationType  = new MerchantAuthenticationType() ;
        merchantAuthenticationType.setName(_apiLoginId);
        merchantAuthenticationType.setTransactionKey(_transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        //payment information (using a nonce in place of credit card data)
        OpaqueDataType op = new OpaqueDataType();
        op.setDataDescriptor("COMMON.ACCEPT.INAPP.PAYMENT");
        op.setDataValue(nonce);
        PaymentType paymentOne = new PaymentType();
        paymentOne.setOpaqueData(op);

        CustomerPaymentProfileType customerPaymentProfileType = new CustomerPaymentProfileType();
        customerPaymentProfileType.setCustomerType(CustomerTypeEnum.INDIVIDUAL);
        customerPaymentProfileType.setPayment(paymentOne);

        //create the customer profile
        CustomerProfileType customerProfileType = new CustomerProfileType();
        customerProfileType.setMerchantCustomerId(subscriberId+"");
        customerProfileType.setDescription(subscriber.getNickname());
        customerProfileType.setEmail(subscriber.getEmail());
        customerProfileType.getPaymentProfiles().add(customerPaymentProfileType);

        CreateCustomerProfileRequest apiRequest = new CreateCustomerProfileRequest();
        apiRequest.setProfile(customerProfileType);
        apiRequest.setValidationMode("SANDBOX".equals(_environment) ? ValidationModeEnum.TEST_MODE : ValidationModeEnum.LIVE_MODE);
        CreateCustomerProfileController controller = new CreateCustomerProfileController(apiRequest);
        controller.execute();
        CreateCustomerProfileResponse response = controller.getApiResponse();

        //check the response
        if (response == null) {
            throw new StoreException("null response from Authorize.Net request");
        }

        if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
            String customerProfileId = response.getCustomerProfileId();

            _dao.insertCustomerProfileMapping(subscriberId, customerProfileId);

            return customerProfileId;

        } else {
            String errorCode, errorMessage;

            errorCode = response.getMessages().getMessage().get(0).getCode();
            errorMessage = response.getMessages().getMessage().get(0).getText();

            String errorMsg = MessageFormat.format("error response from Authorize.net. errorCode: {0}, errorMessage: {1}", errorCode, errorMessage);
            throw new StoreException(errorMsg);
        }
    }

    @Override
    protected CustomerProfile getCustomerProfile(long subscriberId)
    throws StoreException
    {
        _logger.debug("retrieving customer profile for subscriberId: " + subscriberId);

        String customerProfileId = _dao.getCustomerProfileMapping(subscriberId);
        if (customerProfileId == null) {
            _logger.warn("customerProfileId not found for subscriberId: " + subscriberId);
            return null;
        }

        //merchant information
        MerchantAuthenticationType merchantAuthenticationType  = new MerchantAuthenticationType() ;
        merchantAuthenticationType.setName(_apiLoginId);
        merchantAuthenticationType.setTransactionKey(_transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        GetCustomerProfileRequest apiRequest = new GetCustomerProfileRequest();
        apiRequest.setCustomerProfileId(customerProfileId);

        GetCustomerProfileController controller = new GetCustomerProfileController(apiRequest);
        controller.execute();

        GetCustomerProfileResponse response = new GetCustomerProfileResponse();
        response = controller.getApiResponse();

        if (response == null) {
            throw new StoreException("null response from Authorize.Net request");
        }

        if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {

            //_logger.info("response code: " + response.getMessages().getMessage().get(0).getCode());
            //_logger.info("response message: " + response.getMessages().getMessage().get(0).getText());

            CustomerProfile cp = new CustomerProfile();
            cp.setCustomerId(response.getProfile().getCustomerProfileId());
            cp.setSubscriberId(subscriberId); //response.getProfile().getMerchantCustomerId()
            //response.getProfile().getDescription()
            //response.getProfile().getEmail()

            List<CreditCardInfo> ccs = new ArrayList<>();
            cp.setCreditCardsOnFile(ccs);

            if ((!response.getProfile().getPaymentProfiles().isEmpty())) {
                response.getProfile().getPaymentProfiles().forEach(pp -> {
                    CreditCardMaskedType ccType = pp.getPayment().getCreditCard();
                    CreditCardInfo cc = new CreditCardInfo();
                    cc.setExternalRefId(pp.getCustomerPaymentProfileId());
                    cc.setCardType(ccType.getCardType());
                    cc.setNumber(ccType.getCardNumber());
                    cc.setExpDate(ccType.getExpirationDate());
                    ccs.add(cc);
                });
            }

            return cp;

        } else {
            String errorCode = response.getMessages().getMessage().get(0).getCode();
            String errorMessage = response.getMessages().getMessage().get(0).getText();

            if ("E00040".equals(errorCode)) {
                //record not found - just return null
                return null;

            } else {
                String errorMsg = MessageFormat.format("error response from Authorize.net. errorCode: {0}, errorMessage: {1}", errorCode, errorMessage);
                throw new StoreException(errorMsg);
            }
        }
    }

    @Override
    protected String addPaymentMethodToCustomerProfile(long subscriberId, String nonce, boolean makeDefault, String xfirstname, String xlastname)
    throws StoreException
    {
        _logger.debug("adding payment method to customer profile for subscriberId: " + subscriberId);

        String customerProfileId = _dao.getCustomerProfileMapping(subscriberId);
        if (customerProfileId == null) {
            _logger.warn("customerProfileId not found for subscriberId: " + subscriberId);
            return null;
        }

        //merchant information
        MerchantAuthenticationType merchantAuthenticationType  = new MerchantAuthenticationType() ;
        merchantAuthenticationType.setName(_apiLoginId);
        merchantAuthenticationType.setTransactionKey(_transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        //customer information
        CreateCustomerPaymentProfileRequest apiRequest = new CreateCustomerPaymentProfileRequest();
        apiRequest.setMerchantAuthentication(merchantAuthenticationType);
        apiRequest.setCustomerProfileId(customerProfileId);

        //payment information (using a nonce in place of credit card data)
        OpaqueDataType op = new OpaqueDataType();
        op.setDataDescriptor("COMMON.ACCEPT.INAPP.PAYMENT");
        op.setDataValue(nonce);
        PaymentType paymentOne = new PaymentType();
        paymentOne.setOpaqueData(op);

        CustomerPaymentProfileType profile = new CustomerPaymentProfileType();
        profile.setPayment(paymentOne);
        apiRequest.setPaymentProfile(profile);

        //execute the request
        CreateCustomerPaymentProfileController controller = new CreateCustomerPaymentProfileController(apiRequest);
        controller.execute();
        CreateCustomerPaymentProfileResponse response = new CreateCustomerPaymentProfileResponse();
        response = controller.getApiResponse();

        //check the response
        if (response == null) {
            throw new StoreException("null response from Authorize.Net request");
        }

        if (response.getMessages().getResultCode() != MessageTypeEnum.OK) {
            String errorCode = response.getMessages().getMessage().get(0).getCode();
            String errorMessage = response.getMessages().getMessage().get(0).getText();

            String errorMsg = MessageFormat.format("error response from Authorize.net. errorCode: {0}, errorMessage: {1}", errorCode, errorMessage);
            throw new StoreException(errorMsg);
        }

        return null;
    }

    @Override
    protected void deleteCustomerProfile(long subscriberId)
    throws StoreException
    {
        String customerProfileId = _dao.getCustomerProfileMapping(subscriberId);
        if (customerProfileId == null) {
            _logger.warn("customerProfileId not found for subscriberId: " + subscriberId);
            return;
        }

        //merchant information
        MerchantAuthenticationType merchantAuthenticationType  = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(_apiLoginId);
        merchantAuthenticationType.setTransactionKey(_transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        //do the delete
        DeleteCustomerProfileRequest apiRequest = new DeleteCustomerProfileRequest();
        apiRequest.setCustomerProfileId(customerProfileId);
        DeleteCustomerProfileController controller = new DeleteCustomerProfileController(apiRequest);
        controller.execute();
        DeleteCustomerProfileResponse response = new DeleteCustomerProfileResponse();
        response = controller.getApiResponse();

        if (response == null) {
            throw new StoreException("null response from Authorize.Net request");
        }

        if (response.getMessages().getResultCode() != MessageTypeEnum.OK) {
            String msg = MessageFormat.format(
                    "unable to remove customer profile. code: {0}, message: {1}",
                    response.getMessages().getMessage().get(0).getCode(), response.getMessages().getMessage().get(0).getText());
            throw new StoreException(msg);
        }

        _dao.deleteCustomerProfileMapping(subscriberId);

        _logger.debug("3rd party customer profile removed for subscriberId: " + subscriberId);
    }
}
