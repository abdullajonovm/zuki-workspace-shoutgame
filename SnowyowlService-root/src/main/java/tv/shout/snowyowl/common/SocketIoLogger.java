package tv.shout.snowyowl.common;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import com.meinc.trigger.service.ITriggerService;

import tv.shout.snowyowl.service.ISnowyowlService;

public class SocketIoLogger
{
    public static void log(ITriggerService triggerService, Long subscriberId, String messageType, String message, String status)
    {
        if (triggerService == null) return;

        //these values don't matter in our case
        String source = null;
        Set<String> bundleIds = null;
        int contextId = -1;

        //build up the payload
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("subscriberId", subscriberId);
        payload.put("sentDate", new Date());
        payload.put("messageType", messageType);
        payload.put("message", message);
        payload.put("status", status);

        triggerService.enqueue(ISnowyowlService.TRIGGER_KEY_SOCKET_IO_MESSAGE, payload, ISnowyowlService.TRIGGER_SERVICE_ROUTE, source, bundleIds, contextId);
    }
}
