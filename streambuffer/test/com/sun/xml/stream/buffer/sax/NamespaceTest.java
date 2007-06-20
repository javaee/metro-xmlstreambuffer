package com.sun.xml.stream.buffer.sax;

import com.sun.xml.stream.buffer.XMLStreamBuffer;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.parsers.SAXParserFactory;
import junit.framework.*;
import org.xml.sax.XMLReader;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class NamespaceTest extends TestCase {
    
    public NamespaceTest(String testName) {
        super(testName);
    }

    public void testManyNamespaceDeclarations() throws Exception {
        for (int i = 0; i <= 50; i++) {
            _testManyNamespaceDeclarations(i);
        }
    }
    
    private void _testManyNamespaceDeclarations(int n) throws Exception {
        InputStream d = createDocument(n);
        
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        XMLReader r = spf.newSAXParser().getXMLReader();

        XMLStreamBuffer b = XMLStreamBuffer.createNewBufferFromXMLReader(r, d);
    }
    
    private InputStream createDocument(int n) {
        StringBuilder b = new StringBuilder();
        
        b.append("<root");
        for (int i = 0; i <= n; i++) {
            b.append(" xmlns:p" + i + "='urn:" + i + "'");
        }
        b.append(">");
        b.append("</root>");
        
        return new ByteArrayInputStream(b.toString().getBytes());
    }
}
