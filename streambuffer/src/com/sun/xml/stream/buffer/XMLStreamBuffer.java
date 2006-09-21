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
import com.sun.xml.stream.buffer.stax.StreamReaderBufferProcessor;
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
 * An immutable stream-based buffer of an XML infoset.
 * 
 * <p>
 * A XMLStreamBuffer is an abstract class. It is immutable with 
 * respect to the methods on the class, which are non-modifying in terms
 * of state.
 * 
 * <p>
 * A XMLStreamBuffer can be processed using specific SAX and StAX-based 
 * processors. Utility methods on XMLStreamBuffer are provided for 
 * such functionality that utilize SAX and StAX-based processors.
 * The same instance of a XMLStreamBuffer may be processed 
 * multiple times and concurrently by more than one processor.
 * 
 * <p>
 * There are two concrete implementations of XMLStreamBuffer.
 * The first, {@link XMLStreamBuffer}, can be instantiated for the creation 
 * of a buffer using SAX and StAX-based creators, and from which may be 
 * processed as an XMLStreamBuffer. The second,
 * {@link XMLStreamBufferMark}, can be instantiated to mark into an existing
 * buffer that is being created or processed. Thus one or more 
 * XMLStreamBuffer instances may make reference to other buffers. It
 * is the responsibility of the application to manage the relationship between
 * such instances.
 * 
 * <p>
 * A XMLStreamBuffer can represent a complete XML infoset or a fragment 
 * of an XML infoset. For example, a fragment of an XML infoset may be 
 * represented as a mark into an buffer that is being created or processed, see 
 * {@link XMLStreamBufferMark}.
 */
public abstract class XMLStreamBuffer {

    /**
     * In scope namespaces on a fragment
     */
    protected Map<String,String> _inscopeNamespaces = Collections.emptyMap();
    
    /**
     * True if the buffer was created from a parser that interns Strings
     * as specified by the SAX interning features
     */
    protected boolean _hasInternedStrings;

    /**
     * Fragmented array to hold structural information
     */
    protected FragmentedArray<byte[]> _structure;
    protected int _structurePtr;
    
    /**
     * Fragmented array to hold structural information as strings
     */
    protected FragmentedArray<String[]> _structureStrings;
    protected int _structureStringsPtr;
    
    /**
     * Fragmented array to hold content information in a shared char[]
     */
    protected FragmentedArray<char[]> _contentCharactersBuffer;
    protected int _contentCharactersBufferPtr;

    /**
     * Fragmented array to hold content information as objects
     */
    protected FragmentedArray<Object[]> _contentObjects;
    protected int _contentObjectsPtr;

    
    /**
     * Is the buffer created by creator.
     *
     * @return
     * <code>true</code> if the buffer has been created.
     */
    public final boolean isCreated() {
        return _structure.getArray()[0] != AbstractCreatorProcessor.T_END;
    }

    /**
     * Is the buffer a representation of a fragment of an XML infoset.
     *
     * @return
     * <code>true</code> if the buffer is a representation of a fragment
     * of an XML infoset.
     */
    public final boolean isFragment() {
        return (isCreated() && (_structure.getArray()[_structurePtr] & AbstractCreatorProcessor.TYPE_MASK)
                != AbstractCreatorProcessor.T_DOCUMENT);
    }

    /**
     * Is the buffer a representation of a fragment of an XML infoset
     * that is an element (and its contents).
     *
     * @return
     * <code>true</code> if the buffer a representation
     * of a fragment of an XML infoset that is an element (and its contents).
     */
    public final boolean isElementFragment() {
        return (isCreated() && (_structure.getArray()[_structurePtr] & AbstractCreatorProcessor.TYPE_MASK)
                == AbstractCreatorProcessor.T_ELEMENT);
    }

    /**
     * Get the in-scope namespaces.
     *
     * <p>
     *
     * The in-scope namespaces will be empty if the buffer is not a
     * fragment ({@link #isFragment} returns <code>false</code>).
     *
     * The in-scope namespace will correspond to the in-scope namespaces of the
     * fragment if the buffer is a fragment ({@link #isFragment}
     * returns <code>false</code>). The in-scope namespaces will include any
     * namespace delcarations on an element if the fragment correspond to that
     * of an element ({@link #isElementFragment} returns <code>false</code>).
     *
     * @return
     *      The in-scope namespaces of the XMLStreamBuffer.
     *      Prefix to namespace URI.
     */
    public final Map<String,String> getInscopeNamespaces() {
        return _inscopeNamespaces;
    }

    /**
     * Has the buffer been created using Strings that have been interned
     * for certain properties of information items. The Strings that are interned
     * are those that correspond to Strings that are specified by the SAX API
     * "string-interning" property
     * (see <a href="http://java.sun.com/j2se/1.5.0/docs/api/org/xml/sax/package-summary.html#package_description">here</a>).
     *
     * <p>
     * An buffer may have been created, for example, from an XML document parsed
     * using the Xerces SAX parser. The Xerces SAX parser will have interned certain Strings
     * according to the SAX string interning property.
     * This method enables processors to avoid the duplication of
     * String interning if such a feature is required by a procesing application and the
     * buffer being processed was created using Strings that have been interned.
     *
     * @return
     * <code>true</code> if the buffer has been created using Strings that
     * have been interned.
     */
    public final boolean hasInternedStrings() {
        return _hasInternedStrings;
    }

    /**
     * Read the contents of the buffer as a {@link XMLStreamReader}.
     *
     * @return
     * A an instance of a {@link StreamReaderBufferProcessor}. Always non-null.
     */
    public final StreamReaderBufferProcessor readAsXMLStreamReader() throws XMLStreamException {
        return new StreamReaderBufferProcessor(this);
    }

    /**
     * Write the contents of the buffer to an XMLStreamWriter.
     *
     * <p>
     * The XMLStreamBuffer will be written out to the XMLStreamWriter using
     * an instance of {@link StreamWriterBufferProcessor}.
     *
     * @param writer
     * A XMLStreamWriter to write to.
     */
    public final void writeToXMLStreamWriter(XMLStreamWriter writer) throws XMLStreamException, XMLStreamBufferException {
        StreamWriterBufferProcessor p = new StreamWriterBufferProcessor(this);
        p.process(writer);
    }

    /**
     * Reads the contents of the buffer from a {@link XMLReader}.
     *
     * @return
     * A an instance of a {@link SAXBufferProcessor}.
     */
    public final SAXBufferProcessor readAsXMLReader() {
        return new SAXBufferProcessor(this);
    }

    /**
     * Write the contents of the buffer to a {@link ContentHandler}.
     *
     * <p>
     * If the <code>handler</code> is also an instance of other SAX-based
     * handlers, such as {@link LexicalHandler}, than corresponding SAX events
     * will be reported to those handlers.
     *
     * @param handler
     *      The ContentHandler to receive SAX events.
     *
     * @throws SAXException
     *      if a parsing fails, or if {@link ContentHandler} throws a {@link SAXException}.
     */
    public final void writeTo(ContentHandler handler) throws SAXException {
        SAXBufferProcessor p = readAsXMLReader();
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
     * Write the contents of the buffer to a {@link ContentHandler} with errors
     * report to a {@link ErrorHandler}.
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
     *
     * @throws SAXException
     *      if a parsing fails and {@link ErrorHandler} throws a {@link SAXException},
     *      or if {@link ContentHandler} throws a {@link SAXException}.
     */
    public final void writeTo(ContentHandler handler, ErrorHandler errorHandler) throws SAXException {
        SAXBufferProcessor p = readAsXMLReader();
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
     * Create a new buffer from a XMLStreamReader.
     * 
     * @param reader
     * A XMLStreamReader to read from to create.
     * @return XMLStreamBuffer the created buffer
     * @see MutableXMLStreamBuffer#createFromXMLStreamReader(XMLStreamReader)
     */
    public static XMLStreamBuffer createNewBufferFromXMLStreamReader(XMLStreamReader reader)
            throws XMLStreamException, XMLStreamBufferException {
        MutableXMLStreamBuffer b = new MutableXMLStreamBuffer();
        b.createFromXMLStreamReader(reader);
        return b;
    }

    /**
     * Create a new buffer from a {@link XMLReader} and {@link InputStream}.
     * 
     * @param reader
     * The {@link XMLReader} to use for parsing.
     * @param in
     * The {@link InputStream} to be parsed.
     * @return XMLStreamBuffer the created buffer
     * @see MutableXMLStreamBuffer#createFromXMLReader(XMLReader, InputStream)
     */
    public static XMLStreamBuffer createNewBufferFromXMLReader(XMLReader reader, InputStream in) throws XMLStreamBufferException, SAXException, IOException {
        MutableXMLStreamBuffer b = new MutableXMLStreamBuffer();
        b.createFromXMLReader(reader, in);
        return b;
    }

    /**
     * Create a new buffer from a {@link XMLReader} and {@link InputStream}.
     *
     * @param reader
     * The {@link XMLReader} to use for parsing.
     * @param in
     * The {@link InputStream} to be parsed.
     * @param systemId
     * The system ID of the input stream.
     * @return XMLStreamBuffer the created buffer
     * @see MutableXMLStreamBuffer#createFromXMLReader(XMLReader, InputStream, String) 
     */
    public static XMLStreamBuffer createNewBufferFromXMLReader(XMLReader reader, InputStream in,
            String systemId) throws XMLStreamBufferException, SAXException, IOException {
        MutableXMLStreamBuffer b = new MutableXMLStreamBuffer();
        b.createFromXMLReader(reader, in, systemId);
        return b;
    }

    protected final FragmentedArray<byte[]> getStructure() {
        return _structure;
    }
    
    protected final int getStructurePtr() {
        return _structurePtr;
    }
    
    protected final FragmentedArray<String[]> getStructureStrings() {
        return _structureStrings;
    }
    
    protected final int getStructureStringsPtr() {
        return _structureStringsPtr;
    }
        
    protected final FragmentedArray<char[]> getContentCharactersBuffer() {
        return _contentCharactersBuffer;
    }
    
    protected final int getContentCharactersBufferPtr() {
        return _contentCharactersBufferPtr;
    }
    
    protected final FragmentedArray<Object[]> getContentObjects() {
        return _contentObjects;
    }
    
    protected final int getContentObjectsPtr() {
        return _contentObjectsPtr;
    }
}