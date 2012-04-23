/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
