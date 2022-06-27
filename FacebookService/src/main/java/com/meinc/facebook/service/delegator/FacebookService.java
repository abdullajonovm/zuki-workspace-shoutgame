package com.meinc.facebook.service.delegator;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.meinc.facebook.domain.FbPost;
import com.meinc.facebook.domain.FbSubscriber;
import com.meinc.facebook.exception.FacebookAuthenticationNeededException;
import com.meinc.facebook.exception.FacebookGeneralException;
import com.meinc.facebook.exception.FacebookPostInvalidException;
import com.meinc.facebook.exception.FacebookUserExistsException;
import com.meinc.facebook.exception.InvalidAccessTokenException;
import com.meinc.facebook.exception.PostLimitExceededException;
import com.meinc.facebook.service.IFacebookService;
import com.meinc.http.domain.HttpRequest;
import com.meinc.http.domain.HttpResponse;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.annotation.OnStart;
import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;

@Service(
        namespace = FacebookService.MEINC_NAMESPACE,
        name = FacebookService.SERVICE_NAME,
        interfaces = FacebookService.FACEBOOK_INTERFACE,
        version = FacebookService.SERVICE_VERSION,
        exposeAs = IFacebookService.class)
public class FacebookService implements IFacebookService
{
    public static final String MEINC_NAMESPACE = "meinc-service";
    public static final String SERVICE_NAME = "FacebookService";
    public static final String FACEBOOK_INTERFACE = "IFacebookService";
    public static final String SERVICE_VERSION = "2.0";

    protected static Logger _logger = Logger.getLogger(FacebookService.class);

    private IFacebookService facebookServiceDelegate;

    @Override
    @OnStart
    @ServiceMethod
    public void load() {
        facebookServiceDelegate.load();
    }

    @Override
    @ServiceMethod
    public String addFacebookUser(String accessToken, boolean reassignIfExists) throws FacebookGeneralException, InvalidAccessTokenException, FacebookUserExistsException, FacebookAuthenticationNeededException {
        return facebookServiceDelegate.addFacebookUser(accessToken, reassignIfExists);
    }

    @Override
    @ServiceMethod
    public void addWithAccessTokenAndId(String accessToken, String fbId, boolean updateAccessTokenIfExists) {
        facebookServiceDelegate.addWithAccessTokenAndId(accessToken, fbId, updateAccessTokenIfExists);
    }

    @Override
    @ServiceMethod
    public FbSubscriber getSubscriberInfoByAccessToken(String accessToken) throws FacebookGeneralException, InvalidAccessTokenException, FacebookAuthenticationNeededException {
        return facebookServiceDelegate.getSubscriberInfoByAccessToken(accessToken);
    }

    @Override
    @ServiceMethod
    public FbSubscriber getFacebookUser(String fbId) throws FacebookAuthenticationNeededException {
        return facebookServiceDelegate.getFacebookUser(fbId);
    }

    @Override
    @ServiceMethod
    public void removeFacebookUser(String fbId) {
        facebookServiceDelegate.removeFacebookUser(fbId);
    }

    @Override
    @ServiceMethod
    public String getAccessToken(String fbId) {
        return facebookServiceDelegate.getAccessToken(fbId);
    }

    @Override
    @ServiceMethod
    public List<FbSubscriber> getFamily(String fbId) throws FacebookAuthenticationNeededException {
        return facebookServiceDelegate.getFamily(fbId);
    }

    @Override
    @ServiceMethod
    public List<FbSubscriber> getFriends(String fbId) throws FacebookAuthenticationNeededException {
        return facebookServiceDelegate.getFriends(fbId);
    }

    @Override
    @ServiceMethod
    public Map<String, String> publishToFeed(String fbId, List<FbSubscriber> friends, FbPost post) throws FacebookAuthenticationNeededException, PostLimitExceededException, FacebookPostInvalidException {
        return facebookServiceDelegate.publishToFeed(fbId, friends, post);
    }

    @Override
    @ServiceMethod
    public Map<String, String> publishToFeedWithMessages(String fbId, List<FbSubscriber> friends, FbPost post, List<String> messages) throws FacebookAuthenticationNeededException, PostLimitExceededException, FacebookPostInvalidException {
        return facebookServiceDelegate.publishToFeedWithMessages(fbId, friends, post, messages);
    }

    @Override
    @ServiceMethod
    public String handleDeauthorize(HttpRequest request) {
        return facebookServiceDelegate.handleDeauthorize(request);
    }

    @Override
    @ServiceMethod
    public HttpResponse handleFbPost(HttpRequest request, String facebookId) throws FacebookGeneralException, FacebookAuthenticationNeededException, PostLimitExceededException, FacebookPostInvalidException, JsonGenerationException, JsonMappingException, IOException {
        return facebookServiceDelegate.handleFbPost(request, facebookId);
    }

    @Override
    @ServiceMethod
    public HttpResponse handleGetPermissions(HttpRequest request, String facebookId) throws FacebookAuthenticationNeededException {
        return facebookServiceDelegate.handleGetPermissions(request, facebookId);
    }

    @Override
    @ServiceMethod
    public HttpResponse handleFbGet(HttpRequest request, String facebookId) throws JsonGenerationException, JsonMappingException, IOException {
        return facebookServiceDelegate.handleFbGet(request, facebookId);
    }

    @Override
    @ServiceMethod
    public boolean registerCallback(ServiceEndpoint endpoint, String methodName) {
        return facebookServiceDelegate.registerCallback(endpoint, methodName);
    }

    @Override
    @ServiceMethod
    public boolean unregisterCallback(ServiceEndpoint endpoint) {
        return facebookServiceDelegate.unregisterCallback(endpoint);
    }

    @Override
    @ServiceMethod
    public int getPostLimit() {
        return facebookServiceDelegate.getPostLimit();
    }

    public void setFacebookServiceDelegate(IFacebookService facebookServiceDelegate) {
        this.facebookServiceDelegate = facebookServiceDelegate;
    }
}