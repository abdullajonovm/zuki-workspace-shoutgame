package com.meinc.http.jetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class CORSHandler extends AbstractHandler {
    //private static final Log log = LogFactory.getLog(CORSHandler.class);
    
    private boolean handleOptionsRequests;

    public CORSHandler(boolean handleOptionsRequests) {
        this.handleOptionsRequests = handleOptionsRequests;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
        if (!baseRequest.isHandled()) {
            String originHeader = request.getHeader("Origin");
            if (originHeader != null) {
                response.addHeader("Access-Control-Allow-Origin", "*");
                response.addHeader("Access-Control-Expose-Headers", "Date");
            }
            if (HttpMethods.OPTIONS.equals(request.getMethod())) {
                String acrHeaders = request.getHeader("Access-Control-Request-Headers");
                if (acrHeaders != null) {
                    response.addHeader("Access-Control-Allow-Headers", acrHeaders);
                }
                response.addHeader("Access-Control-Expose-Headers", "Date,Last-Modified,Last-Touched");
                response.addHeader("Access-Control-Max-Age", "86400");   //1 day
                if (handleOptionsRequests) {
                    response.addHeader(HttpHeaders.ALLOW, "GET, HEAD, POST, TRACE, OPTIONS");
                    response.setStatus(HttpServletResponse.SC_OK);
                    baseRequest.setHandled(true);
                }
            }
        }
    }
}
