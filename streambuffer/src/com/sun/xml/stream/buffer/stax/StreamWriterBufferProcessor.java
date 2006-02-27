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

import com.sun.xml.stream.buffer.AbstractProcessor;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.stream.buffer.XMLStreamBufferException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.jvnet.staxex.XMLStreamWriterEx;


/**
 * @author Paul.Sandoz@Sun.Com,K.Venugopal@sun.com
 */
public class StreamWriterBufferProcessor extends AbstractProcessor {
    
    
    public StreamWriterBufferProcessor() {
    }
    
    public StreamWriterBufferProcessor(XMLStreamBuffer buffer) {
        setXMLStreamBuffer(buffer);
    }
    
    public final void process(XMLStreamBuffer buffer, XMLStreamWriter writer) throws XMLStreamBufferException, XMLStreamException {
        setXMLStreamBuffer(buffer);
        process(writer);
    }
    
    public void process(XMLStreamWriter writer) throws XMLStreamBufferException, XMLStreamException {
        if(_buffer.isFragment()){
            writeFragment(writer);
        }else{
            write(writer);
        }
    }

    public void setXMLStreamBuffer(XMLStreamBuffer buffer) {
        setBuffer(buffer);
    }
    
    public void writeFragment(XMLStreamWriter writer) throws XMLStreamException {
        if (writer instanceof XMLStreamWriterEx) {
            writeFragmentEx((XMLStreamWriterEx)writer);
        } else {
            writeFragmentNoEx(writer);
        }
    }
    
    public void writeFragmentEx(XMLStreamWriterEx writer) throws XMLStreamException {
        int item = 0;
        int index = 0;
        
        do {
            
            item = _eiiStateTable[readStructure()];
            
            switch(item) {
                case STATE_ELEMENT_U_LN_QN:{
                    index ++;
                    final String uri = readStructureString();
                    final String localName = readStructureString();
                    final String prefix = getPrefixFromQName(readStructureString());
                    writer.writeStartElement(prefix,localName,uri);
                    
                    break;
                }
                case STATE_ELEMENT_P_U_LN:{
                    index ++;
                    final String prefix = readStructureString();
                    final String uri = readStructureString();
                    final String localName = readStructureString();
                    writer.writeStartElement(prefix,localName,uri);
                    break;
                }
                case STATE_ELEMENT_U_LN: {
                    index ++;
                    final String uri = readStructureString();
                    final String localName = readStructureString();
                    writer.writeStartElement(uri,localName);
                    break;
                }
                case STATE_ELEMENT_LN: {
                    index ++;
                    final String localName = readStructureString();
                    writer.writeStartElement(localName);
                    break;
                }
                case STATE_TEXT_AS_CHAR_ARRAY:{
                    final int length = readStructure();
                    final int start = readContentCharactersBuffer(length);
                    writer.writeCharacters(_contentCharactersBuffer,start,length);
                    break;
                }
                case STATE_TEXT_AS_STRING:{
                    final String s = readContentString();
                    writer.writeCharacters(s);
                    break;
                }
                case STATE_TEXT_AS_OBJECT:{
                    final CharSequence c = (CharSequence)readContentObject();
                    writer.writePCDATA(c);
                    break;
                }
                case STATE_COMMENT_AS_CHAR_ARRAY:{
                    final int length = readStructure();
                    final int start = readContentCharactersBuffer(length);
                    final String comment = new String(_contentCharactersBuffer, start, length);
                    writer.writeComment(comment);
                    break;
                }
                case STATE_COMMENT_AS_CHAR_ARRAY_COPY:{
                    final char[] ch = readContentCharactersCopy();
                    writer.writeComment(new String(ch));
                    break;
                }
                case STATE_PROCESSING_INSTRUCTION:{
                    writer.writeProcessingInstruction(readStructureString(), readStructureString());
                    break;
                }
                case STATE_END:{
                    writer.writeEndElement();
                    index --;
                    break;
                }
                default:{
                    throw new XMLStreamException("Invalid State "+item);
                }
            }
        } while(index > 0);
        
    }
    
    public void writeFragmentNoEx(XMLStreamWriter writer) throws XMLStreamException {
        int item = 0;
        int index = 0;
        
        do {
            
            item = _eiiStateTable[readStructure()];
            
            switch(item) {
                case STATE_ELEMENT_U_LN_QN:{
                    index ++;
                    final String uri = readStructureString();
                    final String localName = readStructureString();
                    final String prefix = getPrefixFromQName(readStructureString());
                    writer.writeStartElement(prefix,localName,uri);
                    
                    break;
                }
                case STATE_ELEMENT_P_U_LN:{
                    index ++;
                    final String prefix = readStructureString();
                    final String uri = readStructureString();
                    final String localName = readStructureString();
                    writer.writeStartElement(prefix,localName,uri);
                    break;
                }
                case STATE_ELEMENT_U_LN: {
                    index ++;
                    final String uri = readStructureString();
                    final String localName = readStructureString();
                    writer.writeStartElement(uri,localName);
                    break;
                }
                case STATE_ELEMENT_LN: {
                    index ++;
                    final String localName = readStructureString();
                    writer.writeStartElement(localName);
                    break;
                }
                case STATE_TEXT_AS_CHAR_ARRAY:{
                    final int length = readStructure();
                    final int start = readContentCharactersBuffer(length);
                    writer.writeCharacters(_contentCharactersBuffer,start,length);
                    break;
                }
                case STATE_TEXT_AS_STRING:{
                    final String s = readContentString();
                    writer.writeCharacters(s);
                    break;
                }
                case STATE_TEXT_AS_OBJECT:{
                    final CharSequence c = (CharSequence)readContentObject();
                    writer.writeCharacters(c.toString());
                    break;
                }
                case STATE_COMMENT_AS_CHAR_ARRAY:{
                    final int length = readStructure();
                    final int start = readContentCharactersBuffer(length);
                    final String comment = new String(_contentCharactersBuffer, start, length);
                    writer.writeComment(comment);
                    break;
                }
                case STATE_COMMENT_AS_CHAR_ARRAY_COPY:{
                    final char[] ch = readContentCharactersCopy();
                    writer.writeComment(new String(ch));
                    break;
                }
                case STATE_PROCESSING_INSTRUCTION:{
                    writer.writeProcessingInstruction(readStructureString(), readStructureString());
                    break;
                }
                case STATE_END:{
                    writer.writeEndElement();
                    index --;
                    break;
                }
                default:{
                    throw new XMLStreamException("Invalid State "+item);
                }
            }
        } while(index > 0);
        
    }
    
    public void write(XMLStreamWriter writer) throws XMLStreamException{
        int item = 0;
        while(item != STATE_END) {
            
            item = _eiiStateTable[readStructure()];
            writer.flush();
            
            switch(item) {
                case STATE_DOCUMENT:{
                    writer.writeStartDocument();
                    break;
                }
                case STATE_ELEMENT_U_LN_QN:
                case STATE_ELEMENT_P_U_LN:
                case STATE_ELEMENT_U_LN:
                case STATE_ELEMENT_LN:
                    writeFragment(writer);
                    break;
                case STATE_COMMENT_AS_CHAR_ARRAY:{
                    final int length = readStructure();
                    final int start = readContentCharactersBuffer(length);
                    final String comment = new String(_contentCharactersBuffer, start, length);
                    writer.writeComment(comment);
                    break;
                }
                case STATE_COMMENT_AS_CHAR_ARRAY_COPY:{
                    final char[] ch = readContentCharactersCopy();
                    writer.writeComment(new String(ch));
                    break;
                }
                case STATE_PROCESSING_INSTRUCTION:{
                    writer.writeProcessingInstruction(readStructureString(), readStructureString());
                    break;
                }
                case STATE_END:{
                    writer.writeEndDocument();
                    break;
                }
                default:{
                    throw new XMLStreamException("Invalid State "+item);
                    
                }
            }
        }
        
    }
    
    private void writeAttributes(XMLStreamWriter writer) throws XMLStreamException {
        int item = peakStructure();
        if ((item & TYPE_MASK) == T_NAMESPACE_ATTRIBUTE) {
            // Skip the namespace declarations on the element
            // they will have been added already
            item = writeNamespaceAttributes(item, writer);
        }
        if ((item & TYPE_MASK) == T_ATTRIBUTE) {
            writeAttributes(item, writer);
        }        
    }
    
    private int writeNamespaceAttributes(int item, XMLStreamWriter writer) throws XMLStreamException {
        do {
            switch(_niiStateTable[item]){
                case STATE_NAMESPACE_ATTRIBUTE:
                    // Undeclaration of default namespace
                    writer.writeDefaultNamespace("");
                    break;
                case STATE_NAMESPACE_ATTRIBUTE_P:
                    // Undeclaration of namespace
                    // Declaration with prefix
                    writer.writeNamespace(readStructureString(), "");
                    break;
                case STATE_NAMESPACE_ATTRIBUTE_P_U:
                    // Declaration with prefix
                    writer.writeNamespace(readStructureString(), readStructureString());
                    break;
                case STATE_NAMESPACE_ATTRIBUTE_U:
                    // Default declaration
                    writer.writeDefaultNamespace(readStructureString());
                    break;                
            }
            readStructure();
            
            item = peakStructure();
        } while((item & TYPE_MASK) == T_NAMESPACE_ATTRIBUTE);
                
        return item;
    }
    
    private void writeAttributes(int item, XMLStreamWriter writer) throws XMLStreamException {
        do {
            switch(_aiiStateTable[item]) {
                case STATE_ATTRIBUTE_U_LN_QN: {
                    final String uri = readStructureString();
                    final String localName = readStructureString();
                    final String prefix = getPrefixFromQName(readStructureString());
                    writer.writeAttribute(prefix,uri,localName,readContentString());
                    break;
                }
                case STATE_ATTRIBUTE_P_U_LN:
                    writer.writeAttribute(readStructureString(), readStructureString(), 
                            readStructureString(), readContentString());
                    break;
                case STATE_ATTRIBUTE_U_LN:
                    writer.writeAttribute(readStructureString(), readStructureString(), readContentString());
                    break;
                case STATE_ATTRIBUTE_LN:
                    writer.writeAttribute(readStructureString() ,readContentString());
                    break;
            }
            readStructure();
            
            item = peakStructure();
        } while((item & TYPE_MASK) == T_ATTRIBUTE);
    }
}
