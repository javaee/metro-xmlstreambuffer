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

/**
 * Base class for classes that reads {@link XMLStreamBuffer}
 * and produces infoset in API-specific form.
 */
public abstract class AbstractProcessor extends AbstractCreatorProcessor {
    protected  static final int STATE_ILLEGAL                       = 0;
    protected  static final int STATE_DOCUMENT                      = 1;
    protected  static final int STATE_DOCUMENT_FRAGMENT             = 2;
    protected  static final int STATE_ELEMENT_U_LN_QN               = 3;
    protected  static final int STATE_ELEMENT_P_U_LN                = 4;
    protected  static final int STATE_ELEMENT_U_LN                  = 5;
    protected  static final int STATE_ELEMENT_LN                    = 6;
    protected  static final int STATE_NAMESPACE_ATTRIBUTE           = 7;
    protected  static final int STATE_NAMESPACE_ATTRIBUTE_P         = 8;
    protected  static final int STATE_NAMESPACE_ATTRIBUTE_P_U       = 9;
    protected  static final int STATE_NAMESPACE_ATTRIBUTE_U         = 10;
    protected  static final int STATE_ATTRIBUTE_U_LN_QN             = 11;
    protected  static final int STATE_ATTRIBUTE_P_U_LN              = 12;
    protected  static final int STATE_ATTRIBUTE_U_LN                = 13;
    protected  static final int STATE_ATTRIBUTE_LN                  = 14;
    protected  static final int STATE_TEXT_AS_CHAR_ARRAY            = 15;
    protected  static final int STATE_TEXT_AS_CHAR_ARRAY_COPY       = 16;
    protected  static final int STATE_TEXT_AS_STRING                = 17;
    protected  static final int STATE_COMMENT_AS_CHAR_ARRAY         = 18;
    protected  static final int STATE_COMMENT_AS_CHAR_ARRAY_COPY    = 19;
    protected  static final int STATE_COMMENT_AS_STRING             = 20;
    protected  static final int STATE_PROCESSING_INSTRUCTION        = 21;
    protected  static final int STATE_END                           = 22;
    protected  static final int[] _stateTable = new int[256];
    
    static {
        _stateTable[T_DOCUMENT] = STATE_DOCUMENT;
        _stateTable[T_DOCUMENT_FRAGMENT] = STATE_DOCUMENT_FRAGMENT;
        _stateTable[T_ELEMENT_U_LN_QN] = STATE_ELEMENT_U_LN_QN;
        _stateTable[T_ELEMENT_P_U_LN] = STATE_ELEMENT_P_U_LN;
        _stateTable[T_ELEMENT_U_LN] = STATE_ELEMENT_U_LN;
        _stateTable[T_ELEMENT_LN] = STATE_ELEMENT_LN;
        _stateTable[T_NAMESPACE_ATTRIBUTE] = STATE_NAMESPACE_ATTRIBUTE;
        _stateTable[T_NAMESPACE_ATTRIBUTE_P] = STATE_NAMESPACE_ATTRIBUTE_P;
        _stateTable[T_NAMESPACE_ATTRIBUTE_P_U] = STATE_NAMESPACE_ATTRIBUTE_P_U;
        _stateTable[T_NAMESPACE_ATTRIBUTE_U] = STATE_NAMESPACE_ATTRIBUTE_U;
        _stateTable[T_ATTRIBUTE_U_LN_QN] = STATE_ATTRIBUTE_U_LN_QN;
        _stateTable[T_ATTRIBUTE_P_U_LN] = STATE_ATTRIBUTE_P_U_LN;
        _stateTable[T_ATTRIBUTE_U_LN] = STATE_ATTRIBUTE_U_LN;
        _stateTable[T_ATTRIBUTE_LN] = STATE_ATTRIBUTE_LN;
        _stateTable[T_TEXT_AS_CHAR_ARRAY] = STATE_TEXT_AS_CHAR_ARRAY;
        _stateTable[T_TEXT_AS_CHAR_ARRAY_COPY] = STATE_TEXT_AS_CHAR_ARRAY_COPY;
        _stateTable[T_TEXT_AS_STRING] = STATE_TEXT_AS_STRING;
        _stateTable[T_COMMENT_AS_CHAR_ARRAY] = STATE_COMMENT_AS_CHAR_ARRAY;
        _stateTable[T_COMMENT_AS_CHAR_ARRAY_COPY] = STATE_COMMENT_AS_CHAR_ARRAY_COPY;
        _stateTable[T_COMMENT_AS_STRING] = STATE_COMMENT_AS_STRING;
        _stateTable[T_PROCESSING_INSTRUCTION] = STATE_PROCESSING_INSTRUCTION;
        _stateTable[T_END] = STATE_END;
    }
    
    protected int _structureSize;
    
    protected int _structureStringsSize;
    
    protected int _contentStringsSize;
    
    protected int _contentCharactersSize;
    
    protected boolean _stringInterningFeature = false;
    
    protected final void setBuffer(XMLStreamBuffer buffer) {
        _buffer = buffer;
        
        _currentStructureFragment = _buffer.getStructure();
        _structure = _currentStructureFragment.getArray();
        _structureSize = _currentStructureFragment.getSize();
        _structurePtr = _buffer.getStructurePtr();

        _currentStructureStringFragment = _buffer.getStructureStrings();
        _structureStrings = _currentStructureStringFragment.getArray();
        _structureStringsSize = _currentStructureStringFragment.getSize();
        _structureStringsPtr = _buffer.getStructureStringsPtr();
        
        _currentContentStringFragment = _buffer.getContentStrings();
        _contentStrings = _currentContentStringFragment.getArray();
        _contentStringsSize = _currentContentStringFragment.getSize();
        _contentStringsPtr = _buffer.getContentStringsPtr();
        
        _currentContentCharactersFragment = _buffer.getContentCharacters();
        _contentCharacters = _currentContentCharactersFragment.getArray();
        _contentCharactersSize = _currentContentCharactersFragment.getSize();
        _contentCharactersPtr = _buffer.getContentCharactersPtr();
        
        _currentContentCharactersBufferFragment = _buffer.getContentCharactersBuffer();
        _contentCharactersBuffer = _currentContentCharactersBufferFragment.getArray();
        _contentCharactersBufferPtr = _buffer.getContentCharactersBufferPtr();
        
        _stringInterningFeature = _buffer.hasInternedStrings();
    }
    
    protected final int peakStructure() {
        if (_structurePtr < _structureSize) {
            return _structure[_structurePtr];
        }
        
        return readFromNextStructure(0);
    }
    
    protected final int readStructure() {
        if (_structurePtr < _structureSize) {
            return _structure[_structurePtr++];
        }

        return readFromNextStructure(1);
    }
    
    private int readFromNextStructure(int v) {
        if (_structureSize > 0) {
            _currentStructureFragment = _currentStructureFragment.getNext();
            if (_currentStructureFragment != null) {
                _structurePtr = v;
                _structure = _currentStructureFragment.getArray();
                _structureSize = _currentStructureFragment.getSize();
                if (_structureSize > 0) {
                    return _structure[0];
                }
            }
        }
        
        _structureSize = 0;
        return T_END_OF_BUFFER;        
    }
    
    protected final String readStructureString() {
        if (_structureStringsPtr < _structureStringsSize) {
            return _structureStrings[_structureStringsPtr++];
        }
        
        _structureStringsPtr = 1;
        _currentStructureStringFragment = _currentStructureStringFragment.getNext();
        _structureStrings = _currentStructureStringFragment.getArray();
        _structureStringsSize = _currentStructureStringFragment.getSize();
        return _structureStrings[0];
    }
    
    protected final String readContentString() {
        if (_contentStringsPtr < _contentStringsSize) {
            return _contentStrings[_contentStringsPtr++];
        }
        
        _contentStringsPtr = 1;
        _currentContentStringFragment = _currentContentStringFragment.getNext();
        _contentStrings = _currentContentStringFragment.getArray();
        _contentStringsSize = _currentContentStringFragment.getSize();
        return _contentStrings[0];
    }
    
    protected final char[] readContentCharactersCopy() {
        if (_contentCharactersPtr < _contentCharactersSize) {
            return _contentCharacters[_contentCharactersPtr++];
        }
        
        _contentCharactersPtr = 1;
        _currentContentCharactersFragment = _currentContentCharactersFragment.getNext();
        _contentCharacters = _currentContentCharactersFragment.getArray();
        _contentCharactersSize = _currentContentCharactersFragment.getSize();
        return _contentCharacters[0];
    }
    
    protected final int readContentCharactersBuffer(int length) {
        if (_contentCharactersBufferPtr + length < _contentCharactersBuffer.length) {
            final int start = _contentCharactersBufferPtr;
            _contentCharactersBufferPtr += length;
            return start;
        }

        _contentCharactersBufferPtr = length;
        _currentContentCharactersBufferFragment = _currentContentCharactersBufferFragment.getNext();
        _contentCharactersBuffer = _currentContentCharactersBufferFragment.getArray();
        return 0;
    }
    
    protected final StringBuilder _qNameBuffer = new StringBuilder();
    
    protected final String getQName(String prefix, String localName) {
        _qNameBuffer.append(prefix).append(':').append(localName);
        final String qName = _qNameBuffer.toString();
        _qNameBuffer.setLength(0);
        return (_stringInterningFeature) ? qName.intern() : qName;
    }        
   
    protected final String getPrefixFromQName(String qName) {
        int pIndex = qName.indexOf(':');
        if (_stringInterningFeature) {
            return (pIndex != -1) ? qName.substring(0,pIndex).intern() : "";
        } else {
            return (pIndex != -1) ? qName.substring(0,pIndex) : "";
        }
    }
}
