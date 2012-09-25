/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.stream.buffer.sax;

import com.sun.xml.stream.buffer.XMLStreamBuffer;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.*;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class NamespaceTest extends TestCase {
    
    public NamespaceTest(String testName) {
        super(testName);
    }

    public void testManyNamespaceDeclarations() throws Exception {
        for (int i = 0; i <= 50; i++) {
            _testManyNamespaceDeclarations(i);
        }
    }
    
    private void _testManyNamespaceDeclarations(int n) throws Exception {
        InputStream d = createDocument(n);
        
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        XMLReader r = spf.newSAXParser().getXMLReader();

        XMLStreamBuffer b = XMLStreamBuffer.createNewBufferFromXMLReader(r, d);
    }
    
    private InputStream createDocument(int n) {
        StringBuilder b = new StringBuilder();
        
        b.append("<root");
        for (int i = 0; i <= n; i++) {
            b.append(" xmlns:p" + i + "='urn:" + i + "'");
        }
        b.append(">");
        b.append("</root>");
        
        return new ByteArrayInputStream(b.toString().getBytes());
    }
    

    public void testEndPrefixMappings() throws Exception {
    	String xml =
                "<a:x xmlns:a='http://foo.bar/a' xmlns:b='http://foo.bar/b'>" +
                  "<a:y xmlns:c='http://foo.bar/c' xmlns:d='http://foo.bar/d'>" +
                    "<a:z xmlns:e='http://foo.bar/e'>e:ZZZ</a:z>" +
                  "</a:y>" +
                "</a:x>";
        XMLStreamReader r = getReader(xml);
        do r.next(); while(!"y".equals(r.getLocalName()));

        XMLStreamBuffer b = XMLStreamBuffer.createNewBufferFromXMLStreamReader(r);
        SAXBufferProcessor sp = new SAXBufferProcessor(b, true);
        MyContentHandler ch = new MyContentHandler();
        sp.setContentHandler(ch);
        sp.process();
        assertEquals(3, ch.startPrefixCount);
        assertEquals(3, ch.endPrefixCount);
        assertTrue(ch.prefix_e_Ended);
    }


    static XMLStreamReader getReader(String xml) throws Exception {
    	ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes());
        XMLInputFactory readerFactory = XMLInputFactory.newInstance();
        return readerFactory.createXMLStreamReader(is);
    }
    
    static class MyContentHandler extends DefaultHandler {
    	int startPrefixCount = 0;
    	int endPrefixCount = 0;    	
    	boolean element_z_Ended = false;
    	boolean prefix_e_Ended = false;    	
		public void startPrefixMapping(String prefix, String uri) throws SAXException {
			startPrefixCount ++;
		}
		public void endPrefixMapping(String prefix) throws SAXException {
			endPrefixCount ++;
			if (element_z_Ended && "e".equals(prefix)) prefix_e_Ended = true;
			
		}
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if ("z".equals(localName)) element_z_Ended = true;			
		}
    }
}
