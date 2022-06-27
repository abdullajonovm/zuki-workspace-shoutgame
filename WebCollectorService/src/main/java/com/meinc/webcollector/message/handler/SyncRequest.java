package com.meinc.webcollector.message.handler;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import tv.shout.util.MultiMap;

public class SyncRequest {
    private static final Log log = LogFactory.getLog(SyncRequest.class);

    public static class HttpRequest implements Serializable {
        private static final long serialVersionUID = 3L;

        private byte[] body;
        private MultiMap<String,String> parameters;
        private String remoteAddr;
        private String contentType;
        private String method;
        private String userAgent;
        private String requestUrl;
        private String path;
        private String effectivePath;
        private String serverName;
        private Map<String,String> headers;
        private Set<String> headerNames;

        public HttpRequest() {
            headers = new HashMap<String, String>();
            headerNames = new HashSet<String>();
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public byte[] getBody() {
            return body;
        }

        public void setBody(byte[] body) {
            this.body = body;
        }

        public MultiMap<String,String> getParameters() {
            return parameters;
        }

        public void setParameters(MultiMap<String,String> parameters) {
            this.parameters = parameters;
        }

        public String getHeader(String headerName) {
            return headers.get(headerName.toLowerCase());
        }

        public void addHeader(String headerName, String value) {
            headerNames.add(headerName);
            headers.put(headerName.toLowerCase(), value);
        }

        public Set<String> getHeaderNames()
        {
            return headers.keySet();
        }

        public String getFirstParameter(String parmName) {
            return
                Optional.ofNullable(parameters)
                    .map(parms -> parms.get(parmName))
                    .map(values -> values.isEmpty() ? null : values.get(0))
                    .orElse(null);
        }

        public List<String> getParameter(String parmName) {
            return parameters == null ? null : parameters.get(parmName);
        }

        public String getRemoteAddr() {
            return remoteAddr;
        }

        public void setRemoteAddr(String remoteAddr) {
            this.remoteAddr = remoteAddr;
        }

        public String getRequestURL() {
            return requestUrl;
        }

        public void setRequestURL(String requestUrl) {
            this.requestUrl = requestUrl;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getEffectivePath() {
            return effectivePath;
        }

        public void setEffectivePath(String path) {
            this.effectivePath = path;
        }

        public String getServerName() {
            return serverName;
        }

        public void setServerName(String serverName) {
            this.serverName = serverName;
        }

        public String getRequestUrl() {
            return requestUrl;
        }

        public void setRequestUrl(String requestUrl) {
            this.requestUrl = requestUrl;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }
    }

    public static class HttpResponse implements Serializable {
        private static final long serialVersionUID = 3L;

        private String _contentType;
        private Map<String,String> _headers;
        private byte[] _binaryData;
        private String _binaryDataFilename;
        private Integer _error;
        private String _errorMessage;
        private String _redirect;
        private byte[] _output;
        private transient ByteArrayOutputStream _outputStream;
        private transient PrintWriter _writer;

        public HttpResponse() { }

        public HttpResponse(int httpError) {
            this._error = httpError;
        }

        public Map<String,String> getHeaders() {
            return _headers;
        }

        public String getHeader(String name) {
            return (_headers == null) ? null : _headers.get(name);
        }

        public String setHeader(String name, String value) {
            if (_headers == null)
                _headers = new HashMap<String,String>();
            return _headers.put(name, value);
        }

        public byte[] getBinaryData() {
            return _binaryData;
        }

        public void setBinaryData(byte[] binaryData) {
            this._binaryData = binaryData;
        }

        public String getContentType() {
            return _contentType;
        }

        public void setContentType(String contentType) {
            if (contentType.indexOf("charset=") == -1)
                contentType += "; charset=utf-8";
            this._contentType = contentType;
        }

        public Integer getError() {
            return _error;
        }

        public void setError(Integer error) {
            this._error = error;
        }

        public String getErrorMessage() {
            return _errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            _errorMessage = errorMessage;
        }

        public String getRedirect() {
            return _redirect;
        }

        public void setRedirect(String redirect) {
            this._redirect = redirect;
        }

        public byte[] getOutput() {
            if (_writer != null) _writer.flush();
            if (_outputStream != null) _output = _outputStream.toByteArray();
            return _output;
        }

        public ByteArrayOutputStream getOutputStream() {
            if (_outputStream == null) {
                _outputStream = new ResponseOutputStream();
            }
            return _outputStream;
        }

        public PrintWriter getWriter() {
            if (_writer == null) {
                OutputStream out = getOutputStream();
                try {
                    _writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            return _writer;
        }

        public String getBinaryDataFilename() {
            return _binaryDataFilename;
        }

        public void setBinaryDataFilename(String binaryDataFilename) {
            this._binaryDataFilename = binaryDataFilename;
        }

        public void errorOut(int code, String message) {
            PrintWriter out = getWriter();
            setError(code);
            setContentType("text/html");
            out.println("<html><body>");
            out.println("<h1>");
            out.println(message);
            out.println("</h1>");
            out.println("</body></html>");
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            getOutput();
            out.defaultWriteObject();
        }

        private class ResponseOutputStream extends ByteArrayOutputStream {
            @Override
            public void close() throws IOException {
                super.close();
                _output = toByteArray();
            }
        }
    }

    public static HttpRequest requestToRequest(HttpServletRequest req) {
        HttpRequest request = new HttpRequest();
        request.setPath(req.getPathInfo());
        request.setContentType(request.getContentType());
        request.setUserAgent(req.getHeader("User-Agent"));
        request.setMethod(req.getMethod());
        Enumeration<String> headerNames = req.getHeaderNames();
        log.trace(">>>>> Headers:");
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerVal = req.getHeader(headerName);
            request.addHeader(headerName, headerVal);
            if (log.isTraceEnabled())
                log.trace(headerName + ": " + headerVal);
        }
        MultiMap<String,String> parms = new MultiMap<>();
        Enumeration<String> parmEm = req.getParameterNames();
        while (parmEm.hasMoreElements()) {
            String key = parmEm.nextElement();
            String[] vals = req.getParameterValues(key);
            for (String val : vals) {
                parms.put(key, val);
                if (log.isTraceEnabled())
                    log.trace(">>>>>>" + key + ":" + val + "<<<<<<");
            }
        }
        request.setParameters(parms);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            drainInputStream(req.getInputStream(), baos);
            request.setBody(baos.toByteArray());
        } catch (IOException e) {
            log.warn("Error reading request body: " + e.getMessage(), e);
        }

        request.setRemoteAddr(req.getRemoteAddr());
        request.setRequestURL(req.getRequestURL().toString());
        request.setServerName(req.getServerName());
        return request;
    }

    public static void responseToResponse(HttpResponse response, HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (response == null)
            return;

        if (response.getError() != null) {
            if (response.getErrorMessage() != null)
                res.sendError(response.getError(), response.getErrorMessage());
            else
                res.setStatus(response.getError());
            return;
        }

        if (response.getRedirect() != null) {
            res.sendRedirect(response.getRedirect());
            return;
        }

        if (response.getContentType() != null)
            res.setContentType(response.getContentType());

        if (response.getBinaryData() != null) {
            String responseFilename = response.getBinaryDataFilename();
            if (responseFilename != null) {
                res.setHeader("Content-Disposition", "attachment; filename=" + responseFilename);
            }
            res.setContentType(response.getContentType());
            res.getOutputStream().write(response.getBinaryData());
            return;
        }

        byte[] output = response.getOutput();
        if (output != null) {
            res.getOutputStream().write(output);
            return;
        }
    }

    /**
     * Utility method that "drains" an {@link InputStream} into an {@link OutputStream}
     *
     * @param in   the stream to drain
     * @param out  the stream to fill
     * @throws IOException
     */
    static void drainInputStream(InputStream in, OutputStream out) throws IOException {
        if (in.available() == 0)
            return;
        byte[] procBytes = new byte[4096];
        int bytesRead = 0;
        while (true) {
            try {
                bytesRead = in.read(procBytes);
            } catch (EOFException e) {
                break;
            }
            if (bytesRead == -1) break;
            out.write(procBytes, 0, bytesRead);
        }
    }
}