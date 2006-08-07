package com.sun.xml.stream.buffer;

import com.sun.xml.stream.buffer.stax.StreamReaderBufferCreator;
import junit.framework.Test;
import junit.framework.TestSuite;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class MarkTest extends BaseBufferTestCase {
    private static final String SOAP_MESSAGE = "data/soap-message.xml";

    private static final String SOAP_NAMESPACE_URI = "http://www.w3.org/2003/05/soap-envelope";
    private static final String SOAP_ENVELOPE = "Envelope";
    private static final String SOAP_HEADER = "Header";
    private static final String SOAP_BODY = "Body";

    private URL _soapMessageURL;

    public MarkTest(String testName) {
        super(testName);

        _soapMessageURL = this.getClass().getClassLoader().getResource(SOAP_MESSAGE);
    }

    public void testMark() throws Exception {
        XMLStreamReader reader = XMLInputFactory.newInstance().
                createXMLStreamReader(_soapMessageURL.openStream());

        nextElementContent(reader);
        verifyReaderState(reader,
                XMLStreamReader.START_ELEMENT);
        verifyTag(reader, SOAP_NAMESPACE_URI, SOAP_ENVELOPE);

        // Collect namespaces on soap:Envelope
        Map<String, String> namespaces = new HashMap();
        for (int i = 0; i < reader.getNamespaceCount(); i++) {
            namespaces.put(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
        }

        // Move to next element
        nextElementContent(reader);
        verifyReaderState(reader,
                XMLStreamReader.START_ELEMENT);

        List<XMLStreamBufferMark> marks = new ArrayList();
        StreamReaderBufferCreator creator = new StreamReaderBufferCreator(new MutableXMLStreamBuffer());

        if (reader.getLocalName() == SOAP_HEADER
                && reader.getNamespaceURI() == SOAP_NAMESPACE_URI) {

            // Collect namespaces on soap:Header
            for (int i = 0; i < reader.getNamespaceCount(); i++) {
                namespaces.put(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
            }

            nextElementContent(reader);

            // If SOAP header blocks are present (i.e. not <soap:Header/>)
            if (reader.getEventType() == XMLStreamReader.START_ELEMENT) {
                while(reader.getEventType() == XMLStreamReader.START_ELEMENT) {
                    Map<String, String> headerBlockNamespaces = namespaces;

                    // Collect namespaces on SOAP header block
                    if (reader.getNamespaceCount() > 0) {
                        headerBlockNamespaces = new HashMap();
                        headerBlockNamespaces.putAll(namespaces);
                        for (int i = 0; i < reader.getNamespaceCount(); i++) {
                            headerBlockNamespaces.put(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
                        }
                    }

                    // Mark
                    XMLStreamBufferMark mark = new XMLStreamBufferMark(headerBlockNamespaces, creator);
                    // Create Header
                    marks.add(mark);

                    creator.createElementFragment(reader, false);
                }

                // Move to soap:Body
                nextElementContent(reader);
            }
        }

        // Verify that <soap:Body> is present
        verifyTag(reader, SOAP_NAMESPACE_URI, SOAP_BODY);


        TransformerHandler t = ((SAXTransformerFactory)TransformerFactory.newInstance()).newTransformerHandler();
        t.setResult(new StreamResult(System.out));
        t.startDocument();
        t.startElement("","root","root",new AttributesImpl());

        for (XMLStreamBufferMark mark : marks) {
            XMLStreamReader markReader = mark.readAsXMLStreamReader();

            processFragment(markReader);

            // test subtree->SAX.
            // TODO: think about the way to test the infoset correctness.
            mark.writeTo(t);
        }

        t.endElement("","root","root");
        t.endDocument();
    }

    public void processFragment(XMLStreamReader reader) throws XMLStreamException {
        verifyReaderState(reader,
                XMLStreamReader.START_ELEMENT);

        int depth = 1;
        while(depth > 0) {
            int event = reader.next();
            switch(event) {
                case XMLStreamReader.START_ELEMENT:
                    depth++;
                    break;
                case XMLStreamReader.END_ELEMENT:
                    depth--;
                    break;
            }
        }

        reader.next();
        verifyReaderState(reader,
                XMLStreamReader.END_DOCUMENT);

        boolean exceptionThrown = false;
        try {
            reader.next();
        } catch (XMLStreamException e) {
            exceptionThrown = true;
        }

        assertEquals(true, exceptionThrown);
    }
}
