package com.meinc.http.jetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpParser;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class HttpTraceHandler extends AbstractHandler {
    //private static final Log log = LogFactory.getLog(HttpTraceHandler.class);
    
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
        if (!baseRequest.isHandled()) {
            if (HttpMethods.TRACE.equals(request.getMethod())){
                HttpParser parser = null;
                try {
                    parser = (HttpParser) baseRequest.getConnection().getParser();
                } catch (ClassCastException e) { }
                if (parser != null) {
                    response.setStatus(HttpServletResponse.SC_OK);

                    Buffer headerBuffer = parser.getHeaderBuffer();
                    if (headerBuffer != null)
                        response.getOutputStream().write(headerBuffer.array(), 0, headerBuffer.putIndex());

                    Buffer bodyBuffer = parser.getBodyBuffer();
                    if (bodyBuffer != null)
                        response.getOutputStream().write(bodyBuffer.array(), 0, bodyBuffer.putIndex());

                    response.getOutputStream().flush();

                    baseRequest.setHandled(true);
                }
            }
        }
    }
}
