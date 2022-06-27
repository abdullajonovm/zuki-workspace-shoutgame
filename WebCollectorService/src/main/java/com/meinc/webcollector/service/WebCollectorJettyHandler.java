package com.meinc.webcollector.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.nio.SslConnection.SslEndPoint;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.meinc.commons.encryption.RandomUuidPool;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.webcollector.message.CollectorMessage;
import com.meinc.webcollector.message.WebCollectorMessageBuffer;
import com.meinc.webcollector.message.handler.BadRequestException;
import com.meinc.webcollector.message.handler.CollectorMessageResult;
import com.meinc.webcollector.message.handler.MessageTypeHandlerRegistry;
import com.meinc.webcollector.message.handler.SyncRequest;

public class WebCollectorJettyHandler extends AbstractHandler {
    private static final Log log = LogFactory.getLog(WebCollectorJettyHandler.class);

    private static Map<Principal,String> trustedCaNameByIssuerDn = Collections.emptyMap();

    static void setTrustedCaNameByIssuerDn(Map<Principal,String> trustedCaNameByIssuerDn) {
        WebCollectorJettyHandler.trustedCaNameByIssuerDn = Collections.unmodifiableMap(trustedCaNameByIssuerDn);
        if (log.isDebugEnabled())
            log.debug("Loaded trusted CA's: " + WebCollectorJettyHandler.trustedCaNameByIssuerDn.values());
    }

    private static final String[] RemoteIpAddressHeaders = new String[] {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};

    @Value("${web.collector.message.buffer.path}")
    private String messageBufferPathString;

    @Autowired
    private WebCollectorMessageBuffer messageBuffer;

    @Autowired
    private MessageTypeHandlerRegistry messageTypeHandlerRegistry;

    private RandomUuidPool randomUuidPool = RandomUuidPool.getInstance();

    private static int defaultEstimatedWaitTime = Integer.parseInt(ServerPropertyHolder.getProperty("webcollector.estimated.wait.time.default.ms", "500"));
    private static int estimatedWaitTimeStatisticsWindow = Integer.parseInt(ServerPropertyHolder.getProperty("webcollector.estimated.wait.time.stats.window", "10"));

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
        try {
            if (baseRequest.isHandled())
                return;

            long requestTime = System.currentTimeMillis();

            if (baseRequest.getPathInfo().startsWith("/health-check")) {
                baseRequest.setHandled(true);
                return;
            }

            boolean isSsl = false, isSslAuthVerified = false;
            String sslAuthCaName = null;  // SSL CA Name & Collector endpoint role enforcement is currently not fully implemented
            EndPoint remoteEndpoint = baseRequest.getConnection().getEndPoint();
            if (remoteEndpoint instanceof SslEndPoint) {
                isSsl = true;
                SSLEngine sslEngine = ((SslEndPoint)remoteEndpoint).getSslEngine();
                SSLSession sslSession = sslEngine.getSession();
                try {
                    if (sslSession.getPeerCertificates() != null) {
                        isSslAuthVerified = true;
                        X509Certificate[] certChain = sslSession.getPeerCertificateChain();
                        for (X509Certificate cert : certChain)
                            sslAuthCaName = trustedCaNameByIssuerDn.get(cert.getIssuerDN());
                    }
                } catch (SSLPeerUnverifiedException ignored) { }
            }

            CollectorMessageResult messageResult;
            try {
                messageResult = messageTypeHandlerRegistry.createMessageFromRequest(request, isSsl, isSslAuthVerified, sslAuthCaName);
            } catch (BadRequestException e) {
                if (e.getCause() != null)
                    log.error("Unexpected error while creating message: " + e.getMessage(), e);
                if (e.getErrorStatusCode() == null && e.getErrorResponseBody() == null) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    response.setStatus(e.getErrorStatusCode() != null ? e.getErrorStatusCode() : HttpServletResponse.SC_BAD_REQUEST);
                    if (e.getErrorResponseContentType() != null)
                        response.setContentType(e.getErrorResponseContentType());
                    else if (e.getErrorResponseBody() != null && e.getErrorResponseBody().startsWith("{"))
                        response.setContentType("application/json");
                    if (e.getErrorResponseBody() != null)
                        response.getWriter().write(e.getErrorResponseBody());
                }
                baseRequest.setHandled(true);
                return;
            }

            if (messageResult == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal message type");
                baseRequest.setHandled(true);
                return;
            }

            if (messageResult.getEntireResponse() != null) {
                SyncRequest.responseToResponse(messageResult.getEntireResponse(), request, response);
                baseRequest.setHandled(true);
                return;
            }

            byte[] responseBody = null;
            CollectorMessage message = messageResult.getMessage();
            if (message != null) {
                String ipAddressString = null;
                for (String remoteIpAddressHeader : RemoteIpAddressHeaders) {
                    ipAddressString = request.getHeader(remoteIpAddressHeader);
                    if (ipAddressString != null && !ipAddressString.trim().isEmpty() && !"unknown".equalsIgnoreCase(ipAddressString))
                        break;
                }
                if (ipAddressString == null || ipAddressString.trim().length() == 0 || "unknown".equalsIgnoreCase(ipAddressString))
                    ipAddressString = request.getRemoteAddr();
                if (ipAddressString != null) {
                    try {
                        InetAddress ipAddress = InetAddress.getByName(ipAddressString);
                        message.setRemoteIpAddress(ipAddress);
                    } catch (UnknownHostException e) {
                        log.warn("Invalid remote host: " + ipAddressString);
                    }
                }

                String messageId = addMessageToBuffer(message, requestTime);

                if (messageId == null) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    baseRequest.setHandled(true);
                    return;
                }
                if (messageResult.getResponseBodyContentType() != null)
                    response.setContentType(messageResult.getResponseBodyContentType());
                else {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("utf-8");
                }
                int estWaitTime = (int) getEstimatedWaitTime(message.getMessageType());

                StringBuffer responseBodyString = generateResponseBody(messageId, estWaitTime, messageResult);
                responseBody = responseBodyString.toString().getBytes("UTF-8");
            } else {
                if (messageResult.getResponseBodyContentType() != null)
                    response.setContentType(messageResult.getResponseBodyContentType());
                responseBody = messageResult.getResponseBody();
            }

            response.getOutputStream().write(responseBody);
            baseRequest.setHandled(true);

        } catch (Exception e) {
            log.error("Error processing web request: " + e.getMessage(), e);
        }
    }

    private StringBuffer generateResponseBody(String messageId, int estWaitTime, CollectorMessageResult messageResult) {
        StringBuffer responseBody = new StringBuffer();
        responseBody.append("{\"ticket\":\"");
        responseBody.append(messageId);
        responseBody.append("\",\"estimatedWaitTime\":");
        responseBody.append(estWaitTime);

        if (messageResult.getJsonKeyValueMap() != null)
            for (Entry<String,Object> jsonKeyValueEntry : messageResult.getJsonKeyValueMap().entrySet()) {
                String key = jsonKeyValueEntry.getKey();
                responseBody.append(",\"");
                responseBody.append(key);
                responseBody.append("\":");
                Object valueObject = jsonKeyValueEntry.getValue();
                if (valueObject instanceof String) {
                    responseBody.append("\"");
                    responseBody.append(valueObject);
                    responseBody.append("\"");
                } else {
                    responseBody.append(valueObject);
                }
            }

        responseBody.append("}");

        return responseBody;
    }

    public String addMessageToBuffer(CollectorMessage message)
    throws IOException, InterruptedException {
        return addMessageToBuffer(message, null);
    }

    public String addMessageToBuffer(CollectorMessage message, Long requestTimestamp)
    throws IOException, InterruptedException {
        if (message == null) {
            log.error("Message is null: " + message);
            return null;
        } else if (message.getMessageType() == null) {
            log.error("Message type is null: " + message);
            return null;
        }

        String messageId = randomUuidPool.getRandomUuid();

        message.setMessageId(messageId);
        message.setTimestamp(requestTimestamp != null ? requestTimestamp : System.currentTimeMillis());

        messageBuffer.addToBuffer(message);
        return messageId;
    }

    /*
     * keep a rolling window of the average response times (within 1 std of the mean) of the last 50 messages.
     * this will allow clients to make an intelligent guestimate of when their message will be completed.
     * this is useful in reducing the number of calls to the server.
     */

    private static final Map<String,DescriptiveStatistics> _messageProcessingTimeStats = new HashMap<String,DescriptiveStatistics>();
    private static final ReadWriteLock _messageProcessingTimeStatsLock = new ReentrantReadWriteLock();

    private static DescriptiveStatistics getTypeStats(String messageType) {
        Lock readLock = _messageProcessingTimeStatsLock.readLock();
        Lock writeLock = null;
        readLock.lock();
        try {
            DescriptiveStatistics typeStats = _messageProcessingTimeStats.get(messageType);
            if (typeStats != null)
                return typeStats;
            else {
                readLock.unlock();
                readLock = null;
                writeLock = _messageProcessingTimeStatsLock.writeLock();
                writeLock.lock();
                typeStats = _messageProcessingTimeStats.get(messageType);
                if (typeStats != null)
                    return typeStats;
                else {
                    typeStats = new DescriptiveStatistics(estimatedWaitTimeStatisticsWindow);
                    typeStats.setMeanImpl(new MeanSansOutliers());
                    typeStats.setVarianceImpl(new VarianceSansOutliers());
                    _messageProcessingTimeStats.put(messageType, typeStats);
                    return typeStats;
                }
            }
        } finally {
            if (readLock != null)
                readLock.unlock();
            if (writeLock != null)
                writeLock.unlock();
        }
    }

    public static void addMessageProcessingTime(CollectorMessage message, double processingTime)
    {
        if (log.isDebugEnabled() && processingTime > 500D) {
            log.debug("adding message processing time: " + processingTime + " for " + message.getMessageType() + " message: " + message.getMessageId());
        }
        DescriptiveStatistics typeStats = getTypeStats(message.getMessageType());
        typeStats.addValue(processingTime);
    }

    private static double getEstimatedWaitTime(String messageType)
    {
        DescriptiveStatistics typeStats = getTypeStats(messageType);
        double mean = typeStats.getMean();
        double stdDev = typeStats.getStandardDeviation();
        if (Double.isNaN(mean) || Double.isNaN(stdDev)) {
            log.debug("returning " + messageType + " default estimatedWaitTime: " + defaultEstimatedWaitTime + " n="+typeStats.getN());
            return defaultEstimatedWaitTime;
        }
        double estimatedWaitTime = mean + stdDev;
        if (log.isDebugEnabled() && estimatedWaitTime > 500D) {
            log.debug("returning " + messageType + " estimatedWaitTime: " + estimatedWaitTime + " n="+typeStats.getN());
        }
        return estimatedWaitTime;
    }

    private static double[] copySansOutliers(double[] values, int begin, int length) {
        double[] valuesSansOutliers = new double[length-2];
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        for (int i = begin; i < begin+length; i++) {
            double value = values[i];
            if (value < min)
                min = value;
            if (value > max)
                max = value;
        }
        boolean skippedMin = false, skippedMax = false;
        int j = 0;
        for (int i = begin; i < begin+length; i++) {
            double value = values[i];
            if (!skippedMin && value == min)
                skippedMin = true;
            else if (!skippedMax && value == max)
                skippedMax = true;
            else
                valuesSansOutliers[j++] = value;
        }
        return valuesSansOutliers;
    }

    public static class MeanSansOutliers extends Mean {
        private static final long serialVersionUID = 1L;

        @Override
        public double evaluate(double[] values, int begin, int length) throws MathIllegalArgumentException {
            if (length <= 2) {
                return super.evaluate(values, begin, length);
            } else {
                double[] valuesSansOutliers = copySansOutliers(values, begin, length);
                return super.evaluate(valuesSansOutliers, 0, valuesSansOutliers.length);
            }
        }
    }

    public static class VarianceSansOutliers extends Variance {
        private static final long serialVersionUID = 1L;

        @Override
        public double evaluate(double[] values, int begin, int length) throws MathIllegalArgumentException {
            if (length <= 2) {
                return super.evaluate(values, begin, length);
            } else {
                double[] valuesSansOutliers = copySansOutliers(values, begin, length);
                return super.evaluate(valuesSansOutliers, 0, valuesSansOutliers.length);
            }
        }
    }
}
