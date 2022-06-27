package com.meinc.commons.domain;

import java.io.Serializable;

/**
 * Created this class to hold the byte data for an image.
 */
public class ImageByteArray implements Serializable
{
  private static final long serialVersionUID = -4506807015204525100L;
  
  private byte[] _data;
  
  public ImageByteArray()
  {  	
  }
  
  public ImageByteArray(byte[] data)
  {
  	_data = data;
  }

	public byte[] getData()
	{
		return _data;
	}

	public void setData(byte[] data)
	{
		_data = data;
	}
}
