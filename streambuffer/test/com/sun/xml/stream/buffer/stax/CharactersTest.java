package com.sun.xml.stream.buffer.stax;

import com.sun.xml.stream.buffer.XMLStreamBuffer;
import junit.framework.TestCase;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

/**
 *
 * @author Jitendra.Kotamraju@Sun.Com
 */
public class CharactersTest extends TestCase {
    
    public CharactersTest(String testName) {
        super(testName);
    }

    public void testCharacters() throws Exception {
        useReaderForTesting(0);
        useReaderForTesting(1);
        useReaderForTesting(512);
        useReaderForTesting(1025);
        useReaderForTesting(8192);
        useReaderForTesting(8193);
        useReaderForTesting(10000);
    }

    private void useReaderForTesting(int len) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("<a>");
        for(int i=0; i < len; i++) {
            builder.append('a');
        }
        builder.append("</a>");
        String str = builder.toString();

        XMLStreamReader rdr = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(str));
        XMLStreamBuffer xsb = XMLStreamBuffer.createNewBufferFromXMLStreamReader(rdr);
        XMLStreamReader xsbrdr = xsb.readAsXMLStreamReader();
        rdr = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(str));

        rdr.next(); xsbrdr.next();
        rdr.next(); xsbrdr.next();
        if (rdr.hasText()) {
            rdr.getTextCharacters();
            compareCharacters(rdr, xsbrdr);
        }
    }

    private void compareCharacters(XMLStreamReader rdr1, XMLStreamReader rdr2) throws Exception {
        char[] buf1 = new char[1024];
        char[] buf2 = new char[1024];
        for (int start=0,read1=buf1.length; read1 == buf1.length; start+=buf1.length) {
            read1 = rdr1.getTextCharacters(start, buf1, 0, buf1.length);
            int read2 = rdr2.getTextCharacters(start, buf2, 0, buf2.length);
            assertEquals(read1, read2);
            assertEquals(new String(buf1), new String(buf2));
        }
    }


}
