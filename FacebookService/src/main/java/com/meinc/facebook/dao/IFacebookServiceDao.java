package com.meinc.facebook.dao;

import java.util.Collection;
import java.util.List;

import com.meinc.facebook.domain.FbCallback;
import com.meinc.facebook.domain.FbSubscriber;

public interface IFacebookServiceDao {
    
    public void start();

    public void insertAccessToken(String facebookId, String accessToken);
//    public void setFacebookId(int subscriberId, String facebookId);
    public String getAccessTokenForFbId(String fbId);
//    public int getSubscriberIdForFacebookId(String facebookId);
//    public int getSubscriberIdForAccessToken(String accessToken);
    public void setSubscriberIdAndAccessToken(String accessToken, String facebookId);
    public void removeAuthTokenForFacebookSubscriber(String facebookId);
//    public String getFacebookIdForSubscriber(int subscriberId);
    public void addCallback(FbCallback callback);
    public FbCallback getCallbackForEndpoint(FbCallback callback);
    public Integer removeCallback(FbCallback callback);
    public Collection<FbCallback> getCallbacks(); 
    public List<FbSubscriber> getFbSubscribers();
    
}
