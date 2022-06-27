package tv.shout.collector;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import com.meinc.webcollector.message.CollectorMessage;
import com.meinc.webcollector.message.handler.SyncRequest.HttpRequest;
import com.meinc.webcollector.message.handler.SyncRequest.HttpResponse;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;

public class CollectorEndpointHandler
{
    private CollectorEndpoint collectorEndpoint;
    private BiFunction<CollectorMessage,String,Map<String,Object>> messageHandlerFunction;
    private BiFunction<HttpRequest,String,HttpResponse> syncRequestHandlerFunction;
    private Set<String> validRoles;

    public CollectorEndpointHandler(CollectorEndpoint collectorEndpoint) {
        this.collectorEndpoint = collectorEndpoint;
    }

    public CollectorEndpoint getCollectorEndpoint() {
        return collectorEndpoint;
    }

    public BiFunction<HttpRequest,String, HttpResponse> getSyncRequestHandlerFunction() {
        return syncRequestHandlerFunction;
    }

    public BiFunction<CollectorMessage, String, Map<String, Object>> getMessageHandlerFunction() {
        return messageHandlerFunction;
    }

    public Set<String> getValidRoles()
    {
        return validRoles;
    }

    //fluent api to set async collector function to call
    public CollectorEndpointHandler withMessageHandlerFunction(BiFunction<CollectorMessage,String,Map<String,Object>> messageHandlerFunction) {
        this.messageHandlerFunction = messageHandlerFunction;
        return this;
    }

    //fluent api to set sync collector function to call
    public CollectorEndpointHandler withSyncRequestHandlerFunction(BiFunction<HttpRequest,String,HttpResponse> syncRequestHandlerFunction) {
        this.syncRequestHandlerFunction = syncRequestHandlerFunction;
        return this;
    }

    public CollectorEndpointHandler withValidRoles(Set<String> validRoles)
    {
        this.validRoles = validRoles;
        return this;
    }
}
