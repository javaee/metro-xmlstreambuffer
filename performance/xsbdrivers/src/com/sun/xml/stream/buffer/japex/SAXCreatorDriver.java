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
package com.sun.xml.stream.buffer.japex;

import com.sun.japex.JapexDriverBase;
import com.sun.japex.TestCase;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.stream.buffer.sax.SAXBufferCreator;
import com.sun.xml.stream.buffer.sax.SAXBufferProcessor;
import java.io.FileInputStream;

public class SAXCreatorDriver extends JapexDriverBase {
    XMLStreamBuffer _initialBuffer;
    SAXBufferProcessor _processor;
    SAXBufferCreator _creator;
    XMLStreamBuffer _buffer;
    
    public void initializeDriver() {
        _processor = new SAXBufferProcessor();
        _creator = new SAXBufferCreator();
        
        _processor.setContentHandler(_creator);
        _processor.setLexicalHandler(_creator);
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
            _creator.setXMLStreamBuffer(_buffer);
            _processor.process(_initialBuffer);
            _creator.getXMLStreamBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}