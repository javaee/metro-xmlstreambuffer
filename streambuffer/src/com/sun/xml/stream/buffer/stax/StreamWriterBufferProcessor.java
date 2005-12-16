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

import com.sun.xml.stream.buffer.AbstractProcessor;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.stream.buffer.XMLStreamBufferException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class StreamWriterBufferProcessor extends AbstractProcessor {
    
    public StreamWriterBufferProcessor() {
    }
    
    public StreamWriterBufferProcessor(XMLStreamBuffer buffer) {
        setXMLStreamBuffer(buffer);
    }
    
    public final void process(XMLStreamBuffer buffer, XMLStreamWriter writer) throws XMLStreamBufferException, XMLStreamException {
        setXMLStreamBuffer(buffer);
        process(writer);
    }
    
    public void process(XMLStreamWriter writer) throws XMLStreamBufferException, XMLStreamException {
    }
    
    public void setXMLStreamBuffer(XMLStreamBuffer buffer) {
        setBuffer(buffer);
    }
    
}
