package com.meinc.urlshorten.service;

import java.util.List;
import java.util.Map;

import com.meinc.urlshorten.exception.NoSuchShortUrlException;
import com.meinc.urlshorten.exception.ShortUrlAlreadyExistsException;
import com.meinc.urlshorten.exception.UrlShortenerException;
import com.meinc.webcollector.message.CollectorMessage;
import com.meinc.webcollector.message.IWebCollectorMessageBuffer;
import com.meinc.webcollector.message.handler.BadRequestException;
import com.meinc.webcollector.message.handler.CollectorMessageResult;
import com.meinc.webcollector.message.handler.SyncRequest.HttpRequest;
import com.meinc.webcollector.message.handler.SyncRequest.HttpResponse;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;

public interface IUrlShortenerService 
{
    public static final String SERVICE_NAMESPACE = "meinc-service";
    public static final String SERVICE_NAME = "UrlShortenerService";
    public static final String SERVICE_INTERFACE = "IUrlShortenerService";
    public static final String SERVICE_VERSION = "1.0";
    
    void start();
    void stop();
    
    public String makeShortUrl(String requestHost, String longUrl) throws ShortUrlAlreadyExistsException, UrlShortenerException;

    public String getLongUrl(String shortUrl) throws NoSuchShortUrlException, UrlShortenerException;

    public String flushShortUrlCache(String shortUrlCode);
    public HttpResponse handleSyncRequest(HttpRequest request) throws BadRequestException;
    public void handleMessages(List<CollectorMessage> messages, IWebCollectorMessageBuffer messageBuffer) throws BadRequestException;
    public CollectorEndpoint[] getCollectorEndpoints();
    public String getHandlerMessageType();
    public CollectorMessageResult createMessage(String requestPath, Map<String,String> requestHeaders, Map<String,String> requestParameters) throws BadRequestException;
}
