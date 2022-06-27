package com.meinc.http.domain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse implements Serializable {
    private static final long serialVersionUID = -9051672809276129463L;
  
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
