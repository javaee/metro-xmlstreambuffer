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

import com.sun.xml.stream.buffer.XMLStreamBuffer;

import javax.activation.DataHandler;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jvnet.staxex.NamespaceContextEx;
import org.jvnet.staxex.XMLStreamWriterEx;

import java.io.OutputStream;
import org.jvnet.staxex.Base64Data;

/**
 * {@link XMLStreamWriter} that fills {@link XMLStreamBuffer}.
 */
public class StreamWriterBufferCreator extends StreamBufferCreator implements XMLStreamWriterEx {
    private NamespaceContext namespaceContext = null;

    public StreamWriterBufferCreator() {
    }

    public StreamWriterBufferCreator(XMLStreamBuffer buffer) {
        setXMLStreamBuffer(buffer);
    }

    public Object getProperty(String str) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    public void close() throws XMLStreamException {
    }

    public void flush() throws XMLStreamException {
    }

    public NamespaceContextEx getNamespaceContext() {
        return (NamespaceContextEx)namespaceContext;
    }

    public void setNamespaceContext(NamespaceContext namespaceContext) throws XMLStreamException {
        this.namespaceContext = namespaceContext;
    }

    public void setDefaultNamespace(String str) throws XMLStreamException {
        // TODO: implement this method later
        throw new UnsupportedOperationException();
    }

    public void setPrefix(String str, String str1) throws XMLStreamException {
        // TODO: implement this method later
        throw new UnsupportedOperationException();
    }

    public String getPrefix(String namespaceURI) throws XMLStreamException {
        String prefix = null;
        if (this.namespaceContext != null) {
            prefix = namespaceContext.getPrefix(namespaceURI);
        }
        return prefix;
    }


    public void writeStartDocument() throws XMLStreamException {
        storeStructure(T_DOCUMENT);
    }

    public void writeStartDocument(String version) throws XMLStreamException {
        storeStructure(T_DOCUMENT);
    }

    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        storeStructure(T_DOCUMENT);
    }

    public void writeEndDocument() throws XMLStreamException {
        storeStructure(T_END);
    }

    public void writeStartElement(String localName) throws XMLStreamException {
        storeQualifiedName(T_ELEMENT_LN, null, null, localName);
    }

    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        storeQualifiedName(T_ELEMENT_LN, null, namespaceURI, localName);
    }

    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        storeQualifiedName(T_ELEMENT_LN, prefix, namespaceURI, localName);
    }

    public void writeEmptyElement(String localName) throws XMLStreamException {
        writeStartElement(localName);
        writeEndElement();
    }

    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        writeStartElement(namespaceURI, localName);
        writeEndElement();
    }

    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        writeStartElement(prefix,localName,namespaceURI);
        writeEndElement();
    }

    public void writeEndElement() throws XMLStreamException {
        storeStructure(T_END);
    }

    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        storeNamespaceAttribute(null,namespaceURI);
    }

    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        if("xmlns".equals(prefix))
            prefix = null;
        storeNamespaceAttribute(prefix,namespaceURI);
    }


    public void writeAttribute(String localName, String value) throws XMLStreamException {
        storeAttribute(null,null,localName,"CDATA",value);
    }

    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        // this can work only when we are fixing namespace?
        // TODO: implement this method later
        throw new UnsupportedOperationException();
    }

    public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
        storeAttribute(prefix,namespaceURI,localName,"CDATA",value);
    }

    public void writeCData(String data) throws XMLStreamException {
        storeStructure(T_TEXT_AS_STRING);
        storeContentString(data);
    }

    public void writeCharacters(String charData) throws XMLStreamException {
        storeStructure(T_TEXT_AS_STRING);
        storeContentString(charData);
    }

    public void writeCharacters(char[] buf, int start, int len) throws XMLStreamException {
        storeContentCharacters(T_TEXT_AS_CHAR_ARRAY,buf,start,len);
    }

    public void writeComment(String str) throws XMLStreamException {
        storeStructure(T_COMMENT_AS_STRING);
        storeContentString(str);
    }

    public void writeDTD(String str) throws XMLStreamException {
        // not support. just ignore.
    }

    public void writeEntityRef(String str) throws XMLStreamException {
        storeStructure(T_UNEXPANDED_ENTITY_REFERENCE);
        storeContentString(str);
    }

    public void writeProcessingInstruction(String target) throws XMLStreamException {
        writeProcessingInstruction(target,"");
    }

    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        storeStructure(T_PROCESSING_INSTRUCTION);
        storeContentString(target);
        storeContentString(data);
    }

    public void writePCDATA(CharSequence charSequence) throws XMLStreamException {
        if (charSequence instanceof Base64Data) {
            storeStructure(T_TEXT_AS_OBJECT);
            storeContentObject(((Base64Data)charSequence).clone());
        } else {
            writeCharacters(charSequence.toString());
        }
    }

    public void writeBinary(byte[] bytes, int offset, int length, String endpointURL) throws XMLStreamException {
        Base64Data d = new Base64Data();
        byte b[] = new byte[length];
        System.arraycopy(bytes, offset, b, 0, length);
        d.set(b, length, null, true);
        storeStructure(T_TEXT_AS_OBJECT);
        storeContentObject(d);
    }

    public void writeBinary(DataHandler dataHandler) throws XMLStreamException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public OutputStream writeBinary(String endpointURL) throws XMLStreamException {
        // TODO
        throw new UnsupportedOperationException();
    }

}