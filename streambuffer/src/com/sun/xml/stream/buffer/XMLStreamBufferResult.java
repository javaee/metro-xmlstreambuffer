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

import com.sun.xml.stream.buffer.sax.SAXBufferCreator;
import java.io.OutputStream;
import javax.xml.transform.sax.SAXResult;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

public class XMLStreamBufferResult extends SAXResult {
    XMLStreamBuffer _buffer;
    SAXBufferCreator _bufferCreator;
    
    public XMLStreamBufferResult(XMLStreamBuffer buffer) {
        setXMLStreamBuffer(buffer);
    }
    
    public XMLStreamBuffer getXMLStreamBuffer() {
        return _buffer;
    }    
    
    public void setXMLStreamBuffer(XMLStreamBuffer buffer) {
        _buffer = buffer;
    }    

    public ContentHandler getHandler() {
        ContentHandler handler = super.getHandler();
        if (handler == null) {
            handler = _bufferCreator = new SAXBufferCreator();
            setHandler(handler);
        }
        _bufferCreator.setXMLStreamBuffer(_buffer);
        return handler;        
    }
    
    public LexicalHandler getLexicalHandler() {
        return (LexicalHandler) getHandler();
    }    
}
