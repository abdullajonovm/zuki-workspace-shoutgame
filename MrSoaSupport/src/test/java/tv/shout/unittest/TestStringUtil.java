package tv.shout.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import tv.shout.util.StringUtil;

public class TestStringUtil
{
    @Test
    public void testConvertStringToDocument()
    {
        String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <hickory> <dickory> <value>doc</value> </dickory> </hickory>";
        Document doc = StringUtil.convertStringToDocument(xmlStr);

        assertNotNull(doc);
        assertEquals("hickory", doc.getDocumentElement().getNodeName());
    }

    @Test
    public void testGetNodeValue()
    {
        String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <hickory> <dickory> <value>doc</value> </dickory> </hickory>";
        Document doc = StringUtil.convertStringToDocument(xmlStr);

        assertEquals("doc", StringUtil.getNodeValue(doc, "value"));
    }

    @Test
    public void testIsEmpty()
    {
        assertTrue(StringUtil.isEmpty(null));
        assertTrue(StringUtil.isEmpty(""));
        assertTrue(StringUtil.isEmpty("null"));
        assertTrue(StringUtil.isEmpty("Null"));
        assertTrue(StringUtil.isEmpty("NULL"));
        assertTrue(StringUtil.isEmpty(" null "));

        assertFalse(StringUtil.isEmpty("."));
        assertFalse(StringUtil.isEmpty("_"));
        assertFalse(StringUtil.isEmpty("hello"));
    }
}
