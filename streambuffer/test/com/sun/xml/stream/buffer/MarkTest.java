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

import com.sun.xml.stream.buffer.stax.StreamReaderBufferCreator;
import org.xml.sax.helpers.AttributesImpl;

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
        Map<String,String> namespaces = new HashMap<String,String>();
        for (int i = 0; i < reader.getNamespaceCount(); i++) {
            namespaces.put(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
        }

        // Move to next element
        nextElementContent(reader);
        verifyReaderState(reader,
                XMLStreamReader.START_ELEMENT);

        List<XMLStreamBufferMark> marks = new ArrayList<XMLStreamBufferMark>();
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
                    Map<String,String> headerBlockNamespaces = namespaces;

                    // Collect namespaces on SOAP header block
                    if (reader.getNamespaceCount() > 0) {
                        headerBlockNamespaces = new HashMap<String,String>();
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
            mark.writeTo(t,true);
        }

        t.endElement("","root","root");
        t.endDocument();
    }

    public void processFragment(XMLStreamReader reader) throws XMLStreamException {
        verifyReaderState(reader,
                XMLStreamReader.START_DOCUMENT);

        reader.next();
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
