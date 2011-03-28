/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

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
