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

public class XMLStreamBufferMark extends XMLStreamBuffer {
    
    public XMLStreamBufferMark(Map inscopeNamespaces, AbstractCreator creator) {
        _inscopeNamespaces = inscopeNamespaces;
        
        _structure = creator._currentStructureFragment;
        _structurePtr = creator._structurePtr;
        
        _structureStrings = creator._currentStructureStringFragment;
        _structureStringsPtr = creator._structureStringsPtr;
        
        _contentStrings = creator._currentContentStringFragment;
        _contentStringsPtr = creator._contentStringsPtr;
        
        _contentCharacters = creator._currentContentCharactersFragment;
        _contentCharactersPtr = creator._contentCharactersPtr;
        
        _contentCharactersBuffer = creator._currentContentCharactersBufferFragment;
        _contentCharactersBufferPtr = creator._contentCharactersBufferPtr;
    }    
}
