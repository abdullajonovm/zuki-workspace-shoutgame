package com.meinc.commons.domain;

import java.io.Serializable;
import java.util.Date;

public class Album
implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String _albumType;
	private String _albumId;
	private int _ownerId;
	private String _albumName;
	private Date _createDate;
	
	public Album()
	{
	}
	
	public Album(String albumId, String albumType)
	{
		_albumId = albumId;
		_albumType = albumType;
	}

	public String getAlbumType()
	{
		return _albumType;
	}

	public void setAlbumType(String albumType)
	{
		_albumType = albumType;
	}

	public String getAlbumId()
	{
		return _albumId;
	}

	public void setAlbumId(String albumId)
	{
		_albumId = albumId;
	}

	public int getOwnerId()
	{
		return _ownerId;
	}

	public void setOwnerId(int ownerId)
	{
		_ownerId = ownerId;
	}

	public String getAlbumName()
	{
		return _albumName;
	}

	public void setAlbumName(String albumName)
	{
		_albumName = albumName;
	}

	public Date getCreateDate()
	{
		return _createDate;
	}

	public void setCreateDate(Date createDate)
	{
		_createDate = createDate;
	}
	
}
