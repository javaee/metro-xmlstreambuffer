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
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A mark into a XMLStreamBuffer.
 *
 * <p>
 * A mark can be processed in the same manner as a XMLStreamBuffer but cannot 
 * be reset and created (corresponding methods will throw a 
 * {@link XMLStreamBufferException}).
 *
 * <p>
 * A mark will share a sub set of information of the XMLStreamBuffer that is
 * marked. Reseting and/or creating the marked XMLStreamBuffer will indirectly
 * invalidate the mark. It is the responsibility of the application to manage 
 * the relationship between the marked XMLStreamBuffer and one or more marks.
 *
 * <p>
 */
public class XMLStreamBufferMark extends XMLStreamBuffer {
    
    /**
     * Create a mark from the XMLStreamBuffer that is being created.
     *
     * <p>
     * A mark will be created from the current position of creation of the 
     * XMLStreamBuffer that is being created by a {@link AbstractCreator}.
     *
     * @param inscopeNamespaces
     * The in-scope namespaces on the fragment of XML infoset that is
     * to be marked.
     *
     * @param creator
     * The AbstractCreator from which the current position of creation of
     * the XMLStreamBuffer will be taken as the mark.
     */
    public XMLStreamBufferMark(Map inscopeNamespaces, AbstractCreator creator) {
        _inscopeNamespaces = inscopeNamespaces;
        
        _structure = creator._currentStructureFragment;
        _structurePtr = creator._structurePtr;
        
        _structureStrings = creator._currentStructureStringFragment;
        _structureStringsPtr = creator._structureStringsPtr;
        
        _contentCharactersBuffer = creator._currentContentCharactersBufferFragment;
        _contentCharactersBufferPtr = creator._contentCharactersBufferPtr;
        
        _contentObjects = creator._currentContentObjectFragment;
        _contentObjectsPtr = creator._contentObjectsPtr;
    }
    
    /**
     * Create a mark from the XMLStreamBuffer that is being processed.
     *
     * <p>
     * A mark will be created from the current position of processing of the 
     * XMLStreamBuffer that is being processed by a {@link AbstractProcessor}.
     *
     * @param inscopeNamespaces
     * The in-scope namespaces on the fragment of XML infoset that is
     * to be marked.
     *
     * @param processor
     * The AbstractProcessor from which the current position of processing of
     * the XMLStreamBuffer will be taken as the mark.
     */
    public XMLStreamBufferMark(Map inscopeNamespaces, AbstractProcessor processor) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * This method will throw a XMLStreamBufferException.
     *
     * @throws XMLStreamBufferException
     * Creation cannot be performed on a mark. A creation can only be performed
     * on the XMLStreamBuffer that is marked, which will indirectly invalidate
     * the mark.
     */
    public void createFromXMLStreamReader(XMLStreamReader reader) throws XMLStreamException, XMLStreamBufferException {
        throw new XMLStreamBufferException("A mark of a XMLStreamBuffer cannot be used for creation");
    }
    
    /**
     * This method will throw a XMLStreamBufferException.
     *
     * @throws XMLStreamBufferException
     * Creation cannot be performed on a mark. A creation can only be performed
     * on the XMLStreamBuffer that is marked, which will indirectly invalidate
     * the mark.
     */
    public XMLStreamWriter createFromXMLStreamWriter() throws XMLStreamBufferException {
        throw new XMLStreamBufferException("A mark of a XMLStreamBuffer cannot be used for creation");
    }
    
    /**
     * This method will throw a XMLStreamBufferException.
     *
     * @throws XMLStreamBufferException
     * Creation cannot be performed on a mark. A creation can only be performed
     * on the XMLStreamBuffer that is marked, which will indirectly invalidate
     * the mark.
     */
    public SAXBufferCreator createFromSAXBufferCreator() throws XMLStreamBufferException {
        throw new XMLStreamBufferException("A mark of a XMLStreamBuffer cannot be used for creation");
    }
    
    /**
     * This method will throw a XMLStreamBufferException.
     *
     * @throws XMLStreamBufferException
     * Creation cannot be performed on a mark. A creation can only be performed
     * on the XMLStreamBuffer that is marked, which will indirectly invalidate
     * the mark.
     */
    public void createFromXMLReader(XMLReader reader, InputStream in) throws XMLStreamBufferException, SAXException, IOException {
        throw new XMLStreamBufferException("A mark of a XMLStreamBuffer cannot be used for creation");
    }
    
    /**
     * This method will throw a XMLStreamBufferException.
     *
     * @throws XMLStreamBufferException
     * Reset cannot be performed on a mark. A reset can only be performed
     * on the XMLStreamBuffer that is marked, which will indirectly invalidate
     * the mark.
     */
    public void reset() throws XMLStreamBufferException {
        throw new XMLStreamBufferException("A mark of a XMLStreamBuffer cannot be reset");
    }
}
