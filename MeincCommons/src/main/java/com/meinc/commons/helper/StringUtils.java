package com.meinc.commons.helper;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class StringUtils
{
	private static final SimpleDateFormat FORMAT_ISO8601 = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");

	/**
	 * Turn a string into an xml document.
	 *
	 * @param s
	 * @return
	 * @throws Exception
	 */
	public static Document getXmlFromString(String s)
	throws Exception
	{
		Document doc;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		ByteArrayInputStream bis = new ByteArrayInputStream(s.getBytes());
		doc = docBuilder.parse(bis);
		bis.close();
		return doc;
	}

	public static String dateToIso8601(Date date) {
		if (date == null)
			return "";
		String result = FORMAT_ISO8601.format(date);

		result = result.substring(0, result.length() - 2) + ":"
				+ result.substring(result.length() - 2);

		return result;
	}

	public static Date iso8601ToDate(String str) throws ParseException {
		if (str == null || str.trim().length() == 0
				|| str.trim().toLowerCase().equals("null"))
			return null;

		//since the iso8601 specifies a : in the Time Zone, but java doesn't have one,
		// remove it before parsing
		str = str.substring(0, str.length() - 3)
				+ str.substring(str.length() - 2);

		return FORMAT_ISO8601.parse(str);
	}

	/**
	 * Will return a string value into an int value; if there was an
	 * exception or the value was null, it will return -1.
	 *
	 * @param input
	 * @return
	 */
	public static int convertStringToInt(String input) {
		try {
			if (input == null)
				return -1;
			else if (input.trim().length() == 0)
				return -1;

			return new Integer(input).intValue();
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			return -1;
		}
	}
	
	public static int convertStringToIntUnchecked(String input) throws IllegalArgumentException, NumberFormatException {
        if (input == null) {
            throw new IllegalArgumentException("null value");
        }
        if (input.trim().length() == 0) {
            throw new IllegalArgumentException("empty string");
        }
        return new Integer(input);
	}

	/**
	 * Will return a string value into a long value; if there was an
	 * exception or the value was null, it will return -1.
	 *
	 * @param input
	 * @return
	 */
	public static long convertStringToLong(String input) {
		try {
			if (input == null)
				return -1l;
			else if (input.trim().length() == 0)
				return -1l;

			return new Long(input).longValue();
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			return -1l;
		}
	}

	/**
	 * Will return a string value into a boolean value; if there was an
	 * exception or the value was null, it will return false.  It assumes
	 * that the string input is a "0" or a "1" or "true" or "false".
	 *
	 * @param input
	 * @return
	 */
	public static boolean convertStringToBoolean(String input) {
		try {
			if (input == null)
				return false;
			else if (input.trim().length() == 0)
				return false;

			if ("true".equals(input.toLowerCase()))
				return true;
			else if ("false".equals(input.toLowerCase()))
				return false;
			else
				return new Integer(input).intValue() == 1 ? true : false;
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			return false;
		}
	}

	/**
	 * Since the input format for phone numbers comes in all varieties,
	 * this will fix it up.  Basically it simply strips out non-numeric
	 * characters at this time (it will leave any + characters).
	 *
	 * @param phone
	 * @return
	 */
	public static String formatPhone(String phone) {
		if (phone == null)
			return "";
		StringBuilder buf = new StringBuilder();

		char[] chars = phone.toCharArray();
		int i, count = chars.length;
		for (i = 0; i < count; i++) {
			if (Character.isDigit(chars[i]) || chars[i] == '+')
				buf.append(chars[i]);
		}

		return buf.toString();
	}

	/**
	 * Strip out all "url unfriendly" characters from a string.  This means the following characters:
	 * <ul>
	 *   <li>$</li>
	 *   <li>&</li>
	 *   <li>+</li>
	 *   <li>,</li>
	 *   <li>/</li>
	 *   <li>:</li>
	 *   <li>;</li>
	 *   <li>=</li>
	 *   <li>?</li>
	 *   <li>@</li>
	 *   <li> </li>
	 *   <li>"</li>
	 *   <li>&lt;</li>
	 *   <li>&gt;</li>
	 *   <li>#</li>
	 *   <li>%</li>
	 *   <li>{</li>
	 *   <li>}</li>
	 *   <li>|</li>
	 *   <li>\</li>
	 *   <li>^</li>
	 *   <li>~</li>
	 *   <li>[</li>
	 *   <li>]</li>
	 *   <li>`</li>
	 * </ul>
	 * This list was garnered from: http://www.blooberry.com/indexdot/html/topics/urlencoding.htm
	 *
	 * @param input
	 * @return
	 */
	public static String stripUrlUnfriendlyCharacters(String input)
	{
		if (input == null) return null;
		StringBuilder buf = new StringBuilder();

		char[] chars = input.toCharArray();
		int i, count = chars.length;
		for (i = 0; i < count; i++)
		{
			switch (chars[i])
			{
				//do nothing if it's an unsafe character
				case '$':
				case '&':
				case '+':
				case ',':
				case '/':
				case ':':
				case ';':
				case '=':
				case '?':
				case '@':
				case ' ':
				case '"':
				case '<':
				case '>':
				case '#':
				case '%':
				case '{':
				case '}':
				case '|':
				case '\\':
				case '^':
				case '~':
				case '[':
				case ']':
				case '`':
				break;

				default: //a "safe" character
					buf.append(chars[i]);
			}
		}

		return buf.toString();
	}

	/**
	 * Take any number of input strings and chop it all up into a list of individual words.
	 * Duplicate words are ignored.  Case is ignored (uses lowercase).
	 *
	 * @param input
	 * @return
	 */
	public static List<String> splitIntoKeywords(String ...input)
	{
		String[] words;
		Map<String, String> keywords = new HashMap<String, String>();
		for (String s : input)
		{
			if (s != null)
			{
				s = s.toLowerCase();
				words = s.split("\\s"); //split on whitespace
				for (String word : words)
				{
					keywords.put(word, word);
				}
			}
		}

		return new ArrayList<String>(keywords.values());
	}

	/**
	 * Take the input and make sure it's formatted appropriately for an email subject.
	 *
	 * @param prepend any text to prepend to the subject (ex: "Shout Postcard from ")
	 * @param title
	 * @param fromName
	 * @return
	 */
	public static String getEmailSubject(String prepend, String title, String fromName)
	{
		final int LENGTH = 30;
		if (title == null) title = "";
		StringBuilder emailSubjectBuf = new StringBuilder();
		emailSubjectBuf.append(prepend);
		emailSubjectBuf.append(fromName);
		if (title != null)
		{
			emailSubjectBuf.append(" - ");
			if (title.length() > LENGTH)
			{
				emailSubjectBuf.append(title.substring(0, LENGTH));
				emailSubjectBuf.append("...");
			}
			else
				emailSubjectBuf.append(title);
		}
		String candidateEmailSubject = emailSubjectBuf.toString();

		//IF the subject happens to contain two newlines, when the email is sent, it will interpret that
		// as the end of the email header, and it will mess up the resulting email.  Therefore, check
		// for any newlines that might be in the subject and strip them out
		return candidateEmailSubject.replaceAll("\n", " ");
	}

/**
	public static void main(String[] args)
	{
		try {
			String subject = null;
			String message = "This is a test message and i would just like to say that I love to see a good test";
			System.out.println(
					StringUtils.splitIntoKeywords(message, subject));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
/**/
}
