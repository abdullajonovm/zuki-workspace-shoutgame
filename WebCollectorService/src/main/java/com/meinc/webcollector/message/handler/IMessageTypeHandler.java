package com.meinc.webcollector.message.handler;

import java.util.List;
import java.util.Map;

import com.meinc.webcollector.message.CollectorMessage;
import com.meinc.webcollector.message.IWebCollectorMessageBuffer;
import com.meinc.webcollector.message.handler.SyncRequest.HttpRequest;
import com.meinc.webcollector.message.handler.SyncRequest.HttpResponse;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;

//TODO: rename this?
public interface IMessageTypeHandler {
    
    public CollectorMessageResult createMessage(String requestPath, Map<String,String> requestHeaders, Map<String,String> requestParameters)
    throws BadRequestException;

    public String getHandlerMessageType();
    public CollectorEndpoint[] getCollectorEndpoints();
    
    public void handleMessages(List<CollectorMessage> messages, IWebCollectorMessageBuffer messageBuffer)
    throws BadRequestException;
    
    public HttpResponse handleSyncRequest(HttpRequest request)
    throws BadRequestException;
}
