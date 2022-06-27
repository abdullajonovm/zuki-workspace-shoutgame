package com.meinc.facebook.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.meinc.facebook.domain.FbCallback;
import com.meinc.facebook.domain.FbSubscriber;

public interface IFacebookServiceDaoMapper
{
    public void insertAccessToken(@Param("facebookId") String facebookId, @Param("accessToken") String accessToken);
//    public void setFacebookId(@Param("subscriberId") int subscriberId, @Param("facebookId") String facebookId);
    public String getAccessTokenForFbId(String fbId);
//    public Integer getSubscriberIdForFacebookId(String facebookId);
//    public Integer getSubscriberIdForAccessToken(String accessToken);
//    public String getFacebookIdForSubscriber(int subscriberId);
    public void setSubscriberIdAndAccessToken(
        @Param("accessToken") String accessToken,
        @Param("facebookId") String facebookId
    );
    public void removeAuthTokenForFacebookSubscriber(String facebookId);
    public void addCallback(FbCallback callback);
    public FbCallback getCallbackForEndpoint(FbCallback callback);
    public Integer removeCallback(FbCallback callback);
    public List<FbCallback> getCallbacks(); 
    public List<FbSubscriber> getFbSubscribers();

}
