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
import com.sun.xml.stream.buffer.stax.StreamReaderBufferCreator;
import com.sun.xml.stream.buffer.stax.StreamWriterBufferCreator;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * A mutable stream-based buffer of an XML infoset.
 * 
 * <p>
 * A MutableXMLStreamBuffer is created using specific SAX and StAX-based 
 * creators. Utility methods on MutableXMLStreamBuffer are provided for 
 * such functionality that utilize SAX and StAX-based creators.
 * 
 * <p>
 * Once instantiated the same instance of a MutableXMLStreamBuffer may be reused for
 * creation to reduce the amount of Objects instantiated and garbage
 * collected that are required for internally representing an XML infoset.
 * 
 * <p>
 * A MutableXMLStreamBuffer is not designed to be created and processed 
 * concurrently. If done so unspecified behaviour may occur.
 */
public class MutableXMLStreamBuffer extends XMLStreamBuffer {
    /**
     * The default array size for the arrays used in internal representation 
     * of the XML infoset.
     */
    public static int DEFAULT_ARRAY_SIZE = 512;
    
    /**
     * Create a new MutableXMLStreamBuffer using the 
     * {@link MutableXMLStreamBuffer#DEFAULT_ARRAY_SIZE}.
     */
    public MutableXMLStreamBuffer() {
        this(DEFAULT_ARRAY_SIZE);
    }
    
    /**
     * Create a new MutableXMLStreamBuffer.
     * 
     * @param size
     * The size of the arrays used in the internal representation 
     * of the XML infoset.
     * @throws NegativeArraySizeException
     * If the <code>size</code> argument is less than <code>0</code>.
     */
    public MutableXMLStreamBuffer(int size) {
        _structure = new FragmentedArray<byte[]>(new byte[size]);
        _structureStrings = new FragmentedArray<String[]>(new String[size]);
        _contentCharactersBuffer = new FragmentedArray<char[]>(new char[4096]);
        _contentObjects = new FragmentedArray<Object[]>(new Object[size]);

        // Set the first element of structure array to indicate an empty buffer
        // that has not been created
        _structure.getArray()[0] = AbstractCreatorProcessor.T_END;
    }

    /**
     * Create contents of a buffer from a XMLStreamReader.
     * 
     * <p>
     * The MutableXMLStreamBuffer is reset (see {@link #reset}) before creation.
     * 
     * <p>
     * The MutableXMLStreamBuffer is created by consuming the events on the XMLStreamReader using
     * an instance of {@link StreamReaderBufferCreator}.
     * 
     * @param reader
     * A XMLStreamReader to read from to create.
     */
    public void createFromXMLStreamReader(XMLStreamReader reader) throws XMLStreamException {
        reset();
        StreamReaderBufferCreator c = new StreamReaderBufferCreator(this);
        c.create(reader);
    }

    /**
     * Create contents of a buffer from a XMLStreamWriter.
     * 
     * <p>
     * The MutableXMLStreamBuffer is reset (see {@link #reset}) before creation.
     * 
     * <p>
     * The MutableXMLStreamBuffer is created by consuming events on a XMLStreamWriter using
     * an instance of {@link StreamWriterBufferCreator}.
     */
    public XMLStreamWriter createFromXMLStreamWriter() {
        reset();
        return new StreamWriterBufferCreator(this);
    }

    /**
     * Create contents of a buffer from a {@link SAXBufferCreator}.
     * 
     * <p>
     * The MutableXMLStreamBuffer is reset (see {@link #reset}) before creation.
     * 
     * <p>
     * The MutableXMLStreamBuffer is created by consuming events from a {@link ContentHandler} using
     * an instance of {@link SAXBufferCreator}.
     * 
     * @return The {@link SAXBufferCreator} to create from.
     */
    public SAXBufferCreator createFromSAXBufferCreator() {
        reset();
        SAXBufferCreator c = new SAXBufferCreator();
        c.setBuffer(this);
        return c;
    }

    /**
     * Create contents of a buffer from a {@link XMLReader} and {@link InputStream}.
     * 
     * <p>
     * The MutableXMLStreamBuffer is reset (see {@link #reset}) before creation.
     * 
     * <p>
     * The MutableXMLStreamBuffer is created by using an instance of {@link SAXBufferCreator}
     * and registering associated handlers on the {@link XMLReader}.
     * 
     * @param reader
     * The {@link XMLReader} to use for parsing.
     * @param in
     * The {@link InputStream} to be parsed.
     */
    public void createFromXMLReader(XMLReader reader, InputStream in) throws SAXException, IOException {
        createFromXMLReader(reader, in, null);
    }

    /**
     * Create contents of a buffer from a {@link XMLReader} and {@link InputStream}.
     * 
     * <p>
     * The MutableXMLStreamBuffer is reset (see {@link #reset}) before creation.
     * 
     * <p>
     * The MutableXMLStreamBuffer is created by using an instance of {@link SAXBufferCreator}
     * and registering associated handlers on the {@link XMLReader}.
     * 
     * @param reader
     * The {@link XMLReader} to use for parsing.
     * @param in
     * The {@link InputStream} to be parsed.
     * @param systemId
     * The system ID of the input stream.
     */
    public void createFromXMLReader(XMLReader reader, InputStream in, String systemId) throws SAXException, IOException {
        reset();
        SAXBufferCreator c = new SAXBufferCreator(this);

        reader.setContentHandler(c);
        reader.setDTDHandler(c);
        reader.setProperty(Properties.LEXICAL_HANDLER_PROPERTY, c);

        c.create(reader, in, systemId);
    }
    
    /**
     * Reset the MutableXMLStreamBuffer.
     * 
     * <p>
     * This method will reset the MutableXMLStreamBuffer to a state of being "uncreated"
     * similar to the state of a newly instantiated MutableXMLStreamBuffer.
     * 
     * <p>
     * As many Objects as possible will be retained for reuse in future creation.
     */
    public void reset() {
        // Reset the ptrs in arrays to 0
        _structurePtr =
                _structureStringsPtr =
                _contentCharactersBufferPtr = 
                _contentObjectsPtr = 0;

        // Set the first element of structure array to indicate an empty buffer
        // that has not been created
        _structure.getArray()[0] = AbstractCreatorProcessor.T_END;

        // Clean up content objects
        _contentObjects.setNext(null);
        final Object[] o = _contentObjects.getArray();
        for (int i = 0; i < o.length; i++) {
            if (o[i] != null) {
                o[i] = null;
            } else {
                break;
            }
        }
        
        /*
         * TODO consider truncating the size of _structureStrings and
         * _contentCharactersBuffer to limit the memory used by the buffer
         */
    }
    
    
    protected void setHasInternedStrings(boolean hasInternedStrings) {
        _hasInternedStrings = hasInternedStrings;
    }
}