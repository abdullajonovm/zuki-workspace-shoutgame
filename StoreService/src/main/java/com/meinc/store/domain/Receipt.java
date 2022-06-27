package com.meinc.store.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.meinc.store.exception.InvalidJsonException;

public class Receipt implements Serializable, Cloneable
{
    private static final long serialVersionUID = 2009686508115470514L;

//    private static Logger _logger = Logger.getLogger(Receipt.class);

    //TODO split credit_card type into two, one for recurring the other for one-time
    public enum ReceiptType {
        ITUNES, GPLAY_ONETIME, GPLAY_RECURRING, WIN_STORE, CREDIT_CARD, /*COUPON, */INTERNAL, PAYPAL, AUTHNET_CREDIT_CARD, BRAINTREE_CREDIT_CARD
    }

    public enum SubscriptionState {
        ACTIVE, CANCELED
    }

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    private static final String ITUNES_PAYLOAD_TEMPLATE          = "{'receipt':%s, 'storeReceipt':%s}".replaceAll("'", "\"");
    private static final String GPLAY_ONETIME_PAYLOAD_TEMPLATE   = "{'receipt':%s, 'signature':%s}".replaceAll("'", "\"");
    private static final String GPLAY_RECURRING_PAYLOAD_TEMPLATE = "{'token':%s, 'subscriptionId':%s, 'storeReceipt':%s}".replaceAll("'", "\"");
    private static final String WIN_STORE_PAYLOAD_TEMPLATE       = "{'receipt':%s, 'storeCertificate': %s}".replaceAll("'", "\"");
//    private static final String COUPON_PAYLOAD_TEMPLATE          = "%s"                                .replaceAll("'", "\"");
    private static final String CREDIT_CARD_PAYLOAD_TEMPLATE     = "{'customerId':%s, 'transactionId':%s, 'subscriptionId':%s, 'status':%s, 'createdAt':%s, 'updatedAt':%s}".replaceAll("'", "\"");
    private static final String INTERNAL_PAYLOAD_TEMPLATE        = "{'transactionId':%s, 'description':%s}".replaceAll("'", "\"");
    private static final String PAYPAL_PAYLOAD_TEMPLATE          = "{'transactionId':%s, 'receipt':%s, 'description':%s}".replaceAll("'", "\"");

    private static final String AUTHNET_CREDIT_CARD_TEMPLATE     =
        "{'transactionId':%s, 'responseCode':%s, 'messageCode':%s, 'description':%s, 'authCode':%s}"
        .replaceAll("'", "\"");

    public static String createItunesPayload(String receipt, String storeReceipt)
    throws InvalidJsonException {
        String payload = String.format(Receipt.ITUNES_PAYLOAD_TEMPLATE,
                                        (receipt      == null ? "null" : '"'+receipt+'"'),
                                        (storeReceipt == null ? "null" : storeReceipt));
        try {
            // Simple check to make sure we've done good
            jsonMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            throw new InvalidJsonException(payload, e);
        }
        return payload;
    }

    public static String createGooglePlayOneTimePayload(String receipt, String signature)
    throws InvalidJsonException {
        String payload = String.format(Receipt.GPLAY_ONETIME_PAYLOAD_TEMPLATE,
                                        (receipt   == null ? "null" : receipt),
                                        (signature == null ? "null" : '"'+signature+'"'));
        try {
            // Simple check to make sure we've done good
            jsonMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            throw new InvalidJsonException(payload, e);
        }
        return payload;
    }

    public static String createGooglePlayRecurringPayload(String token, String subscriptionId, String storeReceipt)
    throws InvalidJsonException {
        String payload = String.format(Receipt.GPLAY_RECURRING_PAYLOAD_TEMPLATE,
                        (token          == null ? "null" : '"'+token+'"'),
                        (subscriptionId == null ? "null" : '"'+subscriptionId+'"'),
                        (storeReceipt   == null ? "null" : storeReceipt));
        try {
            // Simple check to make sure we've done good
            jsonMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            throw new InvalidJsonException(payload, e);
        }
        return payload;
    }

    public static String createWindowsStorePayload(String receipt, String storeCertificate)
    throws InvalidJsonException {
        String payload = String.format(Receipt.WIN_STORE_PAYLOAD_TEMPLATE,
                                        (receipt          == null ? "null" : '"'+receipt+'"'),
                                        (storeCertificate == null ? "null" : '"'+storeCertificate+'"'));
        try {
            // Simple check to make sure we've done good
            jsonMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            throw new InvalidJsonException(payload, e);
        }
        return payload;
    }


//    public static String createCouponPayload(String couponCode)
//    throws InvalidJsonException {
//        String payload = String.format(Receipt.COUPON_PAYLOAD_TEMPLATE, couponCode);
//        return payload;
//    }

    public static String createCreditCardPayload(String customerId, String transactionId, String subscriptionId, String status, Date createdAt, Date updatedAt)
    throws InvalidJsonException {
        String createdAtString = ISO8601Utils.format(createdAt);
        String updatedAtString = ISO8601Utils.format(updatedAt);
        String payload = String.format(Receipt.CREDIT_CARD_PAYLOAD_TEMPLATE,
                        (customerId     == null ? "null" : '"'+customerId+'"'),
                        (transactionId  == null ? "null" : '"'+transactionId+'"'),
                        (subscriptionId == null ? "null" : '"'+subscriptionId+'"'),
                        (status         == null ? "null" : '"'+status+'"'),
                        (createdAt      == null ? "null" : '"'+createdAtString+'"'),
                        (updatedAt      == null ? "null" : '"'+updatedAtString+'"'));
        try {
            // Simple check to make sure we've done good
            jsonMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            throw new InvalidJsonException(payload, e);
        }
        return payload;
    }

    public static String createInternalPayload(String internalTransactionId, String description)
    throws InvalidJsonException {
        String payload = String.format(INTERNAL_PAYLOAD_TEMPLATE,
                        (internalTransactionId == null ? "null" : '"'+internalTransactionId+'"'),
                        (description           == null ? "null" : '"'+description+'"'));
        try {
            // Simple check to make sure we've done good
            jsonMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            throw new InvalidJsonException(payload, e);
        }
        return payload;
    }

    public static String createPayPalPayload(String transactionKey, String stuff1, String stuff2)
    throws InvalidJsonException {
        String payload = String.format(PAYPAL_PAYLOAD_TEMPLATE,
                (transactionKey == null ? "null" : '"'+transactionKey+'"'),
                (stuff1         == null ? "null" : '"'+stuff1+'"'),
                (stuff2         == null ? "null" : '"'+stuff2+'"'));
        try {
            // Simple check to make sure we've done good
            jsonMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            throw new InvalidJsonException(payload, e);
        }
        return payload;
    }

    public static String createAuthorizeDotNetCreditardPayload(String transactionId, String resposneCode, String messageCode, String description, String authCode)
    throws InvalidJsonException
    {
        String payload = String.format(AUTHNET_CREDIT_CARD_TEMPLATE,
                (transactionId == null ? "null" : '"' + transactionId + '"'),
                (resposneCode == null ? "null" : '"' + resposneCode + '"'),
                (messageCode == null ? "null" : '"' + messageCode + '"'),
                (description == null ? "null" : '"' + description + '"'),
                (authCode == null ? "null" : '"' + authCode + '"')
            );

        try {
            // Simple check to make sure we've done good
            jsonMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            throw new InvalidJsonException(payload, e);
        }
        return payload;
    }

    private int _receiptId;
    private String _uuid;
    private ReceiptType _type;
    private String _itemUuid;
    private String _storeUid;
    private long _subscriberId;
    private byte[] _payload;
    private Date _expirationDate;
    private boolean _skipVerify;
    private Date _createdDate;
    private Date _updatedDate;
    private SubscriptionState subscriptionState;

    public Receipt() {
        setUuid(UUID.randomUUID().toString());
    }

    @JsonIgnore
    public int getReceiptId()
    {
        return _receiptId;
    }

    public void setReceiptId(int receiptId)
    {
        _receiptId = receiptId;
    }

    @JsonProperty("receiptId")
    public String getUuid()
    {
        return _uuid;
    }

    public void setUuid(String uuid)
    {
        _uuid = uuid;
    }

    public ReceiptType getType()
    {
        return _type;
    }

    public void setType(ReceiptType type)
    {
        _type = type;
    }

    public String getItemUuid()
    {
        return _itemUuid;
    }

    public void setItemUuid(String itemUuid)
    {
        _itemUuid = itemUuid;
    }

    public String getStoreUid() {
        return _storeUid;
    }

    public void setStoreUid(String storeUid) {
        _storeUid = storeUid;
    }

    @JsonIgnore
    public long getSubscriberId()
    {
        return _subscriberId;
    }

    public void setSubscriberId(long subscriberId)
    {
        _subscriberId = subscriberId;
    }

    @JsonIgnore
    public byte[] getPayload()
    {
        return _payload;
    }

    public void setPayload(byte[] payload)
    {
        _payload = payload;
    }

    @JsonSerialize(using=JsonDateSerializer.class)
    public Date getExpirationDate()
    {
        return _expirationDate;
    }

    public void setExpirationDate(Date expirationDate)
    {
        _expirationDate = expirationDate;
    }

    @JsonIgnore
    public boolean isSkipVerify() {
        return _skipVerify;
    }

    public void setSkipVerify(boolean skipVerify) {
        _skipVerify = skipVerify;
    }

    public Date getCreatedDate() {
        return _createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        _createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return _updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        _updatedDate = updatedDate;
    }

    public SubscriptionState getSubscriptionState() {
        return subscriptionState;
    }

    public void setSubscriptionState(SubscriptionState subscriptionState) {
        this.subscriptionState = subscriptionState;
    }

    @Override
    public String toString()
    {
        String payload = (_payload == null) ? "" : new String(_payload);
        return String.format("{receiptId: %d, uuid: %s, type: %s, itemUuid: %s, subscriberId: %d, storeUid: %s, payload: %s}", _receiptId, _uuid, _type.name(), _itemUuid, _subscriberId, _storeUid, payload);
    }

    @Override
    public Receipt clone() {
        try {
            return (Receipt) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
