/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.stream.buffer;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EmptyBufferTest extends BaseBufferTestCase {
    
    public EmptyBufferTest(String testName) {
        super(testName);
    }
    
    public void testEmptyBufferUsingXMLStreamReader() throws Exception {    
        MutableXMLStreamBuffer b = new MutableXMLStreamBuffer();
        XMLStreamReader r = b.readAsXMLStreamReader();
        
        assertEquals(true, r.getEventType() == XMLStreamReader.START_DOCUMENT);
        r.next();
        assertEquals(true, r.getEventType() == XMLStreamReader.END_DOCUMENT);
    }
    
    public void testEmptyBufferUsingContentHandler() throws Exception {    
        MutableXMLStreamBuffer b = new MutableXMLStreamBuffer();
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
        },false);
    }
}
