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

import com.sun.xml.stream.buffer.sax.SAXBufferProcessor;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class XMLStreamBufferSource extends SAXSource {
    XMLStreamBuffer _buffer;
    SAXBufferProcessor _bufferProcessor;
   
    public XMLStreamBufferSource(XMLStreamBuffer buffer) {
        super(new InputSource(
                new ByteArrayInputStream(new byte[0])));
        setXMLStreamBuffer(buffer);
    }

    public XMLStreamBuffer getXMLStreamBuffer() {
        return _buffer;
    }
    
    public void setXMLStreamBuffer(XMLStreamBuffer buffer) {
        buffer = _buffer;
    }
    
    public XMLReader getXMLReader() {
        XMLReader reader = super.getXMLReader();
        if (reader == null) {
            reader = _bufferProcessor = new SAXBufferProcessor();
            setXMLReader(reader);
        }
        _bufferProcessor.setXMLStreamBuffer(_buffer);
        return reader;
    }        
}
