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
import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.stream.buffer.stax.StreamReaderBufferCreator;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import javax.xml.stream.XMLInputFactory;

public class StAXParserCreatorDriver extends JapexDriverBase {
    ByteArrayInputStream _in;
    XMLInputFactory _factory;
    StreamReaderBufferCreator _creator;
    MutableXMLStreamBuffer _buffer;
    
    public void initializeDriver() {
        try {
            _factory = XMLInputFactory.newInstance();
            _creator = new StreamReaderBufferCreator();
            _buffer = new MutableXMLStreamBuffer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }   
    
    public void prepare(TestCase testCase) {
        String xmlFile = TestCaseUtil.getXmlFile(testCase);
        try {
            _in = new ByteArrayInputStream(com.sun.japex.Util.streamToByteArray(new FileInputStream(xmlFile)));        
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void run(TestCase testCase) {
        try {
            _in.reset();
            _buffer.reset();
            _creator.setXMLStreamBuffer(_buffer);
            _creator.create(_factory.createXMLStreamReader(_in));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
