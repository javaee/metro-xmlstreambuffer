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
