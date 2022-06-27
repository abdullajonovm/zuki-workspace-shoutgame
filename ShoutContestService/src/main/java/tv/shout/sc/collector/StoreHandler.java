package tv.shout.sc.collector;

import static com.meinc.webcollector.message.CollectorMessage.PARM_TO_WDS;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Resource;
import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;

import com.fasterxml.jackson.core.type.TypeReference;
import com.meinc.commons.encryption.IEncryption;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.domain.SubscriberAddress;
import com.meinc.store.domain.CreditCardInfo;
import com.meinc.store.domain.Item;
import com.meinc.store.domain.ItemPrice;
import com.meinc.store.domain.Receipt;
import com.meinc.store.domain.Receipt.ReceiptType;
import com.meinc.store.domain.ReceiptItem;
import com.meinc.store.domain.ReceiptResult;
import com.meinc.store.exception.InvalidItemException;
import com.meinc.store.exception.InvalidJsonException;
import com.meinc.store.exception.NoSuchCreditCardException;
import com.meinc.store.exception.StoreException;
import com.meinc.store.service.IStoreService;
import com.meinc.webcollector.message.CollectorMessage;
import com.meinc.webcollector.message.handler.BadRequestException;
import com.meinc.webcollector.message.handler.CollectorMessageResult;
import com.meinc.webcollector.message.handler.SyncRequest.HttpRequest;
import com.meinc.webcollector.message.handler.SyncRequest.HttpResponse;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint.ConnectionType;

import tv.shout.collector.BaseMessageHandler;
import tv.shout.collector.CollectorEndpointHandler;
import tv.shout.collector.PublishResponseError;
import tv.shout.collector.SubscriberUtil;
import tv.shout.sc.dao.IContestDaoMapper;
import tv.shout.sc.domain.BankAccount;
import tv.shout.sc.domain.CashPoolTransaction2;
import tv.shout.sc.domain.CouponCode;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.util.DateUtil;
import tv.shout.util.FastMap;
import tv.shout.util.StringUtil;

public class StoreHandler
extends BaseMessageHandler
{
    private static Logger _logger = Logger.getLogger(StoreHandler.class);

    // Valid form post param names
    private static final List<String> validFormVars = Arrays.asList(
        "demo",
        "venue", "itemUuid", "nonce", "makeDefault", "paymentMethodToken",
        "appId", /*"storeType",*/ "receipt", /*"signature",*/
        /*"checkingAccountName",*/ "phone", "addressId", /*"routingNumber",*/ /*"accountNumber",*/ /*"bankName",*/
        "amount", "account", "accountId", "couponCode",
        "type", "firstname", "lastname"
    );

    @Value("${echeck.client.id}")
    private String _echeckClientId;

    @Value("${echeck.api.password}")
    private String _echeckApiPassword;

    @Value("${echeck.endpoint}")
    private String _echeckEndpoint;

    @Resource(name="storeService")
    private IStoreService _storeService;
    @Resource(name="mockStoreService")
    private IStoreService _mockStoreService;

    @Autowired
    IEncryption _encryptionService;

    @Autowired
    private IShoutContestService _shoutContestService;

    @Autowired
    private SubscriberUtil _subscriberUtil;

    @Autowired
    private IContestDaoMapper _dao;

    @Override
    public String getHandlerMessageType()
    {
        return "STORE_HANDLER";
    }

    @Override
    public CollectorEndpoint[] getCollectorEndpoints()
    {
        CollectorEndpointHandler[] handlers = new CollectorEndpointHandler[] {

                new CollectorEndpointHandler(new CollectorEndpoint("/store/getItemsForVenue", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        getStoreItemsForVenue(message.getProperties())),

//                //authorize.net
//                new CollectorEndpointHandler(new CollectorEndpoint("/store/purchaseItem", ConnectionType.ANY))
//                .withMessageHandlerFunction(
//                        (message, logMessageTag) ->
//                        purchaseStoreItem(message, message.getProperties())),
//
//                //authorize.net
//                new CollectorEndpointHandler(new CollectorEndpoint("/store/getCustomerProfile", ConnectionType.ANY))
//                .withMessageHandlerFunction(
//                        (message, logMessageTag) ->
//                        getCustomerProfile(message, message.getProperties())),
//
//                //authorize.net
//                new CollectorEndpointHandler(new CollectorEndpoint("/store/addPaymentMethod", ConnectionType.ANY))
//                .withMessageHandlerFunction(
//                        (message, logMessageTag) ->
//                        addPaymentMethod(message, message.getProperties())),

                //authorize.net
                //FUTxURE: /deleteCustomerProfile (store method already exists)
                //        /removePaymentMethod

                //ACH-eCheck
                new CollectorEndpointHandler(new CollectorEndpoint("/store/purchaseItemViaACH", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        purchaseItemViaACH(message, message.getProperties())),

                //ACH-eCheck
                new CollectorEndpointHandler(new CollectorEndpoint("/store/redeemViaACH", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        redeemViaACH(message, message.getProperties())),

//                //ITUNES, GOOGLE_PLAY, WIN_STORE
//                new CollectorEndpointHandler(new CollectorEndpoint("/store/purchaseItemViaInAppPurchaseReceipt", ConnectionType.ANY))
//                .withMessageHandlerFunction(
//                        (message, logMessageTag) ->
//                        purchaseItemViaInAppPurchaseReceipt(message, message.getProperties())),

                //used with ACH
                new CollectorEndpointHandler(new CollectorEndpoint("/store/bankAccount/create", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        createBankAccount(message, message.getProperties())),

                //used with ACH
                new CollectorEndpointHandler(new CollectorEndpoint("/store/bankAccount/retrieve", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        retrieveBankAccounts(message, message.getProperties())),

                //used with ACH
                new CollectorEndpointHandler(new CollectorEndpoint("/store/bankAccount/update", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        updateBankAccount(message, message.getProperties())),

                //used with ACH
                new CollectorEndpointHandler(new CollectorEndpoint("/store/bankAccount/delete", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        deleteBankAccount(message, message.getProperties())),

                new CollectorEndpointHandler(new CollectorEndpoint("/store/coupon/redeem", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        couponRedeem(message, message.getProperties())),

                new CollectorEndpointHandler(new CollectorEndpoint("/store/requestPayout", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        requestPayout(message, message.getProperties())),

                new CollectorEndpointHandler(new CollectorEndpoint("/store/braintree/getClientToken", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        braintreeGetClientToken(message, message.getProperties())),

                new CollectorEndpointHandler(new CollectorEndpoint("/store/braintree/addPaymentMethod", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        braintreeAddPaymentMethod(message, message.getProperties())),

                new CollectorEndpointHandler(new CollectorEndpoint("/store/braintree/getPaymentMethods", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        braintreeGetPaymentMethods(message, message.getProperties())),

                new CollectorEndpointHandler(new CollectorEndpoint("/store/braintree/deletePaymentMethod", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        braintreeDeletePaymentMethod(message, message.getProperties())),

                new CollectorEndpointHandler(new CollectorEndpoint("/store/braintree/purchaseViaNonce", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        braintreePurchaseViaNonce(message, message.getProperties())),

                new CollectorEndpointHandler(new CollectorEndpoint("/store/braintree/purchaseViaPaymentMethod", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        braintreePurchaseViaPaymentMethod(message, message.getProperties())),
        };

        for (CollectorEndpointHandler handler : handlers) {
            _collectorEndpointHandlerByPath.put(handler.getCollectorEndpoint().getPath(), handler);
        }

        return Arrays.stream(handlers)
                .map(CollectorEndpointHandler::getCollectorEndpoint)
                .toArray(CollectorEndpoint[]::new);
    }

    @Override
    public CollectorMessageResult createMessage(String requestPath, Map<String, String> requestHeaders, Map<String, String> requestParameters)
    throws BadRequestException
    {
        logCreateMessage();

        CollectorMessage m = new CollectorMessage();
        m.setMessageType(getHandlerMessageType());
        Map<String, String> props = createProps(requestPath, requestHeaders, requestParameters);

        // Add all allowed params to the collector message
        requestParameters.entrySet().stream()
            .filter(map -> validFormVars.contains(map.getKey()))
            .forEach(map -> props.put(map.getKey(), map.getValue()));

        m.setProperties(props);
        CollectorMessageResult result = new CollectorMessageResult(m);
        return result;
    }

    @Override
    public void handleMessage(CollectorMessage message)
    throws PublishResponseError
    {
        Map<String, String> props = message.getProperties();
        String requestPath = props.get("__requestPath");
        _logger.info("processing " + getHandlerMessageType() + " message: " + requestPath);

        String toWds = props.get(PARM_TO_WDS);

        CollectorEndpointHandler collectorEndpointHandler = Optional
                .ofNullable(_collectorEndpointHandlerByPath.get(requestPath))
                .orElseThrow(BadRequestException::new);

        String logMessageTag = getLogMessageTag(requestPath);

        Map<String,Object> extraResponseParms = collectorEndpointHandler.getMessageHandlerFunction().apply(message, logMessageTag);

        publishResponseWdsDoc(toWds, message.getMessageId(), logMessageTag, true, null, null, extraResponseParms);
    }

    @Override
    public HttpResponse handleSyncRequest(HttpRequest request)
    throws BadRequestException
    {
        throw new BadRequestException();
    }

    // HANDLER METHODS //

    /**
     * Get a list of all the items that can be purchased.
     *
     * COLLECTOR call:
     * <pre>/&lt;collector&gt;/getStoreItemsForVenue</pre>
     * Params:
     * <ul>
     *   <li>toWds - required. Must be calculated using the user's primaryIdHash. a hostname of where the document will be published (such as dc2-wds1.shoutgameplay.com)</li>
     *   <li>venue - optional. Defaults to "com.meinc.shout". Alternate venues are: "com.realmadrid.mymadrid", "com.carrusel.deportivo", "com.shout.dailymillionaire", and "tv.shout.shoutmillionaire"</li>
     *   <li>demo - optional. if present, means use the mock store service</li>
     * </ul>
     * On WDS success, a WDS document will be published in this location:
     * <pre>/&lt;ticket&gt;/response.json</pre>
     * It will have this form:
     * <pre>
     * {
     *     "success": true,
     *     "items": [
     *         {@link Item}
     *     ]
     * }
     * </pre>
     * On WDS failure, a WDS document will be published in this location:
     * <pre>/&lt;ticket&gt;/response.json</pre>
     * And it will have a success value, with ONE of the following reasons on failure (plus optionally a message attribute):
     * <pre>
     * {
     *     "success": false,
     *     "subscriberNotFound": true,
     *     "phoneNotVerified": true,
     *     "invalidParam": true, "message": string[paramName]
     * }
     * </pre>
     */
    private Map<String, Object> getStoreItemsForVenue(Map<String, String> props)
    {
        IStoreService storeService = props.containsKey("demo") ? _mockStoreService : _storeService;

        String venue = props.get("venue");
        List<Item> items = storeService.getAllActiveItems();
        List<Item> filteredItems = new ArrayList<Item>();

        //
        // Venue examples: com.shout.flashvote, com.shout.dailymillionaire, tv.shout.snowyowl
        //
        if (venue == null || venue.trim().length() == 0) {
            venue = "com.meinc.shout"; // DEFAULT
        }

        for (Item item : items) {
            if (item.getStoreBundleId().equals(venue)) {
                filteredItems.add(item);
            }
        }

        Map<String, Object> resultMap = new HashMap<String, Object>(1);
        resultMap.put("items", filteredItems);
        return resultMap;
    }

//    /**
//     * Purchase an item.
//     *
//     * COLLECTOR call:
//     * <pre>/&lt;collector&gt;/purchaseStoreItem</pre>
//     * Params:
//     * <ul>
//     *   <li>toWds - required. must be calculated using the user's primaryIdHash. a hostname of where the document will be published (such as dc2-wds1.shoutgameplay.com)</li>
//     *   <li>itemUuid - required. which item to purchase</li>
//     *   <li>nonce - required. The BrainTree single use reply nonce
//     *   <li>demo - optional. if present, means use the mock store service</li>
//     * </ul>
//     * On WDS success, a WDS document will be published in this location:
//     * <pre>/&lt;ticket&gt;/response.json</pre>
//     * It will have this form:
//     * <pre>
//     * {
//     *     "success": true,
//     *     "receipt": {
//     *       "receiptUuid":      [ String ]
//     *       "itemUuid":         [ String ]
//     *       "itemName":         [ String ]
//     *       "itemPrice":        [ Double ]
//     *       "purchaseType":     [ String ENUM ]
//     *       "purchaseCurrency": [ String ]
//     *       "purchaseDatetime": [ DateTime ISO 8601 yyyy-MM-ddTHH:mm:ss+0000 ]
//     *     }
//     * }
//     * </pre>
//     * On WDS failure, a WDS document will be published in this location:
//     * <pre>/&lt;ticket&gt;/response.json</pre>
//     * And it will have a success value, with ONE of the following reasons on failure (plus optionally a message attribute):
//     * <pre>
//     * {
//     *     "success": false,
//     *     "subscriberNotFound": true,
//     *     "phoneNotVerified": true,
//     *     "invalidParam": true, "message": string[paramName],
//     *     "unexpectedError" true, "message": string
//     * }
//     * </pre>
//     *
//     * @param message
//     * @param props bag
//     * @return result map
//     * @throws PublishResponseError
//     */
//    private Map<String, Object> purchaseStoreItem(CollectorMessage message, Map<String, String> props)
//    throws PublishResponseError
//    {
//        String messageId = message.getMessageId();
//
//        if (!props.containsKey("appId")) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "purchaseStoreItem", false, "missingRequiredParam", "appId");
//        }
//
//        IStoreService storeService = props.containsKey("demo") ? _mockStoreService : _storeService;
//
//        Subscriber subscriber = getSubscriber(message, props, "purchaseStoreItem");
//        String toWds = props.get(PARM_TO_WDS);
//        String itemUuid = getParamFromProps(props, messageId, "purchaseStoreItem", "itemUuid", true);
//
//        //i believe this is only stored by the 3rd party and passed back to us so we can tie their data to our user, so a subscriberId is all we need
//        String customerId = subscriber.getSubscriberId()+"";
//        Receipt receipt;
//
//        if (!props.containsKey("demo")) {
//            String nonce = getParamFromProps(props, messageId, "purchaseStoreItem", "nonce", false);
//            String customerProfileCreditCardInfoExternalRefId = getParamFromProps(props, messageId, "", "customerProfileCreditCardInfoExternalRefId", false);
//
//            if (nonce == null && customerProfileCreditCardInfoExternalRefId == null) {
//                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "purchaseStoreItem", false, "missingRequiredParam", "either nonce or customerProfileCreditCardInfoExternalRefId must be passed");
//            }
//            if (nonce != null && customerProfileCreditCardInfoExternalRefId != null) {
//                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "purchaseStoreItem", false, "unexpectedError", "either nonce or customerProfileCreditCardInfoExternalRefId must be passed, but not both");
//            }
//
//            try {
//                if (nonce != null) {
//                    receipt = storeService.purchaseItemViaNonce(subscriber.getSubscriberId(), customerId, itemUuid, nonce);
//                } else {
//                    receipt = storeService.purchaseViaCustomerProfile(subscriber.getSubscriberId(), itemUuid, customerProfileCreditCardInfoExternalRefId);
//                }
//            } catch (InvalidItemException e) {
//                _logger.warn(MessageFormat.format("subscriber {0,number,#} tried to purchase an invalid itemUuid: {1}", subscriber.getSubscriberId(), itemUuid));
//                throw new PublishResponseError(toWds, messageId, "purchaseStoreItem", false, "invalidParam", "itemUuid");
//            } catch (StoreException e) {
//                _logger.error(MessageFormat.format("subscriber {0,number,#} tried to purchase an itemUuid: {1}, but received an error from the store", subscriber.getSubscriberId(), itemUuid), e);
//                throw new PublishResponseError(toWds, messageId, "purchaseStoreItem", false, "unexpectedError", e.getMessage());
//            } catch (NoSuchCreditCardException e) {
//                _logger.error("purchasing via nonce yet got NoSuchCreditCardException!", e);
//                throw new PublishResponseError(toWds, messageId, "purchaseStoreItem", false, "unexpectedError", null);
//            }
//
//        } else {
//            //DEMO mode
//            try {
//                receipt = storeService.purchaseItemViaNonce(subscriber.getSubscriberId(), customerId, itemUuid, (String)null);
//            } catch (InvalidItemException | StoreException | NoSuchCreditCardException e) {
//                _logger.error(MessageFormat.format("failed purchaseStoreItem for item: {0}", itemUuid), e);
//                throw new PublishResponseError(toWds, messageId, "purchaseStoreItem", false, "unexpectedError", e.getMessage());
//            }
//        }
//
//        if (receipt != null) {
//            ReceiptItem receiptItem = storeService.getReceiptItemForReceiptId(receipt.getReceiptId());
//            if (receiptItem != null) {
//                Map<String, Object> resultMap = doPostPurchaseLogic(props, message.getMessageId(), subscriber, receipt, receiptItem, "BrainTree");
//                _shoutContestService.addCashPoolTransaction(
//                        subscriber.getSubscriberId(), receiptItem.getItemPrice(), CashPoolTransaction2.TYPE.PURCHASE, null, receipt.getReceiptId(), null);
//                return resultMap;
//
//            } else {
//                _logger.error(MessageFormat.format("failed purchaseStoreItem, receiptItem is null for item: {0}", itemUuid));
//                throw new PublishResponseError(toWds, messageId, "purchaseStoreItem", false, "unexpectedError", "receiptItem object null after store.purchaseItem");
//            }
//        }else{
//            _logger.error(MessageFormat.format("failed purchaseStoreItem, receipt is null for item: {0}", itemUuid));
//            throw new PublishResponseError(toWds, messageId, "purchaseStoreItem", false, "unexpectedError", "receipt object null after store.purchaseItem");
//        }
//
//    }
//
//    private Map<String, Object> getCustomerProfile(CollectorMessage message, Map<String, String> props)
//    throws PublishResponseError
//    {
//        Subscriber subscriber = getSubscriber(message, props, "getCustomerProfile");
//
//        CustomerProfile cp = getCustomerProfile(subscriber.getSubscriberId(), props, message.getMessageId(), "getCustomerProfile");
//
//        //it's entirely possible that the customer profile is null. clients must expect this
//        return new FastMap<>("customerProfile", cp);
//    }
//
//    private Map<String, Object> addPaymentMethod(CollectorMessage message, Map<String, String> props)
//    throws PublishResponseError
//    {
//        Subscriber subscriber = getSubscriber(message, props, "addPaymentMethod");
//        String nonce = getParamFromProps(props, message.getMessageId(), "addPaymentMethod", "nonce", true);
//
//        //see if there's an existing customer profile. if so, add a payment method, otherwise create a new customer profile
//        CustomerProfile cp = getCustomerProfile(subscriber.getSubscriberId(), props, message.getMessageId(), "addPaymentMethod");
//        try {
//            if (cp == null) {
//                _storeService.createCustomerProfile(subscriber.getSubscriberId(), nonce);
//            } else {
//                _storeService.addPaymentMethodToCustomerProfile(subscriber.getSubscriberId(), nonce);
//            }
//        } catch (StoreException e) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), "addPaymentMethod", false, "unexpectedError", e.getMessage());
//        }
//
//        return null;
//    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> purchaseItemViaACH(CollectorMessage message, Map<String, String> props)
    throws PublishResponseError
    {
        String logTag = "purchaseItemViaACH";
        Subscriber subscriber = getSubscriber(message, props, logTag);
        String itemUuid = getParamFromProps(props, message.getMessageId(), logTag, "itemUuid", true);

        String emailAddress = subscriber.getEmail();

        String phone = getParamFromProps(props, message.getMessageId(), logTag, "phone", false); //this MUST be in ########## format
        if (StringUtil.isEmpty(phone)) {
            phone = subscriber.getPhone();
        } else {
            //FUTURE: store the phone#
        }
        if (StringUtil.isEmpty(phone)) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "missingRequiredField", "phone");
        }

        //grab the address
        int addressId = getIntParamFromProps(props, message.getMessageId(), logTag, "addressId", true);
        SubscriberAddress address =
                _identityService.getSubscriberAddresses(subscriber.getSubscriberId()).stream()
                .filter(a -> a.getType() == SubscriberAddress.ADDRESS_TYPE.BILLING)
                .filter(a -> a.getAddressId() == addressId)
                .findFirst()
                .orElseThrow(() -> new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "invalidParam", "address"));

        //grab the bank account info
        String bankAccountDbId = getParamFromProps(props, message.getMessageId(), logTag, "accountId", true);

        BankAccount account = ((List<BankAccount>) wrapInTransaction(this::retrieveBankAccountsDao, subscriber) ).stream()
            .filter(a -> a.getId().equals(bankAccountDbId))
            .findFirst()
            .orElseThrow(() -> new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "invalidParam", "accountId"));

        //the checkDate must be converted to mm/dd/yyyy format
        Date now = new Date();
        String checkDate = new SimpleDateFormat("MM/dd/yyyy").format(now);

        //look up the item to get the amount
        double checkAmount = 0D;
        Item item = _storeService.getItemByUuid(itemUuid);
        if (item == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "invalidParam", "itemUuid");
        }
        for (ItemPrice itemPrice : item.getItemPrice()) {
            if ("USD".equals(itemPrice.getCurrencyCode())) {
                checkAmount = itemPrice.getPrice();
                break;
            }
        }
        if (checkAmount == 0D) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "unexpectedError", "itemUuid item has no price!");
        }

        //use subscriberId|timestamp. this will make it unique and allow multiple "identical" purchases to be made on the same day if someone wanted to
        String memo = subscriber.getSubscriberId() + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS").format(now);

        //make the API call to their server
        String postUrl = _echeckEndpoint + "/OneTimeDraftRTV";
        Map<String, String> postParams = new FastMap<>(
            "Client_ID", _echeckClientId,
            "ApiPassword", _echeckApiPassword,
            "Name", account.getCheckingAccountName(),
            "EmailAddress", emailAddress,
            "Phone", phone,
            "PhoneExtension", "",
            "Address1", address.getAddr1(),
            "City", address.getCity(),
            "State", address.getState(),
            "Zip", address.getZip(),
            "Country", address.getCountryCode(),
            "RoutingNumber", decryptVal(subscriber, account.getRoutingNumber()),
            "AccountNumber", decryptVal(subscriber, account.getAccountNumber()),
            "BankName", account.getBankName(),
            "CheckMemo", memo,
            "CheckAmount", new Double(checkAmount).toString(),
            "CheckDate", checkDate,
            "CheckNumber" , "",
            "x_delim_data", "",
            "x_delim_char", ""
        );

        if (!StringUtil.isEmpty(address.getAddr2())) {
            postParams.put("Address2", address.getAddr2());
        } else {
            postParams.put("Address2", "");
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug(MessageFormat.format(
                    "about to use greenMoney to purchase an item. sId: {0}, accountId: {1}, addressId: {2}, amount: {3}",
                    subscriber.getSubscriberId(), account.getId(), addressId, checkAmount));
        }

        String response = httpsPost(postUrl, null, postParams);

        if (_logger.isDebugEnabled()) {
            _logger.debug(MessageFormat.format("response from post to {0}, using {1}:\n{2}", postUrl, postParams, response));
        }

        //check the response to see if it was successful
        Document doc = StringUtil.convertStringToDocument(response);
        int eResult = Integer.parseInt(StringUtil.getNodeValue(doc, "Result"));
        int eVerifyResult = Integer.parseInt(StringUtil.getNodeValue(doc, "VerifyResult"));

        if (eResult != 0 || eVerifyResult != 0) {
            String eResultDesc = StringUtil.getNodeValue(doc, "ResultDescription");
            String eVerifyResultDesc = StringUtil.getNodeValue(doc, "VerifyResultDescription");
            _logger.warn(MessageFormat.format(
                    "there was a problem with the echeck. resultCode: {0}, result: {1}, verifyResultCode: {2}, verifyResult: {3}",
                    eResult, eResultDesc, eVerifyResult,  eVerifyResultDesc));

            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "unexpectedError",
                eResult != 0 ? eResultDesc : eVerifyResultDesc);
        }

        //this is copied from PowerupService.createReceipt
        Receipt r;
        try {
            r = createReceipt(subscriber.getSubscriberId(), itemUuid, "INTERNAL", null, null);

            //set the store uid to something here. it doesn't really matter what. we'll use the same value as the memo field
            r.setStoreUid(memo);

        } catch (InvalidItemException | InvalidJsonException | StoreException e) {
            _logger.error("unable to create receipt", e);
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "unexpectedError", e.getMessage());
        }

        //add the receipt to the store
        return manuallyAddReceiptToStore(subscriber, response, r, message, props, logTag, "GreenMoney eCheck", false);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> redeemViaACH(CollectorMessage message, Map<String, String> props)
    throws PublishResponseError
    {
        String logTag = "redeemViaACH";
        Subscriber subscriber = getSubscriber(message, props, logTag);

        //see what the usable balance is for the subscriber (this means how much they have minus any bonuses they were given that can't be withdrawn).
        // the amount they withdraw can't exceed this amount
        Double availableBalance = _shoutContestService.getAvailableBalance(subscriber.getSubscriberId());
        if (availableBalance == null) {
            availableBalance = 0D;
        }

        double amountToWithdraw = getDoubleParamFromProps(props, message.getMessageId(), logTag, "amount", true);
        if (amountToWithdraw > availableBalance) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "overdraft", availableBalance+"");
        }

        //there might also be outstanding manual redemption requests that could make the amount invalid
        float outstandingManualRedemptionAmount = (Float) wrapInTransaction(this::getOutstandingManualRedemptionAmount, new Object[] {subscriber.getSubscriberId()});
        if (amountToWithdraw > (availableBalance - outstandingManualRedemptionAmount)) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "insufficientFundsWithOtherOutstandingManualRedemptionRequests");
        }

        //grab the bank account info
        String bankAccountDbId = getParamFromProps(props, message.getMessageId(), logTag, "accountId", true);

        BankAccount account = ((List<BankAccount>) wrapInTransaction(this::retrieveBankAccountsDao, subscriber) ).stream()
                .filter(a -> a.getId().equals(bankAccountDbId))
                .findFirst()
                .orElseThrow(() -> new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "invalidParam", "accountId"));

        //the checkDate must be converted to mm/dd/yyyy format
        String checkDate = new SimpleDateFormat("MM/dd/yyyy").format(new Date());

        //grab their address
        int addressId = getIntParamFromProps(props, message.getMessageId(), logTag, "addressId", true);
        SubscriberAddress address =
                _identityService.getSubscriberAddresses(subscriber.getSubscriberId()).stream()
                .filter(a -> a.getType() == SubscriberAddress.ADDRESS_TYPE.BILLING)
                .filter(a -> a.getAddressId() == addressId)
                .findFirst()
                .orElseThrow(() -> new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "invalidParam", "addressId"));

        //make the API call to their server
        String postUrl = _echeckEndpoint + "/BillPayCheck";
        Map<String, String> postParams = new FastMap<>(
            "Client_ID", _echeckClientId,
            "ApiPassword", _echeckApiPassword,
            "Name", account.getCheckingAccountName(),
            "Address1", address.getAddr1(),
            "City", address.getCity(),
            "State", address.getState(),
            "Zip", address.getZip(),
            "Country", address.getCountryCode(),
            "RoutingNumber", decryptVal(subscriber, account.getRoutingNumber()),
            "AccountNumber", decryptVal(subscriber, account.getAccountNumber()),
            "BankName", account.getBankName(),
            "CheckMemo", "",
            "CheckAmount", new Double(amountToWithdraw).toString(),
            "CheckNumber", "",
            "CheckDate", checkDate,
            "x_delim_data", "",
            "x_delim_char", ""
        );

        if (!StringUtil.isEmpty(address.getAddr2())) {
            postParams.put("Address2", address.getAddr2());
        } else {
            postParams.put("Address2", "");
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug(MessageFormat.format(
                    "about to use greenMoney to fulfill a payout. sId: {0}, accountId: {1}, addressId: {2}, amount: {3}",
                    subscriber.getSubscriberId(), account.getId(), addressId, amountToWithdraw));
        }

        String response = httpsPost(postUrl, null, postParams);
        if (_logger.isDebugEnabled()) {
            _logger.debug(MessageFormat.format("response from post to {0} using: {1}:\n{2}", postUrl, postParams, response));
        }

        /*
        <?xml version="1.0" encoding="utf-8"?>
        <BillPayCheckResult xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns="CheckProcessing">
            <Result>0</Result>
            <ResultDescription>Data Accepted.</ResultDescription>
            <CheckNumber>8615999</CheckNumber>
            <Check_ID>2653999</Check_ID>
        </BillPayCheckResult>
         */

        //check the response to see if it was successful
        Document doc = StringUtil.convertStringToDocument(response);
        int eResult = Integer.parseInt(StringUtil.getNodeValue(doc, "Result"));
        if (eResult != 0) {
            String eResultDesc = StringUtil.getNodeValue(doc, "ResultDescription");
            _logger.error(MessageFormat.format(
                    "there was a problem with the echeck. resultCode: {0}, result: {1}",
                    eResult, eResultDesc));
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "unexpectedError", eResultDesc);
        }

        String checkNumber = StringUtil.getNodeValue(doc, "CheckNumber");
        String checkId = StringUtil.getNodeValue(doc, "Check_ID");
        String externalRefId = MessageFormat.format("greenMoney|checkNumber:{0}|checkId:{1}", checkNumber, checkId);

        //it was successful. deduct that amount from their balance
        _shoutContestService.addCashPoolTransaction(
                subscriber.getSubscriberId(), -amountToWithdraw, CashPoolTransaction2.TYPE.PAID, externalRefId, null, null);

        return null;
    }

//    private Map<String, Object> purchaseItemViaInAppPurchaseReceipt(CollectorMessage message, Map<String, String> props)
//    throws PublishResponseError
//    {
//        String logTag = "purchaseItemViaInAppPurchaseReceipt";
//        Subscriber subscriber = getSubscriber(message, props, logTag);
//
//        String storeType = getParamFromProps(props, message.getMessageId(), logTag, "storeType", true);
//        if (!"ITUNES".equals(storeType) && !"GOOGLE_PLAY".equals(storeType) && !"WIN_STORE".equals(storeType)) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "invalidParam", "storeType");
//        }
//
//        String itemUuid = getParamFromProps(props, message.getMessageId(), logTag, "itemUuid", true);
//        String receipt = getParamFromProps(props, message.getMessageId(), logTag, "receipt", true);
//        String signature = getParamFromProps(props, message.getMessageId(), logTag, "signature", "GOOGLE_PLAY".equals(storeType));
//
//        //this is copied from PowerupService.createReceipt
//        Receipt r;
//        try {
//            r = createReceipt(subscriber.getSubscriberId(), itemUuid, storeType, receipt, signature);
//        } catch (InvalidItemException | InvalidJsonException | StoreException e) {
//            _logger.error("unable to create receipt", e);
//            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "unexpectedError", e.getMessage());
//        }
//
//        //add the receipt to the store
//        return manuallyAddReceiptToStore(subscriber, receipt, r, message, props, logTag);
//    }

    private Map<String, Object> createBankAccount(CollectorMessage message, Map<String, String> props)
    throws PublishResponseError
    {
        String logTag = "createBankAccount";
        Subscriber subscriber = getSubscriber(message, props, logTag);

        BankAccount account = getJsonObjectFromProps(
            props, message.getMessageId(), logTag, "account", true, new TypeReference<BankAccount>(){});

        account.setId(UUID.randomUUID().toString());
        account.setSubscriberId(subscriber.getSubscriberId());

        //encrypt the routingNumber and accountNumber
        String routingNumber = account.getRoutingNumber();
        String encryptedRoutingNumber = encryptVal(subscriber, routingNumber);
        account.setRoutingNumber(encryptedRoutingNumber);

        String accountNumber = account.getAccountNumber();
        String encryptedAccountNumber = encryptVal(subscriber, accountNumber);
        account.setAccountNumber(encryptedAccountNumber);

        //add to the db
        wrapInTransaction(this::createBankAccountDao, account);

        //return the partially obscured account and routing numbers
        account.setRoutingNumber(replaceChars(routingNumber, '*', 4));
        account.setAccountNumber(replaceChars(accountNumber, '*', 4));

        return new FastMap<>(
            "account", account
        );
    }

    private Void createBankAccountDao(Object params)
    {
        BankAccount account = (BankAccount) params;
        _dao.createBankAccount(account);
        return null;
    }

    private Map<String, Object> retrieveBankAccounts(CollectorMessage message, Map<String, String> props)
    throws PublishResponseError
    {
        String logTag = "retrieveBankAccounts";
        Subscriber subscriber = getSubscriber(message, props, logTag);

        @SuppressWarnings("unchecked")
        List<BankAccount> accounts = (List<BankAccount>) wrapInTransaction(this::retrieveBankAccountsDao, subscriber);

        for (BankAccount account : accounts) {
            //replace the encrypted routing and account numbers with a partially obscured values
            String encryptedRoutingNumber = account.getRoutingNumber();
            String routingNumber = decryptVal(subscriber, encryptedRoutingNumber);
            account.setRoutingNumber(replaceChars(routingNumber, '*', 4));

            String encryptedAccountNumber = account.getAccountNumber();
            String accountNumber = decryptVal(subscriber, encryptedAccountNumber);
            account.setAccountNumber(replaceChars(accountNumber, '*', 4));
        }

        return new FastMap<>(
            "accounts", accounts
        );
    }

    private List<BankAccount> retrieveBankAccountsDao(Object params)
    {
        long subscriberId = ((Subscriber)params).getSubscriberId();
        return _dao.retrieveBankAccounts(subscriberId);
    }

    private Map<String, Object> updateBankAccount(CollectorMessage message, Map<String, String> props)
    throws PublishResponseError
    {
        String logTag = "updateBankAccount";
        Subscriber subscriber = getSubscriber(message, props, logTag);

        BankAccount account = getJsonObjectFromProps(
            props, message.getMessageId(), logTag, "account", true, new TypeReference<BankAccount>(){});

        //encrypt the routingNumber and accountNumber
        String routingNumber = account.getRoutingNumber();
        String encryptedRoutingNumber = encryptVal(subscriber, routingNumber);
        account.setRoutingNumber(encryptedRoutingNumber);

        String accountNumber = account.getAccountNumber();
        String encryptedAccountNumber = encryptVal(subscriber, accountNumber);
        account.setAccountNumber(encryptedAccountNumber);

        wrapInTransaction(this::updateBankAccountDao, account);

        return null;
    }

    private Void updateBankAccountDao(Object params)
    {
        BankAccount account = (BankAccount) params;
        _dao.updateBankAccount(account);
        return null;
    }

    private Map<String, Object> deleteBankAccount(CollectorMessage message, Map<String, String> props)
    throws PublishResponseError
    {
        String logTag = "deleteBankAccount";

        String bankAccountId = getParamFromProps(props, message.getMessageId(), logTag, "accountId", true);

        wrapInTransaction(this::deleteBankAccountDao, bankAccountId);

        return null;
    }

    private Void deleteBankAccountDao(Object params)
    {
        String bankAccountId = (String) params;
        _dao.deleteBankAccount(bankAccountId);
        return null;
    }

    private Map<String, Object> couponRedeem(CollectorMessage message, Map<String, String> props)
    throws PublishResponseError
    {
        String logTag = "couponRedeem";
        Subscriber subscriber = getSubscriber(message, props, logTag);

        String couponCode = getParamFromProps(props, message.getMessageId(), logTag, "couponCode", true);
        CouponCode coupon = (CouponCode) wrapInTransaction(this::getCouponCodeInTransaction, couponCode);
        if (coupon == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "invalidParam", "couponCode");
        }

        if (coupon.getExpireDate() != null && coupon.getExpireDate().before(new Date())) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "couponExpired");
        }

        if (coupon.isCancelled()) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "couponCancelled");
        }

        if (coupon.getRedeemedDate() != null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "couponAlreadyRedeemed");
        }

        _shoutContestService.markCouponAsRedeemed(coupon.getCouponCode(), subscriber.getSubscriberId());
        _shoutContestService.addCashPoolTransaction(
                subscriber.getSubscriberId(), coupon.getAmount(), CashPoolTransaction2.TYPE.BONUS, "redeemed coupon", null, null);

        return null;
    }

    private CouponCode getCouponCodeInTransaction(Object param)
    {
        String couponCode = (String) param;
        return _dao.getCouponCode(couponCode);
    }

    private Map<String, Object> requestPayout(CollectorMessage message, Map<String, String> props)
    throws PublishResponseError
    {
        throw new UnsupportedOperationException();
//        String logTag = "requestPayout";
//        Subscriber subscriber = getSubscriber(message, props, logTag);
//
//        String type = getParamFromProps(props, message.getMessageId(), logTag, "type", true);
//        float amount = getFloatParamFromProps(props, message.getMessageId(), logTag, "amount", true);
//
//        //make sure the subscriber has enough funds to cover the withdrawl
//        Double availableBalance = _shoutContestService.getAvailableBalance(subscriber.getSubscriberId());
//        if (availableBalance == null) availableBalance = 0D;
//
//        if (amount > availableBalance) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "insufficientFunds", null);
//        }
//
//        switch (type)
//        {
//            case "MANUAL": {
//                //there might also be outstanding manual redemption requests that could make the amount invalid
//                float outstandingManualRedemptionAmount = (Float) wrapInTransaction(this::getOutstandingManualRedemptionAmount, new Object[] {subscriber.getSubscriberId()});
//                if (amount > (availableBalance - outstandingManualRedemptionAmount)) {
//                    throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "insufficientFundsWithOtherOutstandingManualRedemptionRequests", null);
//                }
//                wrapInTransaction(this::requestPayoutManualDao, new Object[] {subscriber.getSubscriberId(), amount});
//            }
//            break;
//
//            default:
//                throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "invalidParam", "type");
//        }
//
//        return null;
    }

//    //this must be wrapped in a transaction
//    private Void requestPayoutManualDao(Object params)
//    {
//        int subscriberId = (int) ((Object[]) params)[0];
//        float amount = (float) ((Object[]) params)[1];
//
//        _dao.addManualRedeemRequest(subscriberId, amount);
//
//        return null;
//    }

    private Map<String, Object> braintreeGetClientToken(CollectorMessage message, Map<String, String> props)
    throws PublishResponseError
    {
        String logTag = "braintreeGetClientToken";
        Subscriber subscriber = getSubscriber(message, props, logTag);
        return new FastMap<>("clientToken", _storeService.getClientToken(subscriber.getSubscriberId()));
    }

    private Map<String, Object> braintreeAddPaymentMethod(CollectorMessage message, Map<String, String> props)
    throws PublishResponseError
    {
        String logTag = "braintreeAddPaymentMethod";
        Subscriber subscriber = getSubscriber(message, props, logTag);
        String nonce = getParamFromProps(props, message.getMessageId(), logTag, "nonce", true);
        Boolean dflt = getBooleanParamFromProps(props, message.getMessageId(), logTag, "makeDefault", false);
        String firstname = getParamFromProps(props,  message.getMessageId(), logTag, "firstname", true);
        String lastname = getParamFromProps(props,  message.getMessageId(), logTag, "lastname", true);

        String paymentMethodToken;
        try {
            paymentMethodToken = _storeService.addPaymentMethodToCustomerProfile(subscriber.getSubscriberId(), nonce, dflt == null ? false : dflt, firstname, lastname);
        } catch (StoreException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "storeException", e.getMessage());
        }

        return new FastMap<>("paymentMethodToken", paymentMethodToken);
    }

    private Map<String, Object> braintreeGetPaymentMethods(CollectorMessage message, Map<String, String> props)
    throws PublishResponseError
    {
        String logTag = "braintreeGetPaymentMethods";
        Subscriber subscriber = getSubscriber(message, props, logTag);
        List<CreditCardInfo> ccInfo;

        try {
            ccInfo = _storeService.getPaymentMethodsForCustomerProfile(subscriber.getSubscriberId());
            if (ccInfo == null) ccInfo = new ArrayList<>();
        } catch (StoreException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "storeException", e.getMessage());
        }

        return new FastMap<>("paymentMethods", ccInfo);
    }

    private Map<String, Object> braintreeDeletePaymentMethod(CollectorMessage message, Map<String, String> props)
    throws PublishResponseError
    {
        String logTag = "braintreeDeletePaymentMethod";
        String paymentMethodToken = getParamFromProps(props, message.getMessageId(), logTag, "paymentMethodToken", true);

        try {
            _storeService.deletePaymentMethod(paymentMethodToken);
        } catch (StoreException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "storeException", e.getMessage());
        }

        return null;
    }

    private Map<String, Object> braintreePurchaseViaNonce(CollectorMessage message, Map<String, String> props)
    throws PublishResponseError
    {
        String logTag = "braintreePurchaseViaNonce";
        Subscriber subscriber = getSubscriber(message, props, logTag);
        String itemUuid = getParamFromProps(props, message.getMessageId(), logTag, "itemUuid", true);
        String nonce = getParamFromProps(props, message.getMessageId(), logTag, "nonce", true);
        String firstname = getParamFromProps(props,  message.getMessageId(), logTag, "firstname", true);
        String lastname = getParamFromProps(props,  message.getMessageId(), logTag, "lastname", true);

        Receipt receipt;
        try {
            receipt = _storeService.purchaseItemViaNonce(subscriber.getSubscriberId(), null, itemUuid, nonce, firstname, lastname);
        } catch (InvalidItemException | NoSuchCreditCardException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "storeException", e.getMessage());
        } catch (StoreException e) {
            if (e.getCodeMsgMap() == null) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "storeException", e.getMessage());
            } else {
                throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "storeException", e.getCodeMsgMap());
            }
        }

        //add the receipt to the store
        return manuallyAddReceiptToStore(subscriber, receipt.toString(), receipt, message, props, logTag, "Braintree", true);
    }

    private Map<String, Object> braintreePurchaseViaPaymentMethod(CollectorMessage message, Map<String, String> props)
    throws PublishResponseError
    {
        String logTag = "braintreePurchaseViaPaymentMethod";
        Subscriber subscriber = getSubscriber(message, props, logTag);
        String itemUuid = getParamFromProps(props, message.getMessageId(), logTag, "itemUuid", true);
        String paymentMethodToken = getParamFromProps(props, message.getMessageId(), logTag, "paymentMethodToken", true);

        Receipt receipt;
        try {
            receipt = _storeService.purchaseViaCustomerProfile(subscriber.getSubscriberId(), itemUuid, paymentMethodToken);
        } catch (InvalidItemException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "storeException", e.getMessage());
        } catch (StoreException e) {
            if (e.getCodeMsgMap() == null) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "storeException", e.getMessage());
            } else {
                throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "storeException", e.getCodeMsgMap());
            }
        }

        //add the receipt to the store
        return manuallyAddReceiptToStore(subscriber, receipt.toString(), receipt, message, props, logTag, "Braintree", true);
    }

    private Float getOutstandingManualRedemptionAmount(Object params)
    {
        int subscriberId = (int) ((Object[]) params)[0];
        Float amount = _dao.getOutstandingManualRedeemRequestAmounts(subscriberId);
        return amount == null ? 0.0F : amount;
    }

    // HELPER METHODS //

    private Map<String, Object> manuallyAddReceiptToStore(
        Subscriber subscriber, String receiptAsStr, Receipt r, CollectorMessage message, Map<String, String> props, String logTag, String purchaseDescription, boolean skipAddToStoreService)
    {
        Receipt receipt;
        if (!skipAddToStoreService) {
            //add the receipt to the store (it will verify the receipt with the appropriate store backend)
            int retries = 1;
            ReceiptResult receiptResult = null;
            while (retries >= 0) {
                retries--;
                //add the receipt to the store
                try {
                    receiptResult = _storeService.addReceipt(r);

                } catch (InvalidItemException e) {
                    throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "unexpectedError", e.getMessage());
                }

                if (receiptResult.isDuplicateReceiptError()) {
                    throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "duplicateReceipt");

                } else if (receiptResult.isMalformedReceiptError()) {
                    _logger.error("store receipt was malformed:\n" + receiptAsStr);
                    throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "malformedReceipt");

                } else if (receiptResult.isInvalidReceiptError()) {
                    throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "invalidReceipt",
                            String.format("receipt %s for item %s and store %s is not valid", receiptAsStr, r.getItemUuid(), r.getType().name()));

                } else if (receiptResult.getStoreReceipt() == null) {
                    if (receiptResult.getRetryError() != null) {
                        //possibly recoverable error via a retry
                        try { Thread.sleep(500); } catch (InterruptedException e) {}
                    } else {
                        _logger.error("receipt failed verification: 1");
                        throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "unexpectedError", "receipt failed verification");
                    }
                } else {
                    break;
                }
            }

            if (receiptResult == null || receiptResult.getStoreReceipt() == null) {
                _logger.error("receipt failed verification: 2");
                throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "unexpectedError", "receipt failed verification");
            }

            receipt = receiptResult.getStoreReceipt();

        } else {
            receipt = r;
        }

        //add the cash pool records and return the receipt
        ReceiptItem receiptItem = _storeService.getReceiptItemForReceiptId(receipt.getReceiptId());
        if (receiptItem == null) {
            _logger.error("no ReceiptItem for receipt id: " + receipt.getReceiptId());
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), logTag, false, "unexpectedError", "no ReceiptItem for receipt");
        }

        Map<String, Object> resultMap = doPostPurchaseLogic(props, message.getMessageId(), logTag, subscriber, receipt, receiptItem, purchaseDescription);
        return resultMap;
    }

    /**
     * A simple wrapper around performing an HTTP POST operation. A production system should use a more robust 3rd party HTTP library.
     *
     * @param postUrl the endpoint
     * @param headers any headers to send along with the request. The Content-Type header will be added automatically
     * @param params the parameters to pass along on the request
     */
    private String httpsPost(String postUrl, Map<String, String> headers, Map<String, String> params)
    {
        try {
            URL obj = new URL(postUrl);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection(); //NOTE: for production, this should be using https, and thus HttpsURLConnection
            con.setRequestMethod("POST");

            //add any headers
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    con.setRequestProperty(header.getKey(), header.getValue());
                }
            }

            //build up the form params
            //a string in this form:
            // param1=value&param2=value
            StringBuilder buf = new StringBuilder();
            if (params != null) {
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                boolean first = true;
                for (Map.Entry<String, String> param : params.entrySet()) {
                    if (!first) {
                        buf.append("&");
                    } else {
                        first = false;
                    }

                    buf.append(param.getKey()).append("=").append(encode(param.getValue()));
                }
            }

            //send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(buf.toString());
            wr.flush();
            wr.close();

            //get the response
            int responseCode = con.getResponseCode();

            //read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            if (responseCode == 200) {
                return response.toString();
            } else {
                return MessageFormat.format("non 200 response. code: {0}, headers: {1}, body:\n{2}", responseCode, con.getHeaderFields(), response.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String encode(String value)
    {
        if (value == null) return null;
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException impossible) {
            //if utf-8 isn't supported, there are bigger problems to deal with
            return value;
        }
    }

    private Subscriber getSubscriber(CollectorMessage message, Map<String, String> props, String logTag)
    throws PublishResponseError
    {
        Subscriber subscriber;
        SubscriberUtil.SubscriberResponse response = _subscriberUtil.getSubscriberFromSession(props);
        if (response.subscriber == null) {
            throw new PublishResponseError(
                props.get(PARM_TO_WDS),
                message.getMessageId(),
                logTag,
                false,
                "subscriberNotFound",
                response.noSubscriberReason.toString()
            );
        } else {
            subscriber = response.subscriber;
        }

        return subscriber;
    }

    private Map<String, Object> doPostPurchaseLogic(Map<String, String> props, String messageId, String logTag, Subscriber subscriber, Receipt receipt, ReceiptItem receiptItem, String purchaseDescription)
    {
        Integer contextId = _shoutContestService.getContextId(props);
        if (contextId == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, logTag, false, "invalidParam", "appId");
        }
//        String gameEngineName = "APP:" + contextId;

        Map<String, Object> resultMap = new HashMap<String, Object>(1);
        Map<String, Object> receiptMap = new HashMap<String, Object>(1);

        addReceiptItemToMap(receiptMap, receiptItem);
        resultMap.put("receipt", receiptMap);

//        try {
//            // Enqueue a trigger to register the item purchased event
//            String receiptItemJson = _jsonMapper.writeValueAsString(receiptItem);
//            _triggerService.enqueue(
//                    IShoutContestAwardService.USER_EVENT_KEY_item_purchased,
//                    receiptItemJson,
//                    IShoutContestAwardService.TRIGGER_SERVICE_ROUTE,
//                    gameEngineName,
//                    _shoutContestService.getAppBundleIds(contextId),
//                    contextId);
//        } catch (JsonProcessingException e) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, logTag, false, "unexpectedError", e.getMessage());
//        }

        _shoutContestService.addCashPoolTransaction(
                subscriber.getSubscriberId(), receiptItem.getItemPrice(), CashPoolTransaction2.TYPE.PURCHASE, purchaseDescription, receipt.getReceiptId(), null);

        return resultMap;
    }

    private void addReceiptItemToMap(Map<String, Object> map, ReceiptItem item)
    {
        // map.put("receiptId", item.getReceiptId()); // Skip adding this redundant ID for client usage
        map.put("receiptUuid", item.getReceiptUuid());
        map.put("itemUuid", item.getItemUuid());
        map.put("itemName", item.getItemName());
        map.put("itemPrice", item.getItemPrice());
        map.put("purchaseType", item.getPurchaseType());
        map.put("purchaseCurrency", item.getPurchaseCurrency());
        map.put("purchaseDatetime", DateUtil.dateToIso8601(item.getPurchaseDate()));
    }

//    private CustomerProfile getCustomerProfile(int subscriberId, Map<String, String> props, String messageId, String logTag)
//    throws PublishResponseError
//    {
//        CustomerProfile cp;
//        try {
//            cp = _storeService.getCustomerProfile(subscriberId);
//        } catch (StoreException e) {
//            _logger.error(MessageFormat.format("unable to retrieve subscriberProfile for sId: {0,number,#}", subscriberId), e);
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, logTag, false, "unexpectedError", e.getMessage());
//        }
//        return cp;
//    }

    private Receipt createReceipt(long subscriberId, String itemUuid, String storeType, String receipt, String signature)
    throws InvalidItemException, InvalidJsonException, StoreException
    {
        //create the receipt
        Receipt r = new Receipt();
        r.setItemUuid(itemUuid);
        r.setSubscriberId(subscriberId);

        switch (storeType)
        {
            case "ITUNES":
                r.setType(ReceiptType.ITUNES);
                r.setSkipVerify(false);
                r.setPayload(Receipt.createItunesPayload(receipt, null).getBytes());
                break;

            case "GOOGLE_PLAY":
                r.setType(ReceiptType.GPLAY_ONETIME);
                r.setSkipVerify(false);
                r.setPayload(Receipt.createGooglePlayOneTimePayload(receipt, signature).getBytes());
                break;

            case "WIN_STORE":
                r.setType(ReceiptType.WIN_STORE);
                r.setSkipVerify(false);
                r.setPayload(Receipt.createWindowsStorePayload(receipt, null).getBytes());
                break;

            case "INTERNAL":
                r.setType(ReceiptType.INTERNAL);
                r.setSkipVerify(true);
                r.setPayload(Receipt.createInternalPayload(null, "eCheck").getBytes());
        }

        return r;
    }

    private String encryptVal(Subscriber s, String val)
    {
        String key = s.getEmailSha256Hash().substring(0, 32);
        key = key.replace("-", "");
        key = new StringBuilder(key).reverse().toString();
        char c1 = key.charAt(5);
        char c2 = key.charAt(9);
        key = new StringBuilder().append(key.substring(0, 5)).append(c2).append(key.substring(6,9)).append(c1).append(key.substring(10)).toString();
        String pp = key.substring(0, 16);
        String iv = key.substring(16);
        try {
            return _encryptionService.aesEncrypt(val, "UTF-8", pp, iv);
        } catch (GeneralSecurityException | IOException e) {
            _logger.error("unable to encrypt", e);
            return val;
        }
    }

    private String decryptVal(Subscriber s, String val)
    {
        String key = s.getEmailSha256Hash().substring(0, 32);
        key = key.replace("-", "");
        key = new StringBuilder(key).reverse().toString();
        char c1 = key.charAt(5);
        char c2 = key.charAt(9);
        key = new StringBuilder().append(key.substring(0, 5)).append(c2).append(key.substring(6,9)).append(c1).append(key.substring(10)).toString();
        String pp = key.substring(0, 16);
        String iv = key.substring(16);
        try {
            return _encryptionService.aesDecrypt(val, "UTF-8", pp, iv);
        } catch (GeneralSecurityException | IOException e) {
            _logger.error("unable to decrypt", e);
            return val;
        }
    }

    private String replaceChars(String input, char replaceChar, int numCharsToLeave)
    {
        if (input == null || input.length() <= numCharsToLeave) return input;

        StringBuilder buf = new StringBuilder();
        for (int i=0; i<input.length()-numCharsToLeave; i++) {
            buf.append("*");
        }
        buf.append(input.substring(input.length()-numCharsToLeave));

        return buf.toString();
    }
}
