package com.meinc.commons.domain;

import java.io.Serializable;

/**
 * Represents an "extension" to an object.  This allows a base object, 
 * such as "Subscriber" or "Contact" to have additional attributes
 * that are specific to an end-point app to exist without having to
 * add these attributes on the object itself, since no other end-point
 * apps will need the extended attributes.
 *
 * @author shawker
 */
public class Extension 
implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static enum EXTENSION_TYPE {SUBSCRIBER, CONTACT, GROUP}
	
	private String _name;
	private byte[] _value;
	private String _namespaceAlias;
	
	public Extension()
	{
	}
	
	public Extension(String name, byte[] value, String namespaceAlias)
	{
		_name = name;
		_value = value;
		_namespaceAlias = namespaceAlias;
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public byte[] getValue()
	{
		return _value;
	}

	public void setValue(byte[] value)
	{
		_value = value;
	}
	
	public String getNamespaceAlias()
	{
		return _namespaceAlias;
	}

	public void setNamespaceAlias(String namespaceAlias)
	{
		_namespaceAlias = namespaceAlias;
	}

	public static EXTENSION_TYPE getExtensionTypeFromString(String extensionType)
	{
		if (extensionType == null) 
			throw new IllegalArgumentException("invalid extension type: null");
		else if ("SUBSCRIBER".equals(extensionType))
			return EXTENSION_TYPE.SUBSCRIBER;
		else if ("CONTACT".equals(extensionType))
			return EXTENSION_TYPE.CONTACT;
		else if ("GROUP".equals(extensionType))
			return EXTENSION_TYPE.GROUP;
		else
			throw new IllegalArgumentException("invalid extension type: " + extensionType);
	}
	
	public static String getExtensionTypeAsString(EXTENSION_TYPE extensionType)
	{
		if (EXTENSION_TYPE.SUBSCRIBER == extensionType)
			return "SUBSCRIBER";
		else if (EXTENSION_TYPE.CONTACT == extensionType)
			return "CONTACT";
		else if (EXTENSION_TYPE.GROUP == extensionType)
			return "GROUP";
		else
			throw new IllegalArgumentException("invalid extension type: " + extensionType);
	}

}
