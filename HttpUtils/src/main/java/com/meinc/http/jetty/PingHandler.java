package com.meinc.http.jetty;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class PingHandler extends AbstractHandler {
    //private static final Log log = LogFactory.getLog(HttpTraceHandler.class);

    private volatile byte[] responseData = new byte[0];
    private String pingHandlerKey;
    
    public PingHandler(String pingHandlerKey) {
        this.pingHandlerKey = pingHandlerKey;
    }
    
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
        if (!baseRequest.isHandled()) {
            if (HttpMethods.GET.equals(request.getMethod())){
                String pingKeyParm = request.getHeader("X-PING-KEY");
                if (pingKeyParm == null)
                    pingKeyParm = request.getParameter("X-PING-KEY");
                if (pingKeyParm != null && pingHandlerKey.equals(pingKeyParm)) {
                    response.setStatus(HttpServletResponse.SC_OK);

                    String sleepMsString = request.getParameter("sleep_time");
                    if (sleepMsString != null) {
                        long sleepMs = 0;
                        try {
                            sleepMs = Long.parseLong(sleepMsString);
                        } catch (NumberFormatException e) { }
                        try {
                            Thread.sleep(sleepMs);
                        } catch (InterruptedException e) { }
                    }
                    
                    String responseSizeString = request.getParameter("response_size");
                    int responseSize = 0;
                    if (responseSizeString == null) {
                        response.setContentLength(responseSize);
                    } else {
                        responseSize = Integer.parseInt(responseSizeString);
                        response.setContentLength(responseSize);
                        if (responseSize > responseData.length) {
                            synchronized (responseData) {
                                if (responseSize > responseData.length) {
                                    responseData = new byte[responseSize];
                                    new Random().nextBytes(responseData);
                                }
                            }
                        }
                        response.getOutputStream().write(responseData, 0, responseSize);
                        response.getOutputStream().flush();
                        //try {
                        //    Thread.sleep(100);
                        //} catch (InterruptedException e) { }
                    }

                    baseRequest.setHandled(true);
                }
            }
        }
    }
}
