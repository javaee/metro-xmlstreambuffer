package com.sun.xml.stream.buffer.stax;

import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.stream.buffer.XMLStreamBufferMark;
import junit.framework.TestCase;

import javax.xml.stream.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rama.pulavarthi@sun.com
 * @author Jitendra Kotamraju
 */
public class InscopeNamespaceTest extends TestCase {

    public InscopeNamespaceTest(String testName) {
        super(testName);
    }

    /**
     * In this test, the namespace xmlns:user='http://foo.bar' is declared on top-level
     * element and child uses it but doesn't declare it
     * @throws Exception
     */
    public void testXMLStreamBuffer() throws Exception {
    	String requestStr =
                "<S:Header xmlns:user='http://foo.bar' xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>" +
                "<user:foo>bar</user:foo>" +
                "</S:Header>";
        XMLStreamReader reader2 = getReader(requestStr);
        reader2.next();// go to start element
        assertEquals(reader2.getNamespaceURI(),"http://foo.bar");
    }

    /**
     * In this test, the namespace xmlns:user='http://foo.bar' is declared on top-level element as well as the child 
     * @throws Exception
     */
    public void testXMLStreamBuffer1() throws Exception {
    	String requestStr =
                "<S:Header xmlns:user='http://foo.bar' xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>" +
                "<user:foo xmlns:user='http://foo.bar'>bar</user:foo>" +
                "</S:Header>";
        XMLStreamReader reader2 = getReader(requestStr);
        reader2.next();// go to start element
        assertEquals(reader2.getNamespaceURI(),"http://foo.bar");
    }

    /**
     * In this test, the namespace xmlns:user='http://foo.bar' is declared
     *  on top-level element and child uses same prefix different ns
     * @throws Exception
     */
    public void testXMLStreamBuffer2() throws Exception {
    	String requestStr =
                "<S:Header xmlns:user='http://foo.bar' xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>" +
                "<user:foo xmlns:user='http://foo1.bar1'>bar</user:foo>" +
                "</S:Header>";
        XMLStreamReader reader2 = getReader(requestStr);
        reader2.next();// go to start element
        assertEquals(reader2.getNamespaceURI(),"http://foo1.bar1");
    }

    public void testXMLStreamBuffer3() throws Exception {
    	String requestStr =
                "<Header xmlns='http://foo.bar' >" +
                "<user>bar</user>" +
                "<Header>";
        XMLStreamReader reader2 = getReader(requestStr);
        reader2.next();// go to start element
        assertEquals(reader2.getNamespaceURI(),"http://foo.bar");
    }

    public void testXMLStreamBuffer4() throws Exception {
    	String requestStr =
                "<Header xmlns='http://foo.bar' >" +
                "<user xmlns='http://foo1.bar1'>bar</user>" +
                "<Header>";
        XMLStreamReader reader2 = getReader(requestStr);
        reader2.next();// go to start element
        assertEquals(reader2.getNamespaceURI(),"http://foo1.bar1");
    }

    // returned reader is placed at the first child
    private XMLStreamReader getReader(String requestStr) throws Exception {
        XMLStreamReader reader = createXMLStreamReader(new ByteArrayInputStream(requestStr.getBytes()));
        reader.next();// go to start element: S:Header
        // Collect namespaces on soap:Header
        Map<String,String> namespaces = new HashMap<String,String>();
        for(int i=0; i< reader.getNamespaceCount();i++){
            namespaces.put(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
        }

        while(true) {
            reader.next();
            if(reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                break;
        }
        MutableXMLStreamBuffer buffer = new MutableXMLStreamBuffer();
        StreamReaderBufferCreator creator = new StreamReaderBufferCreator();
        creator.setXMLStreamBuffer(buffer);
        // Mark
        XMLStreamBuffer mark = new XMLStreamBufferMark(namespaces, creator);
        // Cache the header block
        // After caching Reader will be positioned at next header block or
        // the end of the </soap:header>
        creator.createElementFragment(reader, true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLStreamWriter writer = createXMLStreamWriter(baos);
        writer.writeStartDocument();
        if(mark.getInscopeNamespaces().size() > 0)
            mark.writeToXMLStreamWriter(writer,true);
        else
            mark.writeToXMLStreamWriter(writer);
        writer.writeEndDocument();
        writer.flush();

        //baos.writeTo(System.out);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        return createXMLStreamReader(bais);
    }

    private XMLStreamReader createXMLStreamReader(InputStream is) throws Exception {
        XMLInputFactory readerFactory = XMLInputFactory.newInstance();
        return readerFactory.createXMLStreamReader(is);
    }

    private XMLStreamWriter createXMLStreamWriter(OutputStream os) throws Exception {
        XMLOutputFactory writerFactory = XMLOutputFactory.newInstance();
        return writerFactory.createXMLStreamWriter(os);
    }

}