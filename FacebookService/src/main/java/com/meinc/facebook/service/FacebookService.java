package com.meinc.facebook.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.meinc.facebook.dao.IFacebookServiceDao;
import com.meinc.facebook.domain.FbCallback;
import com.meinc.facebook.domain.FbPost;
import com.meinc.facebook.domain.FbSubscriber;
import com.meinc.facebook.exception.FacebookAuthenticationNeededException;
import com.meinc.facebook.exception.FacebookGeneralException;
import com.meinc.facebook.exception.FacebookPostInvalidException;
import com.meinc.facebook.exception.FacebookUserExistsException;
import com.meinc.facebook.exception.InvalidAccessTokenException;
import com.meinc.facebook.exception.PostLimitExceededException;
import com.meinc.http.domain.HttpRequest;
import com.meinc.http.domain.HttpResponse;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.ServiceMessage;
import com.restfb.BinaryAttachment;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookException;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.json.JsonObject;
import com.restfb.types.FacebookType;
import com.restfb.types.Photo;
import com.restfb.types.User;

public class FacebookService implements IFacebookService
{
    protected static Logger _logger = Logger.getLogger(FacebookService.class);
    protected IFacebookServiceDao _dao;
    private String _appSecret;
    private String _facebookApiUrl;

    private final ObjectMapper _mapper = new ObjectMapper();

    public FacebookService()
    {
    }

    public void setDao(final IFacebookServiceDao dao) {
        _dao = dao;
    }

    public void setAppSecret(final String appSecret) {
        _appSecret = appSecret;
    }

    public void setFacebookApiUrl(final String url) {
        _facebookApiUrl = url;
    }

    @Override
    public void load() {
        _dao.start();
    }

    private void fireFacebookEvent(final String facebookId, final int eventId) {
        final Collection<FbCallback> callbacks = _dao.getCallbacks();
        if (callbacks != null) {
            for (final FbCallback callback : callbacks) {
                try {
                    ServiceMessage.send(callback.getEndpoint(), callback.getMethodName(), facebookId, eventId);
                } catch (final Throwable t) {
                    _logger.error("calling a facebook callback had an error", t);
                }
            }
        }
    }

    //    @Override
    //    public void load() {
    //        _logger.debug("loading facebook service...");
    //        
    //        _logger.debug("registering /fb endpoint...");
    //        //register to handle get/post events for the /fb endpoint
    //        ServiceEndpoint myEndpoint = new ServiceEndpoint();
    //        myEndpoint.setNamespace(MEINC_NAMESPACE);
    //        myEndpoint.setServiceName(SERVICE_NAME);
    //        myEndpoint.setVersion(SERVICE_VERSION);
    //
    //        EpsHttpConnectorServiceClientProxy httpConnector = new EpsHttpConnectorServiceClientProxy();
    //        boolean success = httpConnector.registerHttpCallback(myEndpoint, "doGet", "doPost", "/fb", "");
    //        if (success) {
    //            _logger.info("registered " + SERVICE_NAME + " to receive doGet/doPost for /fb endpoint");
    //        }
    //        else {
    //            _logger.error("unable to register " + SERVICE_NAME + " to receive doGet/doPost for /fb endpoint");
    //        }
    //        
    //        _logger.info("FacebookService loaded");
    //    }

    //    @Override
    //    @OnStop(depends=@OnService(proxy=EpsHttpConnectorServiceClientProxy.class))
    //    public void unload() {
    //        EpsHttpConnectorServiceClientProxy httpConnector = new EpsHttpConnectorServiceClientProxy();
    //        boolean success = httpConnector.unregisterHttpCallback("/fb");
    //        if (success) {
    //            _logger.debug("unregister for handling requests to '/fb'");
    //        }
    //    }

    protected Photo getPhoto(final String facebookId, final String id) {
        final String accessToken = _dao.getAccessTokenForFbId(facebookId);
        if (accessToken != null) {
            final FacebookClient client = new DefaultFacebookClient(accessToken);
            _logger.info("fetching photo with id " + id);
            final Photo photo = client.fetchObject(id, Photo.class);
            return photo;
        }
        else {
            _logger.info("no access token found for subscriber " + facebookId);
        }
        return null;
    }

    @Override
    public String addFacebookUser(final String accessToken, final boolean reassignIfExists)
            throws FacebookGeneralException, InvalidAccessTokenException, FacebookUserExistsException, FacebookAuthenticationNeededException
    {
        // using the access token, find out who this is
        _logger.debug("accessing facebook with accessToken: " + accessToken);
        User user = null;
        final FacebookClient client = new DefaultFacebookClient(accessToken);
        try {
            user = getMe(client, User.class);
        } catch (final FacebookOAuthException e) {
            throw new FacebookAuthenticationNeededException();
        } catch (final FacebookException e) {
            //_logger.warn("exception while trying to get information from facebook", e);
            throw new FacebookGeneralException("exception while trying to get information from facebook", e);
        }

        if (user == null || user.getId() == null || user.getId().length() == 0) {
            //_logger.warn("invalid Facebook accessToken, cannot continue");
            throw new InvalidAccessTokenException();
        }
        final String facebookId = user.getId();

        // now that we know who this is, find out if they are in the system by facebookId
        final String existingAccessToken = _dao.getAccessTokenForFbId(facebookId);

        if (existingAccessToken == null) {
            _logger.info("new facebok user, insert new access token '" + accessToken + "' for fbId " + facebookId);
            _dao.insertAccessToken(facebookId, accessToken);
            fireFacebookEvent(facebookId, FACEBOOK_ADDED);
            return facebookId;

        } else if (reassignIfExists) {
            _logger.info("reassigning existing facebok user, access token '" + accessToken + "' for fbId " + facebookId);
            _dao.removeAuthTokenForFacebookSubscriber(facebookId);
            _dao.insertAccessToken(facebookId, accessToken);
            fireFacebookEvent(facebookId, FACEBOOK_ADDED);
            return facebookId;

        } else {
            throw new FacebookUserExistsException();
        }
    }

    @Override
    public void addWithAccessTokenAndId(final String accessToken, final String fbId, final boolean updateAccessTokenIfExists) {
        // TODO Auto-generated method stub
        final String existingToken = _dao.getAccessTokenForFbId(fbId);
        if (existingToken == null) {
            _dao.insertAccessToken(fbId, accessToken);
            fireFacebookEvent(fbId, FACEBOOK_ADDED);
        }
        else if (updateAccessTokenIfExists) {
            _dao.removeAuthTokenForFacebookSubscriber(fbId);
            _dao.insertAccessToken(fbId, accessToken);
        }
    }

    private <T> T getMe(final FacebookClient client, final Class<T> clazz) throws FacebookOAuthException {
        try {
            return client.fetchObject("me", clazz, Parameter.with("fields", "id,name,first_name,last_name,gender,locale,timezone,link,verified,email,picture"));
        } catch (final FacebookOAuthException e) {
            throw e;
        }
    }

    protected FbSubscriber getSubscriberInfoInternal(final String facebookId) throws FacebookException, FacebookAuthenticationNeededException {
        final String accessToken = _dao.getAccessTokenForFbId(facebookId);
        if (accessToken == null) {
            return null;
        }

        final FbSubscriber fbSub;
        final FacebookClient client = new DefaultFacebookClient(accessToken);
        try {
            fbSub = getMe(client, FbSubscriber.class);
        } catch (final FacebookOAuthException e) {
            _logger.error("error getting facebook info for subscriber " + facebookId, e);
            throw new FacebookAuthenticationNeededException(facebookId);
        } catch (final FacebookException e) {
            _logger.error("error getting facebook info for subscriber " + facebookId, e);
            throw e;
        }
        return fbSub;
    }

    //    @Override
    //    //    private FbSubscriber getSubscriberInfo(String facebookId) throws FacebookAuthenticationNeededException {
    //        try {
    //            return getSubscriberInfoInternal(facebookId);
    //        }
    //        catch (FacebookException e) {
    //            
    //        }
    //        return null;
    //    }

    @Override
    public FbSubscriber getSubscriberInfoByAccessToken(final String accessToken)
            throws InvalidAccessTokenException, FacebookGeneralException, FacebookAuthenticationNeededException
    {
        final FbSubscriber result;
        // using the access token, find out who this is 
        final FacebookClient client = new DefaultFacebookClient(accessToken);
        try {
            result = getMe(client, FbSubscriber.class);
        } catch (final FacebookOAuthException e) {
            throw new FacebookAuthenticationNeededException();
        } catch (final FacebookException e) {
            //_logger.warn("exception while trying to get information from facebook for accessToken " + accessToken + ": ", e);
            throw new FacebookGeneralException("exception while trying to get information from facebook for accessToken " + accessToken + ": ", e);
        }

        if (result == null || result.getFbId() == null || result.getFbId().trim().length() == 0) {
            // invalid access token
            //_logger.warn("invalid access token, could not get subscriber information");
            throw new InvalidAccessTokenException();
        }
        return result;
    }

    @Override
    public FbSubscriber getFacebookUser(final String fbId) throws FacebookAuthenticationNeededException
    {
        final String accessToken = _dao.getAccessTokenForFbId(fbId);
        if (accessToken == null) {
            _logger.info("no access token for subscriber to get facebook user, returning");
            return null;
        }
        FbSubscriber result = null;
        final FacebookClient client = new DefaultFacebookClient(accessToken);
        try {
            result = getMe(client, FbSubscriber.class);
        } catch (final FacebookOAuthException e) {
            throw new FacebookAuthenticationNeededException(fbId);
        } catch (final FacebookException e) {
            _logger.warn("invalid access token, could not get subscriber information");
        }
        return result;
    }

    @Override
    public void removeFacebookUser(final String fbId)
    {
        _dao.removeAuthTokenForFacebookSubscriber(fbId);
    }

    @Override
    public String getAccessToken(final String fbId)
    {
        return _dao.getAccessTokenForFbId(fbId);
    }

    @Override
    public List<FbSubscriber> getFamily(final String fbId) throws FacebookAuthenticationNeededException {
        final String accessToken = _dao.getAccessTokenForFbId(fbId);
        if (accessToken == null) {
            _logger.info("no access token for subscriber to get family connections, returning");
            return null;
        }

        final FacebookClient client = new DefaultFacebookClient(accessToken);
        try {
            final Connection<FbSubscriber> family = client.fetchConnection("me/family", FbSubscriber.class, Parameter.with("fields", "id,name,picture"));
            // Force a serializable list
            return new ArrayList<FbSubscriber>(family.getData());
        } catch (final FacebookOAuthException e) {
            throw new FacebookAuthenticationNeededException();
        } catch (final FacebookException e) {
            _logger.error("error getting family connections", e);
        }
        return null;
    }

    @Override
    public List<FbSubscriber> getFriends(final String fbId) throws FacebookAuthenticationNeededException {
        final String accessToken = _dao.getAccessTokenForFbId(fbId);
        if (accessToken == null) {
            _logger.info("no access token for subscriber to get friend connections, returning");
            return null;
        }
        final FacebookClient client = new DefaultFacebookClient(accessToken);
        try {
            final Connection<FbSubscriber> friends = client.fetchConnection("me/friends", FbSubscriber.class, Parameter.with("fields", "id,name,picture"));
            return new ArrayList<FbSubscriber>(friends.getData());
        } catch (final FacebookOAuthException e) {
            throw new FacebookAuthenticationNeededException();
        } catch (final FacebookException e) {
            _logger.error("error getting friends connections for subscriber", e);
        }
        return null;
    }

    private String publishToFriendFeed(final FacebookClient client, final FbSubscriber friend, final FbPost post) throws FacebookAuthenticationNeededException, FacebookPostInvalidException {
        try {
            final Parameter params[] = postToParameters(post);
            FacebookType postId = null;
            final Map<String, File> attachments = post.getAttachments();
            if (attachments != null) {
                BinaryAttachment attachment = null;
                for (final String key : post.getAttachments().keySet()) {
                    attachment = BinaryAttachment.with(key, new FileInputStream(attachments.get(key)));
                    break;
                }
                postId = client.publish(friend.getFbId() + "/photos", FacebookType.class, attachment, params);
            }
            else {
                postId = client.publish(friend.getFbId() + "/feed", FacebookType.class, params);
            }
            _logger.debug("posted new message with id " + postId.getId() + " to feed of " + friend.getName() + "(" + friend.getFbId() + ")");
            return postId.getId();
        } catch (final FacebookOAuthException e) {
            throw new FacebookAuthenticationNeededException();
        } catch (final FacebookException e) {
            _logger.error("error publishing to feed of facebook user " + friend.getName() + "(" + friend.getFbId() + "): ", e);
        } catch (final FileNotFoundException e) {
            _logger.error("error publishing to feed, file not found", e);
            throw new FacebookPostInvalidException("error publishing to facebook feed: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Map<String, String> publishToFeed(final String fbId, final List<FbSubscriber> friends, final FbPost post)
            throws FacebookAuthenticationNeededException, PostLimitExceededException, FacebookPostInvalidException {
        final int postLimit = getPostLimit();
        if (postLimit > 0 && friends.size() > postLimit) {
            throw new PostLimitExceededException(postLimit, friends.size());
        }
        else {
            _logger.info("post_limit is " + postLimit + " and friends.size() is " + friends.size());
        }

        final String accessToken = _dao.getAccessTokenForFbId(fbId);
        final Map<String, String> retVal = new HashMap<String, String>();
        if (accessToken != null) {
            final FacebookClient client = new DefaultFacebookClient(accessToken);
            try {
                final FbSubscriber me = getMe(client, FbSubscriber.class);
                // Grant: as of Feb 5, 2013, we ONLY publish to the feed of the subscriber who makes this call
                // We no longer have permission to post to the feed of another user.
                final String postId = publishToFriendFeed(client, me, post);
                if (postId != null) {
                    retVal.put(me.getFbId(), postId);
                }
            } catch (final FacebookOAuthException e) {
                throw new FacebookAuthenticationNeededException(fbId);
            }
        }
        else {
            _logger.warn("no access token found, will not publish");
            throw new FacebookAuthenticationNeededException();
        }
        return retVal;
    }

    @Override
    public Map<String, String> publishToFeedWithMessages(final String fbId,
            final List<FbSubscriber> friends, final FbPost post, final List<String> messages)
            throws FacebookAuthenticationNeededException,
            PostLimitExceededException, FacebookPostInvalidException
    {
        final int postLimit = getPostLimit();
        if (postLimit > 0 && friends.size() > postLimit) {
            throw new PostLimitExceededException(postLimit, friends.size());
        }
        else {
            _logger.info("post_limit is " + postLimit + " and friends.size() is " + friends.size());
        }

        final String accessToken = _dao.getAccessTokenForFbId(fbId);

        final int curIndex = (int) (Math.random() * messages.size());
        _logger.info("picked index of " + curIndex + " from range of 0 - " + messages.size());
        final Map<String, String> retVal = new HashMap<String, String>();
        if (accessToken != null) {
            final FacebookClient client = new DefaultFacebookClient(accessToken);
            post.setDescription(messages.get(curIndex));
            try {
                final FbSubscriber me = getMe(client, FbSubscriber.class);
                // Grant: as of Feb 5, 2013, we ONLY publish to the feed of the subscriber who makes this call
                // We no longer have permission to post to the feed of another user.
                final String postId = publishToFriendFeed(client, me, post);
                if (postId != null) {
                    retVal.put(me.getFbId(), postId);
                }
            } catch (final FacebookOAuthException e) {
                throw new FacebookAuthenticationNeededException(fbId);
            }
        }
        return retVal;
    }

    //    @Override
    //    //    public FbSubscriber getSubscriberInfoByFbId(String fbId) throws FacebookAuthenticationNeededException
    //    {
    //        int subscriberId = _dao.getSubscriberIdForFacebookId(fbId);
    //        return subscriberId == 0 ? null : getSubscriberInfo(subscriberId);
    //    }
    //    
    //    @Override
    //    //    public Map<String, String> getIntegrationInfo(int subscriberId) throws FacebookAuthenticationNeededException {
    //        String accessToken = _dao.getAccessTokenForSubscriber(subscriberId);
    //        if (accessToken != null) {
    //            FbSubscriber fbSub = getSubscriberInfo(subscriberId);
    //            if (fbSub != null) {
    //                Map<String,String> ret = new HashMap<String, String>();
    //                ret.put("accessToken", accessToken);
    //                ret.put("facebookId", "" + fbSub.getFbId());
    //                return ret;
    //            }
    //        }
    //        return null;
    //    }
    //
    //
    private String padBase64(String base64Str) {
        if (base64Str.length() % 4 != 0) {
            _logger.info("encoded json is not valid base-64");
            base64Str = base64Str.replace('-', '+');
            base64Str = base64Str.replace('_', '/');
            final StringBuilder buf = new StringBuilder(base64Str);
            final int numNeeded = 4 - (base64Str.length() % 4);
            for (int i = 0; i < numNeeded; i++) {
                buf.append("=");
            }
            base64Str = buf.toString();
        }
        return base64Str;
    }

    @Override
    public String handleDeauthorize(final HttpRequest request) {
        String facebookId = null;
        final String signed_request = request.getFirstParameter("signed_request");
        if (signed_request == null || signed_request.length() == 0) {
            _logger.warn("no signed_request parameter");
            return null;
        }
        else {
            _logger.info("signed request: " + signed_request);
            final String[] vals = signed_request.split("\\.");
            if (vals.length != 2) {
                _logger.warn("bad signed request");
                return null;
            }
            _logger.info("sig: " + vals[0] + ", json: " + vals[1]);
            // both sig and json are Base64 encoded
            final byte[] sig = Base64.decode(padBase64(vals[0]));
            if (sig == null) {
                _logger.info("decoded sig is null");
            }
            else {
                _logger.info("sig length = " + sig.length);
            }
            String json = "";
            try {
                final String encodedJson = padBase64(vals[1]);
                final byte[] decoded = Base64.decode(encodedJson);
                if (decoded == null) {
                    _logger.info("decoded byte array is null");
                }
                json = new String(decoded, "UTF-8");
            } catch (final UnsupportedEncodingException e) {
                _logger.warn("could not create string from base-64 encoded json data: ", e);
            }
            _logger.info("json data: " + json);
            Mac hmacSha256;
            try {
                hmacSha256 = Mac.getInstance("hmacSHA256");
                final SecretKeySpec spec = new SecretKeySpec(_appSecret.getBytes(), "HmacSHA256");
                hmacSha256.init(spec);
                final byte[] expectedSig = hmacSha256.doFinal(vals[1].getBytes());
                _logger.info("expected sig length = " + expectedSig.length);
                if (Arrays.equals(sig, expectedSig)) {
                    _logger.info("signatures match");
                    final Map<String, Object> map = _mapper.readValue(json, new TypeReference<Map<String, Object>>() {
                    });
                    facebookId = (String) map.get("user_id");
                    if (facebookId != null && facebookId.trim().length() > 0) {
                        _logger.info("removing auth_token for facebookId " + facebookId);
                        removeFacebookUser(facebookId);
                    }
                }
                else {
                    _logger.warn("signatures DO NOT MATCH!");
                }
            } catch (final NoSuchAlgorithmException e) {
                _logger.warn("could not find algorithm for hmacSHA256");
            } catch (final InvalidKeyException e) {
                _logger.warn("invalid key", e);
            } catch (final JsonParseException e) {
                e.printStackTrace();
            } catch (final JsonMappingException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return facebookId;
    }

    @Override
    public HttpResponse handleFbPost(final HttpRequest request, final String facebookId)
            throws FacebookGeneralException, FacebookAuthenticationNeededException, PostLimitExceededException, FacebookPostInvalidException, JsonGenerationException, JsonMappingException, IOException
    {
        final HttpResponse response = new HttpResponse();
        response.setContentType("application/json; charset=utf-8");
        final FbPost post = new FbPost();
        post.setName(request.getFirstParameter("name"));
        post.setCaption(request.getFirstParameter("caption"));
        post.setDescription(request.getFirstParameter("description"));
        post.setPicture(request.getFirstParameter("iconUrl"));
        post.setLink(request.getFirstParameter("link"));
        post.setMessage(request.getFirstParameter("message"));
        final String to = request.getFirstParameter("to");
        FbSubscriber sub = null;
        boolean subscriberIsRecipient = false;
        if (to != null) {
            sub = new FbSubscriber();
            sub.setFbId(to);
            //            int thisSubscriberId = _dao.getSubscriberIdForFacebookId(to);
            //            if (thisSubscriberId != 0 && thisSubscriberId == subscriberId) {
            //                subscriberIsRecipient = true;
            //            }
            if (to.equals(facebookId)) {
                subscriberIsRecipient = true;
            }
        }
        else {
            sub = getSubscriberInfoInternal(facebookId);
            subscriberIsRecipient = true;
        }
        boolean photoAttached = false;
        //TODO: if this is needed in the future, restore file upload functionality to HttpUtils
        /*final List<HttpFileUpload> uploads = request.getFileUploads();
        if (uploads != null) {
            if (subscriberIsRecipient) {
                _logger.info("we have a file upload, matey");
                for (final HttpFileUpload upload : uploads) {
                    _logger.info("file uploaded with name " + upload.originalFileName + " and saved file " + upload.savedFile.getAbsolutePath());
                    post.addAttachment(upload.originalFileName, upload.savedFile);
                    photoAttached = true;
                }
            }
            else {
                _logger.info("photos and videos may only be posted to the subscribers own feed");
            }
        }*/
        final List<FbSubscriber> friends = new ArrayList<FbSubscriber>();
        friends.add(sub);
        final Map<String, String> postIds = publishToFeed(facebookId, friends, post);
        final Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("success", true);
        if (postIds != null && postIds.size() > 1) {
            ret.put("postIds", postIds);
        }
        else {
            for (final String val : postIds.values()) {
                ret.put("postId", val);
                if (photoAttached) {
                    final Photo photo = getPhoto(facebookId, val);
                    if (photo != null) {
                        ret.put("photoUrl", photo.getSource());
                    }
                    else {
                        _logger.info("photo retrieved from facebook is null");
                    }
                }
            }
        }
        response.getWriter().println(_mapper.writeValueAsString(ret));
        return response;
    }

    @Override
    public HttpResponse handleGetPermissions(final HttpRequest request, final String facebookId) throws FacebookAuthenticationNeededException
    {
        final HttpResponse response = new HttpResponse();
        try {
            response.setContentType("application/json; charset=utf-8");
            final String accessToken = _dao.getAccessTokenForFbId(facebookId);
            final StringBuilder responseStr = new StringBuilder();
            responseStr.append("{\"success\":true, \"permissions\":");
            final FacebookClient client = new DefaultFacebookClient(accessToken);
            final JsonObject results = client.fetchObject("me/permissions", JsonObject.class);
            responseStr.append(results.getJsonArray("data").getString(0));
            responseStr.append("}");
            response.getWriter().println(responseStr);
        } catch (final FacebookOAuthException e) {
            throw new FacebookAuthenticationNeededException(facebookId);
        }
        return response;
    }

    @Override
    public HttpResponse handleFbGet(final HttpRequest request, final String facebookId)
            throws JsonGenerationException, JsonMappingException, IOException
    {
        final HttpResponse response = new HttpResponse();
        response.setContentType("application/json; charset=utf-8");
        final String id = request.getFirstParameter("id");
        final String accessToken = _dao.getAccessTokenForFbId(facebookId);
        final Map<String, Object> vals = new HashMap<String, Object>();
        if (id != null && accessToken != null) {
            final String url = _facebookApiUrl + "/" + id + "?access_token=" + accessToken;
            _logger.info("making request to URL " + url + " using access token for subscriber " + facebookId);
            final FBResponse fb = getUrlContent(url);
            _logger.info("response code: " + fb.getCode() + ", body: " + fb.getBody());
            vals.put("success", true);
        }
        else {
            vals.put("success", false);
        }
        response.getWriter().println(_mapper.writeValueAsString(vals));
        return response;
    }

    private static FBResponse getUrlContent(final String urlStr) {
        try {
            final URL url = new URL(urlStr);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            BufferedInputStream is = null;
            if (connection.getResponseCode() >= 400) {
                is = new BufferedInputStream(connection.getErrorStream());
            }
            else {
                is = new BufferedInputStream(connection.getInputStream());
            }
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            int bytesRead = 0;
            final byte[] bytes = new byte[1024];
            while (bytesRead != -1) {
                bytesRead = is.read(bytes);
                if (bytesRead > 0) {
                    _logger.info("writing " + bytesRead + " bytes to outputstream");
                    out.write(bytes, 0, bytesRead);
                }
            }
            final FBResponse response = new FBResponse();
            response.setCode(connection.getResponseCode());
            response.setBody(out.toString("UTF-8"));
            return response;
        } catch (final MalformedURLException e) {
            _logger.error("malformed url '" + urlStr + "': ", e);
        } catch (final IOException e) {
            _logger.error("error reading url contents from '" + urlStr + "': ", e);
        }
        return null;
    }

    @Override
    public boolean registerCallback(final ServiceEndpoint endpoint, final String methodName) {
        final FbCallback callback = new FbCallback();
        callback.setEndpoint(endpoint);
        callback.setMethodName(methodName);
        if (_dao.getCallbackForEndpoint(callback) == null) {
            _dao.addCallback(callback);
            return true;
        }
        return false;
    }

    @Override
    public boolean unregisterCallback(final ServiceEndpoint endpoint) {
        final FbCallback callback = new FbCallback();
        callback.setEndpoint(endpoint);
        final int rowsDeleted = _dao.removeCallback(callback);
        if (rowsDeleted > 0) {
            return true;
        }
        return false;
    }

    private Parameter[] postToParameters(final FbPost post) {
        final ArrayList<Parameter> params = new ArrayList<Parameter>();
        if (post.getMessage() != null) {
            params.add(Parameter.with("message", post.getMessage()));
        }
        if (post.getName() != null) {
            params.add(Parameter.with("name", post.getName()));
        }
        if (post.getLink() != null) {
            params.add(Parameter.with("link", post.getLink()));
        }
        if (post.getCaption() != null) {
            params.add(Parameter.with("caption", post.getCaption()));
        }
        if (post.getDescription() != null) {
            params.add(Parameter.with("description", post.getDescription()));
        }
        if (post.getActionName() != null && post.getActionLink() != null) {
            final List<Map<String, String>> actions = new ArrayList<Map<String, String>>();
            final Map<String, String> action = new HashMap<String, String>();
            action.put("name", post.getActionName());
            action.put("link", post.getActionLink());
            actions.add(action);
            params.add(Parameter.with("actions", actions));
        }
        if (post.getPicture() != null) {
            params.add(Parameter.with("picture", post.getPicture()));
        }
        if (params.size() > 0) {
            return params.toArray(new Parameter[params.size()]);
        }
        return null;
    }

    @Override
    public int getPostLimit() {
        final Properties props = ServerPropertyHolder.getProps();
        try {
            final Integer limit = new Integer(props.getProperty("facebook.post_limit", "0"));
            _logger.info("Facebook post limit is " + limit);
            return limit;
        } catch (final NumberFormatException e) {
            _logger.warn("could not turn post limit into integer, returning 0");
            return 0;
        }
    }

    //  private String getBaseUrl(HttpRequest request) {
    //  Matcher matcher = _baseUrlPattern.matcher(request.getRequestURL());
    //  String baseUrl = "";
    //  if (matcher.matches()) {
    //      baseUrl = matcher.group(1);
    //  }
    //  return baseUrl;
    //}

    //private int getSubscriberId(HttpRequest request) {
    //  String[] elements = request.getRequestURL().split("/");
    //  String subscriberId = elements[elements.length -1];
    //  return Integer.parseInt(subscriberId);
    //}

    //private static Map<String,String> parseQuery(String query) {
    //  Map<String,String> ret = new HashMap<String, String>();
    //  String[] values = query.split("&");
    //  for (int i = 0; i < values.length; i++) {
    //      String[] keyval = values[i].split("=");
    //      if (keyval.length < 2) {
    //          _logger.warn("bad query keyval: " + values[i]);
    //      }
    //      else {
    //          _logger.info("putting value of '" + keyval[1] + "' for key '" + keyval[0] + "'");
    //          ret.put(keyval[0], keyval[1]);
    //      }
    //  }
    //  return ret;
    //}

    //private HttpResponse handleSignupStart(HttpRequest request) {
    //  HttpResponse response = new HttpResponse(request);
    //  String baseUrl = getBaseUrl(request);
    //  String subscriberId = request.getParameter("subscriberId");
    //  if (subscriberId == null || subscriberId.length() == 0) {
    //      _logger.warn("no subscriber id, returning response code 400");
    //      response.setError(400);
    //      return response;
    //  }
    //  String redirectUrl = baseUrl + SIGNUP_FINISHED_PATH + "/" + subscriberId;
    //  _logger.info("redirect signup request to " + redirectUrl);
    //  response.setRedirect("http://www.facebook.com/dialog/oauth?client_id=" + _applicationId + "&scope=email,read_stream,user_photos,user_relationships,publish_stream,offline_access&redirect_uri=" + redirectUrl);
    //  return response;
    //}

    //private HttpResponse handleSignupFinished(HttpRequest request) {
    //  HttpResponse response = new HttpResponse(request);
    //  String code = request.getParameter("code");
    //  if (code == null || code.length() == 0) {
    //      // the user did not authorize our app,
    //  }
    //  else {
    //      // grab subscriberId from the path
    //      int subscriberId = getSubscriberId(request);
    //      FBResponse authResponse = null;
    //      String url = "";
    //      // the user did authorize our app, get an access token from Facebook
    //      String redirectUrl = getBaseUrl(request) + SIGNUP_FINISHED_PATH + "/" + subscriberId;
    //      _logger.info("generated redirect url: " + redirectUrl + ", request url: " + request.getRequestURL());
    //      url = _facebookApiUrl + "/oauth/access_token?client_id=" + _applicationId + "&client_secret=" + _appSecret + "&code=" + code + "&redirect_uri=" + redirectUrl;
    //      authResponse = getUrlContent(url);
    //      if (authResponse != null) {
    //          if (authResponse.getBody().startsWith("{")) {
    //              _logger.error("error response received from Facebook: " + authResponse.getBody());
    //              //_TODO: handle JSON response - most likely an error like this:
    //              /*
    //               * {"error":{"type":"OAuthException","message":"Error validating verification code."}}
    //               */
    //          }
    //          else {
    //              Map<String,String> vals = parseQuery(authResponse.getBody());
    //              String accessToken = vals.get("access_token");
    //              if (accessToken == null) {
    //                  _logger.error("no access_token parameter in response from Facebook");
    //                  return response;
    //              }
    //              try {
    //                  setAccessTokenForSubscriber(accessToken, subscriberId);
    //              } catch (FacebookSubscriberExistsException e) {
    //                  _logger.error("could not associate access token '" + accessToken + "' with subscriber " + subscriberId, e);
    //              }
    //          }
    //      }
    //  }
    //  return response;
    //}

}
