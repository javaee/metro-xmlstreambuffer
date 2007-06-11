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
        String[] startElement = {"definitions", "types", "binding"};
        String[] startElement_namespaces = {"http://wsdl", "http://types", "http://wsdl"};
        String[] endElement = {"types", "binding", "definitions"};
        String[] endElement_namespaces = {"http://types", "http://wsdl", "http://wsdl"};
        
        String str = "<tns:definitions xmlns:tns='http://wsdl'><tns:types xmlns:tns='http://types'/><tns:binding/></tns:definitions>";
        XMLStreamReader rdr = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(str));
        XMLStreamBuffer xsb = XMLStreamBuffer.createNewBufferFromXMLStreamReader(rdr);
        XMLStreamReader xsbrdr = xsb.readAsXMLStreamReader();
        int i = 0;
        int j = 0;
        while(xsbrdr.hasNext()) {
            int event = xsbrdr.next();
            if (event == XMLStreamReader.START_ELEMENT) {
                assertEquals("Start of Element " + xsbrdr.getName() + " has wrong namespace", 
                        startElement_namespaces[i], xsbrdr.getNamespaceURI("tns"));
                assertEquals(startElement[i++], xsbrdr.getLocalName());
            } else if (event == XMLStreamReader.END_ELEMENT) {
                assertEquals("End of Element " + xsbrdr.getName() + " has wrong namespace", 
                        endElement_namespaces[j], xsbrdr.getNamespaceURI("tns"));
                assertEquals(endElement[j++], xsbrdr.getLocalName());
            }
        }
    }
}
