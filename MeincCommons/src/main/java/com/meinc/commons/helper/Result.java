package com.meinc.commons.helper;

import java.io.Serializable;

/**
 * Contains the result of performing a command.
 * 
 * @author shawker
 */
public class Result implements Serializable
{
  private static final long serialVersionUID = 8113460783921623775L;
  
  private int _code;
  private String _data;
  private String _message;
  private byte[] _binaryData;
  private String _binaryDataFilename;
  private String _mimeType;
  private Object associatedObject;
  private String _redirectUrl = null;
  
	//return codes
	public static final int CODE_SUCCESS = 0;
	public static final int CODE_UNKNOWN_COMMAND = 2;
	public static final int CODE_INVALID_ARGUMENTS = 3;
	public static final int CODE_EXCEPTION = 4;
	public static final int CODE_NOT_AUTHENTICATED = 5;
	public static final int CODE_NOT_AUTHORIZED = 6;
	public static final int CODE_APP_EXCEPTION = 7;
	public static final int CODE_UNSUPPORTED_VERSION = 8;
	public static final int CODE_REQUIRES_PROFILE_UPDATE = 9;
	public static final int CODE_NEW_VERSION_AVAILABLE = 10;
	public static final int CODE_PARTIAL_SUCCESS = 11;
	public static final int CODE_REQUIRES_EULA_ACCEPTANCE = 12;
  public static final int CODE_UNSUPPORTED_APPLICATION = 13;
  
  public Result()
  {
  }
  
  //the default case when all goes well
  public Result(String data)
  {
    this(Result.CODE_SUCCESS, null, data);
  }
  
  public Result(int code)
  {
  	this(code, null, null);
  }
  
  public Result(int code, String message, String data)
  {
    setCode(code);
    setData(data);
    setMessage(message);
  }
  
  public int getCode()
  {
    return _code;
  }
  public void setCode(int code)
  {
    _code = code;
  }
  public String getData()
  {
    return _data;
  }
  public void setData(String data)
  {
    _data = data;
  }
  public String getMessage()
  {
    return _message;
  }
  public void setMessage(String message)
  {
    _message = message;
  }

	public byte[] getBinaryData()
	{
		return _binaryData;
	}

	public void setBinaryData(byte[] binaryData)
	{
		_binaryData = binaryData;
	}

	public String getMimeType()
	{
		return _mimeType;
	}

	public void setMimeType(String mimeType)
	{
		_mimeType = mimeType;
	}

	public Object getAssociatedObject() {
		return associatedObject;
	}

	public void setAssociatedObject(Object associatedObject) {
		this.associatedObject = associatedObject;
	}

  public String getBinaryDataFilename() {
    return _binaryDataFilename;
  }

  public void setBinaryDataFilename(String binaryDataFilename) {
    _binaryDataFilename = binaryDataFilename;
  }
  
  public void setRedirect(String redirectUrl)
  {
  	_redirectUrl = redirectUrl;
  }
  
  public String getRedirectUrl()
  {
  	return _redirectUrl;
  }
}
