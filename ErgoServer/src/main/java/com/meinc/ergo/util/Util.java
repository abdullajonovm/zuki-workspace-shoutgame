package com.meinc.ergo.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Util
{
    public static enum LOG_DIRECTION {IN, OUT, INOUT}
    
    private static Logger LOG = Logger.getLogger("ergoactivity");
//    private static final Logger logger = Logger.getLogger(Util.class);
    
    private static XPath xPath = XPathFactory.newInstance().newXPath();
    private static final SimpleDateFormat FORMAT_ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private static Pattern SENTENCE_CAP = Pattern.compile("([\\?!\\.]\\s*)([a-z])");
    
    /**
     * An action logger to record "what".
     * 
     * @param action the action being formed (SYNC, LOGIN, ADDTASK, etc..)
     * @param message the message to display
     * @param args the arguments to replace within the message  (MessageFormat.format ...)
     */
    public static void log(String action, LOG_DIRECTION dir, String message, Object ... args)
    {
        if (LOG.isDebugEnabled()) {
            String dirArrow;
            switch (dir)
            {
                case OUT:
                    dirArrow = " <- ";
                    break;
                    
                case IN:
                    dirArrow = " -> ";
                    break;

                case INOUT:
                default:
                    dirArrow = " <-> ";
                    break;
            }
            LOG.debug(MessageFormat.format(action + dirArrow + message, args));
        }
    }
    
    //http://my.opera.com/karmazilla/blog/show.dml/693481
    @SuppressWarnings("unchecked")
    public static <T> T clone(T obj)
    {
        T theClone = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);

            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            theClone = (T) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return theClone;
    }
    
    public static Document stringToXml(String xml) 
    throws SAXException, IOException, ParserConfigurationException 
    {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        //domFactory.setNamespaceAware(true); 
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }
    
//    public static String xmlToString(Document doc) 
//    throws TransformerException
//    {
//        TransformerFactory tf = TransformerFactory.newInstance();
//        Transformer transformer = tf.newTransformer();
//        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//        StringWriter writer = new StringWriter();
//        transformer.transform(new DOMSource(doc), new StreamResult(writer));
//        String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
//        return output;
//    }
    
//    public static String evalXPath(String xpath, Document doc) 
//    throws XPathExpressionException 
//    {
//        return xPath.evaluate(xpath, doc);
//    }
    
    public static Node evalXPath(String xpath, Document doc)
    throws XPathExpressionException
    {
        XPathExpression expr = xPath.compile(xpath);
        return (Node) expr.evaluate(doc, XPathConstants.NODE);
    }
    
    public static NodeList getNodesViaXPath(String xpath, Document doc) 
    throws XPathExpressionException
    {
        XPathExpression expr = xPath.compile(xpath);
        return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
    }
    
    public static String dateToIso8601(Date date)
    {
        if (date == null)
            return "";
        String result = FORMAT_ISO8601.format(date);

        result = result.substring(0, result.length() - 2) + ":" + result.substring(result.length() - 2);

        return result;
    }
    
    public static Date Iso8601ToDate(String date) 
    throws ParseException
    {
        if (date == null || date.trim().length() == 0 || date.trim().toLowerCase().equals("null"))
            return null;

        //see if there is any "partial second value" (i.e. ms/ns/us), and if so, strip it off
        int idx = date.lastIndexOf(".");
        int endIdx = date.length();
        if (idx != -1) {
            for (int i=idx+1; i<date.length(); i++) {
                if (!Character.isDigit(date.charAt(i))) {
                    endIdx = i;
                    break;
                }
            }
            String newStr = date.substring(0, idx) + date.substring(endIdx, date.length());
            date = newStr;
        }
        
        // NOTE: SimpleDateFormat uses GMT[-+]hh:mm for the TZ which breaks
        // things a bit. Before we go on we have to repair this.

        // this is zero time so we need to add that TZ indicator for
        if (date.endsWith("Z")) {
            date = date.substring(0, date.length() - 1) + "GMT-00:00";
        } else {
            int inset = 6;

            String s0 = date.substring(0, date.length() - inset);
            String s1 = date.substring(date.length() - inset, date.length());

            date = s0 + "GMT" + s1;
        }

        return FORMAT_ISO8601.parse(date);
    }
    
    //http://tech.chitgoks.com/2010/09/24/capitalize-first-letter-of-every-sentence-in-java/
    public static String capitalizeFirstLetterInEverySentence(String content)
    {
        Matcher m = SENTENCE_CAP.matcher(content);
        while (m.find()) {
            content = m.replaceFirst(m.group(1) + m.group(2).toUpperCase());
            m = SENTENCE_CAP.matcher(content);
        }

        // Capitalize the first letter of the string.
        content = String.format("%s%s", Character.toUpperCase(content.charAt(0)), content.substring(1));

        return content;
    }
    
    /**
     * When a note is edited using an Outlook client, the client adds a bunch of HTML garbage around all the text.
     * 
     * This method does it's best job to clean it up and get back to a straight html-less string.
     * 
     * @param outlookNoteHtml
     * @return
     */
    public static String sanitizeOutlookNoteHtml(String outlookNoteHtml)
    {
        if (outlookNoteHtml == null) return null;
        
        String sanitizedString;
//logger.debug("sanitizing exchange note:\n" + outlookNoteHtml);
        outlookNoteHtml = outlookNoteHtml.replaceAll("<br> ", "~~~<br>");
        outlookNoteHtml = outlookNoteHtml.replaceAll("<br>", "~~~<br>");
        sanitizedString = Jsoup.clean(outlookNoteHtml, Whitelist.simpleText());
        sanitizedString = sanitizedString.replaceAll("~~~~~~", "~~~");
        sanitizedString = sanitizedString.replaceAll("~~~ ", "\n");
        sanitizedString = sanitizedString.replaceAll("~~~", "\n");
//        sanitizedString = sanitizedString.replaceAll("&nbsp;", " ");
//        sanitizedString = sanitizedString.replaceAll("&lt;", "<");
//        sanitizedString = sanitizedString.replaceAll("&gt;", ">");
        sanitizedString = sanitizedString.replaceAll("<b>", "");
        sanitizedString = sanitizedString.replaceAll("</b>", "");
        sanitizedString = sanitizedString.replaceAll("<u>", "");
        sanitizedString = sanitizedString.replaceAll("</u>", "");
        
        //the friendly html library above converts things like ö to &ouml;  ... this will change them back to the unescaped values
        sanitizedString = StringEscapeUtils.unescapeHtml(sanitizedString);
//logger.info("sanitized note:\n" + sanitizedString);
        
        return sanitizedString;
    }
    
    /**
     * Given a timestamp, see if it needs to be adjusted based on the timezone's of where it was created and
     * where it's currently being viewed.  This will allow a "date that doesn't float, regardless of where 
     * it's viewed".
     * 
     * @param originalTime
     * @param originalTimezoneId
     * @param currentTimezoneId
     * @return
     */
    public static long getStickyDate(long originalTime, String originalTimezoneId, String currentTimezoneId)
    {
        if (originalTimezoneId.equals(currentTimezoneId))
            return originalTime;
        
        TimeZone originalTz = TimeZone.getTimeZone(originalTimezoneId);
        TimeZone currentTz = TimeZone.getTimeZone(currentTimezoneId);
        
        int o = originalTz.getDSTSavings() + originalTz.getRawOffset();
        int c = currentTz.getDSTSavings() + currentTz.getRawOffset();
        
        int diff;
        if (o > c) {
            diff = o-c;
        } else {
            diff = c-o;
            diff = -diff;
        }
        
        return originalTime + diff;
    }
    
//    public static void main(String[] args)
//    {
//        String note = "This is a &lt;note&gt; with å, ä, and ö"; 
//        
//        try {
//            String s1 = Util.sanitizeOutlookNoteHtml(note);
//            String s2 = StringEscapeUtils.unescapeHtml(s1);
//            System.out.println(s1);
//            System.out.println(s2);
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    
}
