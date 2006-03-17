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
public class StAXcreatedStAXprocessedComparator extends BaseComparator {
    
    public Document createDocumentFromXMLStreamBufferFromStream(InputStream in) throws Exception {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(in);
        StreamReaderBufferCreator bc = new StreamReaderBufferCreator();
        MutableXMLStreamBuffer buffer = bc.create(reader);

        StreamReaderBufferProcessor bp = new StreamReaderBufferProcessor(buffer);
        StAXSource s = new StAXSource(bp);
        
        Builder b = new Builder(s.getXMLReader());
        return b.build(in);
    }

    public static void main(String args[]) throws Exception {
        StAXcreatedStAXprocessedComparator c = new StAXcreatedStAXprocessedComparator();
        c.compare(args[0]);
    }
}
