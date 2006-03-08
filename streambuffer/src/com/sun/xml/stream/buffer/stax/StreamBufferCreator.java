package com.sun.xml.stream.buffer.stax;

import com.sun.xml.stream.buffer.AbstractCreator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * {@link AbstractCreator} with additional convenience code.
 *
 * @author Paul Sandoz
 * @author Venu
 * @author Kohsuke Kawaguchi
 */
abstract class StreamBufferCreator extends AbstractCreator {
    
    protected final void storeQualifiedName(int item, String prefix, String uri, String localName) {
        if (uri != null && uri.length() > 0) {
            if (prefix != null && prefix.length() > 0) {
                item |= FLAG_PREFIX;
                storeStructureString(prefix);
            }

            item |= FLAG_URI;
            storeStructureString(uri);
        }

        storeStructureString(localName);

        storeStructure(item);
    }

    protected final void storeNamespaceAttribute(String prefix, String uri) {
        int item = T_NAMESPACE_ATTRIBUTE;

        if (prefix != null && prefix.length() > 0) {
            item |= FLAG_PREFIX;
            storeStructureString(prefix);
        }

        if (uri != null && uri.length() > 0) {
            item |= FLAG_URI;
            storeStructureString(uri);
        }

        storeStructure(item);
    }

    protected final void storeAttribute(String prefix, String uri, String localName, String type, String value) throws XMLStreamException {
        storeQualifiedName(T_ATTRIBUTE_LN, prefix, uri, localName);

        storeStructureString(type);
        storeContentString(value);
    }
    
    protected final void storeProcessingInstruction(String target, String data) {
        storeStructure(T_PROCESSING_INSTRUCTION);
        storeStructureString(target);
        storeStructureString(data);
    }
}
