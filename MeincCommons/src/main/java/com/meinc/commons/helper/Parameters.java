package com.meinc.commons.helper;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;

/**
 * Wrapper that will contain name/value pairs, with several convenience methods to grab data
 * in certain formats.
 *
 * @author shawker
 */
public class Parameters implements Serializable
{
  private static final long serialVersionUID = -3825565504405802757L;

  public  static final String FILEUPLOADS_KEY = "__FILEUPLOADS_KEY__";

	private HashMap<String, List<Object>> _map = new HashMap<String, List<Object>>();

	public Parameters()
	{
	}

  public int size() {
    return _map.size();
  }

	/**
	 * Put a key/value in the param list.  If the key already exists, store the new value in
	 * a list with the old value so no data is lost.
	 *
	 * @param key
	 * @param value
	 */
	public void put(String key, Object value)
	{
		List<Object> o = _map.get(key);
		if (o != null) //existing key; append the value
		{
			o.add(value);
		}
		else //new key
		{
			o = new ArrayList<Object>();
			o.add(value);
			_map.put(key, o);
		}
	}

	/**
	 * Put a key/value in the parm list, overriding the value if the key already exists.
	 *
	 * @param key
	 * @param value
	 */
	public void putOverride(String key, Object value)
	{
		List<Object> o = new ArrayList<Object>();
		o.add(value);
		_map.put(key, o);
	}

	public String getString(String key)
	{
		List<Object> o = _map.get(key);
		if (o == null)
			return null;
		else
		{
			String s = o.get(0).toString();
      if (s.trim().length() == 0 ||
          s.trim().equalsIgnoreCase("null"))
        return null;
      else
      	return s;
		}
	}
	
	public int getIntUnchecked(String key) throws IllegalArgumentException, NumberFormatException {
	    List<Object> o = _map.get(key);
	    if (o == null)
	        throw new IllegalArgumentException("null value");
	    return StringUtils.convertStringToIntUnchecked(o.get(0).toString());
	}

	public int getInt(String key)
	{
		List<Object> o = _map.get(key);
		if (o == null)
			return -1;
		else
			return StringUtils.convertStringToInt(o.get(0).toString());
	}

	public long getLong(String key)
	{
		List<Object> o = _map.get(key);
		if (o == null)
			return -1l;
		else
			return StringUtils.convertStringToLong(o.get(0).toString());
	}

	public boolean getBoolean(String key)
	{
		List<Object> o = _map.get(key);
		if (o == null)
			return false;
		else {
			if (o.get(0) instanceof Boolean)
				return (Boolean) o.get(0);
			else
				return StringUtils.convertStringToBoolean(o.get(0).toString());
		}
	}

	public Date getDate(String key)
	throws ParseException
	{
		List<Object> o = _map.get(key);
		if (o == null)
			return null;
		else
		{
	    Date date=null;
			String sDate = o.get(0).toString();
			if (sDate != null)
			  sDate = sDate.replaceAll(" ", "+");
      date = StringUtils.iso8601ToDate(sDate);
	    return date;
		}
	}

	public List<Object> getList(String key)
	{
		return _map.get(key);
	}

	public Document getXml(String key)
	{
		Document doc;
		List<Object> o = _map.get(key);
		if (o == null || o.get(0).toString().trim().length() == 0)
			return null;
		else
		{
      try {
      	doc = StringUtils.getXmlFromString(o.get(0).toString());
  			return doc;
      } catch (Exception e) {
        System.out.println(">>>>>>> XML EXCEPTION <<<<<<<<");
        e.printStackTrace();
        return null;
      }
		}
	}

	public Object getObject(String key)
	{
		List<Object> o = _map.get(key);
		if (o == null)
			return null;
		else
			return o.get(0);
	}

	public List<String> getKeys()
	{
		Set<String> set = _map.keySet();
		String[] keys = (String[]) set.toArray(new String[set.size()]);
		return Arrays.asList(keys);
	}

	public void remove(String key)
	{
		_map.remove(key);
	}

	public String toString()
	{
		StringBuilder buf = new StringBuilder();

		Iterator<String> it = _map.keySet().iterator();
		String key;
		Object value;
		while (it.hasNext())
		{
			key = it.next();
			value = _map.get(key);
			if ("password".equals(key))
			{
				buf.append(key);
				buf.append(": ***\n");
			}
      else if (value.toString().indexOf("password:") != -1 && value.toString().indexOf("|") != -1)
      {
      	String s = value.toString();
      	int b = s.indexOf("password:");
      	int e = s.indexOf("|", b);
      	buf.append(key);
      	buf.append(": ");
      	buf.append(s.substring(0, b+9));
      	buf.append("**");
      	buf.append(s.substring(e));
      	buf.append("\n");
      }
			else if ("response".equals(key))
				;
			else
			{
				buf.append(key);
				buf.append(": ");
				buf.append(value.toString());
				buf.append("\n");
			}
		}

		return buf.toString();
	}

	/**
	 * Determine if the given string is a wellformed xml doc
	 * @param s
	 * @return
	 */
/*
	private boolean isEmbeddedXml(String s)
	{
		if (s.startsWith("<") && s.endsWith(">")) //possible xml doc
		{
			try {
				SAXReader reader = new SAXReader();
				reader.read(new StringReader(s));
				return true;

			} catch (DocumentException e) {
				return false;
			}
		}
		else
			return false;
	}
*/

/**
	public static void main(String[] args)
	{
		try {
			Parameters p = new Parameters();
			p.put("CreateSubscriber", "email:grant@zayda.com|beamedfromemail:|name:Grant Robinson|password:hi|phone:8018675309|phone_id:123456789|carrier:16|country:USA");
			p.put("target_service_version", "2.0");
			p.put("CMD", "1");
			p.put("app_version", "2.23");

			System.out.println(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
/**/
}
