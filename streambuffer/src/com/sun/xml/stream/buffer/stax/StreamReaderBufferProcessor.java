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
package com.sun.xml.stream.buffer.stax;

import com.sun.xml.stream.buffer.AbstractProcessor;
import com.sun.xml.stream.buffer.AttributesHolder;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import org.jvnet.staxex.NamespaceContextEx;
import org.jvnet.staxex.XMLStreamReaderEx;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * {@link XMLStreamReader} implementation that reads the infoset from
 * {@link XMLStreamBuffer}.
 *
 * @author Paul.Sandoz@Sun.Com
 * @author K.Venugopal@sun.com
 */
public class StreamReaderBufferProcessor extends AbstractProcessor implements XMLStreamReaderEx {
    private static final int CACHE_SIZE = 16;

    // Stack to hold element and namespace declaration information
    protected ElementStackEntry[] _stack = new ElementStackEntry[CACHE_SIZE];
    protected ElementStackEntry _stackEntry;
    protected int _depth;

    // Arrays to hold all namespace declarations
    protected String[] _namespaceAIIsPrefix = new String[CACHE_SIZE];
    protected String[] _namespaceAIIsNamespaceName = new String[CACHE_SIZE];
    protected int _namespaceAIIsIndex;

    // Internal namespace context implementation
    protected InternalNamespaceContext _nsCtx = new InternalNamespaceContext();

    // The current event type
    protected int _eventType;

    // Holder of the attributes
    protected AttributesHolder _attributeCache;

    // Characters as a CharSequence
    protected CharSequence _charSequence;

    // Characters as a char array with offset and length
    protected char[] _characters;
    protected int _textOffset;
    protected int _textLen;

    protected String _piTarget;
    protected String _piData;

    /**
     * True if processing a document, otherwise an element fragment.
     */
    private boolean _isDocument;

    //
    // Represents the parser state wrt the end of parsing.
    //
    /**
     * The parser is in the middle of parsing a document,
     * with no end in sight.
     */
    private static final int PARSING = 1;
    /**
     * The parser has already reported the {@link END_ELEMENT},
     * and we are parsing a fragment. We'll report {@link END_DOCUMENT}
     * next and be done.
     */
    private static final int PENDING_END_DOCUMENT = 2;
    /**
     * The parser has reported the {@link END_DOCUMENT} event,
     * so we are really done parsing.
     */
    private static final int COMPLETED = 3;

    /**
     * True if processing is complete.
     */
    private int _completionState;

    public StreamReaderBufferProcessor() {
        for (int i=0; i < _stack.length; i++){
            _stack[i] = new ElementStackEntry();
        }

        _attributeCache = new AttributesHolder();
    }

    public StreamReaderBufferProcessor(XMLStreamBuffer buffer) throws XMLStreamException {
        this();

        setXMLStreamBuffer(buffer);
    }

    public void setXMLStreamBuffer(XMLStreamBuffer buffer) throws XMLStreamException {
        setBuffer(buffer);

        _completionState = PARSING;
        _namespaceAIIsIndex = 0;
        _characters = null;
        _charSequence = null;

        processFirstEvent(buffer);
    }

    public Object getProperty(java.lang.String name) {
        return null;
    }

    public int next() throws XMLStreamException {
        switch(_completionState) {
            case COMPLETED:
                throw new XMLStreamException("Invalid State");
            case PENDING_END_DOCUMENT:
                _completionState = COMPLETED;
                return _eventType = END_DOCUMENT;
        }

        _characters = null;
        _charSequence = null;
        switch(_eiiStateTable[readStructure()]) {
            case STATE_ELEMENT_U_LN_QN: {
                final String uri = readStructureString();
                final String localName = readStructureString();
                final String prefix = getPrefixFromQName(readStructureString());

                processElement(prefix, uri, localName);
                return _eventType = START_ELEMENT;
            }
            case STATE_ELEMENT_P_U_LN:
                processElement(readStructureString(), readStructureString(), readStructureString());
                return _eventType = START_ELEMENT;
            case STATE_ELEMENT_U_LN:
                processElement(null, readStructureString(), readStructureString());
                return _eventType = START_ELEMENT;
            case STATE_ELEMENT_LN:
                processElement(null, null, readStructureString());
                return _eventType = START_ELEMENT;
            case STATE_TEXT_AS_CHAR_ARRAY:
                _textLen = readStructure();
                _textOffset = readContentCharactersBuffer(_textLen);
                _characters = _contentCharactersBuffer;

                return _eventType = CHARACTERS;
            case STATE_TEXT_AS_CHAR_ARRAY_COPY:
                _characters = readContentCharactersCopy();
                _textLen = _characters.length;
                _textOffset = 0;

                return _eventType = CHARACTERS;
            case STATE_TEXT_AS_STRING:
                _eventType = CHARACTERS;
                _charSequence = readContentString();

                return _eventType = CHARACTERS;
            case STATE_TEXT_AS_OBJECT:
                _eventType = CHARACTERS;
                _charSequence = (CharSequence)readContentObject();

                return _eventType = CHARACTERS;
            case STATE_COMMENT_AS_CHAR_ARRAY:
                _textLen = readStructure();
                _textOffset = readContentCharactersBuffer(_textLen);
                _characters = _contentCharactersBuffer;

                return _eventType = COMMENT;
            case STATE_COMMENT_AS_CHAR_ARRAY_COPY:
                _characters = readContentCharactersCopy();
                _textLen = _characters.length;
                _textOffset = 0;

                return _eventType = COMMENT;
            case STATE_COMMENT_AS_STRING:
                _charSequence = readContentString();

                return _eventType = COMMENT;
            case STATE_PROCESSING_INSTRUCTION:
                _piTarget = readStructureString();
                _piData = readStructureString();

                return _eventType = PROCESSING_INSTRUCTION;
            case STATE_END:
                if (_depth > 1) {
                    popElementStack();
                    return _eventType = END_ELEMENT;
                } else if (_depth == 1) {
                    popElementStack();
                    if (!_isDocument) {
                        _completionState = PENDING_END_DOCUMENT;
                    }
                    return _eventType = END_ELEMENT;
                } else {
                    _completionState = COMPLETED;
                    return _eventType = END_DOCUMENT;
                }
            default:
                throw new XMLStreamException("Invalid State");
        }
    }

    public final void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        if( type != _eventType) {
            throw new XMLStreamException("");
        }
        if( namespaceURI != null && !namespaceURI.equals(getNamespaceURI())) {
            throw new XMLStreamException("");
        }
        if(localName != null && !localName.equals(getLocalName())) {
            throw new XMLStreamException("");
        }
    }

    public final String getElementTextTrim() throws XMLStreamException {
        // TODO getElementText* methods more efficiently
        return getElementText().trim();
    }

    public final String getElementText() throws XMLStreamException {
        if(_eventType != START_ELEMENT) {
            throw new XMLStreamException("");
        }

        next();
        return getElementText(true);
    }

    public final String getElementText(boolean startElementRead) throws XMLStreamException {
        if (!startElementRead) {
            throw new XMLStreamException("");
        }

        int eventType = getEventType();
        StringBuffer content = new StringBuffer();
        while(eventType != END_ELEMENT ) {
            if(eventType == CHARACTERS
                    || eventType == CDATA
                    || eventType == SPACE
                    || eventType == ENTITY_REFERENCE) {
                content.append(getText());
            } else if(eventType == PROCESSING_INSTRUCTION
                    || eventType == COMMENT) {
                // skipping
            } else if(eventType == END_DOCUMENT) {
                throw new XMLStreamException("");
            } else if(eventType == START_ELEMENT) {
                throw new XMLStreamException("");
            } else {
                throw new XMLStreamException("");
            }
            eventType = next();
        }
        return content.toString();
    }

    public final int nextTag() throws XMLStreamException {
        next();
        return nextTag(true);
    }

    public final int nextTag(boolean currentTagRead) throws XMLStreamException {
        int eventType = getEventType();
        if (!currentTagRead) {
            eventType = next();
        }
        while((eventType == CHARACTERS && isWhiteSpace()) // skip whitespace
        || (eventType == CDATA && isWhiteSpace())
        || eventType == SPACE
                || eventType == PROCESSING_INSTRUCTION
                || eventType == COMMENT) {
            eventType = next();
        }
        if (eventType != START_ELEMENT && eventType != END_ELEMENT) {
            throw new XMLStreamException("");
        }
        return eventType;
    }

    public final boolean hasNext() {
        return (_eventType != END_DOCUMENT);
    }

    public void close() throws XMLStreamException {
    }

    public final boolean isStartElement() {
        return (_eventType == START_ELEMENT);
    }

    public final boolean isEndElement() {
        return (_eventType == END_ELEMENT);
    }

    public final boolean isCharacters() {
        return (_eventType == CHARACTERS);
    }

    public final boolean isWhiteSpace() {
        if(isCharacters() || (_eventType == CDATA)){
            char [] ch = this.getTextCharacters();
            int start = this.getTextStart();
            int length = this.getTextLength();
            for (int i = start; i < length; i++){
                final char c = ch[i];
                if (c == 0x20 || c == 0x9 || c == 0xD | c == 0xA) {
                    continue;
                } else {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public final String getAttributeValue(String namespaceURI, String localName) {
        if (_eventType != START_ELEMENT) {
            throw new IllegalStateException("");
        }

        if (namespaceURI == null) {
            // Set to the empty string to be compatible with the
            // org.xml.sax.Attributes interface
            namespaceURI = "";
        }

        return _attributeCache.getValue(namespaceURI, localName);
    }

    public final int getAttributeCount() {
        if (_eventType != START_ELEMENT) {
            throw new IllegalStateException("");
        }

        return _attributeCache.getLength();
    }

    public final javax.xml.namespace.QName getAttributeName(int index) {
        if (_eventType != START_ELEMENT) {
            throw new IllegalStateException("");
        }

        final String prefix = _attributeCache.getPrefix(index);
        final String localName = _attributeCache.getLocalName(index);
        final String uri = _attributeCache.getURI(index);
        return new QName(uri,localName,prefix);
    }


    public final String getAttributeNamespace(int index) {
        if (_eventType != START_ELEMENT) {
            throw new IllegalStateException("");
        }
        return _attributeCache.getURI(index);
    }

    public final String getAttributeLocalName(int index) {
        if (_eventType != START_ELEMENT) {
            throw new IllegalStateException("");
        }
        return _attributeCache.getLocalName(index);
    }

    public final String getAttributePrefix(int index) {
        if (_eventType != START_ELEMENT) {
            throw new IllegalStateException("");
        }
        return _attributeCache.getPrefix(index);
    }

    public final String getAttributeType(int index) {
        if (_eventType != START_ELEMENT) {
            throw new IllegalStateException("");
        }
        return _attributeCache.getType(index);
    }

    public final String getAttributeValue(int index) {
        if (_eventType != START_ELEMENT) {
            throw new IllegalStateException("");
        }

        return _attributeCache.getValue(index);
    }

    public final boolean isAttributeSpecified(int index) {
        return false;
    }

    public final int getNamespaceCount() {
        if (_eventType == START_ELEMENT || _eventType == END_ELEMENT) {
            return _stackEntry.namespaceAIIsEnd - _stackEntry.namespaceAIIsStart;
        }

        throw new IllegalStateException("");
    }

    public final String getNamespacePrefix(int index) {
        if (_eventType == START_ELEMENT || _eventType == END_ELEMENT) {
            return _namespaceAIIsPrefix[_stackEntry.namespaceAIIsStart + index];
        }

        throw new IllegalStateException("");
    }

    public final String getNamespaceURI(int index) {
        if (_eventType == START_ELEMENT || _eventType == END_ELEMENT) {
            return _namespaceAIIsNamespaceName[_stackEntry.namespaceAIIsStart + index];
        }

        throw new IllegalStateException("");
    }

    public final String getNamespaceURI(String prefix) {
        return _nsCtx.getNamespaceURI(prefix);
    }

    public final NamespaceContextEx getNamespaceContext() {
        return _nsCtx;
    }

    public final int getEventType() {
        return _eventType;
    }

    public final String getText() {
        if (_characters != null) {
            String s = new String(_characters, _textOffset, _textLen);
            _charSequence = s;
            return s;
        } else if (_charSequence != null) {
            return _charSequence.toString();
        } else {
            throw new IllegalStateException();
        }
    }

    public final char[] getTextCharacters() {
        if (_characters != null) {
            return _characters;
        } else if (_charSequence != null) {
            // TODO try to avoid creation of a temporary String for some
            // CharSequence implementations
            _characters = _charSequence.toString().toCharArray();
            _textLen = _characters.length;
            _textOffset = 0;
            return _characters;
        } else {
            throw new IllegalStateException();
        }
    }

    public final int getTextStart() {
        if (_characters != null) {
            return _textOffset;
        } else if (_charSequence != null) {
            return 0;
        } else {
            throw new IllegalStateException();
        }
    }

    public final int getTextLength() {
        if (_characters != null) {
            return _textLen;
        } else if (_charSequence != null) {
            return _charSequence.length();
        } else {
            throw new IllegalStateException();
        }
    }

    public final int getTextCharacters(int sourceStart, char[] target,
            int targetStart, int length) throws XMLStreamException {
        if (_characters != null) {
        } else if (_charSequence != null) {
            _characters = _charSequence.toString().toCharArray();
            _textLen = _characters.length;
            _textOffset = 0;
        } else {
            throw new IllegalStateException("");
        }

        try {
            System.arraycopy(_characters, sourceStart, target,
                    targetStart, length);
            return length;
        } catch (IndexOutOfBoundsException e) {
            throw new XMLStreamException(e);
        }
    }

    private class CharSequenceImpl implements CharSequence {
        private final int _offset;
        private final int _length;

        CharSequenceImpl(int offset, int length) {
            _offset = offset;
            _length = length;
        }

        public int length() {
            return _length;
        }

        public char charAt(int index) {
            if (index >= 0 && index < _textLen) {
                return _characters[_textOffset + index];
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        public CharSequence subSequence(int start, int end) {
            final int length = end - start;
            if (end < 0 || start < 0 || end > length || start > end) {
                throw new IndexOutOfBoundsException();
            }

            return new CharSequenceImpl(_offset + start, length);
        }

        public String toString() {
            return new String(_characters, _offset, _length);
        }
    }

    public final CharSequence getPCDATA() {
        if (_characters != null) {
            return new CharSequenceImpl(_textOffset, _textLen);
        } else if (_charSequence != null) {
            return _charSequence;
        } else {
            throw new IllegalStateException();
        }
    }

    public final String getEncoding() {
        return "UTF-8";
    }

    public final boolean hasText() {
        return (_characters != null || _charSequence != null);
    }

    public final Location getLocation() {
        return DUMMY_LOCATION;
    }

    public final boolean hasName() {
        return (_eventType == START_ELEMENT || _eventType == END_ELEMENT);
    }

    public final QName getName() {
        return _stackEntry.getQName();
    }

    public final String getLocalName() {
        return _stackEntry.localName;
    }

    public final String getNamespaceURI() {
        return _stackEntry.uri;
    }

    public final String getPrefix() {
        return _stackEntry.prefix;

    }

    public final String getVersion() {
        return "1.0";
    }

    public final boolean isStandalone() {
        return false;
    }

    public final boolean standaloneSet() {
        return false;
    }

    public final String getCharacterEncodingScheme() {
        return "UTF-8";
    }

    public final String getPITarget() {
        if (_eventType == PROCESSING_INSTRUCTION) {
            return _piTarget;
        }
        throw new IllegalStateException("");
    }

    public final String getPIData() {
        if (_eventType == PROCESSING_INSTRUCTION) {
            return _piData;
        }
        throw new IllegalStateException("");
    }



    private void processFirstEvent(XMLStreamBuffer buffer) throws XMLStreamException {
        final int s = readStructure();
        if (s == T_END) {
            // This is an empty buffer
            // Ensure that start and end document events are produced
            // TODO not sure this is correct behaviour as this is not a
            // well formed infoset as there is no element present
            _eventType = START_DOCUMENT;
            _completionState = PENDING_END_DOCUMENT;
            return;
        }

        switch(_eiiStateTable[s]) {
            case STATE_DOCUMENT:
                _eventType = START_DOCUMENT;
                _isDocument = true;
                break;
            case STATE_ELEMENT_U_LN_QN: {
                final String uri = readStructureString();
                final String localName = readStructureString();
                final String prefix = getPrefixFromQName(readStructureString());

                processElementFragment(buffer.getInscopeNamespaces(), prefix, uri, localName);
                break;
            }
            case STATE_ELEMENT_P_U_LN: {
                processElementFragment(buffer.getInscopeNamespaces(), readStructureString(), readStructureString(), readStructureString());
                break;
            }
            case STATE_ELEMENT_U_LN: {
                processElementFragment(buffer.getInscopeNamespaces(), "", readStructureString(), readStructureString());
                break;
            }
            case STATE_ELEMENT_LN: {
                processElementFragment(buffer.getInscopeNamespaces(), "", "", readStructureString());
                break;
            }
            default:
                throw new XMLStreamException("Invalid State");
        }
    }

    private void processElementFragment(Map<String, String> inscopeNamespaces,
            String prefix, String uri, String localName) {
        _eventType = START_ELEMENT;

        pushElementStack();
        _stackEntry.set(prefix, uri, localName);

        // Add the current in-scope namespaces to the list
        // Including any namespace declarations on the element
        for (Map.Entry<String, String> e : inscopeNamespaces.entrySet()) {
            if (_namespaceAIIsIndex == _namespaceAIIsPrefix.length) {
                resizeNamespaceAttributes();
            }

            _namespaceAIIsPrefix[_namespaceAIIsIndex] = e.getKey();
            _namespaceAIIsNamespaceName[_namespaceAIIsIndex++] = e.getValue();
        }
        // Ensure that the in-scope namespace deleclarations are declared
        // on the element.
        _stackEntry.namespaceAIIsEnd = _namespaceAIIsIndex;

        int item = peakStructure();
        if ((item & TYPE_MASK) == T_NAMESPACE_ATTRIBUTE) {
            // Skip the namespace declarations on the element
            // they will have been added already
            item = skipNamespaceAttributes(item);
        }
        if ((item & TYPE_MASK) == T_ATTRIBUTE) {
            processAttributes(item);
        }
    }

    private void processElement(String prefix, String uri, String localName) {
        pushElementStack();
        _stackEntry.set(prefix, uri, localName);

        _attributeCache.clear();

        int item = peakStructure();
        if ((item & TYPE_MASK) == T_NAMESPACE_ATTRIBUTE) {
            // Skip the namespace declarations on the element
            // they will have been added already
            item = processNamespaceAttributes(item);
        }
        if ((item & TYPE_MASK) == T_ATTRIBUTE) {
            processAttributes(item);
        }
    }

    private void resizeNamespaceAttributes() {
        final String[] namespaceAIIsPrefix = new String[_namespaceAIIsIndex * 2];
        System.arraycopy(_namespaceAIIsPrefix, 0, namespaceAIIsPrefix, 0, _namespaceAIIsIndex);
        _namespaceAIIsPrefix = namespaceAIIsPrefix;

        final String[] namespaceAIIsNamespaceName = new String[_namespaceAIIsIndex * 2];
        System.arraycopy(_namespaceAIIsNamespaceName, 0, namespaceAIIsNamespaceName, 0, _namespaceAIIsIndex);
        _namespaceAIIsNamespaceName = namespaceAIIsNamespaceName;
    }

    private int skipNamespaceAttributes(int item){
        do {
            switch(_eiiStateTable[item]){
                case STATE_NAMESPACE_ATTRIBUTE_P:
                case STATE_NAMESPACE_ATTRIBUTE_U:
                    // Undeclaration of namespace
                    readStructureString();
                    break;
                case STATE_NAMESPACE_ATTRIBUTE_P_U:
                    // Declaration with prefix
                    readStructureString();
                    readStructureString();
                    break;
            }
            readStructure();

            item = peakStructure();
        } while((item & (TYPE_MASK)) == T_NAMESPACE_ATTRIBUTE);
        return item;
    }

    private int processNamespaceAttributes(int item){
        _stackEntry.namespaceAIIsStart = _namespaceAIIsIndex;

        do {
            if (_namespaceAIIsIndex == _namespaceAIIsPrefix.length) {
                resizeNamespaceAttributes();
            }

            switch(_niiStateTable[item]){
                case STATE_NAMESPACE_ATTRIBUTE:
                    // Undeclaration of default namespace
                    _namespaceAIIsPrefix[_namespaceAIIsIndex] =
                    _namespaceAIIsNamespaceName[_namespaceAIIsIndex++] = "";
                    break;
                case STATE_NAMESPACE_ATTRIBUTE_P:
                    // Undeclaration of namespace
                    _namespaceAIIsPrefix[_namespaceAIIsIndex] = readStructureString();
                    _namespaceAIIsNamespaceName[_namespaceAIIsIndex++] = "";
                    break;
                case STATE_NAMESPACE_ATTRIBUTE_P_U:
                    // Declaration with prefix
                    _namespaceAIIsPrefix[_namespaceAIIsIndex] = readStructureString();
                    _namespaceAIIsNamespaceName[_namespaceAIIsIndex++] = readStructureString();
                    break;
                case STATE_NAMESPACE_ATTRIBUTE_U:
                    // Default declaration
                    _namespaceAIIsPrefix[_namespaceAIIsIndex] = "";
                    _namespaceAIIsNamespaceName[_namespaceAIIsIndex++] = readStructureString();
                    break;
            }
            readStructure();

            item = peakStructure();
        } while((item & TYPE_MASK) == T_NAMESPACE_ATTRIBUTE);

        _stackEntry.namespaceAIIsEnd = _namespaceAIIsIndex;

        return item;
    }

    private void processAttributes(int item){
        do {
            switch(_aiiStateTable[item]){
                case STATE_ATTRIBUTE_U_LN_QN: {
                    final String uri = readStructureString();
                    final String localName = readStructureString();
                    final String prefix = getPrefixFromQName(readStructureString());
                    _attributeCache.addAttributeWithPrefix(prefix, uri, localName, readStructureString(), readContentString());
                    break;
                }
                case STATE_ATTRIBUTE_P_U_LN:
                    _attributeCache.addAttributeWithPrefix(readStructureString(), readStructureString(), readStructureString(), readStructureString(), readContentString());
                    break;
                case STATE_ATTRIBUTE_U_LN:
                    _attributeCache.addAttributeWithPrefix("", readStructureString(), readStructureString(), readStructureString(), readContentString());
                    break;
                case STATE_ATTRIBUTE_LN: {
                    _attributeCache.addAttributeWithPrefix("", "", readStructureString(), readStructureString(), readContentString());
                    break;
                }
            }
            readStructure();

            item = peakStructure();
        } while((item & TYPE_MASK) == T_ATTRIBUTE);
    }

    private void pushElementStack() {
        if (_depth < _stack.length) {
            _stackEntry = _stack[_depth++];
            return;
        }

        // resize stack
        ElementStackEntry [] tmp = _stack;
        _stack = new ElementStackEntry[_stack.length * 3 /2 + 1];
        System.arraycopy(tmp, 0, _stack, 0, tmp.length);
        for (int i = tmp.length; i < _stack.length; i++){
            _stack[i] = new ElementStackEntry();
        }

        _stackEntry = _stack[_depth++];
    }

    private void popElementStack() {
        // _depth is checked outside this method
        _stackEntry = _stack[--_depth];
        if (_stackEntry.namespaceAIIsEnd > 0) {
            // Move back the position of the namespace index
            _namespaceAIIsIndex = _stackEntry.namespaceAIIsStart;
        }
    }

    private final class ElementStackEntry {
        /**
         * Prefix.
         * Just like everywhere else in StAX, this can be null but can't be empty.
         */
        String prefix;
        /**
         * Namespace URI.
         * Just like everywhere else in StAX, this can be null but can't be empty.
         */
        String uri;
        String localName;
        QName qname;

        // Start and end of namespace declarations
        // in namespace declaration arrays
        int namespaceAIIsStart;
        int namespaceAIIsEnd;

        public void set(String prefix, String uri, String localName) {
            this.prefix = prefix;
            this.uri = uri;
            this.localName = localName;
            this.qname = null;

            this.namespaceAIIsStart = this.namespaceAIIsEnd = 0;
        }

        public QName getQName() {
            if (qname == null) {
                qname = new QName(fixNull(uri), localName, fixNull(prefix));
            }
            return qname;
        }

        private String fixNull(String s) {
            if(s==null) return "";
            return s;
        }
    }

    private final class InternalNamespaceContext implements NamespaceContextEx {
        public String getNamespaceURI(String prefix) {
            if (prefix == null) {
                throw new IllegalArgumentException("Prefix cannot be null");
            }

            /*
             * If the buffer was created using string interning
             * intern the prefix and check for reference equality
             * rather than using String.equals();
             */
            if (_stringInterningFeature) {
                prefix = prefix.intern();

                // Find the most recently declared prefix
                for (int i = _namespaceAIIsIndex - 1; i >=0; i--) {
                    if (prefix == _namespaceAIIsPrefix[i]) {
                        return _namespaceAIIsNamespaceName[i];
                    }
                }
            } else {
                // Find the most recently declared prefix
                for (int i = _namespaceAIIsIndex - 1; i >=0; i--) {
                    if (prefix.equals(_namespaceAIIsPrefix[i])) {
                        return _namespaceAIIsNamespaceName[i];
                    }
                }
            }

            // Check for XML-based prefixes
            if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
                return XMLConstants.XML_NS_URI;
            } else if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
            }

            return null;
        }

        public String getPrefix(String namespaceURI) {
            final Iterator i = getPrefixes(namespaceURI);
            if (i.hasNext()) {
                return (String)i.next();
            } else {
                return null;
            }
        }

        public Iterator getPrefixes(final String namespaceURI) {
            if (namespaceURI == null){
                throw new IllegalArgumentException("NamespaceURI cannot be null");
            }

            if (namespaceURI.equals(XMLConstants.XML_NS_URI)) {
                return Collections.singletonList(XMLConstants.XML_NS_PREFIX).iterator();
            } else if (namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
                return Collections.singletonList(XMLConstants.XMLNS_ATTRIBUTE).iterator();
            }

            return new Iterator() {
                private int i = _namespaceAIIsIndex - 1;
                private boolean requireFindNext = true;
                private String p;

                private String findNext() {
                    while(i >= 0) {
                        // Find the most recently declared namespace
                        if (namespaceURI.equals(_namespaceAIIsNamespaceName[i])) {
                            // Find the most recently declared prefix of the namespace
                            // and check if the prefix is in scope with that namespace
                            if (getNamespaceURI(_namespaceAIIsPrefix[i]).equals(
                                    _namespaceAIIsNamespaceName[i])) {
                                return p = _namespaceAIIsPrefix[i];
                            }
                        }
                        i--;
                    }
                    return p = null;
                }

                public boolean hasNext() {
                    if (requireFindNext) {
                        findNext();
                        requireFindNext = false;
                    }
                    return (p != null);
                }

                public Object next() {
                    if (requireFindNext) {
                        findNext();
                    }
                    requireFindNext = true;

                    if (p == null) {
                        throw new NoSuchElementException();
                    }

                    return p;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        public Iterator<NamespaceContextEx.Binding> iterator() {
            // TODO implement
            return null;
        }
    }

    private static final Location DUMMY_LOCATION = new Location() {
        public int getLineNumber() {
            return -1;
        }

        public int getColumnNumber() {
            return -1;
        }

        public int getCharacterOffset() {
            return -1;
        }

        public String getPublicId() {
            return null;
        }

        public String getSystemId() {
            return null;
        }
    };
}
