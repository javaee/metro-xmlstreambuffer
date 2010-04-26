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

import java.util.Map;

/**
 * A mark into a buffer.
 * 
 * <p>
 * A mark can be processed in the same manner as a XMLStreamBuffer.
 * 
 * <p>
 * A mark will share a sub set of information of the buffer that is
 * marked. If the buffer is directly or indirectly associated with a
 * (mutable) {@link XMLStreamBuffer} which is reset and/or re-created
 * then this will invalidate the mark and processing behvaiour of the mark 
 * is undefined. It is the responsibility of the application to manage the
 * relationship between the marked XMLStreamBuffer and one or more marks.
 */
public class XMLStreamBufferMark extends XMLStreamBuffer {
    
    /**
     * Create a mark from the buffer that is being created.
     *
     * <p>
     * A mark will be created from the current position of creation of the 
     * {@link XMLStreamBuffer} that is being created by a {@link AbstractCreator}.
     *
     * @param inscopeNamespaces
     * The in-scope namespaces on the fragment of XML infoset that is
     * to be marked.
     *
     * @param src
     * The {@link AbstractCreator} or {@link AbstractProcessor} from which the current
     * position of creation of the XMLStreamBuffer will be taken as the mark.
     */
    public XMLStreamBufferMark(Map<String,String> inscopeNamespaces, AbstractCreatorProcessor src) {
        if(inscopeNamespaces != null) {
            _inscopeNamespaces = inscopeNamespaces;
        }
        
        _structure = src._currentStructureFragment;
        _structurePtr = src._structurePtr;
        
        _structureStrings = src._currentStructureStringFragment;
        _structureStringsPtr = src._structureStringsPtr;
        
        _contentCharactersBuffer = src._currentContentCharactersBufferFragment;
        _contentCharactersBufferPtr = src._contentCharactersBufferPtr;
        
        _contentObjects = src._currentContentObjectFragment;
        _contentObjectsPtr = src._contentObjectsPtr;
        treeCount = 1; // TODO: define a way to create a mark over a forest
    }
}
