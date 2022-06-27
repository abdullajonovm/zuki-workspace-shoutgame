package com.meinc.commons.domain;

import java.io.Serializable;

/**
 * @author shawker
 */
public class AttachmentTypes implements Serializable
{
  private static final long serialVersionUID = 8214578203274206351L;
  
  public static final String WAV = "wav";
  public static final String MP3 = "mp3";
  public static final String TXT = "txt";
  
  public AttachmentTypes()
  {
  }
  
  public static String getMimeType(String attachmentType)
  {
    if (WAV.equals(attachmentType))
      return "audio/x-wav";
    else if (MP3.equals(attachmentType))
    	return "audio/x-mp3";
    else
      return "text/plain";
  }
  
  public static String getAttachmentTypeFromMimetype(String mimeType)
  {
  	if ("audio/x-wav".equals(mimeType))
  			return WAV;
  	else if ("audio/x-mp3".equals(mimeType))
  		return MP3;
  	else
  		return TXT;
  }
}
