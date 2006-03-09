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

import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.stream.buffer.BaseBufferTestCase;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Kohsuke Kawaguchi
 */
public class StreamWriterBufferCreatorTest extends BaseBufferTestCase {
    public void test1() throws Exception {
        XMLStreamBuffer buffer = new XMLStreamBuffer();
        XMLStreamWriter writer = buffer.createFromXMLStreamWriter();
        writer.writeStartDocument();
        writer.writeStartElement("foo");
        writer.writeCharacters("body");
        writer.writeEndElement();
        writer.writeEndDocument();

        assertTrue(buffer.isCreated());

        XMLStreamReader reader = buffer.readFromXMLStreamReader();
        assertEquals(XMLStreamConstants.START_DOCUMENT,reader.getEventType());

        assertEquals(XMLStreamConstants.START_ELEMENT,reader.next());
        verifyTag(reader,null,"foo");

        assertEquals(XMLStreamConstants.CHARACTERS,reader.next());
        assertEquals("body",reader.getText());

        assertEquals(XMLStreamConstants.END_ELEMENT,reader.next());
        verifyTag(reader,null,"foo");
    }
}
