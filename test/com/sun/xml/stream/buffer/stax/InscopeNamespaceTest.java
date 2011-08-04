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


    public void testXMLStreamBuffer() throws Exception {
        itestXMLStreamBuffer(false);
    }

    public void testXMLStreamBufferMark() throws Exception {
        itestXMLStreamBuffer(true);
    }

    /**
     * In this test, the namespace xmlns:user='http://foo.bar' is declared on top-level
     * element and child uses it but doesn't declare it
     * @throws Exception
     */
    private void itestXMLStreamBuffer(boolean markReader) throws Exception {
    	String requestStr =
                "<S:Header xmlns:user='http://foo.bar' xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>" +
                "<user:foo>bar</user:foo>" +
                "</S:Header>";
        XMLStreamReader reader2 = markReader ? getReaderFromMark(requestStr) : getReader(requestStr);
        reader2.next();// go to start element
        assertEquals("http://foo.bar", reader2.getNamespaceURI());
    }


    public void testXMLStreamBuffer1() throws Exception {
        itestXMLStreamBuffer1(false);
    }

    public void testXMLStreamBuffer1Mark() throws Exception {
        itestXMLStreamBuffer1(true);
    }

    /**
     * In this test, the namespace xmlns:user='http://foo.bar' is declared on top-level element as well as the child 
     * @throws Exception
     */
    private void itestXMLStreamBuffer1(boolean markReader) throws Exception {
    	String requestStr =
                "<S:Header xmlns:user='http://foo.bar' xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>" +
                "<user:foo xmlns:user='http://foo.bar'>bar</user:foo>" +
                "</S:Header>";
        XMLStreamReader reader2 = markReader ? getReaderFromMark(requestStr) : getReader(requestStr);
        reader2.next();// go to start element
        assertEquals("http://foo.bar", reader2.getNamespaceURI());
    }

    public void testXMLStreamBuffer2() throws Exception {
        itestXMLStreamBuffer2(false);
    }

    public void testXMLStreamBuffer2Mark() throws Exception {
        itestXMLStreamBuffer2(true);
    }

    /**
     * In this test, the namespace xmlns:user='http://foo.bar' is declared
     *  on top-level element and child uses same prefix different ns
     * @throws Exception
     */
    private void itestXMLStreamBuffer2(boolean markReader) throws Exception {
    	String requestStr =
                "<S:Header xmlns:user='http://foo.bar' xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>" +
                "<user:foo xmlns:user='http://foo1.bar1'>bar</user:foo>" +
                "</S:Header>";
        XMLStreamReader reader2 = markReader ? getReaderFromMark(requestStr) : getReader(requestStr);
        reader2.next();// go to start element
        assertEquals("http://foo1.bar1", reader2.getNamespaceURI());
    }

    public void testXMLStreamBuffer3() throws Exception {
        itestXMLStreamBuffer3(false);
    }

    public void testXMLStreamBuffer3Mark() throws Exception {
        itestXMLStreamBuffer3(true);
    }

    private void itestXMLStreamBuffer3(boolean markReader) throws Exception {
    	String requestStr =
                "<Header xmlns='http://foo.bar' >" +
                "<user>bar</user>" +
                "<Header>";
        XMLStreamReader reader2 = markReader ? getReaderFromMark(requestStr) : getReader(requestStr);
        reader2.next();// go to start element
        assertEquals("http://foo.bar", reader2.getNamespaceURI());
    }

    public void testXMLStreamBuffer4() throws Exception {
        itestXMLStreamBuffer4(false);
    }

    public void testXMLStreamBuffer4Mark() throws Exception {
        itestXMLStreamBuffer4(true);
    }

    private void itestXMLStreamBuffer4(boolean markReader) throws Exception {
    	String requestStr =
                "<Header xmlns='http://foo.bar' >" +
                "<user xmlns='http://foo1.bar1'>bar</user>" +
                "<Header>";
        XMLStreamReader reader2 = markReader ? getReaderFromMark(requestStr) : getReader(requestStr);
        reader2.next();// go to start element
        assertEquals("http://foo1.bar1", reader2.getNamespaceURI());
    }

    public void testXMLStreamBuffer5() throws Exception {
        itestXMLStreamBuffer5(false);
    }

    public void testXMLStreamBuffer5Mark() throws Exception {
        itestXMLStreamBuffer5(true);
    }

    private void itestXMLStreamBuffer5(boolean markReader) throws Exception {
    	String requestStr =
                "<S:Header  xmlns:user1='http://foo1.bar1' xmlns:user='http://foo.bar' xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>" +
                "<user:foo user1:att='value'>bar</user:foo>" +
                "</S:Header>";
        XMLStreamReader reader2 = markReader ? getReaderFromMark(requestStr) : getReader(requestStr);
        reader2.next();// go to start element
        assertEquals("http://foo.bar", reader2.getNamespaceURI());
        assertEquals("value", reader2.getAttributeValue("http://foo1.bar1","att"));
    }

    public void testXMLStreamBuffer6() throws Exception {
        itestXMLStreamBuffer6(false);
    }

    public void testXMLStreamBuffer6Mark() throws Exception {
        itestXMLStreamBuffer6(true);
    }

    private void itestXMLStreamBuffer6(boolean markReader) throws Exception {
    	String requestStr =
                "<Header xmlns='http://foo.bar' >" +
                "<user xmlns=''>bar</user>" +
                "<Header>";
        XMLStreamReader reader2 = markReader ? getReaderFromMark(requestStr) : getReader(requestStr);
        reader2.next();// go to start element
        assertNull(reader2.getNamespaceURI());
    }

    public void testXMLStreamBuffer7() throws Exception {
        itestXMLStreamBuffer7(false);
    }

    public void testXMLStreamBuffer7Mark() throws Exception {
        itestXMLStreamBuffer7(true);
    }

    private void itestXMLStreamBuffer7(boolean markReader) throws Exception {
    	String requestStr =
                "<Header xmlns='' >" +
                "<user xmlns='http://foo.bar'>bar</user>" +
                "<Header>";
        XMLStreamReader reader2 = markReader ? getReaderFromMark(requestStr) : getReader(requestStr);
        reader2.next();// go to start element
        assertEquals("http://foo.bar", reader2.getNamespaceURI());
    }

    public void testXMLStreamBuffer8() throws Exception {
        itestXMLStreamBuffer8(false);
    }

    public void testXMLStreamBuffer8Mark() throws Exception {
        itestXMLStreamBuffer8(true);
    }

    private void itestXMLStreamBuffer8(boolean markReader) throws Exception {
    	String requestStr =
                "<S:Header xmlns:user='http://foo.bar' xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>" +
                "<user:foo xmlns:a='anamespace' b='bvalue' c='cvalue'>bar</user:foo>" +
                "</S:Header>";
        XMLStreamReader reader2 = markReader ? getReaderFromMark(requestStr) : getReader(requestStr);
        reader2.next();// go to start element
        assertEquals("http://foo.bar", reader2.getNamespaceURI());
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

    // returned reader is placed at the first child
    private XMLStreamReader getReaderFromMark(String requestStr) throws Exception {
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
        creator.createElementFragment(reader, true);
        return mark.readAsXMLStreamReader();
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
