package com.meinc.store.processor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.store.domain.Receipt;
import com.meinc.store.domain.Receipt.ReceiptType;
import com.meinc.store.domain.ReceiptResult;
import com.meinc.store.exception.InvalidJsonException;

public class ITunesProcessor extends PaymentProcessor {
    private static final Log logger = LogFactory.getLog(ITunesProcessor.class);

    private static Map<Long,ITunesAutoRenewableStatusCode> iTunesCodeMap = new HashMap<Long,ITunesAutoRenewableStatusCode>();

    private static enum ITunesAutoRenewableStatusCode {
        SUCCESS             (0),
        INVALID_JSON        (21000),
        INVALID_RECEIPT     (21002),
        RECEIPT_AUTH_FAIL   (21003),
        BAD_SHARED_SECRET   (21004),
        SERVER_UNAVAILABLE  (21005),
        SUBSCRIPTION_EXPIRED(21006),
        SANDBOX_RECEIPT     (21007),
        PRODUCTION_RECEIPT  (21008);

        //private long status;
        ITunesAutoRenewableStatusCode(long status) {
            //this.status = status;
            iTunesCodeMap.put(status, this);
        }
    }

    static {
        // Force the enum to load on start up so iTunesCodeMap is populated
        ITunesAutoRenewableStatusCode.SUCCESS.name();
    }

    private static enum ITunesServer {
        SANDBOX   ("https://sandbox.itunes.apple.com/verifyReceipt"),
        PRODUCTION("https://buy.itunes.apple.com/verifyReceipt");

        private String serverUrlString;
        private ITunesServer(String urlString) {
            serverUrlString = urlString;
        }
        public String getServerUrlString() {
            return serverUrlString;
        }
    }

    private ITunesServer iTunesServer = ITunesServer.SANDBOX;
    private String appleSharedSecret;
    private ObjectMapper jsonMapper = new ObjectMapper();

    public void setITunesServer(String serverType) {
        if ("production".equals(serverType))
            iTunesServer = ITunesServer.PRODUCTION;
        else
            iTunesServer = ITunesServer.SANDBOX;
    }

    public void setAppleSharedSecret(String appleSharedSecret) {
        this.appleSharedSecret = appleSharedSecret;
    }

    @Override
    List<ReceiptType> getTypes() {
        return Arrays.asList(new ReceiptType[] {ReceiptType.ITUNES});
    }

    @Override
    public ReceiptResult verifyReceipt(Receipt receipt, String storeBundleId) {
        return iTunesVerifyReceipt(receipt, iTunesServer, 0);
    }

    public ReceiptResult iTunesVerifyReceipt(Receipt receipt, ITunesServer tryITunesServer, int recurseDepth) {
        ReceiptResult result = new ReceiptResult();

        @SuppressWarnings("rawtypes")
        Map payloadJson;
        try {
            payloadJson = jsonMapper.readValue(receipt.getPayload(), Map.class);
        } catch (JsonParseException e) {
            logger.error("Error parsing itunes client receipt: " + e.getMessage(), e);
            result.setNoRetryError("Error parsing receipt payload");
            return result;
        } catch (JsonMappingException e) {
            logger.error("Error parsing itunes client receipt: " + e.getMessage(), e);
            result.setNoRetryError("Error parsing receipt payload");
            return result;
        } catch (IOException e) {
            logger.error("Error parsing itunes client receipt: " + e.getMessage(), e);
            result.setNoRetryError("Error parsing receipt payload");
            return result;
        }
        String receiptData = (String) payloadJson.get("receipt");
        if (receiptData == null || receiptData.trim().isEmpty()) {
            logger.error("Missing/empty iTunes receipt");
            result.setNoRetryError("Error reading receipt payload");
            return result;
        }

        HttpPost httpPost = new HttpPost(tryITunesServer.getServerUrlString());

        Map<String,String> jsonMap = new HashMap<String,String>();
        // This should already be Base64 encoded
        jsonMap.put("receipt-data", receiptData);
        jsonMap.put("password", appleSharedSecret);
        String requestJson;
        try {
            requestJson = jsonMapper.writeValueAsString(jsonMap);
        } catch (JsonGenerationException e) {
            logger.error("Error encoding request to iTunes server: " + e.getMessage(), e);
            result.setNoRetryError("Error preparing request");
            return result;
        } catch (JsonMappingException e) {
            logger.error("Error encoding request to iTunes server: " + e.getMessage(), e);
            result.setNoRetryError("Error preparing request");
            return result;
        } catch (IOException e) {
            logger.error("Error encoding request to iTunes server: " + e.getMessage(), e);
            result.setNoRetryError("Error preparing request");
            return result;
        }

        try {
            httpPost.setEntity(new StringEntity(requestJson));
        } catch (UnsupportedEncodingException e) {
            logger.error("Error encoding request to iTunes server: " + e.getMessage(), e);
            result.setNoRetryError("Error preparing request");
            return result;
        }

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        try {
            response = httpclient.execute(httpPost);
        } catch (ClientProtocolException e) {
            logger.error("Error opening connection to iTunes server: " + e.getMessage(), e);
            result.setRetryError("Error sending request");
            return result;
        } catch (IOException e) {
            logger.error("Error opening connection to iTunes server: " + e.getMessage(), e);
            result.setRetryError("Error sending request");
            return result;
        }

        HttpEntity responseEntity = response.getEntity();
        if (responseEntity == null) {
            logger.error("No response from iTunes server");
            result.setRetryError("Error receiving response");
            return result;
        }
        if (responseEntity.getContentLength() > 50000) {
            logger.error("Response from iTunes server exceeded length limits: " + responseEntity.getContentLength());
            result.setNoRetryError("Error receiving response");
            return result;
        }
        String responseString;
        try {
            responseString = EntityUtils.toString(responseEntity);
        } catch (ParseException e) {
            logger.error("Error reading response from iTunes server: " + e.getMessage(), e);
            result.setRetryError("Error receiving response");
            return result;
        } catch (IOException e) {
            logger.error("Error reading response from iTunes server: " + e.getMessage(), e);
            result.setRetryError("Error receiving response");
            return result;
        }

        int responseCode = response.getStatusLine().getStatusCode();
        if (responseCode != 200) {
            logger.error(String.format("Unexpected response (%d) from iTunes server: %s", responseCode, responseString.substring(0, Math.min(200,responseString.length())) ));
            result.setRetryError("Error receiving response");
            return result;
        }

        @SuppressWarnings("rawtypes")
        Map jsonResponse;
        try {
            jsonResponse = jsonMapper.readValue(responseString, Map.class);
        } catch (JsonParseException e) {
            logger.error("Error reading response from iTunes server: " + e.getMessage(), e);
            result.setRetryError("Error reading response");
            return result;
        } catch (JsonMappingException e) {
            logger.error("Error reading response from iTunes server: " + e.getMessage(), e);
            result.setRetryError("Error reading response");
            return result;
        } catch (IOException e) {
            logger.error("Error reading response from iTunes server: " + e.getMessage(), e);
            result.setRetryError("Error reading response");
            return result;
        }

        String receiptStatusString = jsonResponse.get("status").toString();
        if (receiptStatusString != null) {
            Long receiptStatus = null;
            try {
                receiptStatus = Long.parseLong(receiptStatusString);
            } catch (NumberFormatException e) {
                logger.error("iTunes server returned unknown status: " + receiptStatusString);
                result.setRetryError("Error processing response");
                return result;
            }

            ITunesAutoRenewableStatusCode itunesCode = iTunesCodeMap.get(receiptStatus);
            if (itunesCode == null) {
                logger.error("iTunes server returned unknown status: " + receiptStatusString);
                result.setRetryError("Error processing response");
                return result;
            }
            switch (itunesCode) {
            case SUCCESS:
                break;
            case INVALID_JSON:
                result.setMalformedReceiptError(true);
                result.setNoRetryError("invalid receipt");
                logger.error("iTunes server reported invalid JSON");
                return result;
            case INVALID_RECEIPT:
            case RECEIPT_AUTH_FAIL:
                result.setInvalidReceiptError(true);
                result.setNoRetryError("iTunes server reported the receipt is invalid or could not be authenticated");
                logger.error("iTunes server reported the receipt is invalid or could not be authenticated");
                return result;
            case SANDBOX_RECEIPT:
                if (tryITunesServer == ITunesServer.SANDBOX) {
                    logger.error("iTunes sandbox server thinks it's not the sandbox server!");
                    result.setRetryError("Error processing response");
                    return result;
                }
                if (recurseDepth >= 1) {
                    logger.info("iTunes server trying to redirect back to sandbox server - recursion limit reached");
                    result.setRetryError("Error processing response");
                    return result;
                }
                logger.info("iTunes production server redirecting receipt to sandbox server");
                return iTunesVerifyReceipt(receipt, ITunesServer.SANDBOX, recurseDepth+1);
            case PRODUCTION_RECEIPT:
                if (tryITunesServer == ITunesServer.PRODUCTION) {
                    logger.error("iTunes production server thinks it's not the production server!");
                    result.setRetryError("Error processing response");
                    return result;
                }
                if (recurseDepth >= 1) {
                    logger.info("iTunes server trying to redirect back to production server - recursion limit reached");
                    result.setRetryError("Error processing response");
                    return result;
                }
                logger.info("iTunes sandbox server redirecting receipt to production server");
                return iTunesVerifyReceipt(receipt, ITunesServer.PRODUCTION, recurseDepth+1);
            case SUBSCRIPTION_EXPIRED:
                logger.warn(String.format("iTunes receipt expired: %s", receipt));
                result.setNoRetryError("Expired receipt");
                return result;
            default:
                logger.warn(String.format("iTunes server returned status %s for receipt: %s", itunesCode.name(), receipt));
                result.setNoRetryError("Error processing response");
                return result;
            }
        }

        // This is a valid receipt
        @SuppressWarnings("unchecked")
        Map<String,Object> itunesReceipt = (Map<String,Object>) jsonResponse.get("receipt");
        if (itunesReceipt == null) {
            logger.error("Missing receipt in response from iTunes server");
            result.setRetryError("Error processing response");
            return result;
        }

        String receiptProductId = (String) itunesReceipt.get("product_id");
        if (receiptProductId == null) {
            logger.error("iTunes receipt missing product_id");
            result.setRetryError("Error processing response");
            return result;
        }
        String receiptProductIdCompare = receiptProductId.replaceAll("[^a-fA-F0-9]", "");
        String receiptItemIdCompare = receipt.getItemUuid().replaceAll("[^a-fA-F0-9]", "");
        if (!receiptProductIdCompare.equals(receiptItemIdCompare)) {
            logger.warn(String.format("iTunes receipt product id \"%s\" doesn't match client item id \"%s\"", receiptProductIdCompare, receiptItemIdCompare));
            result.setNoRetryError("Receipt product id mismatch");
            return result;
        }
        if (receiptProductId.indexOf('-') == -1 && receiptProductId.length() == 32)
            receiptProductId = String.format("%s-%s-%s-%s-%s", receiptProductIdCompare.substring(0, 8),
                                                               receiptProductIdCompare.substring(8, 12),
                                                               receiptProductIdCompare.substring(12, 16),
                                                               receiptProductIdCompare.substring(16, 20),
                                                               receiptProductIdCompare.substring(20, 32));

        String receiptTransactionId = (String) itunesReceipt.get("transaction_id");
        if (receiptTransactionId == null) {
            logger.error("iTunes receipt missing transaction_id");
            result.setRetryError("Error processing response");
            return result;
        }

        /*Long receiptPurchaseDateMs = Long.parseLong((String)itunesReceipt.get("original_purchase_date_ms"));
        if (receiptPurchaseDateMs == null) {
            logger.error("iTunes receipt missing purchase_date");
            return null;
        }
        Date receiptPurchaseDateDate = new Date(receiptPurchaseDateMs);*/

        Receipt storeReceipt = receipt.clone();

        storeReceipt.setItemUuid(receiptProductId);

        String itunesReceiptString;
        try {
            itunesReceiptString = jsonMapper.writeValueAsString(itunesReceipt);
        } catch (JsonGenerationException e) {
            logger.error("Error saving response from iTunes server: " + e.getMessage(), e);
            result.setRetryError("Error processing response");
            return result;
        } catch (JsonMappingException e) {
            logger.error("Error saving response from iTunes server: " + e.getMessage(), e);
            result.setRetryError("Error processing response");
            return result;
        } catch (IOException e) {
            logger.error("Error saving response from iTunes server: " + e.getMessage(), e);
            result.setRetryError("Error processing response");
            return result;
        }

        String newPayload;
        try {
            newPayload = Receipt.createItunesPayload(receiptData, itunesReceiptString);
        } catch (InvalidJsonException e) {
            logger.error(String.format("Error creating receipt from iTunes server (%s): %s", e.getMessage(), e.getJson()));
            result.setRetryError("Error processing response");
            return result;
        }
        storeReceipt.setPayload(newPayload.getBytes());

        String expiresDateString = (String) itunesReceipt.get("expires_date");
        if (expiresDateString != null) {
            Long expiresDateMs = Long.parseLong(expiresDateString);
            Date expiresDateDate = null;
            if (expiresDateMs != null) {
                expiresDateDate = new Date(expiresDateMs);
                if (new Date().after(expiresDateDate)) {
                    logger.warn(String.format("iTunes receipt appears to have expired on %s for receipt: %s", expiresDateDate, receipt));
                    result.setNoRetryError("Expired receipt");
                    return result;
                }
            }
            storeReceipt.setExpirationDate(expiresDateDate);
        }

        storeReceipt.setPayload(newPayload.getBytes());

        logReceiptVerified(storeReceipt, receiptTransactionId);

        result.setStoreReceipt(storeReceipt);
        return result;
    }

//    private static String mattPrivateTasks64String1 =      "ewoJInNpZ25hdHVyZSIgPSAiQXJWMDdBYWV3VjZGU1plYkxtLzhnTmJxcEg3TnJJTktCdUwxTFgvQ2x0RGwxVmVXN2tJczEyanBUK3VDaHdkL1RWOTh5dVR1ZXN5WGV3cC9xbnhzNGpnelNqSlNsUVhYMnhUMmZ1T3dKNmxQam1qSy9EMkxLYUF0L3FJK0dhY3dreENvSDZlZityWWhIaW4zUElRUzAvOXVyQnlJSXF0WHgya1pWeUZDek9ab0FBQURWekNDQTFNd2dnSTdvQU1DQVFJQ0NHVVVrVTNaV0FTMU1BMEdDU3FHU0liM0RRRUJCUVVBTUg4eEN6QUpCZ05WQkFZVEFsVlRNUk13RVFZRFZRUUtEQXBCY0hCc1pTQkpibU11TVNZd0pBWURWUVFMREIxQmNIQnNaU0JEWlhKMGFXWnBZMkYwYVc5dUlFRjFkR2h2Y21sMGVURXpNREVHQTFVRUF3d3FRWEJ3YkdVZ2FWUjFibVZ6SUZOMGIzSmxJRU5sY25ScFptbGpZWFJwYjI0Z1FYVjBhRzl5YVhSNU1CNFhEVEE1TURZeE5USXlNRFUxTmxvWERURTBNRFl4TkRJeU1EVTFObG93WkRFak1DRUdBMVVFQXd3YVVIVnlZMmhoYzJWU1pXTmxhWEIwUTJWeWRHbG1hV05oZEdVeEd6QVpCZ05WQkFzTUVrRndjR3hsSUdsVWRXNWxjeUJUZEc5eVpURVRNQkVHQTFVRUNnd0tRWEJ3YkdVZ1NXNWpMakVMTUFrR0ExVUVCaE1DVlZNd2daOHdEUVlKS29aSWh2Y05BUUVCQlFBRGdZMEFNSUdKQW9HQkFNclJqRjJjdDRJclNkaVRDaGFJMGc4cHd2L2NtSHM4cC9Sd1YvcnQvOTFYS1ZoTmw0WElCaW1LalFRTmZnSHNEczZ5anUrK0RyS0pFN3VLc3BoTWRkS1lmRkU1ckdYc0FkQkVqQndSSXhleFRldngzSExFRkdBdDFtb0t4NTA5ZGh4dGlJZERnSnYyWWFWczQ5QjB1SnZOZHk2U01xTk5MSHNETHpEUzlvWkhBZ01CQUFHamNqQndNQXdHQTFVZEV3RUIvd1FDTUFBd0h3WURWUjBqQkJnd0ZvQVVOaDNvNHAyQzBnRVl0VEpyRHRkREM1RllRem93RGdZRFZSMFBBUUgvQkFRREFnZUFNQjBHQTFVZERnUVdCQlNwZzRQeUdVakZQaEpYQ0JUTXphTittVjhrOVRBUUJnb3Foa2lHOTJOa0JnVUJCQUlGQURBTkJna3Foa2lHOXcwQkFRVUZBQU9DQVFFQUVhU2JQanRtTjRDL0lCM1FFcEszMlJ4YWNDRFhkVlhBZVZSZVM1RmFaeGMrdDg4cFFQOTNCaUF4dmRXLzNlVFNNR1k1RmJlQVlMM2V0cVA1Z204d3JGb2pYMGlreVZSU3RRKy9BUTBLRWp0cUIwN2tMczlRVWU4Y3pSOFVHZmRNMUV1bVYvVWd2RGQ0TndOWXhMUU1nNFdUUWZna1FRVnk4R1had1ZIZ2JFL1VDNlk3MDUzcEdYQms1MU5QTTN3b3hoZDNnU1JMdlhqK2xvSHNTdGNURXFlOXBCRHBtRzUrc2s0dHcrR0szR01lRU41LytlMVFUOW5wL0tsMW5qK2FCdzdDMHhzeTBiRm5hQWQxY1NTNnhkb3J5L0NVdk02Z3RLc21uT09kcVRlc2JwMGJzOHNuNldxczBDOWRnY3hSSHVPTVoydG04bnBMVW03YXJnT1N6UT09IjsKCSJwdXJjaGFzZS1pbmZvIiA9ICJld29KSW05eWFXZHBibUZzTFhCMWNtTm9ZWE5sTFdSaGRHVXRjSE4wSWlBOUlDSXlNREV6TFRBeUxUSXdJREl5T2pFd09qVXpJRUZ0WlhKcFkyRXZURzl6WDBGdVoyVnNaWE1pT3dvSkluVnVhWEYxWlMxcFpHVnVkR2xtYVdWeUlpQTlJQ0ptTnpjeE1EWTNPR1U0TlRVNVkyRTBNemRsTm1JM1pEUm1ZalE1WXpRM09HTXlOVEUxWVRFNElqc0tDU0p2Y21sbmFXNWhiQzEwY21GdWMyRmpkR2x2YmkxcFpDSWdQU0FpTVRBd01EQXdNREEyTlRjd01qazNNU0k3Q2draVluWnljeUlnUFNBaU1TNHdJanNLQ1NKMGNtRnVjMkZqZEdsdmJpMXBaQ0lnUFNBaU1UQXdNREF3TURBMk5UY3dOVEk1T0NJN0Nna2ljWFZoYm5ScGRIa2lJRDBnSWpFaU93b0pJbTl5YVdkcGJtRnNMWEIxY21Ob1lYTmxMV1JoZEdVdGJYTWlJRDBnSWpFek5qRTBNamN3TlRNd01EQWlPd29KSW5CeWIyUjFZM1F0YVdRaUlEMGdJamxoTTJVd01EaGxYMlJqWkdSZk5EQTVabDloTnpjMFgyUmxabVEzTW1ReU9Ua3hNU0k3Q2draWFYUmxiUzFwWkNJZ1BTQWlOakEwTWpFeE5ESXpJanNLQ1NKaWFXUWlJRDBnSW1OdmJTNXRaUzFwYm1NdVpuSmhibXRzYVc1RGIzWmxlUzVtYVhKemRGUm9hVzVuYzFWdWFYWmxjbk5oYkNJN0Nna2ljSFZ5WTJoaGMyVXRaR0YwWlMxdGN5SWdQU0FpTVRNMk1UUXlOems1TURjeU9DSTdDZ2tpY0hWeVkyaGhjMlV0WkdGMFpTSWdQU0FpTWpBeE15MHdNaTB5TVNBd05qb3lOam96TUNCRmRHTXZSMDFVSWpzS0NTSndkWEpqYUdGelpTMWtZWFJsTFhCemRDSWdQU0FpTWpBeE15MHdNaTB5TUNBeU1qb3lOam96TUNCQmJXVnlhV05oTDB4dmMxOUJibWRsYkdWeklqc0tDU0p2Y21sbmFXNWhiQzF3ZFhKamFHRnpaUzFrWVhSbElpQTlJQ0l5TURFekxUQXlMVEl4SURBMk9qRXdPalV6SUVWMFl5OUhUVlFpT3dwOSI7CgkiZW52aXJvbm1lbnQiID0gIlNhbmRib3giOwoJInBvZCIgPSAiMTAwIjsKCSJzaWduaW5nLXN0YXR1cyIgPSAiMCI7Cn0=";
//    private static String mattPremiumMonthTask64String =   "ewoJInNpZ25hdHVyZSIgPSAiQW00VnMwbHBHelZmTm8xNXJKMDVFZ0lHVlN3WUJjZDNZc3ZpNHhOUW9ZNy9HbFo2RWF2Qkc2NWtVU3hBcmZiMnA1UDNqVlIzZkxUeHJEaDc0M2U5WjI3U3E5YmFlQ2ZTTEpvM09hS1JleVhrTU9WRmRwUWRib0tCUENmWUd1N1psVkpLd0NidWtTZWZtVmpEN3hWN2JEYTJCU05uYWN3SGE1c0U0Tm4vc3ArMEFBQURWekNDQTFNd2dnSTdvQU1DQVFJQ0NHVVVrVTNaV0FTMU1BMEdDU3FHU0liM0RRRUJCUVVBTUg4eEN6QUpCZ05WQkFZVEFsVlRNUk13RVFZRFZRUUtEQXBCY0hCc1pTQkpibU11TVNZd0pBWURWUVFMREIxQmNIQnNaU0JEWlhKMGFXWnBZMkYwYVc5dUlFRjFkR2h2Y21sMGVURXpNREVHQTFVRUF3d3FRWEJ3YkdVZ2FWUjFibVZ6SUZOMGIzSmxJRU5sY25ScFptbGpZWFJwYjI0Z1FYVjBhRzl5YVhSNU1CNFhEVEE1TURZeE5USXlNRFUxTmxvWERURTBNRFl4TkRJeU1EVTFObG93WkRFak1DRUdBMVVFQXd3YVVIVnlZMmhoYzJWU1pXTmxhWEIwUTJWeWRHbG1hV05oZEdVeEd6QVpCZ05WQkFzTUVrRndjR3hsSUdsVWRXNWxjeUJUZEc5eVpURVRNQkVHQTFVRUNnd0tRWEJ3YkdVZ1NXNWpMakVMTUFrR0ExVUVCaE1DVlZNd2daOHdEUVlKS29aSWh2Y05BUUVCQlFBRGdZMEFNSUdKQW9HQkFNclJqRjJjdDRJclNkaVRDaGFJMGc4cHd2L2NtSHM4cC9Sd1YvcnQvOTFYS1ZoTmw0WElCaW1LalFRTmZnSHNEczZ5anUrK0RyS0pFN3VLc3BoTWRkS1lmRkU1ckdYc0FkQkVqQndSSXhleFRldngzSExFRkdBdDFtb0t4NTA5ZGh4dGlJZERnSnYyWWFWczQ5QjB1SnZOZHk2U01xTk5MSHNETHpEUzlvWkhBZ01CQUFHamNqQndNQXdHQTFVZEV3RUIvd1FDTUFBd0h3WURWUjBqQkJnd0ZvQVVOaDNvNHAyQzBnRVl0VEpyRHRkREM1RllRem93RGdZRFZSMFBBUUgvQkFRREFnZUFNQjBHQTFVZERnUVdCQlNwZzRQeUdVakZQaEpYQ0JUTXphTittVjhrOVRBUUJnb3Foa2lHOTJOa0JnVUJCQUlGQURBTkJna3Foa2lHOXcwQkFRVUZBQU9DQVFFQUVhU2JQanRtTjRDL0lCM1FFcEszMlJ4YWNDRFhkVlhBZVZSZVM1RmFaeGMrdDg4cFFQOTNCaUF4dmRXLzNlVFNNR1k1RmJlQVlMM2V0cVA1Z204d3JGb2pYMGlreVZSU3RRKy9BUTBLRWp0cUIwN2tMczlRVWU4Y3pSOFVHZmRNMUV1bVYvVWd2RGQ0TndOWXhMUU1nNFdUUWZna1FRVnk4R1had1ZIZ2JFL1VDNlk3MDUzcEdYQms1MU5QTTN3b3hoZDNnU1JMdlhqK2xvSHNTdGNURXFlOXBCRHBtRzUrc2s0dHcrR0szR01lRU41LytlMVFUOW5wL0tsMW5qK2FCdzdDMHhzeTBiRm5hQWQxY1NTNnhkb3J5L0NVdk02Z3RLc21uT09kcVRlc2JwMGJzOHNuNldxczBDOWRnY3hSSHVPTVoydG04bnBMVW03YXJnT1N6UT09IjsKCSJwdXJjaGFzZS1pbmZvIiA9ICJld29KSW05eWFXZHBibUZzTFhCMWNtTm9ZWE5sTFdSaGRHVXRjSE4wSWlBOUlDSXlNREV6TFRBekxUQTFJREV6T2pRd09qUTFJRUZ0WlhKcFkyRXZURzl6WDBGdVoyVnNaWE1pT3dvSkluQjFjbU5vWVhObExXUmhkR1V0YlhNaUlEMGdJakV6TmpJMU1UazJORFV5T1RraU93b0pJblZ1YVhGMVpTMXBaR1Z1ZEdsbWFXVnlJaUE5SUNKbU56Y3hNRFkzT0dVNE5UVTVZMkUwTXpkbE5tSTNaRFJtWWpRNVl6UTNPR015TlRFMVlURTRJanNLQ1NKdmNtbG5hVzVoYkMxMGNtRnVjMkZqZEdsdmJpMXBaQ0lnUFNBaU1UQXdNREF3TURBMk56QTFOekF4TVNJN0Nna2laWGh3YVhKbGN5MWtZWFJsSWlBOUlDSXhNell5TlRFNU9UUTFNams1SWpzS0NTSjBjbUZ1YzJGamRHbHZiaTFwWkNJZ1BTQWlNVEF3TURBd01EQTJOekExTnpBeE1TSTdDZ2tpYjNKcFoybHVZV3d0Y0hWeVkyaGhjMlV0WkdGMFpTMXRjeUlnUFNBaU1UTTJNalV4T1RZME5UWXhNQ0k3Q2draWQyVmlMVzl5WkdWeUxXeHBibVV0YVhSbGJTMXBaQ0lnUFNBaU1UQXdNREF3TURBeU5qY3dOekl4T0NJN0Nna2lZblp5Y3lJZ1BTQWlNUzR3SWpzS0NTSmxlSEJwY21WekxXUmhkR1V0Wm05eWJXRjBkR1ZrTFhCemRDSWdQU0FpTWpBeE15MHdNeTB3TlNBeE16bzBOVG8wTlNCQmJXVnlhV05oTDB4dmMxOUJibWRsYkdWeklqc0tDU0pwZEdWdExXbGtJaUE5SUNJMk1EVXhPVEkyTlRnaU93b0pJbVY0Y0dseVpYTXRaR0YwWlMxbWIzSnRZWFIwWldRaUlEMGdJakl3TVRNdE1ETXRNRFVnTWpFNk5EVTZORFVnUlhSakwwZE5WQ0k3Q2draWNISnZaSFZqZEMxcFpDSWdQU0FpWldSaU1USXdaVGRmTkRBMk0xODBaV0kxWDJFd01HTmZZV1ZpTVRZNU9USmlZV0ZtSWpzS0NTSndkWEpqYUdGelpTMWtZWFJsSWlBOUlDSXlNREV6TFRBekxUQTFJREl4T2pRd09qUTFJRVYwWXk5SFRWUWlPd29KSW05eWFXZHBibUZzTFhCMWNtTm9ZWE5sTFdSaGRHVWlJRDBnSWpJd01UTXRNRE10TURVZ01qRTZOREE2TkRVZ1JYUmpMMGROVkNJN0Nna2lZbWxrSWlBOUlDSmpiMjB1YldVdGFXNWpMbVp5WVc1cmJHbHVRMjkyWlhrdVptbHljM1JVYUdsdVozTlZibWwyWlhKellXd2lPd29KSW5CMWNtTm9ZWE5sTFdSaGRHVXRjSE4wSWlBOUlDSXlNREV6TFRBekxUQTFJREV6T2pRd09qUTFJRUZ0WlhKcFkyRXZURzl6WDBGdVoyVnNaWE1pT3dvSkluRjFZVzUwYVhSNUlpQTlJQ0l4SWpzS2ZRPT0iOwoJImVudmlyb25tZW50IiA9ICJTYW5kYm94IjsKCSJwb2QiID0gIjEwMCI7Cgkic2lnbmluZy1zdGF0dXMiID0gIjAiOwp9";
//    private static String testuser3PrivateTasks64String2 = "ewoJInNpZ25hdHVyZSIgPSAiQXFCb0hwT3FxcXhSKzU2MGV3N1UzeitiRFF3U291V0o2QlFwRFBQMnRPbXFQZUtlbUxZcGFPL2xxQ296T0dRbzRnVzkyb2xuZ21zN1k0L2E5ZkUzYUt1ck9jTytvNjRiSGhET2MwaS8wNWl2cGxQdWdhTlVObUhOa211ME1EN0laNWpnb21RTThOcW9BOVdpRU1HbS9TWUxnWi9QNmI1L0N3akRaU01HV0Q5dEFBQURWekNDQTFNd2dnSTdvQU1DQVFJQ0NHVVVrVTNaV0FTMU1BMEdDU3FHU0liM0RRRUJCUVVBTUg4eEN6QUpCZ05WQkFZVEFsVlRNUk13RVFZRFZRUUtEQXBCY0hCc1pTQkpibU11TVNZd0pBWURWUVFMREIxQmNIQnNaU0JEWlhKMGFXWnBZMkYwYVc5dUlFRjFkR2h2Y21sMGVURXpNREVHQTFVRUF3d3FRWEJ3YkdVZ2FWUjFibVZ6SUZOMGIzSmxJRU5sY25ScFptbGpZWFJwYjI0Z1FYVjBhRzl5YVhSNU1CNFhEVEE1TURZeE5USXlNRFUxTmxvWERURTBNRFl4TkRJeU1EVTFObG93WkRFak1DRUdBMVVFQXd3YVVIVnlZMmhoYzJWU1pXTmxhWEIwUTJWeWRHbG1hV05oZEdVeEd6QVpCZ05WQkFzTUVrRndjR3hsSUdsVWRXNWxjeUJUZEc5eVpURVRNQkVHQTFVRUNnd0tRWEJ3YkdVZ1NXNWpMakVMTUFrR0ExVUVCaE1DVlZNd2daOHdEUVlKS29aSWh2Y05BUUVCQlFBRGdZMEFNSUdKQW9HQkFNclJqRjJjdDRJclNkaVRDaGFJMGc4cHd2L2NtSHM4cC9Sd1YvcnQvOTFYS1ZoTmw0WElCaW1LalFRTmZnSHNEczZ5anUrK0RyS0pFN3VLc3BoTWRkS1lmRkU1ckdYc0FkQkVqQndSSXhleFRldngzSExFRkdBdDFtb0t4NTA5ZGh4dGlJZERnSnYyWWFWczQ5QjB1SnZOZHk2U01xTk5MSHNETHpEUzlvWkhBZ01CQUFHamNqQndNQXdHQTFVZEV3RUIvd1FDTUFBd0h3WURWUjBqQkJnd0ZvQVVOaDNvNHAyQzBnRVl0VEpyRHRkREM1RllRem93RGdZRFZSMFBBUUgvQkFRREFnZUFNQjBHQTFVZERnUVdCQlNwZzRQeUdVakZQaEpYQ0JUTXphTittVjhrOVRBUUJnb3Foa2lHOTJOa0JnVUJCQUlGQURBTkJna3Foa2lHOXcwQkFRVUZBQU9DQVFFQUVhU2JQanRtTjRDL0lCM1FFcEszMlJ4YWNDRFhkVlhBZVZSZVM1RmFaeGMrdDg4cFFQOTNCaUF4dmRXLzNlVFNNR1k1RmJlQVlMM2V0cVA1Z204d3JGb2pYMGlreVZSU3RRKy9BUTBLRWp0cUIwN2tMczlRVWU4Y3pSOFVHZmRNMUV1bVYvVWd2RGQ0TndOWXhMUU1nNFdUUWZna1FRVnk4R1had1ZIZ2JFL1VDNlk3MDUzcEdYQms1MU5QTTN3b3hoZDNnU1JMdlhqK2xvSHNTdGNURXFlOXBCRHBtRzUrc2s0dHcrR0szR01lRU41LytlMVFUOW5wL0tsMW5qK2FCdzdDMHhzeTBiRm5hQWQxY1NTNnhkb3J5L0NVdk02Z3RLc21uT09kcVRlc2JwMGJzOHNuNldxczBDOWRnY3hSSHVPTVoydG04bnBMVW03YXJnT1N6UT09IjsKCSJwdXJjaGFzZS1pbmZvIiA9ICJld29KSW05eWFXZHBibUZzTFhCMWNtTm9ZWE5sTFdSaGRHVXRjSE4wSWlBOUlDSXlNREV6TFRBeUxUSXdJREl6T2pBME9qTTRJRUZ0WlhKcFkyRXZURzl6WDBGdVoyVnNaWE1pT3dvSkluVnVhWEYxWlMxcFpHVnVkR2xtYVdWeUlpQTlJQ0ptTnpjeE1EWTNPR1U0TlRVNVkyRTBNemRsTm1JM1pEUm1ZalE1WXpRM09HTXlOVEUxWVRFNElqc0tDU0p2Y21sbmFXNWhiQzEwY21GdWMyRmpkR2x2YmkxcFpDSWdQU0FpTVRBd01EQXdNREEyTlRjeE1ESXpOQ0k3Q2draVluWnljeUlnUFNBaU1TNHdJanNLQ1NKMGNtRnVjMkZqZEdsdmJpMXBaQ0lnUFNBaU1UQXdNREF3TURBMk5UY3hNREl6TkNJN0Nna2ljWFZoYm5ScGRIa2lJRDBnSWpFaU93b0pJbTl5YVdkcGJtRnNMWEIxY21Ob1lYTmxMV1JoZEdVdGJYTWlJRDBnSWpFek5qRTBNekF5Tnpnd09UUWlPd29KSW5CeWIyUjFZM1F0YVdRaUlEMGdJamxoTTJVd01EaGxYMlJqWkdSZk5EQTVabDloTnpjMFgyUmxabVEzTW1ReU9Ua3hNU0k3Q2draWFYUmxiUzFwWkNJZ1BTQWlOakEwTWpFeE5ESXpJanNLQ1NKaWFXUWlJRDBnSW1OdmJTNXRaUzFwYm1NdVpuSmhibXRzYVc1RGIzWmxlUzVtYVhKemRGUm9hVzVuYzFWdWFYWmxjbk5oYkNJN0Nna2ljSFZ5WTJoaGMyVXRaR0YwWlMxdGN5SWdQU0FpTVRNMk1UUXpNREkzT0RBNU5DSTdDZ2tpY0hWeVkyaGhjMlV0WkdGMFpTSWdQU0FpTWpBeE15MHdNaTB5TVNBd056b3dORG96T0NCRmRHTXZSMDFVSWpzS0NTSndkWEpqYUdGelpTMWtZWFJsTFhCemRDSWdQU0FpTWpBeE15MHdNaTB5TUNBeU16b3dORG96T0NCQmJXVnlhV05oTDB4dmMxOUJibWRsYkdWeklqc0tDU0p2Y21sbmFXNWhiQzF3ZFhKamFHRnpaUzFrWVhSbElpQTlJQ0l5TURFekxUQXlMVEl4SURBM09qQTBPak00SUVWMFl5OUhUVlFpT3dwOSI7CgkiZW52aXJvbm1lbnQiID0gIlNhbmRib3giOwoJInBvZCIgPSAiMTAwIjsKCSJzaWduaW5nLXN0YXR1cyIgPSAiMCI7Cn0=";
//    private static String premiumOneMonth64String3 =       "ewoJInNpZ25hdHVyZSIgPSAiQWhLRmJqZFIrZGN4RWNiQmpKVkZ4RFZvZFFyZWtxaVJqVWdWaUtZZllGdnlOZm1acFVHdk50Um0zN1Y2QUtzOEJiY3VodzloNk1zOWhrTy8xL2R6blo2YWt1NEE5aEVVa0hLVUVjRnV1VzcrT2x5RUxHWXBKVDA5dW94RXN3dndKUGJ3QXNIYTFnUVZjVWw5aTFHdFhGdGVhT2VLNm9QRWxBVE0wdE1OaWQyV0FBQURWekNDQTFNd2dnSTdvQU1DQVFJQ0NHVVVrVTNaV0FTMU1BMEdDU3FHU0liM0RRRUJCUVVBTUg4eEN6QUpCZ05WQkFZVEFsVlRNUk13RVFZRFZRUUtEQXBCY0hCc1pTQkpibU11TVNZd0pBWURWUVFMREIxQmNIQnNaU0JEWlhKMGFXWnBZMkYwYVc5dUlFRjFkR2h2Y21sMGVURXpNREVHQTFVRUF3d3FRWEJ3YkdVZ2FWUjFibVZ6SUZOMGIzSmxJRU5sY25ScFptbGpZWFJwYjI0Z1FYVjBhRzl5YVhSNU1CNFhEVEE1TURZeE5USXlNRFUxTmxvWERURTBNRFl4TkRJeU1EVTFObG93WkRFak1DRUdBMVVFQXd3YVVIVnlZMmhoYzJWU1pXTmxhWEIwUTJWeWRHbG1hV05oZEdVeEd6QVpCZ05WQkFzTUVrRndjR3hsSUdsVWRXNWxjeUJUZEc5eVpURVRNQkVHQTFVRUNnd0tRWEJ3YkdVZ1NXNWpMakVMTUFrR0ExVUVCaE1DVlZNd2daOHdEUVlKS29aSWh2Y05BUUVCQlFBRGdZMEFNSUdKQW9HQkFNclJqRjJjdDRJclNkaVRDaGFJMGc4cHd2L2NtSHM4cC9Sd1YvcnQvOTFYS1ZoTmw0WElCaW1LalFRTmZnSHNEczZ5anUrK0RyS0pFN3VLc3BoTWRkS1lmRkU1ckdYc0FkQkVqQndSSXhleFRldngzSExFRkdBdDFtb0t4NTA5ZGh4dGlJZERnSnYyWWFWczQ5QjB1SnZOZHk2U01xTk5MSHNETHpEUzlvWkhBZ01CQUFHamNqQndNQXdHQTFVZEV3RUIvd1FDTUFBd0h3WURWUjBqQkJnd0ZvQVVOaDNvNHAyQzBnRVl0VEpyRHRkREM1RllRem93RGdZRFZSMFBBUUgvQkFRREFnZUFNQjBHQTFVZERnUVdCQlNwZzRQeUdVakZQaEpYQ0JUTXphTittVjhrOVRBUUJnb3Foa2lHOTJOa0JnVUJCQUlGQURBTkJna3Foa2lHOXcwQkFRVUZBQU9DQVFFQUVhU2JQanRtTjRDL0lCM1FFcEszMlJ4YWNDRFhkVlhBZVZSZVM1RmFaeGMrdDg4cFFQOTNCaUF4dmRXLzNlVFNNR1k1RmJlQVlMM2V0cVA1Z204d3JGb2pYMGlreVZSU3RRKy9BUTBLRWp0cUIwN2tMczlRVWU4Y3pSOFVHZmRNMUV1bVYvVWd2RGQ0TndOWXhMUU1nNFdUUWZna1FRVnk4R1had1ZIZ2JFL1VDNlk3MDUzcEdYQms1MU5QTTN3b3hoZDNnU1JMdlhqK2xvSHNTdGNURXFlOXBCRHBtRzUrc2s0dHcrR0szR01lRU41LytlMVFUOW5wL0tsMW5qK2FCdzdDMHhzeTBiRm5hQWQxY1NTNnhkb3J5L0NVdk02Z3RLc21uT09kcVRlc2JwMGJzOHNuNldxczBDOWRnY3hSSHVPTVoydG04bnBMVW03YXJnT1N6UT09IjsKCSJwdXJjaGFzZS1pbmZvIiA9ICJld29KSW05eWFXZHBibUZzTFhCMWNtTm9ZWE5sTFdSaGRHVXRjSE4wSWlBOUlDSXlNREV6TFRBeUxUSXdJREl6T2pNM09qUXpJRUZ0WlhKcFkyRXZURzl6WDBGdVoyVnNaWE1pT3dvSkluQjFjbU5vWVhObExXUmhkR1V0YlhNaUlEMGdJakV6TmpFME16SXlOakk0TVRBaU93b0pJblZ1YVhGMVpTMXBaR1Z1ZEdsbWFXVnlJaUE5SUNKbU56Y3hNRFkzT0dVNE5UVTVZMkUwTXpkbE5tSTNaRFJtWWpRNVl6UTNPR015TlRFMVlURTRJanNLQ1NKdmNtbG5hVzVoYkMxMGNtRnVjMkZqZEdsdmJpMXBaQ0lnUFNBaU1UQXdNREF3TURBMk5UY3hOREU1TnlJN0Nna2laWGh3YVhKbGN5MWtZWFJsSWlBOUlDSXhNell4TkRNeU5UWXlPREV3SWpzS0NTSjBjbUZ1YzJGamRHbHZiaTFwWkNJZ1BTQWlNVEF3TURBd01EQTJOVGN4TkRFNU55STdDZ2tpYjNKcFoybHVZV3d0Y0hWeVkyaGhjMlV0WkdGMFpTMXRjeUlnUFNBaU1UTTJNVFF6TWpJMk16RXlPU0k3Q2draWQyVmlMVzl5WkdWeUxXeHBibVV0YVhSbGJTMXBaQ0lnUFNBaU1UQXdNREF3TURBeU5qWTJOalk1TlNJN0Nna2lZblp5Y3lJZ1BTQWlNUzR3SWpzS0NTSmxlSEJwY21WekxXUmhkR1V0Wm05eWJXRjBkR1ZrTFhCemRDSWdQU0FpTWpBeE15MHdNaTB5TUNBeU16bzBNam8wTWlCQmJXVnlhV05oTDB4dmMxOUJibWRsYkdWeklqc0tDU0pwZEdWdExXbGtJaUE5SUNJMk1EVXhPVEkyTlRnaU93b0pJbVY0Y0dseVpYTXRaR0YwWlMxbWIzSnRZWFIwWldRaUlEMGdJakl3TVRNdE1ESXRNakVnTURjNk5ESTZORElnUlhSakwwZE5WQ0k3Q2draWNISnZaSFZqZEMxcFpDSWdQU0FpWldSaU1USXdaVGRmTkRBMk0xODBaV0kxWDJFd01HTmZZV1ZpTVRZNU9USmlZV0ZtSWpzS0NTSndkWEpqYUdGelpTMWtZWFJsSWlBOUlDSXlNREV6TFRBeUxUSXhJREEzT2pNM09qUXlJRVYwWXk5SFRWUWlPd29KSW05eWFXZHBibUZzTFhCMWNtTm9ZWE5sTFdSaGRHVWlJRDBnSWpJd01UTXRNREl0TWpFZ01EYzZNemM2TkRNZ1JYUmpMMGROVkNJN0Nna2lZbWxrSWlBOUlDSmpiMjB1YldVdGFXNWpMbVp5WVc1cmJHbHVRMjkyWlhrdVptbHljM1JVYUdsdVozTlZibWwyWlhKellXd2lPd29KSW5CMWNtTm9ZWE5sTFdSaGRHVXRjSE4wSWlBOUlDSXlNREV6TFRBeUxUSXdJREl6T2pNM09qUXlJRUZ0WlhKcFkyRXZURzl6WDBGdVoyVnNaWE1pT3dvSkluRjFZVzUwYVhSNUlpQTlJQ0l4SWpzS2ZRPT0iOwoJImVudmlyb25tZW50IiA9ICJTYW5kYm94IjsKCSJwb2QiID0gIjEwMCI7Cgkic2lnbmluZy1zdGF0dXMiID0gIjAiOwp9";
//
//    public static void main(String[] args) throws MalformedURLException, InvalidJsonException {
//        BasicConfigurator.configure();
//
//        ITunesProcessor p = new ITunesProcessor();
//        p.setITunesServer("sandbox");
//        p.setAppleSharedSecret("0367626d88c241d1ae5c4c5c331d6bba");
//
//        Receipt r = new Receipt();
//        r.setType(ReceiptType.ITUNES);
////        r.setItemUuid("9a3e008e-dcdd-409f-a774-defd72d29911");
//        r.setItemUuid("edb120e7-4063-4eb5-a00c-aeb16992baaf");
//        String payload = Receipt.createItunesPayload(mattPremiumMonthTask64String, null);
//        r.setPayload(payload.getBytes());
//
//        ReceiptResult r2 = p.verifyReceipt(r, "com.meinc.shout");
//        System.out.println(r2);
//    }

}
