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
import com.sun.xml.stream.buffer.XMLStreamBufferException;
import com.sun.xml.stream.buffer.util.NamespaceContextImpl;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * @author Paul.Sandoz@Sun.Com
 * @author K.Venugopal@sun.com
 */
public class StreamReaderBufferProcessor extends AbstractProcessor implements XMLStreamReader {
    private static final int CACHE_SIZE = 16;
    
    protected ElementStackEntry[] _stack = new ElementStackEntry[CACHE_SIZE];
    protected ElementStackEntry _stackEntry;
    protected int _depth;

    protected String[] _namespaceAIIsPrefix = new String[CACHE_SIZE];
    protected String[] _namespaceAIIsNamespaceName = new String[CACHE_SIZE];
    protected int _namespaceAIIsIndex;
    
    protected int _eventType;
    
    protected AttributesHolder _attributeCache;
    
    protected String _string;
    protected char[] _characters;
    protected int _textLen;
    protected int _textOffset;
    
    protected NamespaceContextImpl _nsCtx = new NamespaceContextImpl();
    
    protected boolean _isDocument;
    
    protected boolean _isProcessingComplete;
    
    protected String _piTarget;
    protected String _piData;
    
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
        
        _isProcessingComplete = false;
        _namespaceAIIsIndex = 0;
        
        processFirstEvent();
    }
    
    public Object getProperty(java.lang.String name) {
        return null;
    }
    
    public int next() throws XMLStreamException {
        if (_isProcessingComplete) {
                throw new XMLStreamException("Invalid State");            
        }
        
        _characters = null;
        _string = null;
        switch(_stateTable[readStructure()]) {
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
                processElement("", readStructureString(), readStructureString());
                return _eventType = START_ELEMENT;
            case STATE_ELEMENT_LN:
                processElement("", "", readStructureString());
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
                _string = readStructureString();
                
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
                _string = readStructureString();
                
                return _eventType = COMMENT;
            case STATE_PROCESSING_INSTRUCTION:
                _piTarget = readStructureString();
                _piData = readStructureString();
                
                return _eventType = PROCESSING_INSTRUCTION;
            case STATE_END:
                if (_depth > 0) {
                    popElementStack();
                    _eventType = END_ELEMENT;
                    if (!_isDocument && _depth == 0) {
                        _isProcessingComplete = true;
                    }
                } else {
                    _eventType = END_DOCUMENT;
                    _isProcessingComplete = true;
                }
                return _eventType;
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
    
    public final boolean hasNext() throws XMLStreamException {
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
        // TODO
        return null;
    }
    
    public final NamespaceContext getNamespaceContext() {
        return _nsCtx;
    }
    
    public final int getEventType() {
        return _eventType;
    }
    
    public final String getText() {
        if (_characters != null) {
            return _string = new String(_characters, _textOffset, _textLen);
        } else if (_string != null) {
            return _string;
        } else {        
            throw new IllegalStateException("");
        }        
    }
    
    public final char[] getTextCharacters() {
        if (_characters != null) {
            return _characters;
        } else if (_string != null) {
            _characters = _string.toCharArray();
            _textLen = _characters.length;
            _textOffset = 0;
            return _characters;
        } else {        
            throw new IllegalStateException("");
        }        
    }
    
    public final int getTextStart() {
        if (_characters != null) {
            return _textOffset;
        } else if (_string != null) {
            return 0;
        } else {        
            throw new IllegalStateException("");
        }        
    }
    
    public final int getTextLength() {
        if (_characters != null) {
            return _textLen;
        } else if (_string != null) {
            return _string.length();
        } else {        
            throw new IllegalStateException("");
        }        
    }
    
    public final int getTextCharacters(int sourceStart, char[] target,
            int targetStart, int length) throws XMLStreamException {
        if (_characters != null) {
        } else if (_string != null) {
            _characters = _string.toCharArray();
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
    
    public final String getEncoding() {
        return "UTF-8";
    }
    
    public final boolean hasText() {
        return (_characters != null || _string != null);
    }
    
    public final Location getLocation() {
        return null;
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
    
    
    
    private void processFirstEvent() throws XMLStreamException {
        switch(_stateTable[readStructure()]) {
            case STATE_DOCUMENT:
                _eventType = START_DOCUMENT;
                _isDocument = true;
                break;
            case STATE_ELEMENT_U_LN_QN: {
                final String uri = readStructureString();
                final String localName = readStructureString();
                final String prefix = getPrefixFromQName(readStructureString());
                
                processElement(prefix, uri, localName);
                break;
            }
            case STATE_ELEMENT_P_U_LN: {
                processElement(readStructureString(), readStructureString(), readStructureString());
                break;
            }
            case STATE_ELEMENT_U_LN: {
                processElement("", readStructureString(), readStructureString());
                break;
            }
            case STATE_ELEMENT_LN: {
                processElement("", "", readStructureString());
                break;
            }
            default:
                throw new XMLStreamException("Invalid State");
        }
    }
    
    private void processElement(String prefix, String uri, String localName) {
        pushElementStack();
        _stackEntry.set(prefix, uri, localName);
        
        _attributeCache.clear();
        
        int item = peakStructure();
        if ((item & TYPE_MASK) == T_ATTRIBUTE) {
            if ((item & FLAG_NAMESPACE_ATTRIBUTE) == 0) {
                processAttributes(item);
            } else {
                processNamespaceAttributes(item);
                item = peakStructure();
                if ((item & TYPE_MASK) == T_ATTRIBUTE) {
                    processAttributes(item);
                }
            }            
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
        
    private void processNamespaceAttributes(int item){
        _stackEntry.namespaceAIIsStart = _namespaceAIIsIndex;
        String prefix = "", namespaceName = "";

        do {
            if (_namespaceAIIsIndex == _namespaceAIIsPrefix.length) {
                resizeNamespaceAttributes();
            }
            
            switch(_stateTable[item]){
                case STATE_NAMESPACE_ATTRIBUTE:
                    // Undeclaration of default namespace
                    prefix = namespaceName =
                            _namespaceAIIsPrefix[_namespaceAIIsIndex] = 
                            _namespaceAIIsNamespaceName[_namespaceAIIsIndex++] = "";
                    break;
                case STATE_NAMESPACE_ATTRIBUTE_P:
                    // Undeclaration of namespace
                    prefix = _namespaceAIIsPrefix[_namespaceAIIsIndex] = readStructureString();
                    namespaceName = _namespaceAIIsNamespaceName[_namespaceAIIsIndex++] = "";
                    break;
                case STATE_NAMESPACE_ATTRIBUTE_P_U:
                    // Declaration with prefix
                    prefix = _namespaceAIIsPrefix[_namespaceAIIsIndex] = readStructureString();
                    namespaceName = _namespaceAIIsNamespaceName[_namespaceAIIsIndex++] = readStructureString();
                    break;
                case STATE_NAMESPACE_ATTRIBUTE_U:
                    // Default declaration
                    prefix = _namespaceAIIsPrefix[_namespaceAIIsIndex] = "";
                    namespaceName = _namespaceAIIsNamespaceName[_namespaceAIIsIndex++] = readStructureString();
                    break;                
            }
            readStructure();
            
            item = peakStructure();
        } while((item & (TYPE_MASK | FLAG_NAMESPACE_ATTRIBUTE)) == T_NAMESPACE_ATTRIBUTE);
        
        _stackEntry.namespaceAIIsEnd = _namespaceAIIsIndex;
    }
    
    private void processAttributes(int item){
        do {
            switch(_stateTable[item]){
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
    
    private ElementStackEntry pushElementStack() {
        if (_depth < _stack.length) {
            return _stackEntry = _stack[_depth++];
        }
        
        ElementStackEntry [] tmp = _stack;
        _stack = new ElementStackEntry[_stack.length * 3 /2 + 1];
        System.arraycopy(tmp, 0, _stack, 0, tmp.length);
        for (int i = tmp.length; i < _stack.length; i++){
            _stack[i] = new ElementStackEntry();
        }
        
        return _stackEntry = _stack[_depth++];
    }
    
    private ElementStackEntry popElementStack() {
        if (_depth > 0) {
            _stackEntry = _stack[--_depth];
            if (_stackEntry.namespaceAIIsEnd > 0) {
                _namespaceAIIsIndex = _stackEntry.namespaceAIIsStart;
            }
            
            return _stackEntry;
        }
        
        return null;
    }
    
    private class ElementStackEntry {
        String prefix;
        String uri;
        String localName;
        QName qname;
        
        int namespaceAIIsStart;
        int namespaceAIIsEnd;
        
        public void set(String prefix, String uri, String localName) {
            this.prefix = prefix;
            this.uri = uri;
            this.localName = localName;
            
            this.qname = null;
            
            this.namespaceAIIsStart = this.namespaceAIIsEnd = 0;
        }
        
        public QName getQName(){
            if(qname == null){
                qname = new QName(uri,localName,prefix);
            }
            return qname;
        }
    }
}
