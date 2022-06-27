package tv.shout.sm.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.commons.encryption.EncryptUtils;
import com.meinc.commons.encryption.EncryptionService;
import com.meinc.commons.encryption.HexUtils;

import tv.shout.sc.domain.Game;
import tv.shout.sm.admin.AdminGame;
import tv.shout.sm.db.DbProvider;
import tv.shout.sm.db.DbProvider.DB;
import tv.shout.util.DateUtil;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;

public class CollectorToWdsResponse
{
    private static Logger _logger = Logger.getLogger(CollectorToWdsResponse.class);

    /** how long to wait (in ms) between each check for a WDS response */
    private static final long[] WAIT_TIMES_MS = {250, 500, 750, 1000, 1500, 2000, 3000, 4000, 5000};

    /**
     * A list of supported request types that this library supports
     */
    public enum REQUEST_TYPE
    {
        SUBSCRIBER_SIGNUP
        ,SUBSCRIBER_AUTH

        ,GET_QUESTION_CATEGORIES_FROM_KEYS
        ,UPLOAD_PHOTO
        ,STORE_GET_CLIENT_TOKEN
        ,STORE_GET_ITEMS
        ,STORE_PURCHASE_ITEM
        ,PLAYER_GET_DETAILS
        ,PLAYER_GET_GAMES
        ,PLAYER_GET_ROUNDS
        ,GAME_JOIN
        ,GAME_LEAVE
        ,GAME_BEGIN_POOL_PLAY
        ,GAME_CANCEL_POOL_PLAY
        ,GAME_BEGIN_BRACKET_PLAY
        ,GAME_GET_SYNC_MESSAGES
        //,BOT_GAME_ADD
        //,BOT_GAME_LIST
        //,BOT_GAME_REMOVE
        ,QUESTION_GET_DECRYPT_KEY
        ,QUESTION_SUBMIT_ANSWER

        ,ADMIN_BEGIN_POOL_PLAY
        ,ADMIN_BEGIN_BRACKET_PLAY
        ,ADMIN_GAME_CANCEL
        ,ADMIN_LIST_GAMES
        ,ADMIN_OPEN_GAME
        ,ADMIN_GET_GAME_ROUNDS
        ,ADMIN_GET_CATEGORY_IDS_TO_KEYS
        ,ADMIN_GAME_CREATE
        ,ADMIN_GET_GAME
        ,ADMIN_CLONE_GAME
        ,ADMIN_CATEGORY_CREATE
        ,ADMIN_GAME_FORENSICS
    }

    /**
     * A list of possible error types that can be returned from the requests
     */
    public enum ERROR_TYPE
    {
        MISSING_REQUIRED_PARAM
        ,INVALID_PARAM

        ,ROUND_LOCKED
        ,DUPLICATE_ANSWER

        ,UNEXPECTED_ERROR
    }

    /**
     * An interface that callers of this library must implement in order to receive asynchronous notification when the data has been retrieved
     */
    public interface DataReceiver
    {
        /**
         * Callback method for when a call is successful (HTTP response is 200 <i>and</i> the json success=true)
         *
         * @param requestType which type of request was made
         * @param json the json payload of the response
         */
        void dataCallbackSuccess(REQUEST_TYPE requestType, JsonNode json);

        /**
         * Callback method for when a call is <b>not</b> successful (HTTP response is 40x/50x <i>or</i> the json success=false)
         *
         * @param requestType which type of request was made
         * @param errorType which type of error occurred (only relevant if httpResponseCode != 200)
         * @param httpResponseCode the HTTP response code
         * @param responseMessage the server response message, if any
         * @param responseHeaders a list of HTTP response headers
         * @param responseBody the raw HTTP response body
         */
        void dataCallbackFailure(REQUEST_TYPE requestType, ERROR_TYPE errorType, int httpResponseCode, String responseMessage, Map<String, List<String>> responseHeaders, String responseBody);
    }

    private DB _which;

    public CollectorToWdsResponse(DbProvider.DB which)
    {
        _which = which;
    }

    public void subscriberSignup(
        DataReceiver callback,
        String email, String password, String deviceId, String firstname, String lastname, String nickname, String countryCode, String languageCode
    )
    {
        String sha256OfPassword = HexUtils.stringToSha256HexString(password, true);
        String scryptOfSha256OfPassword = new EncryptionService().scryptEncode(sha256OfPassword);

        Map<String, String> params = getDefaultParams();
        params.put("email", email);
        params.put("password", scryptOfSha256OfPassword);
        params.put("firstName", firstname);
        params.put("lastName", lastname);
        params.put("nickName", nickname);
        params.put("languageCode", languageCode);
        params.put("countryCode", countryCode);

        Map<String, String> headers = new HashMap<>();
        headers.put("X-REST-DEVICE-ID", deviceId);
        headers.put("X-REST-APPLICATION-ID", "snowyowl");
        headers.put("X-REST-APPLICATION-VERSION", "1.0");
        //headers.put("deviceModel", "loadtest"");
        //headers.put("deviceName", "Load Test");
        //headers.put("deviceVersion", "1.0");
        //headers.put("deviceOsName", "Loadtest");
        //headers.put("deviceOsType", "deadsimple"");

        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/auth/signup", REQUEST_TYPE.SUBSCRIBER_SIGNUP);
    }

    public void subscriberLogin(DataReceiver callback, String email, String password, String deviceId)
    {
        String sha256OfPassword = HexUtils.stringToSha256HexString(password, true);

        Map<String, String> params = getDefaultParams();
        params.put("email", email);
        params.put("password", sha256OfPassword);

        Map<String, String> headers = new HashMap<>();
        headers.put("X-REST-DEVICE-ID", deviceId);
        headers.put("X-REST-APPLICATION-ID", "snowyowl");
        headers.put("X-REST-APPLICATION-VERSION", "1.0");
        //headers.put("deviceModel", "loadtest"");
        //headers.put("deviceName", "Load Test");
        //headers.put("deviceVersion", "1.0");
        //headers.put("deviceOsName", "Loadtest");
        //headers.put("deviceOsType", "deadsimple"");

        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/auth/login", REQUEST_TYPE.SUBSCRIBER_AUTH);
    }

    public void getQuestionCategoriesFromCategoryKeys(DataReceiver callback, List<String> categories)
    {
        Map<String, String> params = getDefaultParams();
        params.put("categories", categories.stream().collect(Collectors.joining(","))); //convert to comma-delimited string
        executeInitialNetworkCallOnBackgroundThread(callback, null, params, "/snowl/getQuestionCategoriesFromCategoryKeys", REQUEST_TYPE.GET_QUESTION_CATEGORIES_FROM_KEYS);
    }

    public void uploadPhoto(DataReceiver callback, String filename)
    {
        //do the networking on a background thread
        new Thread() {
            @Override
            public void run()
            {
                try {
                    int idx = filename.lastIndexOf(".");
                    String extension = filename.substring(idx+1);
//_logger.info("extension: " + extension);

                    String attachmentFileName = UUID.randomUUID().toString() + "." + extension;
                    String attachmentName = "doesnotmatter";
                    String path = "/" + attachmentFileName;
//_logger.info("path: " + path);

                    //read the file to a byte[]
                    Path p = Paths.get(filename);
                    byte[] data = Files.readAllBytes(p);
//_logger.info("filesize: " + data.length);

                    String response = makeHttpMultipartPostMediaCallWithFallbacks(path, attachmentName, attachmentFileName, data);
                    _logger.info(MessageFormat.format("media response:\n{0}", response));

                    String finalUrl = getMediaUrlsFromSRD()[0] + path;

                    ObjectMapper mapper = JsonUtil.getObjectMapper();
                    Map<String, Object> responseMap = new FastMap<>("success", true, "url", finalUrl);
                    JsonNode json = mapper.readTree(mapper.writeValueAsString(responseMap));
                    callback.dataCallbackSuccess(REQUEST_TYPE.UPLOAD_PHOTO, json);

                } catch (IOException e) {
                    //something bad happened. unable to even make the request (network dropped perhaps? or unable to parse the response json)
                    callback.dataCallbackFailure(REQUEST_TYPE.UPLOAD_PHOTO, ERROR_TYPE.UNEXPECTED_ERROR, 200, e.getMessage(), null, null);

                } catch (NetworkException e) {
                    //some type of server error, but at least the request was made (perhaps a 50x error due to server error of some type)
                    callback.dataCallbackFailure(REQUEST_TYPE.UPLOAD_PHOTO, ERROR_TYPE.UNEXPECTED_ERROR, e.httpResponseCode, null, e.responseHeaders, e.responseBody);
                }
            }
        }.start();
    }

    public void storeGetClientToken(DataReceiver callback, Map<String, String> headers)
    {
        Map<String, String> params = getDefaultParams();
        params.put("demo", "true");
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/store/getClientToken", REQUEST_TYPE.STORE_GET_CLIENT_TOKEN);
    }

    public void storeGetItems(DataReceiver callback, Map<String, String> headers)
    {
        Map<String, String> params = getDefaultParams();
        params.put("demo", "true");
        params.put("venue", "com.shout.dailymillionaire");
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/store/getItemsForVenue", REQUEST_TYPE.STORE_GET_ITEMS);
    }

    public void storePurchaseItem(DataReceiver callback, Map<String, String> headers, String itemUuid)
    {
        Map<String, String> params = getDefaultParams();
        params.put("demo", "true");
        params.put("itemUuid", itemUuid);
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/store/purchaseItem", REQUEST_TYPE.STORE_PURCHASE_ITEM);
    }

    public void playerGetDetails(DataReceiver callback, Map<String, String> headers)
    {
        Map<String, String> params = getDefaultParams();
        params.put("demo", "true");
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowl/player/details", REQUEST_TYPE.PLAYER_GET_DETAILS);
    }

    public void playerGetGames(DataReceiver callback, Map<String, String> headers, String filter)
    {
        Map<String, String> params = getDefaultParams();
        params.put("filter", filter);
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowl/player/games", REQUEST_TYPE.PLAYER_GET_GAMES);
    }

    public void playerGetRounds(DataReceiver callback, Map<String, String> headers, String gameId)
    {
        Map<String, String> params = getDefaultParams();
        params.put("gameId", gameId);
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowl/player/rounds", REQUEST_TYPE.PLAYER_GET_ROUNDS);
    }

    public void joinGame(DataReceiver callback, Map<String, String> headers, String gameId)
    {
        Map<String, String> params = getDefaultParams();
        params.put("gameId", gameId);
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowl/game/join", REQUEST_TYPE.GAME_JOIN);
    }

    public void leaveGame(DataReceiver callback, Map<String, String> headers, String gameId)
    {
        Map<String, String> params = getDefaultParams();
        params.put("gameId", gameId);
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowl/game/leave", REQUEST_TYPE.GAME_LEAVE);
    }

    public void beginPoolPlay(DataReceiver callback, Map<String, String> headers, String gameId)
    {
        Map<String, String> params = getDefaultParams();
        params.put("demo", "true");
        params.put("gameId", gameId);
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowl/game/beginPoolPlay", REQUEST_TYPE.GAME_BEGIN_POOL_PLAY);
    }

    public void cancelPoolPlay(DataReceiver callback, Map<String, String> headers, String gameId)
    {
        Map<String, String> params = getDefaultParams();
        params.put("demo", "true");
        params.put("gameId", gameId);
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowl/game/cancelPoolPlay", REQUEST_TYPE.GAME_CANCEL_POOL_PLAY);
    }

    public void beginBracketPlay(DataReceiver callback, Map<String, String> headers, String gameId)
    {
        Map<String, String> params = getDefaultParams();
        params.put("gameId", gameId);
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowl/game/beginBracketPlay", REQUEST_TYPE.GAME_BEGIN_BRACKET_PLAY);
    }

    public void getSyncMessages(DataReceiver callback, Map<String, String> headers, String gameId, Date fromDate)
    {
        Map<String, String> params = getDefaultParams();
        params.put("gameId", gameId);
        params.put("fromDate", DateUtil.dateToIso8601(fromDate));
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowl/game/getSyncMessages", REQUEST_TYPE.GAME_GET_SYNC_MESSAGES);
    }

//    public void botGameAdd(DataReceiver callback, Map<String, String> headers, String gameId)
//    {
//        Map<String, String> params = getDefaultParams();
//        params.put("gameId", gameId);
//        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/bottest/game/add", REQUEST_TYPE.BOT_GAME_ADD);
//    }
//
//    public void botGameList(DataReceiver callback, Map<String, String> headers, String gameId)
//    {
//        Map<String, String> params = getDefaultParams();
//        params.put("gameId", gameId);
//        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/bottest/game/list", REQUEST_TYPE.BOT_GAME_LIST);
//    }
//
//    public void botGameRemove(DataReceiver callback, Map<String, String> headers, int botSubscriberId)
//    {
//        Map<String, String> params = getDefaultParams();
//        params.put("botSubscriberId", botSubscriberId+"");
//        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/bottest/game/remove", REQUEST_TYPE.BOT_GAME_REMOVE);
//    }

    public void getQuestionDecryptKey(DataReceiver callback, Map<String, String> headers, String subscriberQuestionAnswerId)
    {
        Map<String, String> params = getDefaultParams();
        params.put("subscriberQuestionAnswerId", subscriberQuestionAnswerId);
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowl/question/getDecryptKey", REQUEST_TYPE.QUESTION_GET_DECRYPT_KEY);
    }

    public void submitAnswer(DataReceiver callback, Map<String, String> headers, String subscriberQuestionAnswerId, String selectedAnswerId)
    {
        Map<String, String> params = getDefaultParams();
        params.put("subscriberQuestionAnswerId", subscriberQuestionAnswerId);
        params.put("selectedAnswerId", selectedAnswerId);
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowl/question/submitAnswer", REQUEST_TYPE.QUESTION_SUBMIT_ANSWER);
    }

    public void adminListGames(DataReceiver callback, Map<String, String> headers, List<Game.GAME_STATUS> statuses)
    {
        String statusAsCommaDelimitedList = statuses.stream().map(s -> s.toString()).collect(Collectors.joining(","));
        Map<String, String> params = getDefaultParams();
        params.put("statuses", statusAsCommaDelimitedList);
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowladmin/games/list", REQUEST_TYPE.ADMIN_LIST_GAMES);
    }

    public void adminGetGame(DataReceiver callback, Map<String, String> headers, String gameId)
    {
        Map<String, String> params = getDefaultParams();
        params.put("gameId", gameId);
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowladmin/game/get", REQUEST_TYPE.ADMIN_GET_GAME);
    }

    public void adminCloneGame(
            DataReceiver callback, Map<String, String> headers,
            String gameId, Date expectedStartDateForPoolPlay, Date expectedStartDateForBracketPlay, Map<String, String> gameNames)
    {
        //convert to json string
        ObjectMapper mapper = JsonUtil.getObjectMapper();
        String gameNamesAsJsonString;
        try {
            gameNamesAsJsonString = mapper.writeValueAsString(gameNames);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Map<String, String> params = getDefaultParams();
        params.put("gameId", gameId);
        params.put("expectedStartDateForPoolPlay", DateUtil.dateToIso8601(expectedStartDateForPoolPlay));
        params.put("expectedStartDateForBracketPlay", DateUtil.dateToIso8601(expectedStartDateForBracketPlay));
        params.put("gameNames", gameNamesAsJsonString);
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowladmin/game/clone", REQUEST_TYPE.ADMIN_CLONE_GAME);
    }

    public void adminOpenGame(DataReceiver callback, Map<String, String> headers, String gameId)
    {
        Map<String, String> params = getDefaultParams();
        params.put("gameId", gameId);
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowladmin/game/open", REQUEST_TYPE.ADMIN_OPEN_GAME);
    }

    public void adminGetGameRounds(DataReceiver callback, Map<String, String> headers, String gameId)
    {
        Map<String, String> params = getDefaultParams();
        params.put("gameId", gameId);
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowladmin/game/getrounds", REQUEST_TYPE.ADMIN_GET_GAME_ROUNDS);
    }

    public void adminStartPoolPlay(DataReceiver callback, Map<String, String> headers, String gameId)
    {
        Map<String, String> params = getDefaultParams();
        params.put("gameId", gameId);
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowladmin/game/beginPoolPlay", REQUEST_TYPE.ADMIN_BEGIN_POOL_PLAY);
    }

    public void adminStartBracketPlay(DataReceiver callback, Map<String, String> headers, String gameId, long beginsInMs)
    {
        Map<String, String> params = getDefaultParams();
        params.put("gameId", gameId);
        params.put("beginsInMs", beginsInMs+"");
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowladmin/game/beginBracketPlay", REQUEST_TYPE.ADMIN_BEGIN_BRACKET_PLAY);
    }

    public void adminCancelGame(DataReceiver callback, Map<String, String> headers, String gameId)
    {
        Map<String, String> params = getDefaultParams();
        params.put("gameId", gameId);
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowladmin/game/cancel", REQUEST_TYPE.ADMIN_GAME_CANCEL);
    }

    public void adminGetQuestionCategoryIdsToKeys(DataReceiver callback, Map<String, String> headers)
    {
        Map<String, String> params = getDefaultParams();
        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowladmin/categories/getIdsToKeys", REQUEST_TYPE.ADMIN_GET_CATEGORY_IDS_TO_KEYS);
    }

    public void adminGameCreate(DataReceiver callback, Map<String, String> headers,
            AdminGame game, Date expectedStartDateForPoolPlay, Date expectedStartDateForBracketPlay, float minimumPayoutAmount, int payoutModelId)
    {
        //convert to json string
        ObjectMapper mapper = JsonUtil.getObjectMapper();
        String gameAsJsonString;
        try {
            gameAsJsonString = mapper.writeValueAsString(game);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Map<String, String> params = getDefaultParams();
        params.put("game", gameAsJsonString);
        params.put("expectedStartDateForPoolPlay", DateUtil.dateToIso8601(expectedStartDateForPoolPlay));
        params.put("expectedStartDateForBracketPlay", DateUtil.dateToIso8601(expectedStartDateForBracketPlay));
        params.put("minimumPayoutAmount", minimumPayoutAmount+"");
        params.put("payoutModelId", payoutModelId+"");

        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowladmin/game/create", REQUEST_TYPE.ADMIN_GAME_CREATE);
    }

    public void adminCreateCategory(DataReceiver callback, Map<String, String> headers, String categoryJson)
    {
        Map<String, String> params = getDefaultParams();
        params.put("category", categoryJson);

        executeInitialNetworkCallOnBackgroundThread(callback, headers, params, "/snowladmin/category/create", REQUEST_TYPE.ADMIN_CATEGORY_CREATE);
    }

    public void adminGetGameForensics(DataReceiver callback, Map<String, String> headers, String gameId)
    {
        Map<String, String> params = getDefaultParams();
        params.put("gameId", gameId);

        executeSyncNetworkCallOnBackgroundThread(callback, headers, params, "/snowladmin/forensics/getGame", REQUEST_TYPE.ADMIN_GAME_FORENSICS);
    }

    private void executeInitialNetworkCallOnBackgroundThread(
        DataReceiver callback, Map<String, String> headers, Map<String, String> params, String path, REQUEST_TYPE requesType)
    {
//        _logger.info("executing call for path: " + path);

        //do the networking on a background thread
        new Thread() {
            @Override
            public void run()
            {
                try {
                    String httpPostJsonResponse = makeHttpPostCollectorCallWithFallbacks(params, headers, path);
                    _logger.debug(MessageFormat.format("collector response:\n{0}", httpPostJsonResponse));

                    CollectorResponse collectorResponse = CollectorResponse.fromJsonString(httpPostJsonResponse);

                    //wait for the async server response on the WDS
                    waitForAsyncResponse(requesType, collectorResponse, -1, callback);

                } catch (IOException e) {
                    //something bad happened. unable to even make the request (network dropped perhaps? or unable to parse the response json)
                    _logger.error("IOE", e);
                    callback.dataCallbackFailure(requesType, ERROR_TYPE.UNEXPECTED_ERROR, 200, e.getMessage(), null, null);

                } catch (NetworkException e) {
                    //some type of server error, but at least the request was made (perhaps a 50x error due to server error of some type)
                    callback.dataCallbackFailure(requesType, ERROR_TYPE.UNEXPECTED_ERROR, e.httpResponseCode, null, e.responseHeaders, e.responseBody);
                }
            }
        }.start();
    }

    private void executeSyncNetworkCallOnBackgroundThread(
        DataReceiver callback, Map<String, String> headers, Map<String, String> params, String path, REQUEST_TYPE requestType)
    {
//        _logger.info("executing call for path: " + path);

        //do the networking on a background thread
        new Thread() {
            @Override
            public void run()
            {
                try {
                    String httpPostJsonResponse = makeHttpPostCollectorCallWithFallbacks(params, headers, path);
                    _logger.debug(MessageFormat.format("collector response:\n{0}", httpPostJsonResponse));

                    ObjectMapper mapper = JsonUtil.getObjectMapper();
                    JsonNode json = mapper.readTree(httpPostJsonResponse);
                    callback.dataCallbackSuccess(requestType, json);

                } catch (IOException e) {
                    //something bad happened. unable to even make the request (network dropped perhaps? or unable to parse the response json)
                    callback.dataCallbackFailure(requestType, ERROR_TYPE.UNEXPECTED_ERROR, 200, e.getMessage(), null, null);

                } catch (NetworkException e) {
                    //some type of server error, but at least the request was made (perhaps a 50x error due to server error of some type)
                    callback.dataCallbackFailure(requestType, ERROR_TYPE.UNEXPECTED_ERROR, e.httpResponseCode, null, e.responseHeaders, e.responseBody);
                }
            }
        }.start();
    }

    /*
     * This will try each collector url, falling back to the next one if there is a 5xx error, until the call succeeds, or there are no more servers to try.
     */
    private String makeHttpPostCollectorCallWithFallbacks(Map<String, String> params, Map<String, String> headers, String path)
    throws IOException, NetworkException
    {
    	String[] collectorUrls = getCollectorUrlsFromSRD();
    	int socketRetryCount = 3;

        String httpPostJsonResponse = null;
        while (socketRetryCount > 0 && httpPostJsonResponse == null) {
            for (int i=0; i<collectorUrls.length; i++) {
            	String postUrl = collectorUrls[i] + path;
_logger.info("executing HTTP POST to collector url: " + postUrl);
            	try {
            		httpPostJsonResponse = HttpLibrary.httpPost(postUrl, headers, params);

            		//if execution gets here it means there was not a network exception; break the loop
            		break;

            	} catch (NetworkException e) {
            		if (e.httpResponseCode >= 500 && e.httpResponseCode <= 599 && i < collectorUrls.length-1) {
            			//the server is down. there is another server to try. try it
            		} else {
            			//there is a different type of network error or all the servers have been tried. give up
            			throw e;
            		}
                } catch (SocketException e) {
                    if (socketRetryCount > 0) {
                        socketRetryCount--;
                        _logger.warn("got socket exception. # of retries left: " + socketRetryCount);
                        try {
                            Thread.sleep(50L);
                        } catch (InterruptedException ignored) {
                        }
                    } else {
                        throw e;
                    }
            	}
            }
        }

        //this shouldn't happen, but just in case
        if (httpPostJsonResponse == null) {
        	throw new IOException("no response from server");
        }

        return httpPostJsonResponse;
    }

    private String makeHttpGetWdsResponseCallWithFallbacks(String ticket)
	throws IOException, NetworkException
    {
    	String[] wdsUrls = getWdsUrlsFromSRD();
    	int socketRetryCount = 3;

    	String wdsResponse = null;
    	while (socketRetryCount > 0 && wdsResponse == null) {
        	for (int i=0; i<wdsUrls.length; i++) {
        		String getUrl = wdsUrls[i] + "/" + ticket + "/response.json";
    //_logger.debug("executing HTTP GET to wds url: " + getUrl);
        		try {
        			wdsResponse = HttpLibrary.httpGet(getUrl);

            		//if execution gets here it means there was not a network exception; break the loop
            		break;

            	} catch (NetworkException e) {
            		if (e.httpResponseCode >= 500 && e.httpResponseCode <= 599 && i < wdsUrls.length-1) {
            			//the server is down. there is another server to try. try it
            		} else {
            			//there is a different type of network error or all the servers have been tried. give up
            			throw e;
            		}
            	} catch (SocketException e) {
            	    if (socketRetryCount > 0) {
            	        socketRetryCount--;
            	        _logger.warn("got socket exception. # of retries left: " + socketRetryCount);
            	        try {
                            Thread.sleep(50L);
                        } catch (InterruptedException ignored) {
                        }
            	    } else {
            	        throw e;
            	    }
            	}
    		}
    	}

        //this shouldn't happen, but just in case
        if (wdsResponse == null) {
        	throw new IOException("no response from server");
        }

        return wdsResponse;
    }

    private String makeHttpMultipartPostMediaCallWithFallbacks(String path, String attachmentName, String attachmentFileName, byte[] data)
    throws IOException, NetworkException
    {
        String[] mediaUrls = getMediaUrlsFromSRD();

        String response = null;

        for (int i=0; i<mediaUrls.length; i++) {
            String mediaUrl = mediaUrls[i] + path;
//_logger.info("executing HTTP MULTIPART-POST to media url: " + mediaUrl);

            try {
                response = HttpLibrary.httpMultipartPost(mediaUrl, attachmentName, attachmentFileName, data);

                break;
            } catch (NetworkException e) {
                if (e.httpResponseCode >= 500 && e.httpResponseCode <= 599 && i < mediaUrls.length-1) {
                    //the server is down. there is another server to try. try it
                } else {
                    //there is a different type of network error or all the servers have been tried. give up
                    throw e;
                }
            }
        }

        return response;
    }

//    private String makeHttpGetToWds(String path)
//    throws IOException, NetworkException
//    {
//        String[] wdsUrls = getWdsUrlsFromSRD();
//
//        String wdsResponse = null;
//        for (int i=0; i<wdsUrls.length; i++) {
//            String getUrl = wdsUrls[i] + "/" + path;
//_logger.debug("executing HTTP GET to wds url: " + getUrl);
//            try {
//                wdsResponse = HttpLibrary.httpGet(getUrl);
//
//                //if execution gets here it means there was not a network exception; break the loop
//                break;
//
//            } catch (NetworkException e) {
//                if (e.httpResponseCode >= 500 && e.httpResponseCode <= 599 && i < wdsUrls.length-1) {
//                    //the server is down. there is another server to try. try it
//                } else {
//                    //there is a different type of network error or all the servers have been tried. give up
//                    throw e;
//                }
//            }
//        }
//
//        //this shouldn't happen, but just in case
//        if (wdsResponse == null) {
//            throw new IOException("no response from server");
//        }
//
//        return wdsResponse;
//    }

    /**
     * Decrypt the response from the server. Only call this method if the response is encrypted, otherwise the results will be undefined.
     *
     * @param encryptedResponse the base64 encoded encrypted response
     * @param encryptionKey the encryption key used by the server when encrypting the payload
     * @return the enencrypted payload
     * @throws IOException if something goes wrong
     */
    private String decryptResponse(String encryptedResponse, String encryptionKey)
    throws IOException
    {
        //if there was no response, do nothing
        if (encryptedResponse == null) {
            return encryptedResponse;
        }

        //means it's not really encrypted (most likely an error response json)
        if (encryptedResponse.startsWith("{")) {
        	return encryptedResponse;
        }

        byte[] decodedEncryptedBytes;
        try {
            decodedEncryptedBytes = Base64.getDecoder().decode(encryptedResponse.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException impossible) {
            //if utf-8 isn't supported, there are bigger problems to deal with
            throw new IllegalStateException(impossible);
        }

        return new String(EncryptUtils.aes256Decode(decodedEncryptedBytes, encryptionKey));
    }

    /**
     * After a call to the collector has been made, this method is called to asynchronously wait for the corresponding WebDataStore document to be published
     *
     * @param requestType the type of request originally made (such as ACCOUNT_LOGIN)
     * @param response the response from the collector. if the expected WDS document is encrypted, this value will also need to have the encryptKey value set
     * @param waitArrayIndexPos if the server's original estimatedWaitTime was not sufficient for the WDS document to be generated, this is the position
     *  in the backoff wait array of how long to wait on subsequent attempts
     * @param callback the object to notify when the call is complete
     */
    private void waitForAsyncResponse(REQUEST_TYPE requestType, CollectorResponse response, int waitArrayIndexPos, DataReceiver callback)
    {
        //see if it's time to "give up" waiting for a response that will never come
        if (waitArrayIndexPos >= WAIT_TIMES_MS.length) {
        	_logger.error("timeout waiting for response");
            callback.dataCallbackFailure(requestType, ERROR_TYPE.UNEXPECTED_ERROR, 200, null, null, null);
            return;
        }

        //sleep for the allotted amount of time (the first time is given by the server and is usually sufficient, but sometimes the server goes long and
        // the code must go into a backoff wait loop)
        try {
            Thread.sleep(waitArrayIndexPos == -1 ? response.estimatedWaitTime : WAIT_TIMES_MS[waitArrayIndexPos]);
        } catch (InterruptedException ignored) {
        }
        waitArrayIndexPos++;

        //[re]fetch the WDS response.json
        try {
        	String wdsResponse = makeHttpGetWdsResponseCallWithFallbacks(response.ticket);

            //see if the response needs to be decrypted. this is determined by the CollectorResponse having a value in the encryptKey field
            if (response.encryptKey != null) {
                wdsResponse = decryptResponse(wdsResponse, response.encryptKey);
            }

            //convert the response to a json object and check for the success value
            ObjectMapper mapper = JsonUtil.getObjectMapper();
            JsonNode json = mapper.readTree(wdsResponse);
            JsonNode successJsonNode = json.get("success");
            if (successJsonNode == null) {
                //for whatever reason there is no "success" node, so we'll assume success
                callback.dataCallbackSuccess(requestType, json);
                return;

            } else {
                boolean success = successJsonNode.asBoolean();
                if (success) {
                    //all is well
                    callback.dataCallbackSuccess(requestType, json);

                } else {
                	_logger.error("error response:\n" + wdsResponse);

                    //attempt to determine the specific error type and convert it to an enum for easy consumption
                    ERROR_TYPE errorType;
                    String errorMessage = json.has("message") ? json.get("message").asText() : null;

                    if (json.has("missingRequiredParam")) {
                        errorType = ERROR_TYPE.MISSING_REQUIRED_PARAM;

                    } else if (json.has("invalidParam")) {
                        errorType = ERROR_TYPE.INVALID_PARAM;

                    } else if (json.has("roundLocked")) {
                        errorType = ERROR_TYPE.ROUND_LOCKED;

                    } else if (json.has("duplicateAnswer")) {
                        errorType = ERROR_TYPE.DUPLICATE_ANSWER;

                    } else {
                        errorType = ERROR_TYPE.UNEXPECTED_ERROR;
                    }

                    callback.dataCallbackFailure(requestType, errorType, 200, errorMessage, null, null);
                }
            }

        } catch (IOException e) {
        	_logger.error("io exception with response parsing", e);
            callback.dataCallbackFailure(requestType, ERROR_TYPE.UNEXPECTED_ERROR, 200, e.getMessage(), null, null);

        } catch (NetworkException e) {
            //if the response was a 404, it most likely means the document isn't ready yet, so go into the wait loop and try again
            if (e.httpResponseCode == 404) {
            	//_logger.info(MessageFormat.format("received 404 for {0}. trying again...", requestType));
                waitForAsyncResponse(requestType, response, waitArrayIndexPos, callback);
            } else {
            	_logger.error("non 404 network exception with response parsing", e);
                callback.dataCallbackFailure(requestType, ERROR_TYPE.UNEXPECTED_ERROR, e.httpResponseCode, null, e.responseHeaders, e.responseBody);
            }
        }
    }

    /**
     * Using the SRD, grab the WDS URLs.
     *
     * As a performance enhancement, the massagedlist could be cached, but it must then be cleared out anytime the SRD is refreshed
     */
    private String[] getWdsUrlsFromSRD()
    {
        String[] wdsUrls = SRD.getInstance(_which, null).getWdsUrls();
        String[] massagedWdsUrls = new String[wdsUrls.length];

        for (int i=0; i<wdsUrls.length; i++) {
	        //work around srd issues and hardcoded ports
	        String prefix = "http://";
	        int idx = wdsUrls[i].indexOf(":");
	        if (idx != -1) {
	            String s = wdsUrls[i].substring(idx);
	            if (s.contains("44")) {
	                prefix = "https://";
	            }
	        }

	        massagedWdsUrls[i] = prefix + wdsUrls[i];
        }

        return massagedWdsUrls;
    }

    /**
     * Using the SRD, grab the Collector URLs.
     *
     * As a performance enhancement, the massagedlist could be cached, but it must then be cleared out anytime the SRD is refreshed
     */
    public String[] getCollectorUrlsFromSRD()
    {
        String[] collectorUrls = SRD.getInstance(_which, null).getCollectorUrls();
        String[] massagedCollectorUrls = new String[collectorUrls.length];

        //work around srd issues and hardcoded ports
        for (int i=0; i<collectorUrls.length; i++) {
	        String prefix = "http://";
	        int idx = collectorUrls[i].indexOf(":");
	        if (idx != -1) {
	            String s = collectorUrls[i].substring(idx);
	            if (s.contains("44")) {
	                prefix = "https://";
	            }
	        }

	        massagedCollectorUrls[i] = prefix + collectorUrls[i];
        }

        return massagedCollectorUrls;
    }

    private String[] getMediaUrlsFromSRD()
    {
        String[] mediaUrls = SRD.getInstance(_which, null).getMediaUrls();
        String[] massagedMeiaUrls = new String[mediaUrls.length];

        //work around srd issues and hardcoded ports
        for (int i=0; i<mediaUrls.length; i++) {
            String prefix = "http://";
            int idx = mediaUrls[i].indexOf(":");
            if (idx != -1) {
                String s = mediaUrls[i].substring(idx);
                if (s.contains("44")) {
                    prefix = "https://";
                }
            }

            massagedMeiaUrls[i] = prefix + mediaUrls[i];
        }

        return massagedMeiaUrls;
    }

    private String getToWdsParam()
    {
        //get the toWds value (without protocol or port)
        String toWds = SRD.getInstance(_which, null).getWdsUrls()[0];
        int portIdx = toWds.indexOf(":");
        if (portIdx != -1) {
            toWds = toWds.substring(0, portIdx);
        }

        return toWds;
    }

    private Map<String, String> getDefaultParams()
    {
        return new FastMap<>(
            "toWds", getToWdsParam(),
            "appId", "snowyowl"
        );
    }
}
