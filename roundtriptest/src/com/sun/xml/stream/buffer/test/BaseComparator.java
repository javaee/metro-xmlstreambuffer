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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.canonical.Canonicalizer;

public abstract class BaseComparator {
    
    public void compare(String file) throws Exception {
        System.out.print(file + ": ");
        Document fromXMLFile = createDocumentFromStream(toInputStream(file));
        Document fromXMLStreamBuffer = createDocumentFromXMLStreamBufferFromStream(toInputStream(file));
        
        String canonicalFromXMLFile = file + "fromXML.c14n.xml";
        canonicalize(fromXMLFile, canonicalFromXMLFile);
        String canonicalFromXMLStreamBuffer = file + "fromXMLStreamBuffer.c14n.xml";
        canonicalize(fromXMLStreamBuffer, canonicalFromXMLStreamBuffer);
                
        if (diff(canonicalFromXMLFile, canonicalFromXMLStreamBuffer)) {
        } else {
            System.out.println("PASSED");
        }
    }
    
    public Document createDocumentFromStream(InputStream in) throws Exception {
        Builder b = new Builder();
        return b.build(in);
    }

    public abstract Document createDocumentFromXMLStreamBufferFromStream(InputStream in) throws Exception;
    
    public InputStream toInputStream(String file) throws Exception {
        return new BufferedInputStream(new FileInputStream(file));
    }
    
    public OutputStream toOutputStream(String file) throws Exception {
        return new BufferedOutputStream(new FileOutputStream(file));
    }
    
    public void canonicalize(Document d, String file) throws Exception {
       OutputStream out = toOutputStream(file);
       Canonicalizer c14ner = new Canonicalizer(out);
       c14ner.write(d);
    }
    
    public boolean diff(String f1, String f2) throws Exception {
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
