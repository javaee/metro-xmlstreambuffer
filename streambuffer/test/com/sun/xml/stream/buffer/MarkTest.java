package com.sun.xml.stream.buffer;

import com.sun.xml.stream.buffer.stax.StreamReaderBufferCreator;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class MarkTest extends TestCase {
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

    public static Test suite() {
        TestSuite suite = new TestSuite(MarkTest.class);
        
        return suite;
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
        StreamReaderBufferCreator creator = new StreamReaderBufferCreator(new XMLStreamBuffer());
        
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
                
        for (XMLStreamBufferMark mark : marks) {
            XMLStreamReader markReader = mark.processUsingXMLStreamReader();
            
            processFragment(markReader);
        }        
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
        
        boolean exceptionThrown = false;
        try {
            reader.next();   
        } catch (XMLStreamException e) {
            exceptionThrown = true;
        }
        
        assertEquals(true, exceptionThrown);
    }
    
    public int next(XMLStreamReader reader) throws XMLStreamException {
        int readerEvent = reader.next();

        while (readerEvent != XMLStreamReader.END_DOCUMENT) {
            switch (readerEvent) {
                case XMLStreamReader.START_ELEMENT:
                case XMLStreamReader.END_ELEMENT:
                case XMLStreamReader.CDATA:
                case XMLStreamReader.CHARACTERS:
                case XMLStreamReader.PROCESSING_INSTRUCTION:
                    return readerEvent;
                default:
                    // falls through ignoring event
            }
            readerEvent = reader.next();
        }

        return readerEvent;
    }
    
    public int nextElementContent(XMLStreamReader reader) throws XMLStreamException {
        int state = nextContent(reader);
        if (state == XMLStreamReader.CHARACTERS) {
            throw new XMLStreamException(
                "Unexpected Character Content: " + reader.getText());
        }
        return state;
    }

    public int nextContent(XMLStreamReader reader) throws XMLStreamException {
        for (;;) {
            int state = next(reader);
            switch (state) {
                case XMLStreamReader.START_ELEMENT:
                case XMLStreamReader.END_ELEMENT:
                case XMLStreamReader.END_DOCUMENT:
                    return state;
                case XMLStreamReader.CHARACTERS:
                    if (!reader.isWhiteSpace()) {
                        return XMLStreamReader.CHARACTERS;
                    }
            }
        }
    }
    
    public void verifyReaderState(XMLStreamReader reader, int expectedState) throws XMLStreamException {
        int state = reader.getEventType();
        if (state != expectedState) {
            throw new XMLStreamException(
                "Unexpected State: " + getStateName(expectedState) + " " + getStateName(state));
        }
    }
    
    public static void verifyTag(XMLStreamReader reader, String namespaceURI, String localName) throws XMLStreamException {
        if (localName != reader.getLocalName() || namespaceURI != reader.getNamespaceURI()) {
            throw new XMLStreamException(
                "Unexpected State Tag: " + 
                    "{" + namespaceURI + "}" + localName + " " +
                    "{" + reader.getNamespaceURI() + "}" + reader.getLocalName());
        }
    }
    
    public String getStateName(int state) {
        switch (state) {
            case XMLStreamReader.ATTRIBUTE:
                return "ATTRIBUTE";
            case XMLStreamReader.CDATA:
                return "CDATA";
            case XMLStreamReader.CHARACTERS:
                return "CHARACTERS";
            case XMLStreamReader.COMMENT:
                return "COMMENT";
            case XMLStreamReader.DTD:
                return "DTD";
            case XMLStreamReader.END_DOCUMENT:
                return "END_DOCUMENT";
            case XMLStreamReader.END_ELEMENT:
                return "END_ELEMENT";
            case XMLStreamReader.ENTITY_DECLARATION:
                return "ENTITY_DECLARATION";
            case XMLStreamReader.ENTITY_REFERENCE:
                return "ENTITY_REFERENCE";
            case XMLStreamReader.NAMESPACE:
                return "NAMESPACE";
            case XMLStreamReader.NOTATION_DECLARATION:
                return "NOTATION_DECLARATION";
            case XMLStreamReader.PROCESSING_INSTRUCTION:
                return "PROCESSING_INSTRUCTION";
            case XMLStreamReader.SPACE:
                return "SPACE";
            case XMLStreamReader.START_DOCUMENT:
                return "START_DOCUMENT";
            case XMLStreamReader.START_ELEMENT:
                return "START_ELEMENT";
            default :
                return "UNKNOWN";
        }
    }
    
}
