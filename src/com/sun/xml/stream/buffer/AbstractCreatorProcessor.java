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

public abstract class AbstractCreatorProcessor {
    /**
     * Flag on a T_DOCUMENT to indicate if a fragment
     */
    protected static final int FLAG_DOCUMENT_FRAGMENT      = 1 << 0;

    /*
     * Flags on T_ELEMENT, T_ATTRIBUTE, T_NAMESPACE_ATTRIBUTE
     * to indicate namespace information is represent
     */
    protected static final int FLAG_PREFIX                 = 1 << 0;
    protected static final int FLAG_URI                    = 1 << 1;
    protected static final int FLAG_QUALIFIED_NAME         = 1 << 2;
    
    /*
     * Types of content for T_TEXT and T_COMMENT
     * <p>
     * Highest 2 bits of lower nibble are used.
     */
    protected static final int CONTENT_TYPE_CHAR_ARRAY        = 0 << 2;
    protected static final int CONTENT_TYPE_CHAR_ARRAY_COPY   = 1 << 2;
    protected static final int CONTENT_TYPE_STRING            = 2 << 2;
    protected static final int CONTENT_TYPE_OBJECT            = 3 << 2;

    /*
     * Size of the length of character content for CONTENT_TYPE_CHAR_ARRAY
     * <p>
     * Last bit of lower nibble is used.
     */
    protected static final int CHAR_ARRAY_LENGTH_SMALL        = 0;
    protected static final int CHAR_ARRAY_LENGTH_MEDIUM       = 1;
    protected static final int CHAR_ARRAY_LENGTH_SMALL_SIZE   = 1 << 8;
    protected static final int CHAR_ARRAY_LENGTH_MEDIUM_SIZE  = 1 << 16;
    
    /*
     * Types of value for T_ATTRIBUTE
     * <p>
     * Highest bit of lower nibble is used.
     */
    protected static final int VALUE_TYPE_STRING              = 0;
    protected static final int VALUE_TYPE_OBJECT              = 1 << 3;

    /*
     * Mask for types.
     * <p>
     * Highest nibble is used.
     */
    protected static final int TYPE_MASK                     = 0xF0;
    protected static final int T_DOCUMENT                    = 0x10;
    protected static final int T_ELEMENT                     = 0x20;
    protected static final int T_ATTRIBUTE                   = 0x30;
    protected static final int T_NAMESPACE_ATTRIBUTE         = 0x40;
    protected static final int T_TEXT                        = 0x50;
    protected static final int T_COMMENT                     = 0x60;
    protected static final int T_PROCESSING_INSTRUCTION      = 0x70;
    protected static final int T_UNEXPANDED_ENTITY_REFERENCE = 0x80;
    protected static final int T_END                         = 0x90;

    /*
     * Composed types.
     * <p>
     * One octet is used.
     */
    protected static final int T_DOCUMENT_FRAGMENT           = T_DOCUMENT | FLAG_DOCUMENT_FRAGMENT;
    
    protected static final int T_ELEMENT_U_LN_QN             = T_ELEMENT | FLAG_URI | FLAG_QUALIFIED_NAME;
    protected static final int T_ELEMENT_P_U_LN              = T_ELEMENT | FLAG_PREFIX | FLAG_URI;
    protected static final int T_ELEMENT_U_LN                = T_ELEMENT | FLAG_URI;
    protected static final int T_ELEMENT_LN                  = T_ELEMENT;
    
    protected static final int T_NAMESPACE_ATTRIBUTE_P       = T_NAMESPACE_ATTRIBUTE | FLAG_PREFIX;
    protected static final int T_NAMESPACE_ATTRIBUTE_P_U     = T_NAMESPACE_ATTRIBUTE | FLAG_PREFIX | FLAG_URI;
    protected static final int T_NAMESPACE_ATTRIBUTE_U       = T_NAMESPACE_ATTRIBUTE | FLAG_URI;
    
    protected static final int T_ATTRIBUTE_U_LN_QN           = T_ATTRIBUTE | FLAG_URI | FLAG_QUALIFIED_NAME;
    protected static final int T_ATTRIBUTE_P_U_LN            = T_ATTRIBUTE | FLAG_PREFIX | FLAG_URI;
    protected static final int T_ATTRIBUTE_U_LN              = T_ATTRIBUTE | FLAG_URI;
    protected static final int T_ATTRIBUTE_LN                = T_ATTRIBUTE;
    protected static final int T_ATTRIBUTE_U_LN_QN_OBJECT    = T_ATTRIBUTE_U_LN_QN | VALUE_TYPE_OBJECT;
    protected static final int T_ATTRIBUTE_P_U_LN_OBJECT     = T_ATTRIBUTE_P_U_LN | VALUE_TYPE_OBJECT;
    protected static final int T_ATTRIBUTE_U_LN_OBJECT       = T_ATTRIBUTE_U_LN | VALUE_TYPE_OBJECT;
    protected static final int T_ATTRIBUTE_LN_OBJECT         = T_ATTRIBUTE_LN | VALUE_TYPE_OBJECT;
    
    protected static final int T_TEXT_AS_CHAR_ARRAY          = T_TEXT;
    protected static final int T_TEXT_AS_CHAR_ARRAY_SMALL    = T_TEXT | CHAR_ARRAY_LENGTH_SMALL;
    protected static final int T_TEXT_AS_CHAR_ARRAY_MEDIUM   = T_TEXT | CHAR_ARRAY_LENGTH_MEDIUM;
    protected static final int T_TEXT_AS_CHAR_ARRAY_COPY     = T_TEXT | CONTENT_TYPE_CHAR_ARRAY_COPY;
    protected static final int T_TEXT_AS_STRING              = T_TEXT | CONTENT_TYPE_STRING;
    protected static final int T_TEXT_AS_OBJECT              = T_TEXT | CONTENT_TYPE_OBJECT;
    
    protected static final int T_COMMENT_AS_CHAR_ARRAY        = T_COMMENT;
    protected static final int T_COMMENT_AS_CHAR_ARRAY_SMALL  = T_COMMENT | CHAR_ARRAY_LENGTH_SMALL;
    protected static final int T_COMMENT_AS_CHAR_ARRAY_MEDIUM = T_COMMENT | CHAR_ARRAY_LENGTH_MEDIUM;
    protected static final int T_COMMENT_AS_CHAR_ARRAY_COPY   = T_COMMENT | CONTENT_TYPE_CHAR_ARRAY_COPY;
    protected static final int T_COMMENT_AS_STRING            = T_COMMENT | CONTENT_TYPE_STRING;
    
    protected static final int T_END_OF_BUFFER               = -1;
    
    protected FragmentedArray<byte[]> _currentStructureFragment;
    protected byte[] _structure;
    protected int _structurePtr;
    
    protected FragmentedArray<String[]> _currentStructureStringFragment;
    protected String[] _structureStrings;
    protected int _structureStringsPtr;
    
    protected FragmentedArray<char[]> _currentContentCharactersBufferFragment;
    protected char[] _contentCharactersBuffer;
    protected int _contentCharactersBufferPtr;        
    
    protected FragmentedArray<Object[]> _currentContentObjectFragment;
    protected Object[] _contentObjects;
    protected int _contentObjectsPtr;        
}
