/*
 * ABC.java
 *
 * Created on March 8, 2006, 12:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.stream.buffer.test;

import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.stream.buffer.stax.StreamReaderBufferCreator;
import com.sun.xml.stream.buffer.stax.StreamReaderBufferProcessor;
import com.sun.xml.stream.buffer.stax.StreamWriterBufferCreator;
import com.sun.xml.stream.buffer.stax.StreamWriterBufferProcessor;
import java.io.InputStream;
import javanet.staxutils.StAXSource;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import nu.xom.Builder;
import nu.xom.Document;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class StAXwriterCreatedStAXwriterProcessedComparator extends BaseComparator {
    
    public Document createDocumentFromXMLStreamBufferFromStream(InputStream in) throws Exception {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(in);
        StreamReaderBufferCreator srbc = new StreamReaderBufferCreator();
        MutableXMLStreamBuffer originalBuffer = srbc.create(reader);

        MutableXMLStreamBuffer buffer = new MutableXMLStreamBuffer();
        StreamWriterBufferCreator swbc = new StreamWriterBufferCreator();
        swbc.setXMLStreamBuffer(buffer);
        
        StreamWriterBufferProcessor swbp = new StreamWriterBufferProcessor();
        swbp.process(originalBuffer, swbc);
        
        StreamReaderBufferProcessor bp = new StreamReaderBufferProcessor(buffer);
        StAXSource s = new StAXSource(bp);
        
        Builder b = new Builder(s.getXMLReader());
        return b.build(in);
    }

    public static void main(String args[]) throws Exception {
        StAXwriterCreatedStAXwriterProcessedComparator c = new StAXwriterCreatedStAXwriterProcessedComparator();
        c.compare(args[0]);
    }
}