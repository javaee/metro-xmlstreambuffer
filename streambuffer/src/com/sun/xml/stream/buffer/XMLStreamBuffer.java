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

public class XMLStreamBuffer {
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
    
    public XMLStreamBuffer() {
        this(DEFAULT_ARRAY_SIZE);
    }
    
    public XMLStreamBuffer(int size) {
        _structure = new FragmentedArray(new int[size], size);
        _structureStrings = new FragmentedArray(new String[size], size);
        _contentStrings = new FragmentedArray(new String[size], size);
        _contentCharacters = new FragmentedArray(new char[size][], size);
        _contentCharactersBuffer = new FragmentedArray(new char[4096], size);
    }
    
    public boolean isMark() {
        return this instanceof XMLStreamBufferMark;
    }
    
    public Map<String, String> getInscopeNamespaces() {
        return _inscopeNamespaces;
    }
    
    public boolean getHasInternedStrings() {
        return _hasInternedStrings;
    }
    
    public StreamReaderBufferProcessor processUsingStreamReaderBufferProcessor() {
        return new StreamReaderBufferProcessor(this);
    }
    
    public XMLStreamReader processUsingXMLStreamReader() {
        return processUsingStreamReaderBufferProcessor();
    }

    public void createFromXMLStreamReader(XMLStreamReader reader) throws XMLStreamException, XMLStreamBufferException {
        StreamReaderBufferCreator c = new StreamReaderBufferCreator(this);
        c.create(reader);
    }

    public void processUsingXMLStreamWriter(XMLStreamWriter writer) throws XMLStreamException, XMLStreamBufferException {
        StreamWriterBufferProcessor p = new StreamWriterBufferProcessor(this);
        p.process(writer);
    }

    public XMLStreamWriter createFromXMLStreamWriter() {
        return new StreamWriterBufferCreator(this);
    }
    
    public SAXBufferProcessor processUsingSAXBufferProcessor() {
        return new SAXBufferProcessor(this);
    }
    
    public void processUsingSAXContentHandler(ContentHandler handler) {
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
    }
    
    public void processUsingSAXContentHandler(ContentHandler handler, ErrorHandler errorHandler) {
        SAXBufferProcessor p = processUsingSAXBufferProcessor();
        p.setContentHandler(handler);
        if (p instanceof LexicalHandler) {
            p.setLexicalHandler((LexicalHandler)handler);
        }
        if (p instanceof DTDHandler) {
            p.setDTDHandler((DTDHandler)handler);
        }
        
        p.setErrorHandler(errorHandler);
    }
    
    public SAXBufferCreator createFromSAXBufferCreator() {
        reset();
        SAXBufferCreator c = new SAXBufferCreator();
        c.setBuffer(this);
        return c;
    }
    
    public void createFromXMLReader(XMLReader reader, InputStream in) throws SAXException, IOException {
        reset();
        SAXBufferCreator c = new SAXBufferCreator(this);
        
        reader.setContentHandler(c);
        reader.setDTDHandler(c);
        reader.setProperty(Properties.LEXICAL_HANDLER_PROPERTY, c);
        
        c.create(reader, in);
    }
    
    protected void setHasInternedStrings(boolean hasInternedStrings) {
        _hasInternedStrings = hasInternedStrings;
    }
    
    protected void reset() {
        _structurePtr = 
                _structureStringsPtr =
                _contentStringsPtr =
                _contentCharactersPtr = 
                _contentCharactersBufferPtr = 0;

        // Reset the size of some arrays
        final int size = _structure.getArray().length;
        _structure.setSize(size);
        _structureStrings.setSize(size);
        
        _contentStrings.setNext(null);
        final String[] s = _contentStrings.getArray();
        for (int i = 0; i < _contentStrings.getSize(); i++) {
            s[i] = null;
        }
        _contentStrings.setSize(size);
        
        _contentCharacters.setNext(null);
        final char[][] c = _contentCharacters.getArray();
        for (int i = 0; i < _contentCharacters.getSize(); i++) {
            c[i] = null;
        }
        _contentStrings.setSize(size);
        
        /* 
         * TODO consider truncating the size of _structureStrings and
         * _contentCharactersBuffer to limit the memory used by the buffer
         */
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