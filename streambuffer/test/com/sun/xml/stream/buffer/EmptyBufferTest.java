/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.xml.stream.buffer;

import javax.xml.stream.XMLStreamReader;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EmptyBufferTest extends BaseBufferTestCase {
    
    public EmptyBufferTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(EmptyBufferTest.class);
        
        return suite;
    }    
    
    public void testEmptyBufferUsingXMLStreamReader() throws Exception {    
        XMLStreamBuffer b = new XMLStreamBuffer();
        XMLStreamReader r = b.readFromXMLStreamReader();
        
        assertEquals(true, r.getEventType() == XMLStreamReader.START_DOCUMENT);
        r.next();
        assertEquals(true, r.getEventType() == XMLStreamReader.END_DOCUMENT);
    }
    
    public void testEmptyBufferUsingContentHandler() throws Exception {    
        XMLStreamBuffer b = new XMLStreamBuffer();
        b.writeTo(new ContentHandler() {
            boolean _startDocument = false;
            
            public void setDocumentLocator(Locator locator) {
            }

            public void startDocument() throws SAXException {
                _startDocument = true;
            }

            public void endDocument() throws SAXException {
                assertEquals(true, _startDocument);
            }

            public void startPrefixMapping(String prefix, String uri) throws SAXException {
                assertEquals(false, _startDocument);
            }

            public void endPrefixMapping(String prefix) throws SAXException {
                assertEquals(false, _startDocument);
            }

            public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                assertEquals(false, _startDocument);
            }

            public void endElement(String uri, String localName, String qName) throws SAXException {
                assertEquals(false, _startDocument);
            }

            public void characters(char[] ch, int start, int length) throws SAXException {
                assertEquals(false, _startDocument);
            }

            public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
                assertEquals(false, _startDocument);
            }

            public void processingInstruction(String target, String data) throws SAXException {
                assertEquals(false, _startDocument);
            }

            public void skippedEntity(String name) throws SAXException {
                assertEquals(false, _startDocument);
            }
        });        
    }
}
