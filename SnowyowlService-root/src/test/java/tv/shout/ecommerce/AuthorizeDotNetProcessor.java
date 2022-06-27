package tv.shout.ecommerce;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.meinc.identity.domain.Subscriber;
import com.meinc.store.domain.CreditCardInfo;
import com.meinc.store.domain.CustomerProfile;
import com.meinc.store.domain.Item;
import com.meinc.store.domain.Receipt;
import com.meinc.store.domain.Receipt.ReceiptType;
import com.meinc.store.exception.InvalidJsonException;
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
import tv.shout.sm.db.DbProvider;

public class AuthorizeDotNetProcessor
{
    private static final String PROP_FILE = "/Volumes/Encrypted Data/ShoutMeinc/snowyowl.properties";
    private static final String KEY_API_LOGIN_ID = "authorize.net.apiloginid";
    private static final String KEY_TRANSACTION_KEY = "authorize.net.transactionkey";
    private static final String KEY_ENVIRONMENT_TYPE = "authorize.net.environment";

    private static Logger _logger = Logger.getLogger(AuthorizeDotNetProcessor.class);

    //@Value("#{authorize.net.environment}")
    private String _environment;

    //@Value("#{authorize.net.apiloginid}")
    private String _apiLoginId;

    //@Value("#{authorize.net.transactionkey}")
    private String _transactionKey;

    //@Autowired
    private MockStoreService _storeService;

    public AuthorizeDotNetProcessor(DbProvider.DB which)
    throws Exception
    {
        _storeService = new MockStoreService(which);

        Properties props = Util.getProperties(PROP_FILE);
        _environment = props.getProperty(KEY_ENVIRONMENT_TYPE);
        _apiLoginId = props.getProperty(KEY_API_LOGIN_ID);
        _transactionKey = props.getProperty(KEY_TRANSACTION_KEY);

        ApiOperationBase.setEnvironment(Environment.valueOf(_environment));
    }

    public void stop()
    {
        _storeService.stop();
    }

    public Receipt purchaseViaNonce(int subscriberId, String itemUuid, String nonce)
    throws StoreException, InvalidJsonException
    {
//        if (_logger.isDebugEnabled()) {
//            _logger.debug(MessageFormat.format("subscriberId: {0,number,#}, itemUuid: {1}, nonce: {2}", subscriberId, itemUuid, nonce));
//        }
        _logger.info(MessageFormat.format("purchaseViaNonce - subscriberId: {0,number,#}, itemUuid: {1}, nonce: {2}", subscriberId, itemUuid, nonce));

        if (itemUuid == null) {
            throw new StoreException("unable to purchase: null itemUuid");
        }
        Item item = _storeService.getItemByUuid(itemUuid);
        if (item == null) {
            throw new StoreException("unable to purchase: item not found for itemUuid: " + itemUuid);
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

    public Receipt purchaseViaCustomerProfile(int subscriberId, String itemUuid, String customerProfileCreditCardInfoExternalRefId)
    throws StoreException, InvalidJsonException
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

        if (itemUuid == null) {
            throw new StoreException("unable to purchase: null itemUuid");
        }
        Item item = _storeService.getItemByUuid(itemUuid);
        if (item == null) {
            throw new StoreException("unable to purchase: item not found for itemUuid: " + itemUuid);
        }

        _logger.info(MessageFormat.format("purchaseViaCustomerProfile - subscriberId: {0,number,#}, itemUuid: {1}, customerProfileCreditCardInfoExternalRefId: {2}", subscriberId, itemUuid, customerProfileCreditCardInfoExternalRefId));

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

    private Receipt getReceiptFromCreateTransactionResponse(CreateTransactionResponse response, int subscriberId, Item item)
    throws StoreException, InvalidJsonException
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

//                if (_logger.isDebugEnabled()) {
                String msg = MessageFormat.format(
                    "Authorize.Net transaction successful. transactionId: {0}, responseCode: {1}, messageCode: {2}, description: {3}, authCode: {4}",
                    transactionId, responseCode, messageCode, description, authCode);
//                    _logger.debug(msg);
//                }
                _logger.info(msg);

                //convert response to a receipt
                Receipt receipt = new Receipt();
                receipt.setSubscriberId(subscriberId);
                receipt.setItemUuid(item.getUuid());
                receipt.setStoreUid(transactionId);
                receipt.setType(ReceiptType.AUTHNET_CREDIT_CARD);
                receipt.setPayload(Receipt.createAuthorizeDotNetCreditardPayload(transactionId, responseCode, messageCode, description, authCode).getBytes());
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

    public String createCustomerProfile(int subscriberId, String nonce)
    throws StoreException
    {
        //grab the subscriber
        Subscriber subscriber;
        if (subscriberId == 8) {
            subscriber  = new Subscriber();
            subscriber.setSubscriberId(subscriberId);
            subscriber.setEmail("shawker@me-inc.com");
            subscriber.setNickname("yarell");
        } else {
            throw new UnsupportedOperationException("only subscriber 8 is supported in the test rig");
        }

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

            _storeService.insertCustomerProfileMapping(subscriberId, customerProfileId);

            return customerProfileId;

        } else {
            String errorCode, errorMessage;

            errorCode = response.getMessages().getMessage().get(0).getCode();
            errorMessage = response.getMessages().getMessage().get(0).getText();

            String errorMsg = MessageFormat.format("error response from Authorize.net. errorCode: {0}, errorMessage: {1}", errorCode, errorMessage);
            throw new StoreException(errorMsg);
        }
    }

    public void addPaymentMethodToCustomerProfile(int subscriberId, String nonce)
    throws StoreException
    {
        String customerProfileId = _storeService.getCustomerProfileMapping(subscriberId);
        if (customerProfileId == null) {
            _logger.warn("customerProfileId not found for subscriberId: " + subscriberId);
            return;
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
    }

    public CustomerProfile getCustomerProfile(int subscriberId)
    throws StoreException
    {
        _logger.info("retrieving customer profile for subscriberId: " + subscriberId);

        String customerProfileId = _storeService.getCustomerProfileMapping(subscriberId);
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


    public void deleteCustomerProfile(int subscriberId)
    throws StoreException
    {
        String customerProfileId = _storeService.getCustomerProfileMapping(subscriberId);
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

        _storeService.deleteCustomerProfileMapping(subscriberId);

        _logger.info("profile removed");
    }
}
