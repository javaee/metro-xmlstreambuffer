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

import com.sun.xml.stream.buffer.BaseBufferTestCase;
import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import javax.xml.stream.XMLStreamConstants;
import org.jvnet.staxex.Base64Data;
import org.jvnet.staxex.XMLStreamReaderEx;
import org.jvnet.staxex.XMLStreamWriterEx;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class Base64Test extends BaseBufferTestCase {
    byte[] bytes = {0, 1, 2, 3};
    Base64Data data;
    String base64EncodedString;
    
    /** Creates a new instance of Base64Test */
    public Base64Test() {
        data = new Base64Data();
        data.set(bytes, null);
        base64EncodedString = data.toString();
    }
    
    MutableXMLStreamBuffer createBuffer() throws Exception {
        MutableXMLStreamBuffer buffer = new MutableXMLStreamBuffer();
        XMLStreamWriterEx writer = (XMLStreamWriterEx)buffer.createFromXMLStreamWriter();
        writer.writeStartDocument();
        writer.writeStartElement("foo");
        writer.writeBinary(bytes, 0, bytes.length, null);
        writer.writeEndElement();
        writer.writeEndDocument();
        
        assertTrue(buffer.isCreated());
        
        return buffer;
    }
    
    public void testReadingAsString() throws Exception {
        MutableXMLStreamBuffer buffer = createBuffer();

        XMLStreamReaderEx reader = (XMLStreamReaderEx)buffer.readAsXMLStreamReader();
        assertEquals(XMLStreamConstants.START_DOCUMENT,reader.getEventType());

        assertEquals(XMLStreamConstants.START_ELEMENT,reader.next());
        verifyTag(reader,null,"foo");

        assertEquals(XMLStreamConstants.CHARACTERS,reader.next());
        assertEquals(base64EncodedString,reader.getText());

        assertEquals(XMLStreamConstants.END_ELEMENT,reader.next());
        verifyTag(reader,null,"foo");
    }
    
    void readPCData(XMLStreamReaderEx reader) throws Exception {
        assertEquals(XMLStreamConstants.START_DOCUMENT,reader.getEventType());

        assertEquals(XMLStreamConstants.START_ELEMENT,reader.next());
        verifyTag(reader,null,"foo");

        assertEquals(XMLStreamConstants.CHARACTERS,reader.next());
        CharSequence c = reader.getPCDATA();
        assertEquals(true, c instanceof Base64Data);
        Base64Data d = (Base64Data)c;
        byte[] b = d.getExact();
        assertEquals(bytes.length, b.length);
        for (int i = 0; i < bytes.length; i++) {
            assertEquals(bytes[i], b[i]);
        }

        assertEquals(XMLStreamConstants.END_ELEMENT,reader.next());
        verifyTag(reader,null,"foo");
    }
    
    public void testReadingAsPCDATA() throws Exception {
        MutableXMLStreamBuffer buffer = createBuffer();

        XMLStreamReaderEx reader = (XMLStreamReaderEx)buffer.readAsXMLStreamReader();
        readPCData(reader);
    }
    
    public void testReadingAsPCDATAUsingCopyOfBuffer() throws Exception {
        MutableXMLStreamBuffer originalBuffer = createBuffer();
        XMLStreamReaderEx originalReader = (XMLStreamReaderEx)originalBuffer.readAsXMLStreamReader();
        
        MutableXMLStreamBuffer buffer = new MutableXMLStreamBuffer();
        buffer.createFromXMLStreamReader(originalReader);
        
        XMLStreamReaderEx reader = (XMLStreamReaderEx)buffer.readAsXMLStreamReader();
        readPCData(reader);
    }
}
