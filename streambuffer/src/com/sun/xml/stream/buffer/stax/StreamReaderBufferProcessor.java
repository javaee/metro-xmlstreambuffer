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
    protected int _eventType;
    protected int _internalEventType;
    protected char[] _characters;
    IQName _internalQName = null;
    private static final int CACHE_SIZE=20;
    int index =0;
    IQName [] _qnameCache = new IQName[CACHE_SIZE];
    private boolean  _readAttrs = false;
    AttributesHolder _attributeCache = null;
    String _text;
    StringBuilder _buffer;
    int _textLen =0;
    int _textOffset =0;
    
    //false if data associated with current state has not been read.
    boolean _stateRead = true;
    public StreamReaderBufferProcessor() {
        for(int i=0;i<_qnameCache.length;++i){
            _qnameCache[i]=new IQName();
        }
        _attributeCache = new AttributesHolder();
        _buffer = new StringBuilder();
    }
    
    public StreamReaderBufferProcessor(XMLStreamBuffer buffer) {
        this();
        setBuffer(buffer);
    }
    
    public Object getProperty(java.lang.String name) {
        return null;
    }
    
    public int next() throws XMLStreamException {
        
        if(!_stateRead){
            skipToNextEvent();
        }
        int item = readStructure();
        _internalEventType = item;
        
        _stateRead = false;
        _readAttrs = false;
        switch(item) {
            case STATE_ELEMENT_U_LN_QN:{
                _eventType = START_ELEMENT;
                _internalQName= getInternalQName(index);
                _internalQName.reset();
                _internalQName.setUri(readStructureString());
                _internalQName.setLocalName(readStructureString());
                _internalQName.setPrefix(getPrefixFromQName(readStructureString()));
                index++;
                _stateRead = true;
                break;
            }
            case STATE_ELEMENT_P_U_LN:{
                _eventType = START_ELEMENT;
                _internalQName= getInternalQName(index);
                _internalQName.reset();
                _internalQName.setPrefix(readStructureString());
                _internalQName.setUri(readStructureString());
                _internalQName.setLocalName(readStructureString());
                index++;
                _stateRead = true;
                break;
            }
            case STATE_ELEMENT_U_LN: {
                _eventType = START_ELEMENT;
                _internalQName= getInternalQName(index);
                _internalQName.reset();
                _internalQName.setUri(readStructureString());
                _internalQName.setLocalName(readStructureString());
                index++;
                _stateRead = true;
                break;
            }
            case STATE_ELEMENT_LN: {
                _eventType = START_ELEMENT;
                _internalQName= getInternalQName(index);
                _internalQName.reset();
                _internalQName.setLocalName(readStructureString());
                index++;
                _stateRead = true;
                break;
            }
            case STATE_NAMESPACE_ATTRIBUTE_P_U:
            case STATE_NAMESPACE_ATTRIBUTE_U:{
                _eventType = NAMESPACE;
                readAttributes();
                _readAttrs = true;
                _stateRead = true;
                break;
            }
            case STATE_ATTRIBUTE_U_LN_QN:
            case STATE_ATTRIBUTE_P_U_LN:
            case STATE_ATTRIBUTE_U_LN :
            case STATE_ATTRIBUTE_LN: {
                _eventType = ATTRIBUTE;
                //wait till user calls any of the attributes;
                break;
            }
            case STATE_TEXT_AS_CHAR_ARRAY:{
                _eventType = CHARACTERS;
                break;
            }
            case STATE_TEXT_AS_STRING:{
                _eventType = CHARACTERS;
                break;
            }
            case STATE_COMMENT_AS_CHAR_ARRAY:{
                _eventType = COMMENT;
                break;
            }
            case STATE_COMMENT_AS_CHAR_ARRAY_COPY:{
                _eventType = COMMENT;
                break;
            }
            case STATE_PROCESSING_INSTRUCTION:{
                _eventType = PROCESSING_INSTRUCTION;
                break;
            }
            case STATE_END:{
                _eventType = END_ELEMENT;
                index--;
                _internalQName= getInternalQName(index);
                break;
            }
            default:{
                throw new XMLStreamException("Invalid State "+item);
            }
        }
        return _eventType;
    }
    
    /*
     * Reading some data from the buffer could be expensive, attributes, characters.
     * skipToNextEvent is called to move the pointer to proper position in the buffer incase
     * user does not read data for the current event and calls next();
     */
    private int skipToNextEvent() throws XMLStreamException {
        int item = _internalEventType;
        switch(item) {
            case STATE_ELEMENT_U_LN_QN:
            case STATE_ELEMENT_P_U_LN:
            case STATE_ELEMENT_U_LN:
            case STATE_ELEMENT_LN:{
                break;
            }
            case STATE_NAMESPACE_ATTRIBUTE_P_U:
            case STATE_NAMESPACE_ATTRIBUTE_U:
            case STATE_ATTRIBUTE_U_LN_QN:
            case STATE_ATTRIBUTE_P_U_LN:
            case STATE_ATTRIBUTE_U_LN :
            case STATE_ATTRIBUTE_LN: {
                _eventType = ATTRIBUTE;
                skipAttributes(_eventType);
                //wait till user calls any of the attributes;
                break;
            }
            case STATE_COMMENT_AS_CHAR_ARRAY:
            case STATE_TEXT_AS_CHAR_ARRAY:{
                readStructure();
                //readContentCharactersBuffer(length);
                break;
            }
            case STATE_TEXT_AS_STRING:{
                _eventType = CHARACTERS;
                readStructureString();
                break;
            }          
            
            case STATE_COMMENT_AS_CHAR_ARRAY_COPY:{
                _eventType = COMMENT;
                readContentCharactersCopy();
                break;
            }
            case STATE_PROCESSING_INSTRUCTION:{
                _eventType = PROCESSING_INSTRUCTION;
                break;
            }
            case STATE_END:{
                _eventType = END_ELEMENT;
                index--;
                _internalQName= getInternalQName(index);
                break;
            }
            default:{
                throw new XMLStreamException("Invalid State "+item);
            }
        }
        return _eventType;
    }
    public final void require(int type, String namespaceURI, String localName)
    throws XMLStreamException {
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
        if(getEventType() != START_ELEMENT) {
            throw new XMLStreamException("");
        }
        
        //current is StartElement, move to the next
        int eventType = next();
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
        int eventType = next();
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
    
    public final String getNamespaceURI(String prefix) {
        return null;
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
            for (int i=start; i< length;i++){
                // if(!XMLChar.isSpace(ch[i])){
                return false;
                // }
            }
            return true;
        }
        return false;
    }
    
    public final String getAttributeValue(String namespaceURI, String localName) {
        if (_eventType != START_ELEMENT || _eventType == ATTRIBUTE) {
            throw new IllegalStateException("");
        }
        
        if(!_readAttrs){
            _attributeCache.clear();
            readAttributes();
            _readAttrs = true;
        }
        return _attributeCache.getValue(namespaceURI,localName);
        
    }
    
    public final int getAttributeCount() {
        if (_eventType != START_ELEMENT || _eventType == ATTRIBUTE) {
            throw new IllegalStateException("");
        }
        if(!_readAttrs){
            _attributeCache.clear();
            readAttributes();
            _readAttrs = true;
        }
        return _attributeCache.getLength();
    }
    
    public final javax.xml.namespace.QName getAttributeName(int index) {
        if (_eventType != START_ELEMENT || _eventType == ATTRIBUTE) {
            throw new IllegalStateException("");
        }
        if(!_readAttrs){
            _attributeCache.clear();
            readAttributes();
            _readAttrs = true;
        }
        
        final String prefix = _attributeCache.getPrefix(index);
        final String localName = _attributeCache.getLocalName(index);
        final String uri = _attributeCache.getURI(index);
        QName qname = null;
        if(prefix.length() == 0 && uri.length() == 0){
            qname = new QName(localName);
        }else if(prefix.length() == 0){
            qname = new QName(uri,localName);
        }else{
            qname = new QName(uri,localName,prefix);
        }
        return qname;
    }
    
    
    public final String getAttributeNamespace(int index) {
        if (_eventType != START_ELEMENT || _eventType == ATTRIBUTE) {
            throw new IllegalStateException("");
        }
        if(!_readAttrs){
            _attributeCache.clear();
            readAttributes();
            _readAttrs = true;
        }
        return _attributeCache.getURI(index);
    }
    
    public final String getAttributeLocalName(int index) {
        if (_eventType != START_ELEMENT || _eventType == ATTRIBUTE) {
            throw new IllegalStateException("");
        }
        if(!_readAttrs){
            _attributeCache.clear();
            readAttributes();
            _readAttrs = true;
        }
        return _attributeCache.getLocalName(index);
    }
    
    public final String getAttributePrefix(int index) {
        if (_eventType != START_ELEMENT || _eventType == ATTRIBUTE) {
            throw new IllegalStateException("");
        }
        if(!_readAttrs){
            _attributeCache.clear();
            readAttributes();
            _readAttrs = true;
        }
        return _attributeCache.getPrefix(index);
    }
    
    public final String getAttributeType(int index) {
        if (_eventType != START_ELEMENT || _eventType == ATTRIBUTE) {
            throw new IllegalStateException("");
        }
        if(!_readAttrs){
            _attributeCache.clear();
            readAttributes();
            _readAttrs = true;
        }
        return _attributeCache.getType(index);
    }
    
    public final String getAttributeValue(int index) {
        if (_eventType != START_ELEMENT || _eventType == ATTRIBUTE) {
            throw new IllegalStateException("");
        }
        if(!_readAttrs){
            _attributeCache.clear();
            readAttributes();
            _readAttrs = true;
        }
        return _attributeCache.getValue(index);
    }
    
    public final boolean isAttributeSpecified(int index) {
        return false;
    }
    
    public final int getNamespaceCount() {
        if (_eventType == START_ELEMENT || _eventType == END_ELEMENT) {
            return -1;
        }
        throw new IllegalStateException("");
    }
    
    public final String getNamespacePrefix(int index) {
        if (_eventType == START_ELEMENT || _eventType == END_ELEMENT) {
            return null;
        }
        throw new IllegalStateException("");
    }
    
    public final String getNamespaceURI(int index) {
        if (_eventType == START_ELEMENT || _eventType == END_ELEMENT) {
            return null;
        }
        throw new IllegalStateException("");
    }
    
    public final NamespaceContext getNamespaceContext() {
        return null;
    }
    
    public final int getEventType() {
        return _eventType;
    }
    
    public final String getText() {
        if(_eventType != CHARACTERS || _eventType != COMMENT){
            throw new IllegalStateException("");
        }
        
        if(!_stateRead){
            _stateRead = true;
            _buffer.setLength(0);
            switch(_internalEventType){
                case STATE_COMMENT_AS_CHAR_ARRAY:
                case STATE_TEXT_AS_CHAR_ARRAY:{
                    _textLen = readStructure();
                    _textOffset = readContentCharactersBuffer(_textLen);
                    break;
                }
                case STATE_TEXT_AS_STRING:{
                    _text = readStructureString();
                    return _text;
                }
                case STATE_COMMENT_AS_CHAR_ARRAY_COPY:{
                    _characters = readContentCharactersCopy();
                    _textLen = _characters.length;
                    _textOffset = 0;
                    break;
                }
            }
            
        }
        if(_text == null){
            _buffer.append(_characters,_textOffset,_textLen);
            _text = _buffer.toString();
        }
        return _text;
    }
    
    public final char[] getTextCharacters() {
        if(_eventType != CHARACTERS || _eventType != COMMENT){
            throw new IllegalStateException("");
        }
        
        if(!_stateRead){
            _stateRead = true;
            _buffer.setLength(0);
            switch(_internalEventType){
                case STATE_COMMENT_AS_CHAR_ARRAY:
                case STATE_TEXT_AS_CHAR_ARRAY:{
                    _textLen = readStructure();
                    _textOffset = readContentCharactersBuffer(_textLen);
                    _characters = _contentCharactersBuffer;
                    break;
                }
                case STATE_TEXT_AS_STRING:{
                    _text = readStructureString();
                    _characters = _text.toCharArray();
                    _textLen = _characters.length;
                    _textLen =0;
                    break;
                }
                case STATE_COMMENT_AS_CHAR_ARRAY_COPY:{
                    _characters = readContentCharactersCopy();
                    _textLen = _characters.length;
                    _textOffset = 0;
                    break;
                }
            }
            
        }
        return _characters;
    }
    
    public final int getTextStart() {
        if(_eventType != CHARACTERS || _eventType != COMMENT){
            throw new IllegalStateException("");
        }
        return _textOffset;
    }
    
    public final int getTextLength() {
        if(_eventType != CHARACTERS || _eventType != COMMENT){
            throw new IllegalStateException("");
        }
        return _textLen;
    }
    
    public final int getTextCharacters(int sourceStart, char[] target,
            int targetStart, int length) throws XMLStreamException {
        return -1;
    }
    
    public final String getEncoding() {
        return null;
    }
    
    public final boolean hasText() {
        return (_characters != null);
    }
    
    public final Location getLocation() {
        return null;
    }
    
    public final QName getName() {
        return _internalQName.getQName();
    }
    
    public final String getLocalName() {
        return _internalQName.getLocalName();
    }
    
    public final boolean hasName() {
        return (_eventType == START_ELEMENT || _eventType == END_ELEMENT);
    }
    
    public final String getNamespaceURI() {
        return _internalQName.getUri();
    }
    
    public final String getPrefix() {
        return _internalQName.getPrefix();
        
    }
    
    public final String getVersion() {
        return null;
    }
    
    public final boolean isStandalone() {
        return false;
    }
    
    public final boolean standaloneSet() {
        return false;
    }
    
    public final String getCharacterEncodingScheme() {
        return null;
    }
    
    public final String getPITarget() {
        if (_eventType == PROCESSING_INSTRUCTION) {
            return null;
        }
        throw new IllegalStateException("");
    }
    
    public final String getPIData() {
        if (_eventType == PROCESSING_INSTRUCTION) {
            return null;
        }
        throw new IllegalStateException("");
    }
    
    private void readAttributes(){
        int eventType = _eventType;
        if(eventType == START_ELEMENT){
            int topEvent = peakStructure();
            if((topEvent & TYPE_MASK) != T_ATTRIBUTE){
                return;
            }else{
                readStructure();
            }
        }
        
        do{
            switch(eventType){
                case T_ATTRIBUTE_U_LN_QN:
                    _attributeCache.addAttributeWithQName(readStructureString(), readStructureString(), readStructureString(), readStructureString(), readContentString());
                    break;
                case T_ATTRIBUTE_P_U_LN:
                {
                    final String p = readStructureString();
                    final String u = readStructureString();
                    final String ln = readStructureString();
                    _attributeCache.addAttributeWithQName(u, ln, getQName(p, ln), readStructureString(), readContentString());
                    break;
                }
                case T_ATTRIBUTE_U_LN: {
                    final String u = readStructureString();
                    final String ln = readStructureString();
                    _attributeCache.addAttributeWithQName(u, ln, ln, readStructureString(), readContentString());
                    break;
                }
                case T_ATTRIBUTE_LN: {
                    final String ln = readStructureString();
                    _attributeCache.addAttributeWithQName("", ln, ln, readStructureString(), readContentString());
                    break;
                }
                case STATE_NAMESPACE_ATTRIBUTE_P_U:{
                    break;
                }
                case STATE_NAMESPACE_ATTRIBUTE_U:{
                    break;
                }
            }
            eventType = peakStructure();
            if((eventType & TYPE_MASK) == T_ATTRIBUTE){
                readStructure();
            }else{
                break;
            }
        }while(true);
    }
    
    
    private void skipAttributes(int eventType){
        if(eventType == START_ELEMENT){
            int topEvent = peakStructure();
            if((topEvent & TYPE_MASK) != T_ATTRIBUTE){
                return;
            }else{
                readStructure();
            }
        }
        
        do{
            switch(eventType){
                case T_ATTRIBUTE_U_LN_QN:
                    readStructureString();
                    readStructureString();
                    readStructureString();
                    readStructureString();
                    readContentString();
                    //equivalent to skip
                    // _attributeCache.addAttributeWithQName(readStructureString(), readStructureString(), readStructureString(), readStructureString(), readContentString());
                    break;
                case T_ATTRIBUTE_P_U_LN:
                {
                    readStructureString();
                    readStructureString();
                    readStructureString();
                    readStructureString();
                    readContentString();
                    break;
                }
                case T_ATTRIBUTE_U_LN: {
                    readStructureString();
                    readStructureString();
                    readStructureString();
                    readContentString();
                    break;
                }
                case T_ATTRIBUTE_LN: {
                    readStructureString();
                    readStructureString();
                    readContentString();
                }
                case STATE_NAMESPACE_ATTRIBUTE_P_U:{
                    break;
                }
                case STATE_NAMESPACE_ATTRIBUTE_U:{
                    break;
                }
            }
            eventType = peakStructure();
            if((eventType & TYPE_MASK) == T_ATTRIBUTE){
                readStructure();
            }else{
                break;
            }
        }while(true);
    }
    
    
    
    private IQName getInternalQName(int index){
        if(index >= _qnameCache.length ){
            IQName [] tmp =  _qnameCache;
            _qnameCache = new IQName[_qnameCache.length+CACHE_SIZE];
            System.arraycopy(tmp,0,_qnameCache,0,tmp.length);
            for(int i=index;i<_qnameCache.length;++i){
                _qnameCache[i]=new IQName();
            }
        }
        return _qnameCache[index];
    }
    
    class IQName {
        private String uri="";
        private String prefix="";
        private String localName="";
        private QName qname = null;
        public String getUri() {
            return uri;
        }
        
        public void setUri(String uri) {
            this.uri = uri;
        }
        
        public String getPrefix() {
            return prefix;
        }
        
        public String getLocalName() {
            return localName;
        }
        
        public void setPrefix(String prefix){
            this.prefix = prefix;
        }
        
        public void setLocalName(String name){
            this.localName = name;
        }
        
        public QName getQName(){
            if(qname == null){
                if(prefix.length() == 0 && uri.length() == 0){
                    qname = new QName(localName);
                }else if(prefix.length() == 0){
                    qname = new QName(uri,localName);
                }else{
                    qname = new QName(uri,localName,prefix);
                }
            }
            return qname;
        }
        
        public void reset(){
            uri="";
            prefix="";
            localName  ="";
            qname = null;
        }
    }
    
}
