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

public class AttributesHolder {   
    protected static final int DEFAULT_CAPACITY = 8;
    protected static final int ITEM_SIZE = 1 << 3;
    
    protected static final int PREFIX     = 0;
    protected static final int URI        = 1;
    protected static final int LOCAL_NAME = 2;
    protected static final int QNAME      = 3;
    protected static final int TYPE       = 4;
    protected static final int VALUE      = 5;
    
    protected int _attributeCount;
    
    protected String[] _strings;
        
    public AttributesHolder() {
        _strings = new String[DEFAULT_CAPACITY * ITEM_SIZE];
    }
        
    // org.xml.sax.Attributes
    
    public final int getLength() {
        return _attributeCount;
    }

    public final String getLocalName(int index) {
        return _strings[(index << 3) + LOCAL_NAME];
    }

    public final String getQName(int index) {
        return _strings[(index << 3) + QNAME];
    }

    public final String getType(int index) {
        return _strings[(index << 3) + TYPE];
    }

    public final String getURI(int index) {
        return _strings[(index << 3) + URI];
    }

    public final String getValue(int index) {
        return _strings[(index << 3) + VALUE];
    }

    public final int getIndex(String qName) {
        for (int i = 0; i < _attributeCount; i++) {
            if (qName.equals(_strings[(i << 3) + QNAME])) {
                return i;
            }
        }
        return -1;
    }

    public final String getType(String qName) {
        return _strings[(getIndex(qName) << 3) + TYPE];
    }

    public final String getValue(String qName) {
        return _strings[(getIndex(qName) << 3) + VALUE];
    }

    public final int getIndex(String uri, String localName) {
        for (int i = 0; i < _attributeCount; i++) {
            if (localName.equals(_strings[(i << 3) + LOCAL_NAME]) &&
                uri.equals(_strings[(i << 3) + URI])) {
                return i;
            }
        }
        return -1;
    }

    public final String getType(String uri, String localName) {
        return _strings[(getIndex(uri, localName) <<3) + TYPE];
    }

    public final String getValue(String uri, String localName) {
        return _strings[(getIndex(uri, localName) <<3) + VALUE];
    }

    public final void clear() {
        if (_attributeCount > 0) {
            for (int i = 0; i < _attributeCount; i++) {
                _strings[(i << 3) + VALUE] = null;
            }
            _attributeCount = 0;
        }
    }

    
    // -----
    
    public final void addAttributeWithQName(String uri, String localName, String qName, String type, String value) {
        final int i = _attributeCount << 3;
        if (i == _strings.length) {
            resize(i);
        }

        _strings[i + PREFIX] = null;
        _strings[i + URI] = uri;
        _strings[i + LOCAL_NAME] = localName;
        _strings[i + QNAME] = qName;
        _strings[i + TYPE] = type;
        _strings[i + VALUE] = value;
        
        _attributeCount++;
    }
    
    public final void addAttributeWithPrefix(String prefix, String uri, String localName, String type, String value) {
        final int i = _attributeCount << 3;
        if (i == _strings.length) {
            resize(i);
        }

        _strings[i + PREFIX] = prefix;
        _strings[i + URI] = uri;
        _strings[i + LOCAL_NAME] = localName;
        _strings[i + QNAME] = null;
        _strings[i + TYPE] = type;
        _strings[i + VALUE] = value;
        
        _attributeCount++;
    }
    
    private final void resize(int length) {
        final int newLength = length * 2;
        final String[] strings = new String[newLength];         
        System.arraycopy(_strings, 0, strings, 0, length);
        _strings = strings;
    }    
    
    public final String getPrefix(int index) {
        return _strings[(index << 3) + PREFIX];
    }    
}
