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
