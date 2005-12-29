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

class FragmentedArray<T> {
    protected int _size;
    protected T _item;
    protected FragmentedArray<T> _next;
    protected FragmentedArray<T> _previous;
    
    FragmentedArray(T item) {
        this(item, null);
    }
    
    FragmentedArray(T item, FragmentedArray<T> previous) {
        setArray(item);
        if (previous != null) {
            previous._next = this;
            _previous = previous;
        }
    }
    
    T getArray() {
        return _item;
    }
    
    void setArray(T item) {
        assert(item.getClass().isArray());
        
        _item = item;
    }
    
    int getSize() {
        return _size;
    }

    void setSize(int size) {
        _size = size;
    }
    
    FragmentedArray<T> getNext() {
        return _next;
    }
    
    void setNext(FragmentedArray<T> next) {
        _next = next;
        if (next != null) {
            next._previous = this;
        }
    }
    
    FragmentedArray<T> getPrevious() {
        return _previous;
    }
    
    void setPrevious(FragmentedArray<T> previous) {
        _previous = previous;
        if (previous != null) {
            previous._next = this;
        }
    }
}
