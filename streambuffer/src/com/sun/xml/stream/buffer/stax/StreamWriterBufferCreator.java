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
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;

public class StreamWriterBufferCreator extends AbstractCreator implements javax.xml.stream.XMLStreamWriter {
        
    private NamespaceContext namespaceContext = null;
        
    public StreamWriterBufferCreator() {
    }
    
    public StreamWriterBufferCreator(XMLStreamBuffer buffer) {
        setXMLStreamBuffer(buffer);
    }
    
    public Object getProperty (String str) throws IllegalArgumentException {
        throw new UnsupportedOperationException ();
    }
    
    public void close () throws XMLStreamException {
    }
    
    public void flush () throws XMLStreamException {
    }

    
    public javax.xml.namespace.NamespaceContext getNamespaceContext () {
        return namespaceContext;
    }
    
    public void setNamespaceContext (javax.xml.namespace.NamespaceContext namespaceContext) throws XMLStreamException {
        this.namespaceContext = namespaceContext;
    }
    
    public void setDefaultNamespace (String str) throws XMLStreamException {
    }
    
    public void setPrefix (String str, String str1) throws XMLStreamException {
    }
    
    public String getPrefix (String namespaceURI) throws XMLStreamException {
        String prefix = null;
        if(this.namespaceContext != null){
            prefix = namespaceContext.getPrefix (namespaceURI);
        }
        return prefix;
    }

    
    public void writeStartDocument () throws XMLStreamException {
    }    
    
    public void writeStartDocument (String version) throws XMLStreamException {
    }
    
    public void writeStartDocument (String encoding, String version) throws XMLStreamException {
    }
    
    public void writeEndDocument () throws XMLStreamException {
    }
    
    
    public void writeStartElement (String localName) throws XMLStreamException {
    }
    
    public void writeStartElement (String namespaceURI, String localName) throws XMLStreamException {
    }
    
    public void writeStartElement (String prefix, String localName, String namespaceURI) throws XMLStreamException {        
    }
    
    public void writeEmptyElement (String localName) throws XMLStreamException {
    }
    
    public void writeEmptyElement (String namespaceURI, String localName) throws XMLStreamException {
    }
    
    public void writeEmptyElement (String prefix, String localName, String namespaceURI) throws XMLStreamException {
    }
    
    public void writeEndElement () throws XMLStreamException {
    }
    

    public void writeDefaultNamespace (String namespaceURI) throws XMLStreamException {        
    }
    
    public void writeNamespace (String prefix, String namespaceURI) throws XMLStreamException {
    }
    
    
    public void writeAttribute (String localName, String value) throws XMLStreamException {
    }
    
    public void writeAttribute (String namespaceURI,String localName,String value)throws XMLStreamException {
    }
    
    public void writeAttribute (String prefix,String namespaceURI,String localName,String value)throws XMLStreamException {        
    }
    
    
    public void writeCData (String data) throws XMLStreamException {        
    }
    
    
    public void writeCharacters (String charData) throws XMLStreamException {        
    }
    
    public void writeCharacters (char[] values, int param, int param2) throws XMLStreamException {        
    }
    
    
    public void writeComment (String str) throws XMLStreamException {        
    }
    
    public void writeDTD (String str) throws XMLStreamException {
    }
    
    public void writeEntityRef (String str) throws XMLStreamException {
    }
    
    public void writeProcessingInstruction (String str) throws XMLStreamException {
    }
    
    public void writeProcessingInstruction (String str, String str1) throws XMLStreamException {
    }
}

