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
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * A JAXP Source implementation that supports the parsing
 * of {@link XMLStreamBuffer} for use by applications that expect a Source.
 *
 * <p>
 * The derivation of XMLStreamBufferSource from SAXSource is an implementation
 * detail.
 *
 * <p>Applications shall obey the following restrictions:
 * <ul>
 * <li>The setXMLReader and setInputSource shall not be called.</li>
 * <li>The XMLReader object obtained by the getXMLReader method shall
 *     be used only for parsing the InputSource object returned by
 *     the getInputSource method.</li>
 * <li>The InputSource object obtained by the getInputSource method shall 
 *     be used only for being parsed by the XMLReader object returned by 
 *     the getXMLReader method.</li>
 * </ul>
 */
public class XMLStreamBufferSource extends SAXSource {
    protected XMLStreamBuffer _buffer;
    protected SAXBufferProcessor _bufferProcessor;
   
    /**
     * XMLStreamBufferSource constructor.
     *
     * @param buffer the {@link XMLStreamBuffer} to use.
     */
    public XMLStreamBufferSource(XMLStreamBuffer buffer) {
        super(new InputSource(
                new ByteArrayInputStream(new byte[0])));
        setXMLStreamBuffer(buffer);
    }

    /**
     * Get the {@link XMLStreamBuffer} that is used.
     *
     * @return the {@link XMLStreamBuffer}.
     */
    public XMLStreamBuffer getXMLStreamBuffer() {
        return _buffer;
    }
    
    /**
     * Set the {@link XMLStreamBuffer} to use.
     *
     * @param buffer the {@link XMLStreamBuffer}.
     */
    public void setXMLStreamBuffer(XMLStreamBuffer buffer) {
        if (buffer == null) {
            throw new NullPointerException("buffer cannot be null");
        }        
        _buffer = buffer;
        
        if (_bufferProcessor != null) {
            _bufferProcessor.setBuffer(_buffer);
        }
    }
    
    public XMLReader getXMLReader() {
        if (_bufferProcessor == null) {
            _bufferProcessor = new SAXBufferProcessor(_buffer);
            setXMLReader(_bufferProcessor);
        } else if (super.getXMLReader() == null) {
            setXMLReader(_bufferProcessor);
        }

        return _bufferProcessor;
    }        
}