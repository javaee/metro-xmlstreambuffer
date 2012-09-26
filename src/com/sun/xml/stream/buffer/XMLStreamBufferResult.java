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

package com.sun.xml.stream.buffer;

import com.sun.xml.stream.buffer.sax.SAXBufferCreator;
import javax.xml.transform.sax.SAXResult;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

/**
 * A JAXP Result implementation that supports the serialization to an
 * {@link MutableXMLStreamBuffer} for use by applications that expect a Result.
 *
 * <p>
 * Reuse of a XMLStreamBufferResult more than once will require that the 
 * MutableXMLStreamBuffer is reset by called
 * {@link #.getXMLStreamBuffer()}.reset(), or by calling 
 * {@link #.setXMLStreamBuffer()} with a new instance of
 * {@link MutableXMLStreamBuffer}.
 *
 * <p>
 * The derivation of XMLStreamBufferResult from SAXResult is an implementation
 * detail.
 *  
 * <p>General applications shall not call the following methods:
 * <ul>
 * <li>setHandler</li>
 * <li>setLexicalHandler</li>
 * <li>setSystemId</li>
 * </ul>
 */
public class XMLStreamBufferResult extends SAXResult {
    protected MutableXMLStreamBuffer _buffer;
    protected SAXBufferCreator _bufferCreator;
    
    /**
     * The default XMLStreamBufferResult constructor.
     *
     * <p>
     * A {@link MutableXMLStreamBuffer} is instantiated and used.
     */
    public XMLStreamBufferResult() {
        setXMLStreamBuffer(new MutableXMLStreamBuffer());
    }
    
    /**
     * XMLStreamBufferResult constructor.
     *
     * @param buffer the {@link MutableXMLStreamBuffer} to use.
     */
    public XMLStreamBufferResult(MutableXMLStreamBuffer buffer) {
        setXMLStreamBuffer(buffer);
    }
    
    /**
     * Get the {@link MutableXMLStreamBuffer} that is used.
     *
     * @return the {@link MutableXMLStreamBuffer}.
     */
    public MutableXMLStreamBuffer getXMLStreamBuffer() {
        return _buffer;
    }    
    
    /**
     * Set the {@link MutableXMLStreamBuffer} to use.
     *
     * @param buffer the {@link MutableXMLStreamBuffer}.
     */
    public void setXMLStreamBuffer(MutableXMLStreamBuffer buffer) {
        if (buffer == null) {
            throw new NullPointerException("buffer cannot be null");
        }
        _buffer = buffer;
        setSystemId(_buffer.getSystemId());
        
        if (_bufferCreator != null) {
            _bufferCreator.setXMLStreamBuffer(_buffer);
        }
    }    

    public ContentHandler getHandler() {
        if (_bufferCreator == null) {
            _bufferCreator = new SAXBufferCreator(_buffer);
            setHandler(_bufferCreator);
        } else if (super.getHandler() == null) {            
            setHandler(_bufferCreator);
        }
        
        return _bufferCreator;        
    }
    
    public LexicalHandler getLexicalHandler() {
        return (LexicalHandler) getHandler();
    }    
}
