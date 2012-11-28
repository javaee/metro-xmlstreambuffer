/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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
            _bufferProcessor.setBuffer(_buffer,false);
        }
    }
    
    public XMLReader getXMLReader() {
        if (_bufferProcessor == null) {
            _bufferProcessor = new SAXBufferProcessor(_buffer,false);
            setXMLReader(_bufferProcessor);
        } else if (super.getXMLReader() == null) {
            setXMLReader(_bufferProcessor);
        }

        return _bufferProcessor;
    }        
}
