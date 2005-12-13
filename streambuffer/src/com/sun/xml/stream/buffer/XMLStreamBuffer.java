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

import java.util.Collections;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;

/*
 * TODO 
 * Use character array to store multiple sets of characters rather than
 * instantiate a new array of characters for each piece of content. Arrays
 * can be reused.
 */
public class XMLStreamBuffer {
    public static int DEFAULT_ARRAY_SIZE = 512;
    
    public static final int FLAG_PREFIX                 = 1 << 0;
    public static final int FLAG_URI                    = 1 << 1;
    public static final int FLAG_QUALIFIED_NAME         = 1 << 2;
    public static final int FLAG_NAMESPACE_ATTRIBUTE    = 1 << 3;
    
    public static final int FLAG_AS_CHAR_ARRAY_COPY     = 1 << 0;
    public static final int FLAG_AS_STRING              = 1 << 1;
    
    public static final int FLAG_DOCUMENT_FRAGMENT      = 1 << 0;
    
    public static final int TYPE_MASK                   = 0xF0;
    
    public static final int END                         = 0x00;
    public static final int DOCUMENT                    = 0x10;
    public static final int ELEMENT                     = 0x20;
    public static final int ATTRIBUTE                   = 0x30;
    public static final int TEXT                        = 0x40;
    public static final int COMMENT                     = 0x50;
    public static final int PROCESSING_INSTRUCTION      = 0x60;
    public static final int UNEXPANDED_ENTITY_REFERENCE = 0x70;

    public static final int DOCUMENT_FRAGMENT           = DOCUMENT | FLAG_DOCUMENT_FRAGMENT;
    
    public static final int ELEMENT_U_LN_QN             = ELEMENT | FLAG_URI | FLAG_QUALIFIED_NAME;
    public static final int ELEMENT_P_U_LN              = ELEMENT | FLAG_PREFIX | FLAG_URI;
    public static final int ELEMENT_U_LN                = ELEMENT | FLAG_URI;
    public static final int ELEMENT_LN                  = ELEMENT;
    
    public static final int NAMESPACE_ATTRIBUTE         = ATTRIBUTE | FLAG_NAMESPACE_ATTRIBUTE;
    public static final int NAMESPACE_ATTRIBUTE_P       = NAMESPACE_ATTRIBUTE | FLAG_PREFIX;
    public static final int NAMESPACE_ATTRIBUTE_P_U     = NAMESPACE_ATTRIBUTE | FLAG_PREFIX | FLAG_URI;
    public static final int NAMESPACE_ATTRIBUTE_U       = NAMESPACE_ATTRIBUTE | FLAG_URI;
    
    public static final int ATTRIBUTE_U_LN_QN           = ATTRIBUTE | FLAG_URI | FLAG_QUALIFIED_NAME;
    public static final int ATTRIBUTE_P_U_LN            = ATTRIBUTE | FLAG_PREFIX | FLAG_URI;
    public static final int ATTRIBUTE_U_LN              = ATTRIBUTE | FLAG_URI;
    public static final int ATTRIBUTE_LN                = ATTRIBUTE;
    
    public static final int TEXT_AS_CHAR_ARRAY          = TEXT;
    public static final int TEXT_AS_CHAR_ARRAY_COPY     = TEXT | FLAG_AS_CHAR_ARRAY_COPY;
    public static final int TEXT_AS_STRING              = TEXT | FLAG_AS_STRING;
    
    public static final int COMMENT_AS_CHAR_ARRAY       = COMMENT;
    public static final int COMMENT_AS_CHAR_ARRAY_COPY  = COMMENT | FLAG_AS_CHAR_ARRAY_COPY;
    public static final int COMMENT_AS_STRING           = COMMENT | FLAG_AS_STRING;
    
    public static final int END_OF_BUFFER               = -1;

    protected static final Map<String, String> EMTPY_MAP = Collections.emptyMap();
    
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
    
    public Map<String, String> getInscopeNamespaces() {
        return EMTPY_MAP;
    }
    
    public FragmentedArray<int[]> getStructure() {
        return _structure;
    }
    
    public int getStructurePtr() {
        return _structurePtr;
    }
    
    public FragmentedArray<String[]> getStructureStrings() {
        return _structureStrings;
    }
    
    public int getStructureStringsPtr() {
        return _structureStringsPtr;
    }
    
    public FragmentedArray<String[]> getContentStrings() {
        return _contentStrings;
    }
    
    public int getContentStringsPtr() {
        return _contentStringsPtr;
    }
    
    public FragmentedArray<char[][]> getContentCharacters() {
        return _contentCharacters;
    }
    
    public int getContentCharactersPtr() {
        return _contentCharactersPtr;
    }
        
    public FragmentedArray<char[]> getContentCharactersBuffer() {
        return _contentCharactersBuffer;
    }
    
    public int getContentCharactersBufferPtr() {
        return _contentCharactersBufferPtr;
    }
    
    public boolean getHasInternedStrings() {
        return _hasInternedStrings;
    }
    
    public void setHasInternedStrings(boolean hasInternedStrings) {
        _hasInternedStrings = hasInternedStrings;
    }
    
    public void reset() {
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
}