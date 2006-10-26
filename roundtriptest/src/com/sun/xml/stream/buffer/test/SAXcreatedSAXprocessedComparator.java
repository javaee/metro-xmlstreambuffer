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
import com.sun.xml.stream.buffer.sax.SAXBufferCreator;
import com.sun.xml.stream.buffer.sax.SAXBufferProcessor;
import java.io.InputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import nu.xom.Builder;
import nu.xom.Document;

/**
 * Round trip from the creation of a buffer using {@link SAXBufferCreator}
 * and processing of the buffer using {@link SAXBufferProcessor}.
 */
public class SAXcreatedSAXprocessedComparator extends BaseComparator {
    
    protected Document createDocumentFromXMLStreamBufferFromStream(InputStream in) throws Exception {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SAXParser sp = spf.newSAXParser();        
        SAXBufferCreator bc = new SAXBufferCreator();
        MutableXMLStreamBuffer buffer = bc.create(sp.getXMLReader(), in);
        
        SAXBufferProcessor bp = new SAXBufferProcessor(buffer,false);
        Builder b = new Builder(bp);
        return b.build(in);
    }

    public static void main(String args[]) throws Exception {
        SAXcreatedSAXprocessedComparator c = new SAXcreatedSAXprocessedComparator();
        c.roundTripTest(args[0]);
    }
}
