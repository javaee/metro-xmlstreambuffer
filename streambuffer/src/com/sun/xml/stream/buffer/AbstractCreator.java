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

public class AbstractCreator extends AbstractCreatorProcessor {
    
    public void setXMLStreamBuffer(XMLStreamBuffer buffer) {
        setBuffer(buffer);
    }
    
    public XMLStreamBuffer getXMLStreamBuffer() {
        compactBuffer();
        return _buffer;
    }
    
    
    protected final void createBuffer() {
        setBuffer(new XMLStreamBuffer());
    }
    
    protected final void setBuffer(XMLStreamBuffer buffer) {
        _buffer = buffer;
        
        _currentStructureFragment = _buffer.getStructure();
        _structure = _currentStructureFragment.getArray();
        _structurePtr = 0;

        _currentStructureStringFragment = _buffer.getStructureStrings();
        _structureStrings = _currentStructureStringFragment.getArray();
        _structureStringsPtr = 0;

        _currentContentStringFragment = _buffer.getContentStrings();
        _contentStrings = _currentContentStringFragment.getArray();
        _contentStringsPtr = 0;
        
        _currentContentCharactersFragment = _buffer.getContentCharacters();
        _contentCharacters = _currentContentCharactersFragment.getArray();
        _contentCharactersPtr = 0;
        
        _currentContentCharactersBufferFragment = _buffer.getContentCharactersBuffer();
        _contentCharactersBuffer = _currentContentCharactersBufferFragment.getArray();
        _contentCharactersBufferPtr = 0;
    }
    
    protected final void setHasInternedStrings(boolean hasInternedStrings) {
        _buffer.setHasInternedStrings(hasInternedStrings);
    }
    
    protected final void storeStructure(int b) {
        _structure[_structurePtr++] = b;
        if (_structurePtr == _structure.length) {
            resizeStructure();
        }
    }
    
    protected final void resizeStructure() {
        _structurePtr = 0;
        _currentStructureFragment.setSize(_structure.length);
        if (_currentStructureFragment.getNext() != null) {
            _currentStructureFragment = _currentStructureFragment.getNext();
            _structure = _currentStructureFragment.getArray();
        } else {
            _structure = new int[_structure.length];
            _currentStructureFragment = new FragmentedArray(_structure, _currentStructureFragment);
        }
    }
    
    protected final void storeStructureString(String s) {
        _structureStrings[_structureStringsPtr++] = s;
        if (_structureStringsPtr == _structureStrings.length) {
            resizeStructureStrings();
        }
    }
    
    protected final void resizeStructureStrings() {
        _structureStringsPtr = 0;
        _currentStructureStringFragment.setSize(_structureStrings.length);
        if (_currentStructureStringFragment.getNext() != null) {
            _currentStructureStringFragment = _currentStructureStringFragment.getNext();
            _structureStrings = _currentStructureStringFragment.getArray();
        } else {
            _structureStrings = new String[_structureStrings.length];
            _currentStructureStringFragment = new FragmentedArray(_structureStrings, _currentStructureStringFragment);
        }
    }
    
    protected final void storeContentString(String s) {
        _contentStrings[_contentStringsPtr++] = s;
        if (_contentStringsPtr == _contentStrings.length) {
            resizeContentStrings();
        }
    }
    
    protected final void resizeContentStrings() {
        _contentStringsPtr = 0;
        _currentContentStringFragment.setSize(_contentStrings.length);
        if (_currentContentStringFragment.getNext() != null) {
            _currentContentStringFragment = _currentContentStringFragment.getNext();
            _contentStrings = _currentContentStringFragment.getArray();
        } else {
            _contentStrings = new String[_contentStrings.length];
            _currentContentStringFragment = new FragmentedArray(_contentStrings, _currentContentStringFragment);
        }
    }

    protected final void storeContentCharacters(int type, char[] ch, int start, int length) {        
        if (_contentCharactersBufferPtr + length >= _contentCharactersBuffer.length) {
            if (length >= 512) {
                storeStructure(type | FLAG_AS_CHAR_ARRAY_COPY);
                storeContentCharactersCopy(ch, start, length);
                return;
            }
            resizeContentCharacters();            
        }

        storeStructure(type);
        storeStructure(length);
        System.arraycopy(ch, start, _contentCharactersBuffer, _contentCharactersBufferPtr, length);
        _contentCharactersBufferPtr += length;        
    }
    
    protected final void resizeContentCharacters() {
        _contentCharactersBufferPtr = 0;
        if (_currentContentCharactersBufferFragment.getNext() != null) {
            _currentContentCharactersBufferFragment = _currentContentCharactersBufferFragment.getNext();
            _contentCharactersBuffer = _currentContentCharactersBufferFragment.getArray();
        } else {
            _contentCharactersBuffer = new char[_contentCharactersBuffer.length];
            _currentContentCharactersBufferFragment = new FragmentedArray(_contentCharactersBuffer, 
                    _currentContentCharactersBufferFragment);
        }
    }
    
    protected final void storeContentCharactersCopy(char[] ch, int start, int length) {
        char[] copyOfCh = new char[length];
        System.arraycopy(ch, start, copyOfCh, 0, length);
        _contentCharacters[_contentCharactersPtr++] = copyOfCh;
        if (_contentCharactersPtr  == _contentCharacters.length) {
            resizeContentCharacters();
        }
    }

    protected final void resizeContentCharactersCopy() {
        _contentCharactersPtr = 0;
        _currentContentCharactersFragment.setSize(_contentCharacters.length);
        if (_currentContentCharactersFragment.getNext() != null) {
            _currentContentCharactersFragment = _currentContentCharactersFragment.getNext();
            _contentCharacters = _currentContentCharactersFragment.getArray();
        } else {
            _contentCharacters = new char[_contentCharacters.length][];
            _currentContentCharactersFragment = new FragmentedArray(_contentCharacters, _currentContentCharactersFragment);
        }
    }
    
    protected final void compactBuffer() {
        if (_buffer == null) {
            return;
        }
        
        _currentStructureFragment.setSize(_structurePtr);
        if (_structurePtr > 0) {
            // Set the size of the next structure fragment to 0, if one exists,
            // to correctly terminate the buffer
            final FragmentedArray<int[]> structureFragment = _currentStructureFragment.getNext();
            if (structureFragment != null) {
                structureFragment.setSize(0);
            }
        }
                
        _currentStructureStringFragment.setSize(_structureStringsPtr);
        _currentContentStringFragment.setSize(_contentStringsPtr);
        _currentContentCharactersFragment.setSize(_contentCharactersPtr);
    } 
}