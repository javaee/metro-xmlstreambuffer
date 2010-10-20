/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.stream.buffer;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

/**
 * {@link TestCase} with more convenience methods for testing XMLStreamBuffer.
 *
 * @author Paul Sandoz
 */
public abstract class BaseBufferTestCase extends junit.framework.TestCase {
    protected BaseBufferTestCase() {
    }

    protected BaseBufferTestCase(String name) {
        super(name);
    }

    public int next(XMLStreamReader reader) throws XMLStreamException {
        int readerEvent = reader.next();

        while (readerEvent != XMLStreamReader.END_DOCUMENT) {
            switch (readerEvent) {
                case XMLStreamReader.START_ELEMENT:
                case XMLStreamReader.END_ELEMENT:
                case XMLStreamReader.CDATA:
                case XMLStreamReader.CHARACTERS:
                case XMLStreamReader.PROCESSING_INSTRUCTION:
                    return readerEvent;
                default:
                    // falls through ignoring event
            }
            readerEvent = reader.next();
        }

        return readerEvent;
    }

    public int nextElementContent(XMLStreamReader reader) throws XMLStreamException {
        int state = nextContent(reader);
        if (state == XMLStreamReader.CHARACTERS) {
            throw new XMLStreamException(
                "Unexpected Character Content: " + reader.getText());
        }
        return state;
    }

    public int nextContent(XMLStreamReader reader) throws XMLStreamException {
        for (;;) {
            int state = next(reader);
            switch (state) {
                case XMLStreamReader.START_ELEMENT:
                case XMLStreamReader.END_ELEMENT:
                case XMLStreamReader.END_DOCUMENT:
                    return state;
                case XMLStreamReader.CHARACTERS:
                    if (!reader.isWhiteSpace()) {
                        return XMLStreamReader.CHARACTERS;
                    }
            }
        }
    }

    public void verifyReaderState(XMLStreamReader reader, int expectedState) throws XMLStreamException {
        int state = reader.getEventType();
        if (state != expectedState) {
            throw new XMLStreamException(
                "Unexpected State: " + getStateName(expectedState) + " " + getStateName(state));
        }
    }

    public static void verifyTag(XMLStreamReader reader, String namespaceURI, String localName) throws XMLStreamException {
        if (localName != reader.getLocalName() || namespaceURI != reader.getNamespaceURI()) {
            throw new XMLStreamException(
                "Expected: " +
                    "{" + namespaceURI + "}" + localName + " " +
                "But found: " +
                    "{" + reader.getNamespaceURI() + "}" + reader.getLocalName());
        }
    }

    public static String getStateName(int state) {
        switch (state) {
            case XMLStreamReader.ATTRIBUTE:
                return "ATTRIBUTE";
            case XMLStreamReader.CDATA:
                return "CDATA";
            case XMLStreamReader.CHARACTERS:
                return "CHARACTERS";
            case XMLStreamReader.COMMENT:
                return "COMMENT";
            case XMLStreamReader.DTD:
                return "DTD";
            case XMLStreamReader.END_DOCUMENT:
                return "END_DOCUMENT";
            case XMLStreamReader.END_ELEMENT:
                return "END_ELEMENT";
            case XMLStreamReader.ENTITY_DECLARATION:
                return "ENTITY_DECLARATION";
            case XMLStreamReader.ENTITY_REFERENCE:
                return "ENTITY_REFERENCE";
            case XMLStreamReader.NAMESPACE:
                return "NAMESPACE";
            case XMLStreamReader.NOTATION_DECLARATION:
                return "NOTATION_DECLARATION";
            case XMLStreamReader.PROCESSING_INSTRUCTION:
                return "PROCESSING_INSTRUCTION";
            case XMLStreamReader.SPACE:
                return "SPACE";
            case XMLStreamReader.START_DOCUMENT:
                return "START_DOCUMENT";
            case XMLStreamReader.START_ELEMENT:
                return "START_ELEMENT";
            default :
                return "UNKNOWN";
        }
    }
}
