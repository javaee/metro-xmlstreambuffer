package com.sun.xml.stream.buffer.stax;

import com.sun.xml.stream.buffer.XMLStreamBuffer;
import java.io.StringReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class OverrideNamespaceTest extends TestCase {
    
    public OverrideNamespaceTest(String testName) {
        super(testName);
    }

    public void testOverrideNamespace() throws Exception {
        String[] namespaces = {"http://wsdl", "http://types", "http://wsdl"};
        
        String str = "<tns:definitions xmlns:tns='http://wsdl'><tns:types xmlns:tns='http://types'/><tns:binding/></tns:definitions>";
        XMLStreamReader rdr = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(str));
        XMLStreamBuffer xsb = XMLStreamBuffer.createNewBufferFromXMLStreamReader(rdr);
        XMLStreamReader xsbrdr = xsb.readAsXMLStreamReader();
        int i = 0;
        while(xsbrdr.hasNext()) {
            if (xsbrdr.next() == XMLStreamReader.START_ELEMENT) {
                assertEquals("Element " + xsbrdr.getNamespaceURI("tns") + " has wrong namespace", namespaces[i++], xsbrdr.getNamespaceURI("tns"));
            }
        }
    }
}
