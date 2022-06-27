package com.meinc.store.processor;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisher.Builder;
import com.google.api.services.androidpublisher.model.InappPurchase;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.store.domain.Receipt;
import com.meinc.store.domain.Receipt.ReceiptType;
import com.meinc.store.domain.ReceiptResult;
import com.meinc.store.exception.InvalidJsonException;

public class GooglePlayProcessor extends PaymentProcessor {

    class GooglePlayCredentialHolder {
        AndroidPublisher publisher;
        String accessToken;
        Long accessTokenLastObtainedMs;
        String googlePlayAppPackageName;
        String googlePlayAppName;
        private PublicKey googlePlayAppPublicKey;
        public void setGooglePlayAppPublicKey(String key) {
            byte[] publicKeyBytes = Base64.decodeBase64(key);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory;
            try {
                keyFactory = KeyFactory.getInstance("RSA");
            } catch (NoSuchAlgorithmException e) {
                logger.error("Error preparing Google Play app public key: " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
            try {
                this.googlePlayAppPublicKey = keyFactory.generatePublic(publicKeySpec);
            } catch (InvalidKeySpecException e) {
                logger.error("Error preparing Google Play app public key: " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        public PublicKey getGooglePlayAppPublicKey() {
            return googlePlayAppPublicKey;
        }
    }

    private static final Log logger = LogFactory.getLog(GooglePlayProcessor.class);
    private static final Log logger2 = LogFactory.getLog("AndroidPurchase");

    private Map<String, GooglePlayCredentialHolder> _publisherByStoreId = new ConcurrentHashMap<String, GooglePlayProcessor.GooglePlayCredentialHolder>();

    private ObjectMapper jsonMapper = new ObjectMapper();

    public GooglePlayProcessor() {
        obtainPublisherApi("com.meinc.shout");
    }

//    public GooglePlayProcessor(boolean delay) {
//    }

    @Override
    List<ReceiptType> getTypes() {
        return Arrays.asList(new ReceiptType[] {ReceiptType.GPLAY_ONETIME, ReceiptType.GPLAY_RECURRING});
    }

    @Override
    protected ReceiptResult verifyReceipt(Receipt receipt, String storeBundleId) {
        switch (receipt.getType()) {
        case GPLAY_ONETIME:
            return verifyOneTimeReceipt(receipt, storeBundleId);
        case GPLAY_RECURRING:
            return verifyRecurringReceipt(receipt, storeBundleId);
        default:
            logger.warn("received invalid google receipt: " + receipt);
            return null;
        }
    }

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    private synchronized GooglePlayCredentialHolder createOrUpdateHolder(String storeBundleId) {
        GooglePlayCredentialHolder holder = _publisherByStoreId.get(storeBundleId);
        if (holder != null) {
            // someone else already created it, just use it
            logger2.info("returning preexisting credential holder from createOrUpdateHolder: " + storeBundleId);
            return holder;
        }
        holder = new GooglePlayCredentialHolder();

        long now = System.currentTimeMillis();
        // Throttle communication with Google, even if it triggers errors
        if (holder.accessTokenLastObtainedMs != null && now - holder.accessTokenLastObtainedMs <= 100) {
            logger2.info("ignoring duplicate request to createOrUpdateHolder: " + storeBundleId);
            return null;
        }

        int repeat = 0;
        String refreshToken = mRefreshToken != null ? mRefreshToken : ServerPropertyHolder.getProperty(String.format("store.%s.google.play.refreshToken", storeBundleId));
        String clientId = mClientId != null ? mClientId : ServerPropertyHolder.getProperty(String.format("store.%s.google.play.clientId", storeBundleId));
        String clientSecret = mClientSecret != null ? mClientSecret : ServerPropertyHolder.getProperty(String.format("store.%s.google.play.clientSecret", storeBundleId));
//        logger2.info(MessageFormat.format(
//                "createorUpdateHolder, data from prop file (bundleId: {0}), refreshToken: {1}, clientId: {2}, clientSecret: {3}",
//                storeBundleId, refreshToken, clientId, clientSecret));
        while (repeat <= 2) {
            repeat += 1;
            GoogleTokenResponse response;
            try {
                GoogleRefreshTokenRequest request = new GoogleRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(), refreshToken, clientId, clientSecret);
                response = request.execute();
            } catch (IOException e) {
                logger2.warn("io exception when getting new access token: " + e.getMessage());
                logger.warn("Error communicating with google for new access token: " + e.getMessage());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) { break; }
                // Retry
                continue;
            }
            holder.accessToken = response.getAccessToken();
            holder.accessTokenLastObtainedMs = now;
            holder.googlePlayAppName = mAppName != null ? mAppName : ServerPropertyHolder.getProperty(String.format("store.%s.google.play.app.name", storeBundleId));
            holder.googlePlayAppPackageName = mAppPackageName != null ? mAppPackageName : ServerPropertyHolder.getProperty(String.format("store.%s.google.play.app.package.name", storeBundleId));
            holder.setGooglePlayAppPublicKey(mAppPublicKey != null ? mAppPublicKey : ServerPropertyHolder.getProperty(String.format("store.%s.google.play.app.public.key", storeBundleId)));

//            logger2.info(MessageFormat.format(
//                    "got refresh token response. accessToken: {0}, appName: {1}, packageName: {2}, publicKey: {3}",
//                    holder.accessToken, holder.googlePlayAppName, holder.googlePlayAppPackageName, holder.getGooglePlayAppPublicKey()));

            GoogleCredential googleCredential = new GoogleCredential().setAccessToken(holder.accessToken);
            Builder apiBuilder = new AndroidPublisher.Builder(HTTP_TRANSPORT, JSON_FACTORY, googleCredential);
            apiBuilder.setApplicationName(holder.googlePlayAppName);
            holder.publisher = apiBuilder.build();

            logger2.info("adding holder to map by key: " + storeBundleId);
            _publisherByStoreId.put(storeBundleId, holder);

            return holder;
        }

        logger2.warn("did not obtain data in createOrUpdateHolder for: " + storeBundleId);
        return null;
    }

    private GooglePlayCredentialHolder obtainPublisherApi(String storeBundleId) {
        GooglePlayCredentialHolder holder = _publisherByStoreId.get(storeBundleId);
        if (holder == null) {
            return createOrUpdateHolder(storeBundleId);
        } else {
            return holder;
        }
    }

    private ReceiptResult verifyRecurringReceipt(Receipt receipt, String storeBundleId) {
        ReceiptResult result = new ReceiptResult();

        Map<String,String> payloadJson;
        try {
            payloadJson = jsonMapper.readValue(receipt.getPayload(), new TypeReference<Map<String,String>>() { });
        } catch (Exception e) {
            logger.error("Error parsing google client receipt: " + e.getMessage(), e);
            result.setNoRetryError("Error parsing receipt payload");
            return result;
        }
        String tokenString = payloadJson.get("token");
        String subscriptionId = payloadJson.get("subscriptionId");
        if (tokenString == null || subscriptionId == null) {
            logger.error("Invalid recurring google receipt - cannot verify: " + receipt);
            result.setNoRetryError("Error reading receipt payload");
            return result;
        }

        String subscriptionIdCompare = subscriptionId.replaceAll("[^a-fA-F0-9]", "");
        String receiptItemUuidCompare = receipt.getItemUuid().replaceAll("[^a-fA-F0-9]", "");
        if (!subscriptionIdCompare.equals(receiptItemUuidCompare)) {
            logger.warn(String.format("Google recurring receipt subscription id \"%s\" doesn't match client item id \"%s\"",
                                      subscriptionIdCompare, receiptItemUuidCompare));
            result.setNoRetryError("Invalid subscriptionId/itemId");
            return result;
        }

        int repeat = 0;
        SubscriptionPurchase purchase = null;
        while (repeat <= 2) {
            repeat += 1;
            try {
                GooglePlayCredentialHolder holder = obtainPublisherApi(storeBundleId);
                if (holder == null) {
                    logger.warn("Error - no publisher, and unable to obtain publisher API");
                    result.setRetryError("No publisher");
                    return result;
                }
                logger.info("*** about to get purchase from google, APP_PACKAGE_NAME: " + holder.googlePlayAppPackageName + ", subscriptionId: " + subscriptionId + ", tokenString: " + tokenString);
                AndroidPublisher publisher = holder.publisher;
                com.google.api.services.androidpublisher.AndroidPublisher.Purchases.Get request = publisher.purchases().get(holder.googlePlayAppPackageName, subscriptionId, tokenString);
                purchase = request.execute();
                if (logger.isDebugEnabled())
                    logger.debug("Received google receipt: " + purchase);
            } catch (HttpResponseException e) {
                String errorMessage = null, errorReason = null;
                try {
                    String reason = e.getMessage();
                    String reasonJson = reason.replaceFirst(".*\n", "");

//                    Map<String,List<Map<String,String>>> reasonJsonMap =
//                            jsonMapper.readValue(reasonJson, new TypeReference<Map<String,List<Map<String,String>>>>() {});
//                    List<Map<String,String>> errorsList = reasonJsonMap.get("errors");
//                    Map<String,String> error = errorsList.get(0);
//                    errorMessage = error.get("message");
//                    errorReason = error.get("reason");

                    JsonNode root = jsonMapper.readValue(reasonJson, JsonNode.class);
                    JsonNode errorsNode = root.get("errors").get(0);
                    errorMessage = errorsNode.get("message").textValue();
                    errorReason = errorsNode.get("reason").textValue();

                    logger.warn(String.format("Error response from Google: %s, reason = %s", errorMessage, errorReason));
                } catch (Exception e1) {
                    logger.error("Error parsing error response from Google: " + e1.getMessage() + "\nReason: " + e.getMessage());
                }

                int errorCode = e.getStatusCode();
                switch (errorCode) {
                case 401:
                    logger.info("*** got a 401, getting new access token");
                    _publisherByStoreId.remove(storeBundleId);
                    obtainPublisherApi(storeBundleId);
                    continue;
                case 503:
                    logger.info("*** got a 503, retry");
                    continue;
                case 404:
                default:
                    logger.info("*** got some other errorCode: " + errorCode);
                    // Receipt not valid
                    break;
                }
            } catch (IOException e) {
                logger.error("Error communicating with google (retry): " + e.getMessage(), e);
                try {
                    Thread.sleep(500);
                    // Retry
                    continue;
                } catch (InterruptedException e1) { break; }
            }
            break;
        }
        if (purchase == null) {
            logger.warn("Error - No purchase response from google");
            result.setRetryError("No purchase response");
            return result;
        }

        Receipt storeReceipt = receipt.clone();

        Long expirationMs = purchase.getValidUntilTimestampMsec();
        Date expirationDate = null;
        if (expirationMs != null)
            expirationDate = new Date(expirationMs);
        storeReceipt.setExpirationDate(expirationDate);

        try {
            String storePayload = Receipt.createGooglePlayRecurringPayload(tokenString, subscriptionId, purchase.toString());
            storeReceipt.setPayload(storePayload.getBytes());
        } catch (InvalidJsonException e) {
            logger.error(String.format("Error creating google store receipt payload (%s): %s", e.getMessage(), purchase.toString()), e);
            result.setRetryError("No purchase response");
            return result;
        }

        logReceiptVerified(storeReceipt, tokenString);

        result.setStoreReceipt(storeReceipt);
        return result;
    }

    private ReceiptResult verifyOneTimeReceipt(Receipt receipt, String storeBundleId)
    {
        logger2.info(MessageFormat.format("verify 1x receipt, storeBundleId: {0}, receipt: {1}", storeBundleId, receipt));
        ReceiptResult result = new ReceiptResult();

        //grab the data that was passed
        Map<String,Object> payloadJsonMap;
        String packageName = null;
        String productId = null;
        String token = null;
        String receiptString = null;
        String signature = null;
        try {
            payloadJsonMap = jsonMapper.readValue(new String(receipt.getPayload()), new TypeReference<Map<String,Object>>() { });
            @SuppressWarnings("unchecked")
            Map<String,Object> receiptMap = (Map<String,Object>) payloadJsonMap.get("receipt");
            signature = (String) payloadJsonMap.get("signature");
            receiptString = receiptMap.toString();
            packageName = (String) receiptMap.get("packageName");
            productId = (String) receiptMap.get("productId");
            token = (String) receiptMap.get("purchaseToken");

            logger2.info(MessageFormat.format("parse of receipt; signature: {0}, packageName: {1}, productId: {2}, token: {3}", signature, packageName, productId, token));

        } catch (Exception e) {
            logger2.error("error parsing google client receipt: " + e.getMessage());
            logger.error("Error parsing google client receipt: " + e.getMessage());
            result.setNoRetryError("Error parsing receipt payload");
            result.setMalformedReceiptError(true);
            return result;
        }

        //call Google to verify the result
        int repeat = 0;
        InappPurchase purchase = null;
        while (repeat <= 2) {
            repeat += 1;
            try {
                GooglePlayCredentialHolder holder = obtainPublisherApi(storeBundleId);
                if (holder == null) {
                    logger2.warn("error obtaining holder; not found for: " + storeBundleId);
                    logger.warn("Error - no publisher, and unable to obtain publisher API");
                    result.setRetryError("No publisher");
                    return result;
                }
                AndroidPublisher publisher = holder.publisher;
                com.google.api.services.androidpublisher.AndroidPublisher.Inapppurchases.Get request = publisher.inapppurchases().get(packageName, productId, token);
                purchase = request.execute();

            } catch (HttpResponseException e) {
                String errorMessage = null, errorReason = null;
                try {
                    String reason = e.getMessage();
                    String reasonJson = reason.replaceFirst(".*\n", "");

//                    Map<String,List<Map<String,String>>> reasonJsonMap =
//                            jsonMapper.readValue(reasonJson, new TypeReference<Map<String,List<Map<String,String>>>>() {});
//                    List<Map<String,String>> errorsList = reasonJsonMap.get("errors");
//                    Map<String,String> error = errorsList.get(0);
//                    errorMessage = error.get("message");
//                    errorReason = error.get("reason");

                    JsonNode root = jsonMapper.readValue(reasonJson, JsonNode.class);
                    JsonNode errorsNode = root.get("errors").get(0);
                    errorMessage = errorsNode.get("message").textValue();
                    errorReason = errorsNode.get("reason").textValue();

                    logger2.warn(MessageFormat.format("error respone while verifying receipt, message: {0}, reason: {1}", errorMessage, errorReason));
                    logger.warn(String.format("Error response from Google: %s, reason = %s", errorMessage, errorReason));
                } catch (Exception e1) {
                    logger.error("Error parsing error response from Google: " + e1.getMessage() + "\nReason: " + e.getMessage());
                    logger2.warn(MessageFormat.format("error parsing response while verifying receipt, message: {0}", e1.getMessage()));
                }

                int errorCode = e.getStatusCode();
                switch (errorCode) {
                    case 401:
                        logger2.info("got a 401 while verifying receipt; trying to get a new access token");
                        logger.info("*** got a 401, getting new access token");
                        _publisherByStoreId.remove(storeBundleId);
                        obtainPublisherApi(storeBundleId);
                        continue;
                    case 503:
                        logger2.info("got a 503 while verifying receipt; doing a retry");
                        logger.info("*** got a 503, retry");
                        continue;
                    case 404:
                    default:
                        //Receipt not valid
                        logger2.info(MessageFormat.format("unhandled error response while verifying receipt. code: {0}", errorCode));
                        logger.info("*** got some other errorCode: " + errorCode);
                    break;
                }
            } catch (IOException e) {
                logger2.info(MessageFormat.format("error talking with google while verifying receipt; doing a retry, message: {0}", e.getMessage()));
                logger.error("Error communicating with google (retry): " + e.getMessage(), e);
                try { Thread.sleep(500); continue; } catch (InterruptedException e1) { break; }
            }
            break;
        }

        logger2.info(MessageFormat.format("received response from google while verifying receipt: {0}", purchase));
        logger.info("Received google one-time receipt. purchase response: " + purchase);

        //make sure the purchase is valid
        Integer purchaseState = purchase == null ? null : purchase.getPurchaseState();
        if (purchaseState == null || purchaseState != 0) {
            logger2.error(MessageFormat.format("invalid receipt - purchaseState is null or non 0", purchaseState));
            logger.error("Invalid one-time google receipt - cannot verify receipt: " + receipt);
            result.setNoRetryError("Error validating receipt");
            result.setInvalidReceiptError(true);
            return result;
        }

        //store off the result of the purchase query
        Receipt storeReceipt = receipt.clone();
        try {
            String storePayload = Receipt.createGooglePlayOneTimePayload(purchase.toString(), signature);
            storeReceipt.setPayload(storePayload.getBytes());
        } catch (InvalidJsonException e) {
            logger2.info(MessageFormat.format("error creating receipt from google response: {0}; {1}", e.getMessage(), purchase.toString()));
            logger.error(String.format("Error creating google store receipt payload (%s): %s", e.getMessage(), purchase.toString()), e);
            result.setRetryError("No purchase response");
            return result;
        }

        logger2.info("receipt verified and valid!");
        logReceiptVerified(storeReceipt, receiptString);

        result.setStoreReceipt(storeReceipt);
        return result;
    }

    //this is similar to what the clients will need to do; they should pass the "refreshToken" to the server, which it will store and grab access tokens as needed.
    //https://developers.google.com/google-apps/tasks/auth
    /*public static void main(String[] args)
    {
        try {
            String REDIRECT_URL = "urn:ietf:wg:oauth:2.0:oob";
            String SCOPE = "https://www.googleapis.com/auth/androidpublisher";

            //build an oauth url and read the response code
            String url = new GoogleAuthorizationCodeRequestUrl(CLIENT_ID, REDIRECT_URL, Arrays.asList(SCOPE)).setAccessType("offline").build();
            System.out.println("Open this URL in your browser: " + url);
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter the authorization code: ");
            String code = in.readLine();

            //get an offline refresh/access token
            GoogleTokenResponse response = new GoogleAuthorizationCodeTokenRequest(new NetHttpTransport(), new JacksonFactory(), CLIENT_ID, CLIENT_SECRET, code, REDIRECT_URL).execute();
            //String accessToken = response.getAccessToken();
            String refreshToken = response.getRefreshToken();

            System.out.println("refresh token: " + refreshToken);
            //System.out.println("access token: " + accessToken);

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }*/

    /*public static void main(String[] args) {
        GooglePlayProcessor gpp = new GooglePlayProcessor();
        Receipt r = gpp.verifyRecurringReceipt(null, "foo", "monthlygas001");
        System.out.println(">>"+r);
    }*/

//    public static void main(String[] args)
//    {
//        Map<String,Object> payloadJsonMap;
//        String payload = "{\"receipt\":{\"orderId\":\"12999763169054705758.1337852220365699\",\"packageName\":\"com.meinc.shout\",\"productId\":\"9a9b48c2263111e38d6912313932f90c\",\"purchaseTime\":1395773326245,\"purchaseState\":0,\"developerPayload\":\"c8940ae1-cb23-42e7-8684-73ef45477049\",\"purchaseToken\":\"jnclojapongfapccaniobgfa.AO-J1Ow1mjPcsL-Ue1eijNnNLuMq7z_uhDOfqGooQSGg9D5dJnJWsu4BHLo0KpUT60NJL9mQT9zEADnxJZ3DKYVtmIn9ious9vcGKro-MDdyIeHCrYBizTKz-sCN8e2e_pRFqciP9omOI8XrZCti0SR-si01_ADFag\"}, \"signature\":\"jnclojapongfapccaniobgfa.AO-J1Ow1mjPcsL-Ue1eijNnNLuMq7z_uhDOfqGooQSGg9D5dJnJWsu4BHLo0KpUT60NJL9mQT9zEADnxJZ3DKYVtmIn9ious9vcGKro-MDdyIeHCrYBizTKz-sCN8e2e_pRFqciP9omOI8XrZCti0SR-si01_ADFag\"}";
//
//        try {
//            payloadJsonMap = new ObjectMapper().readValue(payload, new TypeReference<Map<String,Object>>() { });
//            System.out.println(payloadJsonMap);
//            Map<String,Object> receiptMap = (Map<String,Object>) payloadJsonMap.get("receipt");
//            String receiptString = receiptMap.toString();
//            System.out.println(receiptMap);
//            System.out.println("receipt: " + receiptString);
//            String packageName = (String) receiptMap.get("packageName");
//            String productId = (String) receiptMap.get("productId");
//            String token = (String) receiptMap.get("purchaseToken");
//            System.out.println("packageName: " + packageName);
//            System.out.println("  productId: " + productId);
//            System.out.println("      token: " + token);
//
//        } catch (Exception e) {
//            logger.error("Error parsing google client receipt: " + e.getMessage());
//        }
//    }

    private String mRefreshToken;
    private String mClientId;
    private String mClientSecret;
    private String mAppName;
    private String mAppPackageName;
    private String mAppPublicKey;

//    public static void main(String[] args)
//    throws Exception
//    {
//        String sReceipt = "{\"orderId\":\"12999763169054705758.1310843889363105\",\"packageName\":\"com.meinc.shout\",\"productId\":\"9a9b48c2263111e38d6912313932f90c\",\"purchaseTime\":1398800274113,\"purchaseState\":0,\"developerPayload\":\"aefa2109-fe4b-441d-9347-68001f5be15e\",\"purchaseToken\":\"egdenhjpgkfehfkmaglkdfno.AO-J1OyYqvLqRG1uX9q9Gw5HTVcWnsbfpd--1ofTlNFiyOX9VN7AE6iloaJdWAAbHuYI62XwgO67X7dbp5wYjIw-xxqoWdqqDeb92XqlTFMmbKNVXfr6V3dKNqAVRCK25ekcLvB8Uq6nHICDx32q0JyHwNtHzATBYw\"}";
//        String sSignature = "egdenhjpgkfehfkmaglkdfno.AO-J1OyYqvLqRG1uX9q9Gw5HTVcWnsbfpd--1ofTlNFiyOX9VN7AE6iloaJdWAAbHuYI62XwgO67X7dbp5wYjIw-xxqoWdqqDeb92XqlTFMmbKNVXfr6V3dKNqAVRCK25ekcLvB8Uq6nHICDx32q0JyHwNtHzATBYw";
//        String itemUuid = "9a9b48c2-2631-11e3-8d69-12313932f90c";
//        int subscriberId = 22406;
//
//        Receipt r = new Receipt();
//        r.setItemUuid(itemUuid);
//        r.setSubscriberId(subscriberId);
//        r.setSkipVerify(false);
//        r.setType(ReceiptType.GPLAY_ONETIME);
//        r.setPayload(Receipt.createGooglePlayOneTimePayload(sReceipt, sSignature).getBytes());
//
//        GooglePlayProcessor gpp = new GooglePlayProcessor(true);
//        gpp.mRefreshToken = "1/7bwCYUiAwnNGekc0-0pJ1lHuiOSLUtOTj3-kYtiIe9o";
//        gpp.mClientId = "1016558683356-rt106dr9quv9n1gpi39shsgab4j0sn1s.apps.googleusercontent.com";
//        gpp.mClientSecret = "HS1NzVkAJbCc6TFfYyGQKQi5";
//        gpp.mAppName = "TestDrive/1.0";
//        gpp.mAppPackageName = "com.meinc.shout";
//        gpp.mAppPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA47FYzV3WuUddwZ3zD3SGC22QnJL2gBRvmF8zC4ZONiNPdvKvCbrGb3jWLAyZHKD+DXBVTwdsncXRPm0ppWk9Gsla/r4s3NA1C1a8z2lfuEhW+Tbs1XWjZ0+1Zw/5gh0h0BQmyqGxWCplLfxIyObzGYWFfng6RC2uqE9+SN8JKqaZpxoxzHUB2h/kSNa5A5eeK9Klbw518mlHf5jFDGlOSX94pZV5GItfnEQkMxTb3+kc+Gg9MfCxeI6HXxOQNSmrL/oXjrIl3WyjSKmbsVZLh7sUM5BGkF7KVbtGbvW1t0s5TNn95oteTEGwvbKKt12qDGCa3EYvX5kClaf/5gb8FQIDAQAB";
//        gpp.obtainPublisherApi("com.meinc.shout");
//
//        ReceiptResult result = gpp.verifyOneTimeReceipt(r, "com.meinc.shout");
//        System.out.println(result);
//    }
}
