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

public abstract class AbstractCreatorProcessor {
    protected XMLStreamBuffer _buffer;
    
    protected FragmentedArray<int[]> _currentStructureFragment;
    protected int[] _structure;
    protected int _structurePtr;
    
    protected FragmentedArray<String[]> _currentStructureStringFragment;
    protected String[] _structureStrings;
    protected int _structureStringsPtr;
    
    protected FragmentedArray<String[]> _currentContentStringFragment;
    protected String[] _contentStrings;
    protected int _contentStringsPtr;
    
    protected FragmentedArray<char[][]> _currentContentCharactersFragment;
    protected char[][] _contentCharacters;
    protected int _contentCharactersPtr;
    
    protected FragmentedArray<char[]> _currentContentCharactersBufferFragment;
    protected char[] _contentCharactersBuffer;
    protected int _contentCharactersBufferPtr;        
}
