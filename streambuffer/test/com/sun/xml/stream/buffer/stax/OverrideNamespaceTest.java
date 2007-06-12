package com.sun.xml.stream.buffer.stax;

import com.sun.xml.stream.buffer.XMLStreamBuffer;
import java.io.StringReader;
import java.util.*;
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

    public void testOverrideNamespace1() throws Exception {
        Map<String, String> ns = new LinkedHashMap<String, String>();
        ns.put("definitions", "http://wsdl");
        ns.put("types", "http://types");
        ns.put("binding", "http://wsdl");
        ns.put("operation", "http://operation");
        ns.put("port", "http://wsdl");

        String str = "<tns:definitions xmlns:tns='http://wsdl'><tns:types xmlns:tns='http://types'/><tns:binding><tns:operation xmlns:tns='http://operation'/></tns:binding><tns:port/></tns:definitions>";

        XMLStreamReader rdr = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(str));
        XMLStreamBuffer xsb = XMLStreamBuffer.createNewBufferFromXMLStreamReader(rdr);
        XMLStreamReader xsbrdr = xsb.readAsXMLStreamReader();
        Iterator<String> nsItr = ns.keySet().iterator();
        while(xsbrdr.hasNext()) {
            int event = xsbrdr.next();
            if (event == XMLStreamReader.START_ELEMENT) {
		        assertEquals("Wrong Start of Element", nsItr.next(), xsbrdr.getLocalName());
                assertEquals("Start of Element " + xsbrdr.getName() + " has wrong namespace", 
                        ns.get(xsbrdr.getLocalName()), xsbrdr.getNamespaceURI("tns"));
            } else if (event == XMLStreamReader.END_ELEMENT) {
                assertEquals("End of Element " + xsbrdr.getName() + " has wrong namespace", 
                        ns.get(xsbrdr.getLocalName()), xsbrdr.getNamespaceURI("tns"));
            }
        }
    }

    public void testNamespaceScope1() throws Exception {
        String str = "<html:html xmlns:html='http://www.w3.org/1999/xhtml'>" +
                "<html:head><html:title>Frobnostication</html:title></html:head>" +
                "<html:body>" +
                "<html:p><html:a href='http://frob.example.com'>here.</html:a></html:p>" +
                "</html:body>" +
                "</html:html>";

        useReaderForTesting(str, "html");
    }

    public void testNamespaceScope2() throws Exception {
        String str =
            "<book xmlns='urn:loc.gov:books' xmlns:isbn='urn:ISBN:0-395-36341-6'>"+
            "<title>Cheaper by the Dozen</title>"+
            "<isbn:number>1568491379</isbn:number>"+
            "<notes>"+
              "<!-- make HTML the default namespace for some commentary -->"+
              "<p xmlns='http://www.w3.org/1999/xhtml'>"+
                  "This is a <i>funny</i> book!"+
              "</p>"+
            "</notes>"+
            "</book>";

        useReaderForTesting(str, "", "isbn");
    }

    public void testNamespaceScope3() throws Exception {
        String str =
            "<Beers>"+
              "<!-- the default namespace inside tables is that of HTML -->"+
              "<table xmlns='http://www.w3.org/1999/xhtml'>"+
               "<th><td>Name</td><td>Origin</td><td>Description</td></th>"+
               "<tr>"+
                 "<!-- no default namespace inside table cells -->"+
                 "<td><brandName xmlns=''>Huntsman</brandName></td>"+
                 "<td><origin xmlns=''>Bath, UK</origin></td>"+
                 "<td>"+
                   "<details xmlns=''><class>Bitter</class><hop>Fuggles</hop>"+
                     "<pro>Wonderful hop, light alcohol, good summer beer</pro>"+
                     "<con>Fragile; excessive variance pub to pub</con>"+
                     "</details>"+
                    "</td>"+
                  "</tr>"+
                "</table>"+
              "</Beers>";

        useReaderForTesting(str, "");
    }


    private void useReaderForTesting(String str, String ... prefixes) throws Exception {

        XMLStreamReader rdr = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(str));
        XMLStreamBuffer xsb = XMLStreamBuffer.createNewBufferFromXMLStreamReader(rdr);
        XMLStreamReader xsbrdr = xsb.readAsXMLStreamReader();
        rdr = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(str));

        while(rdr.hasNext()) {
            assertTrue(xsbrdr.hasNext());
            int expected = rdr.next();
            int actual = xsbrdr.next();
            assertEquals(expected, actual);
            if (expected == XMLStreamReader.START_ELEMENT || expected == XMLStreamReader.END_ELEMENT) {
                assertEquals(rdr.getName(), xsbrdr.getName());
                for(String prefix : prefixes) {
                    //System.out.println("|"+rdr.getNamespaceURI(prefix)+"|"+xsbrdr.getNamespaceURI(prefix)+"|");
                    assertEquals(fixNull(rdr.getNamespaceURI(prefix)), fixNull(xsbrdr.getNamespaceURI(prefix)));
                }
            }
        }
    }

    private static String fixNull(String s) {
        if (s == null) return "";
        else return s;
    }
}
