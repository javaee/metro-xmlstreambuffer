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

package com.sun.xml.stream.buffer.japex;

import com.sun.japex.JapexDriverBase;
import com.sun.japex.TestCase;
import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.stream.buffer.stax.StreamReaderBufferCreator;
import com.sun.xml.stream.buffer.stax.StreamReaderBufferProcessor;
import java.io.FileInputStream;

public class StAXCreatorDriver extends JapexDriverBase {
    XMLStreamBuffer _initialBuffer;
    StreamReaderBufferProcessor _processor;
    StreamReaderBufferCreator _creator;
    MutableXMLStreamBuffer _buffer;
    
    public void initializeDriver() {
        _processor = new StreamReaderBufferProcessor();
        _creator = new StreamReaderBufferCreator();
        _buffer = new MutableXMLStreamBuffer();
    }   
    
    public void prepare(TestCase testCase) {
        String xmlFile = TestCaseUtil.getXmlFile(testCase);
        
        try {
            _initialBuffer = TestCaseUtil.createXMLStreamBufferFromStream(new FileInputStream(xmlFile));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void run(TestCase testCase) {
        try {
            _buffer.reset();
            _processor.setXMLStreamBuffer(_initialBuffer);
            _creator.setXMLStreamBuffer(_buffer);
            _creator.create(_processor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
