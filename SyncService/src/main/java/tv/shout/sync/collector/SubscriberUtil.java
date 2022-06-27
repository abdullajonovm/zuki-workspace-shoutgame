package tv.shout.sync.collector;

import static com.meinc.webcollector.message.CollectorMessage.PARM_TO_WDS;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.domain.SubscriberSession;
import com.meinc.identity.exception.InvalidSessionException;
import com.meinc.identity.service.IIdentityService;
import com.meinc.webcollector.message.CollectorMessage;

public class SubscriberUtil
{
    public static final String X_REST_SESSION_KEY = "X-REST-SESSION-KEY";
    public static final String X_REST_DEVICE_ID   = "X-REST-DEVICE-ID";
    public static final String X_REST_APPLICATION_ID = "X-REST-APPLICATION-ID";
    public static final String X_REST_APPLICATION_VERSION  = "X-REST-APPLICATION-VERSION";
    
    private static Logger _logger = Logger.getLogger(SubscriberUtil.class);
    
    @Autowired
    private IIdentityService _identityService;
    
    public Subscriber getAuthenticatedSubscriber(String logTag, CollectorMessage message) 
    throws PublishResponseError
    {
        Subscriber subscriber = getSubscriberFromSession(message.getProperties());
        if (subscriber == null) {
            throw new PublishResponseError(message.getProperties().get(PARM_TO_WDS), message.getMessageId(), logTag, false, "unexpectedError", "subscriber not extractable from HTTP headers");
        }
        return subscriber;
    }
    
    public Subscriber getSubscriberFromSession(Map<String, String> props)
    {
        //
        // Extract a subscriber using the HTTP HEADERS to identify the subscriber
        // It's OK if we don't find them -- we'll test for the subscriber on each usage.
        // And signup won't have a subscriber anyway.
        //
        String deviceId    = props.get("HEADER_" + X_REST_DEVICE_ID); 
        String sessionKey  = props.get("HEADER_" + X_REST_SESSION_KEY);
        String appId       = props.get("HEADER_" + X_REST_APPLICATION_ID);
        String appVersion  = props.get("HEADER_" + X_REST_APPLICATION_VERSION);
        String requestPath = props.get("__requestPath");
        
        if (deviceId == null || sessionKey == null) { // || applicationId == null || applicationVersion == null) {
            _logger.warn("attempt to call '" + requestPath + "' without the requisite auth headers");
            return null;
        }
        
        //make sure subscriber exists
        Subscriber subscriber = null;
        try {
            subscriber = _identityService.getSubscriberByDeviceSessionKey(deviceId, sessionKey, appId, appVersion);
        } catch (InvalidSessionException e) {
            _logger.warn("subscriber not found during call to '" + requestPath + "'");
            return null;
        }
        return subscriber;
    }

    public SubscriberSession getUnauthenticatedSession(Map<String, String> props)
    {
        SubscriberSession session = new SubscriberSession();
        session.setDeviceId(     props.get("HEADER_" + X_REST_DEVICE_ID));
        session.setAppId(        props.get("HEADER_" + X_REST_APPLICATION_ID));
        session.setAppVersion(   props.get("HEADER_" + X_REST_APPLICATION_VERSION));
        session.setDeviceModel(  props.get("HEADER_deviceModel"));
        session.setDeviceName(   props.get("HEADER_deviceName"));
        session.setDeviceVersion(props.get("HEADER_deviceVersion"));
        session.setOsName(       props.get("HEADER_deviceOsName"));
        session.setOsType(       props.get("HEADER_deviceOsType"));
        return session;        
    }


}
