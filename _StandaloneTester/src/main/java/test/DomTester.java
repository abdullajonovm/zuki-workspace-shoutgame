package test;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class DomTester
{
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

    public static void main(String[] args)
    {
        String xmlStr = "<?xml version=\"1.0\" encoding=\"utf-8\"?><DraftResult xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"CheckProcessing\">  <Result>89</Result>  <ResultDescription>Error DUPLICATE CHECK (check_ID's:2638695)</ResultDescription>  <VerifyResult>5</VerifyResult>  <VerifyResultDescription>Check Not Accepted.</VerifyResultDescription></DraftResult>";
        Document doc = convertStringToDocument(xmlStr);
        //System.out.println(xml);
        System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

        System.out.println("Result: " + getNodeValue(doc, "Result") + ": " + getNodeValue(doc, "ResultDescription"));
        System.out.println("VerifyResult: " + getNodeValue(doc, "VerifyResult") + ": " + getNodeValue(doc, "VerifyResultDescription"));
    }

}
