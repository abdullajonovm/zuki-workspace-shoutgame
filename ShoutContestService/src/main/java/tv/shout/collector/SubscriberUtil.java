package tv.shout.collector;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.domain.SubscriberSession;
import com.meinc.identity.exception.InvalidSessionException;
import com.meinc.identity.service.IIdentityService;
import com.meinc.webcollector.message.handler.SyncRequest.HttpRequest;

public class SubscriberUtil
{
    public static final String X_REST_SESSION_KEY = "X-REST-SESSION-KEY";
    public static final String X_REST_DEVICE_ID   = "X-REST-DEVICE-ID";
    public static final String X_REST_APPLICATION_ID = "X-REST-APPLICATION-ID";
    public static final String X_REST_APPLICATION_VERSION  = "X-REST-APPLICATION-VERSION";

    private static Logger _logger = Logger.getLogger(SubscriberUtil.class);

    @Autowired
    private IIdentityService _identityService;

    public enum NO_SUBSCRIBER_REASON {MISSING_X_REST_DEVICE_ID_HEADER, MISSING_X_REST_SESSION_KEY_HEADER, SUBSCRIBER_NOT_FOUND_FOR_GIVEN_HEADERS}

    public class SubscriberResponse
    implements Serializable
    {
        private static final long serialVersionUID = 1L;

        public Subscriber subscriber;
        public NO_SUBSCRIBER_REASON noSubscriberReason;

        public SubscriberResponse(Subscriber subscriber)
        {
            this.subscriber = subscriber;
        }

        public SubscriberResponse(NO_SUBSCRIBER_REASON noSubscriberReason)
        {
            this.noSubscriberReason = noSubscriberReason;
        }
    }

    public SubscriberResponse getSubscriberFromSession(Map<String, String> props)
    {
        String deviceId    = props.get("HEADER_" + X_REST_DEVICE_ID);
        String sessionKey  = props.get("HEADER_" + X_REST_SESSION_KEY);
        String appId       = props.get("HEADER_" + X_REST_APPLICATION_ID);
        String appVersion  = props.get("HEADER_" + X_REST_APPLICATION_VERSION);
        String requestPath = props.get("__requestPath");

        return getSubscriberRefactor(deviceId, sessionKey, appId, appVersion, requestPath);
    }

    public SubscriberResponse getSubscriberFromSession(HttpRequest request)
    {
        String deviceId    = request.getHeader(X_REST_DEVICE_ID);
        String sessionKey  = request.getHeader(X_REST_SESSION_KEY);
        String appId       = request.getHeader(X_REST_APPLICATION_ID);
        String appVersion  = request.getHeader(X_REST_APPLICATION_VERSION);
        String requestPath = request.getPath();

        return getSubscriberRefactor(deviceId, sessionKey, appId, appVersion, requestPath);
    }

    private SubscriberResponse getSubscriberRefactor(String deviceId, String sessionKey, String appId, String appVersion, String requestPath)
    {
        if (deviceId == null) {
            _logger.warn("no '"+X_REST_DEVICE_ID+"' header for: " + requestPath);
            return new SubscriberResponse(NO_SUBSCRIBER_REASON.MISSING_X_REST_DEVICE_ID_HEADER);
        } else if (sessionKey == null) {
            _logger.warn("no '"+X_REST_SESSION_KEY+"' header for: " + requestPath);
            return new SubscriberResponse(NO_SUBSCRIBER_REASON.MISSING_X_REST_SESSION_KEY_HEADER);
        }

        //make sure subscriber exists
        Subscriber subscriber = null;
        try {
            subscriber = _identityService.getSubscriberByDeviceSessionKey(deviceId, sessionKey, appId, appVersion);
        } catch (InvalidSessionException e) {
            _logger.warn(MessageFormat.format(
                    "subscriber not found for deviceId: {0}, sessionKey: {1}, appId: {2}, appVersion: {3} on call: {4}",
                    deviceId, sessionKey, appId, appVersion, requestPath));
            return new SubscriberResponse(NO_SUBSCRIBER_REASON.SUBSCRIBER_NOT_FOUND_FOR_GIVEN_HEADERS);
        }

        return new SubscriberResponse(subscriber);
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
