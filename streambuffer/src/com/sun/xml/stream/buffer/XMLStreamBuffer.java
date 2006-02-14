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

import com.sun.xml.stream.buffer.sax.Properties;
import com.sun.xml.stream.buffer.sax.SAXBufferCreator;
import com.sun.xml.stream.buffer.sax.SAXBufferProcessor;
import com.sun.xml.stream.buffer.stax.StreamReaderBufferCreator;
import com.sun.xml.stream.buffer.stax.StreamReaderBufferProcessor;
import com.sun.xml.stream.buffer.stax.StreamWriterBufferCreator;
import com.sun.xml.stream.buffer.stax.StreamWriterBufferProcessor;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 *
 * An stream-based buffer of an XML infoset.
 *
 * <p>
 * A XMLStreamBuffer is created and processed using specific SAX and StAX-based 
 * creators and processors. Utility methods on XMLStreamBuffer are provided for 
 * such functionality that utilize SAX and StAX-based creators and processors.
 *
 * <p>
 * Once instantiated the same instance of a XMLStreamBuffer may be reused for
 * creation to reduce the amount of Objects instantiated and garbage
 * collected that are required for internally representing an XML infoset.
 *
 * <p>
 * Once created the same instance of a XMLStreamBuffer may be processed 
 * multiple times and concurrently by more than one processor. From the 
 * perspective of a processor a XMLStreamBuffer is immutable.
 * 
 * <p>
 * A XMLStreamBuffer is not designed to be created and processed 
 * concurrently. If done so unspecified behaviour may occur.
 *
 * <p>
 * A XMLStreamBuffer can represent a complete XML infoset or a fragment of an 
 * XML infoset. A fragment of an XML infoset may be represented as a mark into
 * an XMLStreamBuffer, see {@link XMLStreamBufferMark}.
 */
public class XMLStreamBuffer {
    /**
     * The default array size for the arrays used in internal representation 
     * of the XML infoset.
     */
    public static int DEFAULT_ARRAY_SIZE = 512;
    
    protected static final Map<String, String> EMTPY_MAP = Collections.emptyMap();
    
    protected Map<String, String> _inscopeNamespaces = EMTPY_MAP;
    
    protected boolean _hasInternedStrings;
    
    protected FragmentedArray<int[]> _structure;
    protected int _structurePtr;
    
    protected FragmentedArray<String[]> _structureStrings;
    protected int _structureStringsPtr;
    
    protected FragmentedArray<String[]> _contentStrings;
    protected int _contentStringsPtr;
    
    protected FragmentedArray<char[][]> _contentCharacters;
    protected int _contentCharactersPtr;
    
    protected FragmentedArray<char[]> _contentCharactersBuffer;
    protected int _contentCharactersBufferPtr;

    /**
     * Create a new XMLStreamBuffer using the 
     * {@link XMLStreamBuffer#DEFAULT_ARRAY_SIZE}.
     */
    public XMLStreamBuffer() {
        this(DEFAULT_ARRAY_SIZE);
    }
    
    /**
     * Create a new XMLStreamBuffer.
     *
     * @throws NegativeArraySizeException
     * If the <code>size</code> argument is less than <code>0</code>.
     *
     * @param size
     * The size of the arrays used in the internal representation 
     * of the XML infoset. 
     */
    public XMLStreamBuffer(int size) {
        _structure = new FragmentedArray(new int[size]);
        _structureStrings = new FragmentedArray(new String[size]);
        _contentStrings = new FragmentedArray(new String[size]);
        _contentCharacters = new FragmentedArray(new char[size][]);
        _contentCharactersBuffer = new FragmentedArray(new char[4096]);
        
        // Set the first element of structure array to indicate an empty buffer 
        // that has not been created
        _structure.getArray()[0] = AbstractCreatorProcessor.T_END;
    }

    /**
     * Is the XMLStreamBuffer created by creator.
     *
     * <p>
     * When first instantiated a XMLStreamBuffer is not created and represents
     * an empty XML infoset that contains no information.
     *
     * @return
     * <code>true</code> if the XMLStreamBuffer has been created.
     */
    public boolean isCreated() {
        return _structure.getArray()[0] != AbstractCreatorProcessor.T_END;
    }
    
    /**
     * Is the XMLStreamBuffer a representation of a fragment of an XML infoset.
     *
     * @return
     * <code>true</code> if the XMLStreamBuffer is a representation of a fragment
     * of an XML infoset.
     */
    public boolean isFragment() {
        return (isCreated() && (_structure.getArray()[_structurePtr] & AbstractCreatorProcessor.TYPE_MASK) 
                != AbstractCreatorProcessor.T_DOCUMENT);
    }
    
    /**
     * Is the XMLStreamBuffer a representation of a fragment of an XML infoset
     * that is an element (and its contents).
     *
     * @return
     * <code>true</code> if the XMLStreamBuffer XMLStreamBuffer a representation 
     * of a fragment of an XML infoset that is an element (and its contents).
     */
    public boolean isElementFragment() {
        return (isCreated() && (_structure.getArray()[_structurePtr] & AbstractCreatorProcessor.TYPE_MASK) 
                == AbstractCreatorProcessor.T_ELEMENT);        
    }
   
    /**
     * Get the in-scope namespaces.
     *
     * <p>
     * 
     * The in-scope namespaces will be empty if the XMLStreamBuffer is not a 
     * fragment ({@link #isFragment} returns <code>false</code>).
     *
     * The in-scope namespace will correspond to the in-scope namespaces of the
     * fragment if the XMLStreamBuffer is a fragment ({@link #isFragment} 
     * returns <code>false</code>). The in-scope namespaces will include any
     * namespace delcarations on an element if the fragment correspond to that 
     * of an element ({@link #isElementFragment} returns <code>false</code>).
     *
     * @return
     * The in-scope namespaces of the XMLStreamBuffer.
     */
    public Map<String, String> getInscopeNamespaces() {
        return _inscopeNamespaces;
    }
    
    /**
     * Has the XMLStreamBuffer been created using Strings that have been interned
     * for certain properties of information items. The Strings that are interned 
     * are those that correspond to Strings that are specified by the SAX API 
     * "string-interning" property
     * (see <a href="http://java.sun.com/j2se/1.5.0/docs/api/org/xml/sax/package-summary.html#package_description">here</a>).
     *
     * <p>
     * An XMLStreamBuffer may have been created, for example, from an XML document parsed
     * using the Xerces SAX parser. The Xerces SAX parser will have interned certain Strings
     * according to the SAX string interning property.
     * This method enables processors to avoid the duplication of 
     * String interning if such a feature is required by a procesing application and the 
     * XMLStreamBuffer being processed was created using Strings that have been interned.
     *
     * @return
     * <code>true</code> if the XMLStreamBuffer has been created using Strings that
     * have been interned.
     */
    public boolean hasInternedStrings() {
        return _hasInternedStrings;
    }
    
    /**
     * Process using the {@link StreamReaderBufferProcessor} for StAX related
     * processing.
     *
     * <p>
     * The XMLStreamBuffer can be processed using XMLStreamReader on 
     * {@link StreamReaderBufferProcessor}.
     *
     * @return
     * A an instance of a {@link StreamReaderBufferProcessor}.
     */
    public StreamReaderBufferProcessor processUsingStreamReaderBufferProcessor() throws XMLStreamException {
        return new StreamReaderBufferProcessor(this);
    }
    
    /**
     * Process using XMLStreamReader.
     *
     * @return
     * A XMLStreamReader to read.
     */
    public XMLStreamReader processUsingXMLStreamReader() throws XMLStreamException {
        return processUsingStreamReaderBufferProcessor();
    }
    
    /**
     * Create from a XMLStreamReader.
     *
     * <p>
     * The XMLStreamBuffer is reset (see {@link #reset}) before creation.
     *
     * <p>
     * The XMLStreamBuffer is created by consuming the events on the XMLStreamReader using
     * an instance of {@link StreamReaderBufferCreator}.
     *
     * @param reader
     * A XMLStreamReader to read from to create.
     */
    public void createFromXMLStreamReader(XMLStreamReader reader) throws XMLStreamException, XMLStreamBufferException {
        reset();
        StreamReaderBufferCreator c = new StreamReaderBufferCreator(this);
        c.create(reader);
    }
    
    /**
     * Process using XMLStreamWriter.
     *
     * <p>
     * The XMLStreamBuffer will be written out to the XMLStreamWriter using
     * an instance of {@link StreamWriterBufferProcessor}.
     *
     * @param writer
     * A XMLStreamWriter to write to.
     */
    public void processUsingXMLStreamWriter(XMLStreamWriter writer) throws XMLStreamException, XMLStreamBufferException {
        StreamWriterBufferProcessor p = new StreamWriterBufferProcessor(this);
        p.process(writer);
    }
    
    /**
     * Create from a XMLStreamWriter.
     *
     * <p>
     * The XMLStreamBuffer is reset (see {@link #reset}) before creation.
     *
     * <p>
     * The XMLStreamBuffer is created by consuming events on a XMLStreamWriter using
     * an instance of {@link StreamWriterBufferCreator}.
     */
    public XMLStreamWriter createFromXMLStreamWriter() throws XMLStreamBufferException {
        reset();
        return new StreamWriterBufferCreator(this);
    }
    
    /**
     * Process using the {@link SAXBufferProcessor} for SAX related
     * processing.
     *
     * <p>
     * The XMLStreamBuffer can be processed using XMLReader on 
     * {@link SAXBufferProcessor}.
     *
     * @return
     * A an instance of a {@link SAXBufferProcessor}.
     */
    public SAXBufferProcessor processUsingSAXBufferProcessor() {
        return new SAXBufferProcessor(this);
    }
    
    /**
     * Process using {@link ContentHandler}.
     *
     * <p>
     * If the <code>handler</code> is also an instance of other SAX-based
     * handlers, such as {@link LexicalHandler}, than corresponding SAX events
     * will be reported to those handlers.
     *
     * @param handler
     * The ContentHandler to receive SAX events.
     */
    public void processUsingSAXContentHandler(ContentHandler handler) throws XMLStreamBufferException {
        SAXBufferProcessor p = processUsingSAXBufferProcessor();
        p.setContentHandler(handler);
        if (p instanceof LexicalHandler) {
            p.setLexicalHandler((LexicalHandler)handler);
        }
        if (p instanceof DTDHandler) {
            p.setDTDHandler((DTDHandler)handler);
        }
        if (p instanceof ErrorHandler) {
            p.setErrorHandler((ErrorHandler)handler);
        }
        p.process();
    }
    
    /**
     * Process using {@link ContentHandler} and {@link ErrorHandler}.
     *
     * <p>
     * If the <code>handler</code> is also an instance of other SAX-based
     * handlers, such as {@link LexicalHandler}, than corresponding SAX events
     * will be reported to those handlers.
     *
     * @param handler
     * The ContentHandler to receive SAX events.
     * @param errorHandler
     * The ErrorHandler to receive error events.
     */
    public void processUsingSAXContentHandler(ContentHandler handler, ErrorHandler errorHandler) throws XMLStreamBufferException {
        SAXBufferProcessor p = processUsingSAXBufferProcessor();
        p.setContentHandler(handler);
        if (p instanceof LexicalHandler) {
            p.setLexicalHandler((LexicalHandler)handler);
        }
        if (p instanceof DTDHandler) {
            p.setDTDHandler((DTDHandler)handler);
        }
        
        p.setErrorHandler(errorHandler);
        
        p.process();
    }
    
    /**
     * Create from a {@link SAXBufferCreator}.
     *
     * <p>
     * The XMLStreamBuffer is reset (see {@link #reset}) before creation.
     *
     * <p>
     * The XMLStreamBuffer is created by consuming events from a {@link ContentHandler} using
     * an instance of {@link SAXBufferCreator}.
     *
     * @return
     * The {@link SAXBufferCreator} to create from.
     */
    public SAXBufferCreator createFromSAXBufferCreator() throws XMLStreamBufferException {
        reset();
        SAXBufferCreator c = new SAXBufferCreator();
        c.setBuffer(this);
        return c;
    }
    
    /**
     * Create from a {@link XMLReader} and {@link InputStream}.
     *
     * <p>
     * The XMLStreamBuffer is reset (see {@link #reset}) before creation.
     *
     * <p>
     * The XMLStreamBuffer is created by using an instance of {@link SAXBufferCreator}
     * and registering associated handlers on the {@link XMLReader}.
     *
     * @param reader
     * The {@link XMLReader} to use for parsing.
     * @param in
     * The {@link InputStream} to be parsed.
     */
    public void createFromXMLReader(XMLReader reader, InputStream in) throws XMLStreamBufferException, SAXException, IOException {
        reset();
        SAXBufferCreator c = new SAXBufferCreator(this);
        
        reader.setContentHandler(c);
        reader.setDTDHandler(c);
        reader.setProperty(Properties.LEXICAL_HANDLER_PROPERTY, c);
        
        c.create(reader, in);
    }
    
    /**
     * Reset the XMLStreamBuffer.
     *
     * <p>
     * This method will reset the XMLStreamBuffer to a state of being "uncreated"
     * similar to the state of a newly instantiated XMLStreamBuffer.
     *
     * <p>
     * As many Objects as possible will be retained for reuse in future creation.
     *
     * @throws XMLStreamBufferException
     * If the reset cannot be performed, for example if the XMLStreamBuffer
     * is a mark (see {@link XMLStreamBufferMark}).
     *
     */
    public void reset() throws XMLStreamBufferException {
        // Reset the ptrs in arrays to 0
        _structurePtr =
                _structureStringsPtr =
                _contentStringsPtr =
                _contentCharactersPtr =
                _contentCharactersBufferPtr = 0;
        
        // Set the first element of structure array to indicate an empty buffer 
        // that has not been created
        _structure.getArray()[0] = AbstractCreatorProcessor.T_END;

        // Clean up content strings
        _contentStrings.setNext(null);
        final String[] s = _contentStrings.getArray();
        for (int i = 0; i < s.length; i++) {
            s[i] = null;
        }
        
        // Clean up content characters
        _contentCharacters.setNext(null);
        final char[][] c = _contentCharacters.getArray();
        for (int i = 0; i < c.length; i++) {
            c[i] = null;
        }
        
        /*
         * TODO consider truncating the size of _structureStrings and
         * _contentCharactersBuffer to limit the memory used by the buffer
         */
    }
    
    
    protected void setHasInternedStrings(boolean hasInternedStrings) {
        _hasInternedStrings = hasInternedStrings;
    }
    
    protected FragmentedArray<int[]> getStructure() {
        return _structure;
    }
    
    protected int getStructurePtr() {
        return _structurePtr;
    }
    
    protected FragmentedArray<String[]> getStructureStrings() {
        return _structureStrings;
    }
    
    protected int getStructureStringsPtr() {
        return _structureStringsPtr;
    }
    
    protected FragmentedArray<String[]> getContentStrings() {
        return _contentStrings;
    }
    
    protected int getContentStringsPtr() {
        return _contentStringsPtr;
    }
    
    protected FragmentedArray<char[][]> getContentCharacters() {
        return _contentCharacters;
    }
    
    protected int getContentCharactersPtr() {
        return _contentCharactersPtr;
    }
    
    protected FragmentedArray<char[]> getContentCharactersBuffer() {
        return _contentCharactersBuffer;
    }
    
    protected int getContentCharactersBufferPtr() {
        return _contentCharactersBufferPtr;
    }
    
}