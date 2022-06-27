package com.meinc.gameplay.domain;

import java.io.Serializable;

public class Bucket 
implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7309611561510521326L;
	private String _name;
	
	public Bucket(String name)
	{
		_name = name;
	}

	public String getName() 
	{
		return _name;
	}
	
	//TODO
}
