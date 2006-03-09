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
package com.sun.xml.stream.buffer.stax;

import com.sun.xml.stream.buffer.AbstractCreator;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.stream.buffer.XMLStreamBufferException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.jvnet.staxex.Base64Data;
import org.jvnet.staxex.XMLStreamReaderEx;

/*
 * TODO
 * Implement the marking the stream on the element when an ID
 * attribute on the element is defined
 */
public class StreamReaderBufferCreator extends StreamBufferCreator {
    private int _eventType;
    private boolean _manageInScopeNamespaces;
    private boolean _storeInScopeNamespacesOnElementFragment;
    private boolean _markElementsWithIDs;
    private boolean _storeInScopeNamespacesOnMarkedElements;
    private Map<String, Integer> _inScopePrefixes;

    public StreamReaderBufferCreator() {
    }

    public StreamReaderBufferCreator(XMLStreamBuffer buffer) {
        setBuffer(buffer);
    }

    public XMLStreamBuffer create(XMLStreamReader reader) throws XMLStreamException, XMLStreamBufferException {
        if (_buffer == null) {
            createBuffer();
        }
        store(reader);

        return getXMLStreamBuffer();
    }

    public XMLStreamBuffer createElementFragment(XMLStreamReader reader,
            boolean storeInScopeNamespaces) throws XMLStreamException, XMLStreamBufferException {
        if (_buffer == null) {
            createBuffer();
        }

        if (!reader.hasNext()) {
            return _buffer;
        }

        _manageInScopeNamespaces = _storeInScopeNamespacesOnElementFragment = storeInScopeNamespaces;

        _eventType = reader.getEventType();
        if (_eventType != XMLStreamReader.START_ELEMENT) {
            do {
                _eventType = reader.next();
            } while(_eventType != XMLStreamReader.START_ELEMENT && _eventType != XMLStreamReader.END_DOCUMENT);
        }

        if (_manageInScopeNamespaces) {
            _inScopePrefixes = new HashMap();
        }

        storeElementAndChildren(reader);

        return getXMLStreamBuffer();
    }

    private void store(XMLStreamReader reader) throws XMLStreamException, XMLStreamBufferException {
        if (!reader.hasNext()) {
            return;
        }

        _eventType = reader.getEventType();
        switch (_eventType) {
            case XMLStreamReader.START_DOCUMENT:
                storeDocumentAndChildren(reader);
                break;
            case XMLStreamReader.START_ELEMENT:
                storeElementAndChildren(reader);
                break;
            default:
                throw new XMLStreamBufferException("");
        }
    }

    private void storeDocumentAndChildren(XMLStreamReader reader) throws XMLStreamException {
        storeStructure(T_DOCUMENT);

        _eventType = reader.next();
        while (_eventType != XMLStreamReader.END_DOCUMENT) {
            switch (_eventType) {
                case XMLStreamReader.START_ELEMENT:
                    storeElementAndChildren(reader);
                    continue;
                case XMLStreamReader.COMMENT:
                    storeComment(reader);
                    break;
                case XMLStreamReader.PROCESSING_INSTRUCTION:
                    storeProcessingInstruction(reader);
                    break;
            }
            _eventType = reader.next();
        }

        storeStructure(T_END);
    }

    private void storeElementAndChildren(XMLStreamReader reader) throws XMLStreamException {
        if (reader instanceof XMLStreamReaderEx) {
            storeElementAndChildrenEx((XMLStreamReaderEx)reader);
        } else {
            storeElementAndChildrenNoEx(reader);
        }
    }

    private void storeElementAndChildrenEx(XMLStreamReaderEx reader) throws XMLStreamException {
        int depth = 1;
        if (_storeInScopeNamespacesOnElementFragment) {
            storeElementWithInScopeNamespaces(reader);
        } else {
            storeElement(reader);
        }

        while(depth > 0) {
            _eventType = reader.next();
            switch (_eventType) {
                case XMLStreamReader.START_ELEMENT:
                    depth++;
                    storeElement(reader);
                    break;
                case XMLStreamReader.END_ELEMENT:
                    depth--;
                    storeStructure(T_END);
                    break;
                case XMLStreamReader.NAMESPACE:
                    storeNamespaceAttributes(reader);
                    break;
                case XMLStreamReader.ATTRIBUTE:
                    storeAttributes(reader);
                    break;
                case XMLStreamReader.SPACE:
                case XMLStreamReader.CHARACTERS:
                case XMLStreamReader.CDATA: {
                    CharSequence c = reader.getPCDATA();
                    if (c instanceof Base64Data) {
                        storeStructure(T_TEXT_AS_OBJECT);
                        storeContentObject(((Base64Data)c).clone());
                    } else {
                        storeContentCharacters(T_TEXT_AS_CHAR_ARRAY,
                                reader.getTextCharacters(), reader.getTextStart(),
                                reader.getTextLength());
                    }
                    break;
                }
                case XMLStreamReader.COMMENT:
                    storeComment(reader);
                    break;
                case XMLStreamReader.PROCESSING_INSTRUCTION:
                    storeProcessingInstruction(reader);
                    break;
            }
        }

        /*
         * Move to next item after the end of the element
         * that has been stored
         */
        _eventType = reader.next();
    }

    private void storeElementAndChildrenNoEx(XMLStreamReader reader) throws XMLStreamException {
        int depth = 1;
        if (_storeInScopeNamespacesOnElementFragment) {
            storeElementWithInScopeNamespaces(reader);
        } else {
            storeElement(reader);
        }

        while(depth > 0) {
            _eventType = reader.next();
            switch (_eventType) {
                case XMLStreamReader.START_ELEMENT:
                    depth++;
                    storeElement(reader);
                    break;
                case XMLStreamReader.END_ELEMENT:
                    depth--;
                    storeStructure(T_END);
                    break;
                case XMLStreamReader.NAMESPACE:
                    storeNamespaceAttributes(reader);
                    break;
                case XMLStreamReader.ATTRIBUTE:
                    storeAttributes(reader);
                    break;
                case XMLStreamReader.SPACE:
                case XMLStreamReader.CHARACTERS:
                case XMLStreamReader.CDATA: {
                    storeContentCharacters(T_TEXT_AS_CHAR_ARRAY,
                            reader.getTextCharacters(), reader.getTextStart(),
                            reader.getTextLength());
                    break;
                }
                case XMLStreamReader.COMMENT:
                    storeComment(reader);
                    break;
                case XMLStreamReader.PROCESSING_INSTRUCTION:
                    storeProcessingInstruction(reader);
                    break;
            }
        }

        /*
         * Move to next item after the end of the element
         * that has been stored
         */
        _eventType = reader.next();
    }

    private void storeElementWithInScopeNamespaces(XMLStreamReader reader) throws XMLStreamException {
        storeQualifiedName(T_ELEMENT_LN,
                reader.getPrefix(), reader.getNamespaceURI(), reader.getLocalName());

        if (reader.getNamespaceCount() > 0) {
            storeNamespaceAttributes(reader);
        }

        if (reader.getAttributeCount() > 0) {
            storeAttributes(reader);
        }
    }

    private void storeElement(XMLStreamReader reader) throws XMLStreamException {
        storeQualifiedName(T_ELEMENT_LN,
                reader.getPrefix(), reader.getNamespaceURI(), reader.getLocalName());

        if (reader.getNamespaceCount() > 0) {
            storeNamespaceAttributes(reader);
        }

        if (reader.getAttributeCount() > 0) {
            storeAttributes(reader);
        }
    }

    private void storeNamespaceAttributes(XMLStreamReader reader) {
        for (int i = 0; i < reader.getNamespaceCount(); i++) {
            storeNamespaceAttribute(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
        }
    }

    private void storeAttributes(XMLStreamReader reader) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            storeAttribute(reader.getAttributePrefix(i), reader.getAttributeNamespace(i), reader.getAttributeLocalName(i),
                    reader.getAttributeType(i), reader.getAttributeValue(i));
        }
    }

    private void storeComment(XMLStreamReader reader) {
        storeContentCharacters(T_COMMENT_AS_CHAR_ARRAY,
                reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
    }

    private void storeProcessingInstruction(XMLStreamReader reader) {
        storeProcessingInstruction(reader.getPITarget(), reader.getPIData());
    }
}
