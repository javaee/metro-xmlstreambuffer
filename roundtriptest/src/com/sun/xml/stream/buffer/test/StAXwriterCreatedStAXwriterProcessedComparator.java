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
 * Round trip using {@link StreamWriterBufferCreator} and 
 * {@link StreamWriterBufferProcessor}.
 *
 * Round tripping is performed in the following manner:
 * <ul>
 * <li>create a buffer (A, say) using {@link StreamReaderBufferCreator};</li>
 * <li>processing buffer A using {@link StreamWriterBufferProcessor} where the
 *     XMLStreamWriter implementation is {@link StreamWriterBufferCreator}
 *     that is creating a buffer (B, say).
 * <li>process buffer B using {@link StreamReaderBufferProcessor};</li>
 * </ul>
 */
public class StAXwriterCreatedStAXwriterProcessedComparator extends BaseComparator {
    
    protected Document createDocumentFromXMLStreamBufferFromStream(InputStream in) throws Exception {
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
        c.roundTripTest(args[0]);
    }
}