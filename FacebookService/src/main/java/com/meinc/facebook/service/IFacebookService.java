package com.meinc.facebook.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
import com.meinc.http.domain.HttpRequest;
import com.meinc.http.domain.HttpResponse;
import com.meinc.mrsoa.service.ServiceEndpoint;

public interface IFacebookService 
{
    public static final int FACEBOOK_ADDED = 1;
    public static final int FACEBOOK_REMOVED = 2;
    
//    /**
//     * Called when the service loads
//     */
    public void load();
    
//    /**
//     * Called when the service unloads
//     */
//    public void unload();
    
//    /**
//     * Return the facebookId (in String form) associated with the given subscriberId,
//     * or null if no association exists for the given subscriber
//     * @param subscriberId
//     * @return
//     */
//    public String getFacebookIdBySubscriberId(int subscriberId);
//
    /**
     * Add a new facebook user to the system
     * 
     * @param accessToken the access token received from facebook oauth
     * @param reassignIfExists if true, and if some other subscriber is already using the facebook id, reassign it to the new subscriber
     * @return the facebookId
     * @throws FacebookAuthenticationNeededException 
     */
    public String addFacebookUser(String accessToken, boolean reassignIfExists)
    throws FacebookGeneralException, InvalidAccessTokenException, FacebookUserExistsException, FacebookAuthenticationNeededException;
    
    /**
     * Add a new facebook user without doing a lookup.
     * @param accessToken
     * @param fbId
     * @return
     */
    public void addWithAccessTokenAndId(String accessToken, String fbId, boolean updateAccessTokenIfExists);
//    /**
//     * Return the info for a given subscriber from the Facebook service.  The given subscriber must have
//     * already had an access token set for this method to return a valid object.
//     * @param subscriberId
//     * @return
//     * @throws FacebookAuthenticationNeededException
//     */
//    public FbSubscriber getSubscriberInfo(int subscriberId) throws FacebookAuthenticationNeededException;
//    
    /**
     * Return the info for a given subscriber from the Facebook service if this access token is already 
     * associated with a subscriber.  
     * @param accessToken The access token to use to find a subscriber with.
     * @return FbSubscriber The subscriber info found for the accessToken.
     * @throws FacebookAuthenticationNeededException 
     */
    public FbSubscriber getSubscriberInfoByAccessToken(String accessToken)
    throws FacebookGeneralException, InvalidAccessTokenException, FacebookAuthenticationNeededException;
    
    /**
     * Return the info for the Facebook user that is represented by the given accessToken.
     * Returns null if the given accessToken is invalid.
     * @param fbId
     * @return FbSubscriber The Facebook users info, or null if the accessToken is invalid
     * @throws FacebookAuthenticationNeededException 
     */
    public FbSubscriber getFacebookUser(String fbId) throws FacebookAuthenticationNeededException;
    
    public void removeFacebookUser(String fbId);
    
    public String getAccessToken(String fbId);
    
    /**
     * Return a list of the subscribers family memebers from Facebook
     * @param fbId
     * @return
     * @throws FacebookAuthenticationNeededException
     */
    public List<FbSubscriber> getFamily(String fbId) 
    throws FacebookAuthenticationNeededException;
    
    /**
     * Returns a list of the subscribers friends from Facebook
     * @param fbId
     * @return List<FbSubscriber> the list of friends
     * @throws FacebookAuthenticationNeededException
     */
    public List<FbSubscriber> getFriends(String fbId) 
    throws FacebookAuthenticationNeededException;
    
    /**
     * Publish the given post to the wall of each person in the list of friends
     * @param fbId
     * @param friends
     * @param post
     * @return Map<String,String> map of people who's walls were posted to
     * @throws FacebookAuthenticationNeededException
     * @throws PostLimitExceededException if the list of friends passed in is greater than the max, this exception is thrown
     * @throws FacebookPostInvalidException 
     */
    public Map<String,String> publishToFeed(String fbId, List<FbSubscriber> friends, FbPost post) 
    throws FacebookAuthenticationNeededException, PostLimitExceededException, FacebookPostInvalidException;

    /**
     * Publish the given post to the wall of each person in the list of friends by selecting a message from the
     * provided list.
     * @param fbId
     * @param friends
     * @param post
     * @param messages
     * @return
     * @throws FacebookAuthenticationNeededException
     * @throws PostLimitExceededException
     * @throws FacebookPostInvalidException 
     */
    public Map<String,String> publishToFeedWithMessages(String fbId, List<FbSubscriber> friends, FbPost post, List<String> messages) 
    throws FacebookAuthenticationNeededException, PostLimitExceededException, FacebookPostInvalidException;
    
//    /**
//     * Grab the info for a given subscriber from the Facebook service by the facebook id.
//     * @param fbId
//     * @return
//     * @throws FacebookAuthenticationNeededException TODO
//     */
//    public FbSubscriber getSubscriberInfoByFbId(String fbId) throws FacebookAuthenticationNeededException;
//    
//    /**
//     * Returns the integration info for the given subscriber id.  The returned map will have
//     * keys of "facebookId" and "accessToken" if the subscriber has done the Facebook integration.
//     * @param subscriberId
//     * @return the integration info, or null if there has been no integration done.
//     * @throws FacebookAuthenticationNeededException TODO
//     */
//    public Map<String,String> getIntegrationInfo(int subscriberId) throws FacebookAuthenticationNeededException;
    
    public String handleDeauthorize(HttpRequest request);
    
    public HttpResponse handleFbPost(HttpRequest request, String facebookId)
    throws FacebookGeneralException, FacebookAuthenticationNeededException, PostLimitExceededException, FacebookPostInvalidException, JsonGenerationException, JsonMappingException, IOException;
    
    public HttpResponse handleGetPermissions(HttpRequest request, String facebookId) throws FacebookAuthenticationNeededException;
    
    public HttpResponse handleFbGet(HttpRequest request, String facebookId)
    throws JsonGenerationException, JsonMappingException, IOException;
    
//    
    /**
     * Register a callback to be notified when various things happen inside of the Facebook service.
     * The method name passed in should have the following signature:
     *     void fbCallback(String facebookId, int eventType)
     * 
     * @param endpoint
     * @param methodName
     * @return
     */
    public boolean registerCallback(ServiceEndpoint endpoint, String methodName);
    
    /**
     * Un-register a callback for the given endpoint
     * @param endpoint
     * @return true if a callback was un-registered, false if no callback was registered for the endpoint
     */
    public boolean unregisterCallback(ServiceEndpoint endpoint);
    
    public int getPostLimit();
}
