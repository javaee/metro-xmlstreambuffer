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
import com.sun.xml.stream.buffer.sax.SAXBufferCreator;
import com.sun.xml.stream.buffer.sax.Properties;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class SAXParserCreatorDriver extends JapexDriverBase {
    ByteArrayInputStream _in;
    SAXParser _parser;
    XMLReader _reader;
    SAXBufferCreator _creator;
    MutableXMLStreamBuffer _buffer;
    
    public void initializeDriver() {
        _creator = new SAXBufferCreator();
        
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            _parser = spf.newSAXParser();        
            _reader = _parser.getXMLReader();
            
            _reader.setContentHandler(_creator);
            _reader.setProperty(Properties.LEXICAL_HANDLER_PROPERTY, _creator);
            
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
            _reader.parse(new InputSource(_in));
            _creator.getXMLStreamBuffer();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
