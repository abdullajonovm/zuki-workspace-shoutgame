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

import com.meinc.commons.application.HttpFileUpload;
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
import com.meinc.mrsoa.service.annotation.OnStart;
import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;
import com.restfb.FacebookClient;
import com.restfb.exception.FacebookException;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.Photo;

//@XService(
//        namespace = FacebookLoadtestService.MEINC_NAMESPACE,
//        name = FacebookLoadtestService.SERVICE_NAME,
//        interfaces = FacebookLoadtestService.FACEBOOK_INTERFACE,
//        version = FacebookLoadtestService.SERVICE_VERSION,
//        exposeAs = IFacebookService.class)
public class FacebookLoadtestService extends FacebookService {

    private final ObjectMapper _mapper = new ObjectMapper();

    private final boolean bypassFacebookServers = Boolean.valueOf(ServerPropertyHolder.getProperty("bypass.facebook.servers.enabled", "false").trim().toLowerCase());
    private final String bypassFacebookServersAccessTokenPrefix = ServerPropertyHolder.getProperty("bypass.facebook.servers.access.token.prefix", "19992").trim();

    public FacebookLoadtestService() {
    }

    @Override
    public void load() {
        super.load();
    }

    @Override
    protected Photo getPhoto(final String facebookId, final String id) {
        if ((bypassFacebookServers) && (facebookId.startsWith(bypassFacebookServersAccessTokenPrefix))) {
            return new Photo();
        }
        return super.getPhoto(facebookId, id);
    }

    @Override
    public String addFacebookUser(final String accessToken, final boolean reassignIfExists)
            throws FacebookGeneralException, InvalidAccessTokenException, FacebookUserExistsException, FacebookAuthenticationNeededException {

        if ((bypassFacebookServers) && (accessToken.startsWith(bypassFacebookServersAccessTokenPrefix))) {
            return accessToken;
        }
        return super.addFacebookUser(accessToken, reassignIfExists);
    }

    @Override
    public void addWithAccessTokenAndId(final String accessToken, final String fbId, final boolean updateAccessTokenIfExists) {

        if ((bypassFacebookServers) && (accessToken.startsWith(bypassFacebookServersAccessTokenPrefix))) {
            final String existingToken = _dao.getAccessTokenForFbId(fbId);
            if (existingToken == null) {
                _dao.insertAccessToken(fbId, accessToken);
            } else if (updateAccessTokenIfExists) {
                _dao.removeAuthTokenForFacebookSubscriber(fbId);
                _dao.insertAccessToken(fbId, accessToken);
            }
        } else {
            super.addWithAccessTokenAndId(accessToken, fbId, updateAccessTokenIfExists);
        }
    }

    @Override
    protected FbSubscriber getSubscriberInfoInternal(final String facebookId) throws FacebookException, FacebookAuthenticationNeededException {
        final String accessToken = _dao.getAccessTokenForFbId(facebookId);
        if (accessToken == null) {
            return null;
        }

        if ((bypassFacebookServers) && (accessToken.startsWith(bypassFacebookServersAccessTokenPrefix))) {
            return createLoadtestFbSubscriber(accessToken);
        }
        return super.getSubscriberInfoInternal(facebookId);
    }

    @Override
    public FbSubscriber getSubscriberInfoByAccessToken(final String accessToken)
            throws InvalidAccessTokenException, FacebookGeneralException, FacebookAuthenticationNeededException
    {
        final FbSubscriber result = null;
        if ((bypassFacebookServers) && (accessToken.startsWith(bypassFacebookServersAccessTokenPrefix))) {
            return createLoadtestFbSubscriber(accessToken);
        }
        return super.getSubscriberInfoByAccessToken(accessToken);
    }

    /**
     * Creates a facebook subscriber with the facebook id set to the accessToken
     * which is the phone number of the loadtest workerbee. The email is created
     * the same way as loadtest AppUser constructor does it.
     * 
     * @param accessToken
     * @return FbSubscriber
     */
    private FbSubscriber createLoadtestFbSubscriber(final String accessToken) {
        final FbSubscriber fbSub = new FbSubscriber();
        fbSub.setFbId(accessToken);
        fbSub.setEmail("email" + accessToken + "@shoutgp.com");
        return fbSub;
    }

    @Override
    public FbSubscriber getFacebookUser(final String fbId) throws FacebookAuthenticationNeededException {
        if ((bypassFacebookServers) && (fbId.startsWith(bypassFacebookServersAccessTokenPrefix))) {
            return createLoadtestFbSubscriber(fbId);
        }
        return super.getFacebookUser(fbId);
    }

    @Override
    public void removeFacebookUser(final String fbId)
    {
        _dao.removeAuthTokenForFacebookSubscriber(fbId);
    }

    @Override
    public String getAccessToken(final String fbId) {
        if ((bypassFacebookServers) && (fbId.startsWith(bypassFacebookServersAccessTokenPrefix))) {
            return fbId;
        }
        return super.getAccessToken(fbId);
    }

    @Override
    public List<FbSubscriber> getFamily(final String fbId) throws FacebookAuthenticationNeededException {
        if ((bypassFacebookServers) && (fbId.startsWith(bypassFacebookServersAccessTokenPrefix))) {
            return null;
        }
        return super.getFamily(fbId);
    }

    @Override
    public List<FbSubscriber> getFriends(final String fbId) throws FacebookAuthenticationNeededException {
        if ((bypassFacebookServers) && (fbId.startsWith(bypassFacebookServersAccessTokenPrefix))) {
            return null;
        }
        return super.getFriends(fbId);
    }

    @Override
    public Map<String, String> publishToFeed(final String fbId, final List<FbSubscriber> friends, final FbPost post)
            throws FacebookAuthenticationNeededException, PostLimitExceededException, FacebookPostInvalidException {
        if ((bypassFacebookServers) && (fbId.startsWith(bypassFacebookServersAccessTokenPrefix))) {
            return null;
        }
        return super.publishToFeed(fbId, friends, post);
    }

    @Override
    public Map<String, String> publishToFeedWithMessages(final String fbId,
            final List<FbSubscriber> friends, final FbPost post, final List<String> messages)
            throws FacebookAuthenticationNeededException,
            PostLimitExceededException, FacebookPostInvalidException {

        if ((bypassFacebookServers) && (fbId.startsWith(bypassFacebookServersAccessTokenPrefix))) {
            return null;
        }
        return super.publishToFeedWithMessages(fbId, friends, post, messages);
    }

    @Override
    public String handleDeauthorize(final HttpRequest request) {
        return super.handleDeauthorize(request);
    }

    @Override
    public HttpResponse handleFbPost(final HttpRequest request, final String facebookId)
            throws FacebookGeneralException, FacebookAuthenticationNeededException, PostLimitExceededException,
            FacebookPostInvalidException, JsonGenerationException, JsonMappingException, IOException
    {
        return super.handleFbPost(request, facebookId);
    }

    @Override
    public HttpResponse handleGetPermissions(final HttpRequest request, final String facebookId) throws FacebookAuthenticationNeededException
    {
        if ((bypassFacebookServers) && (facebookId.startsWith(bypassFacebookServersAccessTokenPrefix))) {
            final HttpResponse response = new HttpResponse();
            response.setContentType("application/json; charset=utf-8");
            final StringBuilder responseStr = new StringBuilder();
            responseStr.append("{\"success\":true, \"permissions\":");
            responseStr.append("[]"); // an empty json array
            responseStr.append("}");
            response.getWriter().println(responseStr);
            return response;
        }
        return super.handleGetPermissions(request, facebookId);
    }

    @Override
    public HttpResponse handleFbGet(final HttpRequest request, final String facebookId)
            throws JsonGenerationException, JsonMappingException, IOException
    {
        return super.handleFbGet(request, facebookId);
    }

    @Override
    public boolean registerCallback(final ServiceEndpoint endpoint, final String methodName) {
        return super.registerCallback(endpoint, methodName);
    }

    @Override
    public boolean unregisterCallback(final ServiceEndpoint endpoint) {
        return super.unregisterCallback(endpoint);
    }

    @Override
    public int getPostLimit() {
        return super.getPostLimit();
    }

}
