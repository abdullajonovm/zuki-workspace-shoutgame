package tv.shout.util;

import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class StringUtil
{
    public static <T> void tos(List<T> list)
    {
        if (list == null || list.size() == 0) {
            System.out.println("no records");
        } else {
            //for (T obj : list) { System.out.println(obj); }
            list.stream().forEach(System.out::println);
        }
    }

    public static boolean isEmpty(String val)
    {
        return val == null || val.trim().length() == 0 || val.trim().toLowerCase().equals("null");
    }

    //https://www.journaldev.com/1237/java-convert-string-to-xml-document-and-xml-document-to-string
    public static Document convertStringToDocument(String xmlStr)
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try
        {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse( new InputSource( new StringReader( xmlStr ) ) );
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //this assumes there is only 1 node of a given name being searched for. if there is/can be more than 1, don't use this method
    public static String getNodeValue(Document doc, String nodeName)
    {
        NodeList nList = doc.getElementsByTagName(nodeName);
        Node node = nList.item(0);
        return node.getTextContent();
    }

}
