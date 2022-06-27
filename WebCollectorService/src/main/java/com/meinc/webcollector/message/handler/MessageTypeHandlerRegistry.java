package com.meinc.webcollector.message.handler;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.ServiceMessage;
import com.meinc.webcollector.message.handler.SyncRequest.HttpRequest;
import com.meinc.webcollector.message.handler.SyncRequest.HttpResponse;
import com.meinc.webcollector.service.ForbiddenAccessException;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;

public class MessageTypeHandlerRegistry {
    private static final Log log = LogFactory.getLog(MessageTypeHandlerRegistry.class);

    private static class CollectorCallback {
        private CollectorEndpoint path;
        private ServiceEndpoint endpoint;
        public CollectorCallback(CollectorEndpoint path, ServiceEndpoint endpoint) {
            this.path = path;
            this.endpoint = endpoint;
        }
    }

    private Map<String,CollectorCallback> collectorCallbackByMessageType = new HashMap<>();
    private Map<String,CollectorCallback> collectorCallbackByPath = new HashMap<>();
    private Map<String,CollectorCallback> collectorCallbackByPathPrefix = new HashMap<>();
    private ReadWriteLock messageTypeMapLock = new ReentrantReadWriteLock();

    public void registerMessageTypeHandler(CollectorEndpoint collectorEndpoint, String messageType, ServiceEndpoint messageTypeHandlerEndpoint) {
        if (collectorEndpoint.getPath() == null)
            throw new IllegalArgumentException("null path");
        Lock writeLock = messageTypeMapLock.writeLock();
        writeLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Registering collector callback for %s %s (%s) -> %s",
                                        messageType, collectorEndpoint.getPath(),
                                        collectorEndpoint.getHandlerStyle(), messageTypeHandlerEndpoint));
            }
            CollectorCallback collectorCallback = new CollectorCallback(collectorEndpoint, messageTypeHandlerEndpoint);
            collectorCallbackByMessageType.put(messageType, collectorCallback);
            if (collectorEndpoint.getPath() != null) {
                if (collectorEndpoint.isPathIsPrefix()) {
                    collectorCallbackByPathPrefix.put(collectorEndpoint.getPath(), collectorCallback);
                } else {
                    collectorCallbackByPath.put(collectorEndpoint.getPath(), collectorCallback);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void unregisterMessageTypeHandler(String messageType) {
        Lock writeLock = messageTypeMapLock.writeLock();
        writeLock.lock();
        try {
            CollectorCallback collectorCallback = collectorCallbackByMessageType.remove(messageType);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Unregistering collector callback for %s %s (%s)",
                                        messageType, collectorCallback.path.getPath(),
                                        collectorCallback.path.getHandlerStyle()));
            }
            collectorCallbackByPath.remove(collectorCallback.path.getPath());
            collectorCallbackByPathPrefix.remove(collectorCallback.path.getPath());
        } finally {
            writeLock.unlock();
        }
    }

    public ServiceEndpoint getMessageTypeHandlerEndpoint(String messageType) {
        Lock readLock = messageTypeMapLock.readLock();
        readLock.lock();
        try {
            return collectorCallbackByMessageType.get(messageType).endpoint;
        } finally {
            readLock.unlock();
        }
    }

    public CollectorCallback getCollectorEndpoint(String collectorPath) {
        Lock readLock = messageTypeMapLock.readLock();
        readLock.lock();
        try {
            CollectorCallback callback = collectorCallbackByPath.get(collectorPath);
            if (callback != null)
                return callback;
            //TODO if more than a handful of path prefixes are registered it will be more performant to use a different algorithm
            callback = collectorCallbackByPathPrefix.entrySet().stream()
                .filter(entry -> collectorPath.startsWith(entry.getKey()))
                .map(entry -> entry.getValue())
                .findFirst().orElse(null);
            if (callback == null)
                return callback;
            return callback;
        } finally {
            readLock.unlock();
        }
    }

    public CollectorMessageResult createMessageFromRequest(HttpServletRequest request, boolean isSsl, boolean isSslAuthVerified, String sslAuthCaName)
    throws BadRequestException, ForbiddenAccessException {
        Map<String,String> requestHeaders = new HashMap<String,String>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerKey = headerNames.nextElement();
            requestHeaders.put(headerKey, request.getHeader(headerKey));
        }

        Map<String,String> requestParameters = new HashMap<String,String>(request.getParameterMap().size());
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            requestParameters.put(parameterName, request.getParameter(parameterName));
        }

//unless this becomes a bug (submitting vs processing), don't bother to print this. it's just duplicated in a moment by the MessageProcessorDaemon
//        if (log.isDebugEnabled()) {
//            //exception: if there is an acra report, it's just too much to log. don't even try
//            String params = request.getPathInfo().contains("/acra/report") ? " --- ACRA DATA (not displayed for brevity) ---" : requestParameters.toString();
//            log.debug(String.format("RAW Message isSsl/isSslAuth/sslAuthCaName=%b/%b/%s path=%s props=%s parms=%s", isSsl, isSslAuthVerified, sslAuthCaName, request.getPathInfo(), requestHeaders, params));
//        }

        CollectorCallback collectorCallback = getCollectorEndpoint(request.getPathInfo());

        if (collectorCallback == null) {
            log.warn("No handler found for request " + request.getPathInfo() + " headers=" + requestHeaders + " params=" + requestParameters);
            return null;
        }

        switch (collectorCallback.path.getType()) {
            case AUTH_SSL:
                if (!isSslAuthVerified) {
                    if (log.isDebugEnabled())
                        log.warn("Message must be SSL authenticated");
                    throw new ForbiddenAccessException();
                }
                break;
            case SSL:
                if (!isSsl) {
                    if (log.isDebugEnabled())
                        log.warn("Message must be SSL encrypted");
                    throw new ForbiddenAccessException();
                }
                break;
            case INTERNAL:
                if (log.isDebugEnabled())
                    log.warn("Message must be internal");
                throw new ForbiddenAccessException();
            case ANY:
            default:
                break;
        }

        switch (collectorCallback.path.getHandlerStyle()) {
        case ASYNC_MESSAGE:
            try {
                CollectorMessageResult messageResult =
                        (CollectorMessageResult) ServiceMessage.sendFast(collectorCallback.endpoint, "createMessage",
                                                                         request.getPathInfo(), requestHeaders, requestParameters);
                return messageResult;
            } catch (Exception e) {
                if (e instanceof BadRequestException)
                    throw (BadRequestException) e;
                throw new BadRequestException(e);
            }
        case SYNC_REQUEST:
            try {
                HttpRequest httpRequest = SyncRequest.requestToRequest(request);
                HttpResponse httpResponse = (HttpResponse) ServiceMessage.sendFast(collectorCallback.endpoint, "handleSyncRequest", httpRequest);
                CollectorMessageResult messageResult = new CollectorMessageResult();
                messageResult.setEntireResponse(httpResponse);
                return messageResult;
            } catch (Exception e) {
                if (e instanceof BadRequestException)
                    throw (BadRequestException) e;
                throw new BadRequestException(e);
            }
        default:
            throw new IllegalStateException("unknown handler style " + collectorCallback.path.getHandlerStyle().name());
        }
    }
}
