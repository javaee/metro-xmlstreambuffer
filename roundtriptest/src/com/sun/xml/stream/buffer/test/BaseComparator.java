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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.canonical.Canonicalizer;

/**
 * General functionality for performing round trip tests independent of
 * the creation and processing of a buffer.
 */
public abstract class BaseComparator {
    
    /**
     * Test the round tripping of an XML infoset (A, say) to an XMLStreamBuffer
     * and back again to an XML infoset (B, say).
     *
     * <p>
     * If correctly roundtripped the XML infoset A will be equivalent to the
     * XML infoset B. The canonical XML serializations of XML infosets A and B
     * are compared to ascertain whether the two infosets are equivalent.
     *
     * @param file the file name of the XML document to test (XML infoset A).
     */
    public void roundTripTest(String file) throws Exception {
        System.out.print(file + ": ");
        
        ByteArrayOutputStream canonicalFromXMLFile = new ByteArrayOutputStream();
        ByteArrayOutputStream canonicalFromXMLStreamBuffer = new ByteArrayOutputStream();
        
        if (compare(file, canonicalFromXMLFile, canonicalFromXMLStreamBuffer) == false) {
            System.out.println("FAILED");
        
            String canonicalFromXMLFileName = file + ".fromXML.c14n.xml";
            createFile(canonicalFromXMLFileName, canonicalFromXMLFile);
            String canonicalFromXMLStreamBufferName = file + ".fromXMLStreamBuffer.c14n.xml";
            createFile(canonicalFromXMLStreamBufferName, canonicalFromXMLStreamBuffer);
                
            diff(canonicalFromXMLFileName, canonicalFromXMLStreamBufferName);
        } else {
            System.out.println("PASSED");
        }
    }
    
    /**
     * Compare the canonical XML produced from an XML document with the 
     * canonical XML processed from an XMLStreamBuffer that is created from
     * the same XML document.
     * 
     * @param file the file name of the XML document to test.
     * @param canonicalFromXMLFile the ByteArrayOutputStream to write the
     *        canonical XML of the XML document.
     * @param canonicalFromXMLStreamBuffer the ByteArrayOutputStream to write
     *        canonical XML from the XMLStreamBuffer.
     * @return true if canonicalFromXMLFile and canonicalFromXMLStreamBuffer
     *        are byte-per-byte equivalent, otherwise false.
     */
    public boolean compare(String file, 
            ByteArrayOutputStream canonicalFromXMLFile, 
            ByteArrayOutputStream canonicalFromXMLStreamBuffer) throws Exception {
        Document fromXMLFile = createDocumentFromStream(toInputStream(file));
        Document fromXMLStreamBuffer = createDocumentFromXMLStreamBufferFromStream(toInputStream(file));
        
        canonicalize(fromXMLFile, canonicalFromXMLFile);
        canonicalize(fromXMLStreamBuffer, canonicalFromXMLStreamBuffer);
        
        return compare(canonicalFromXMLFile, canonicalFromXMLStreamBuffer);
    }

    private boolean compare(ByteArrayOutputStream o1, ByteArrayOutputStream o2) {
        return Arrays.equals(o1.toByteArray(),o2.toByteArray());
    }
    
    private Document createDocumentFromStream(InputStream in) throws Exception {
        Builder b = new Builder();
        return b.build(in);
    }

    protected abstract Document createDocumentFromXMLStreamBufferFromStream(InputStream in) throws Exception;
    
    private InputStream toInputStream(String file) throws Exception {
        return new BufferedInputStream(new FileInputStream(file));
    }
    
    private OutputStream toOutputStream(String file) throws Exception {
        return new BufferedOutputStream(new FileOutputStream(file));
    }
    
    private void canonicalize(Document d, OutputStream out) throws Exception {
       Canonicalizer c14ner = new Canonicalizer(out);
       c14ner.write(d);
    }

    private void createFile(String file, ByteArrayOutputStream o) throws Exception {
        OutputStream fo = toOutputStream(file);
        o.writeTo(fo);
        o.flush();
        fo.close();
    }
    
    private boolean diff(String f1, String f2) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("diff", f1, f2);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        try {
            int e = p.waitFor();
        } catch (Exception e) {
        }
        InputStream in = p.getInputStream();
        int v = 0;
        while (v != -1) {
            v = in.read();
            if (v != -1)
                System.out.write(v);
        }
        return p.exitValue() < 0;
    }
}
