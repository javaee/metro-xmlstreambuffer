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
    
    public void writeFragment(XMLStreamWriter writer)throws XMLStreamException{
        int item = 0;
        int index = 0;
        do {
            
            item = _stateTable[readStructure()];
            
            switch(item) {
                
                case STATE_ELEMENT_U_LN_QN:{
                    index ++;
                    String uri = readStructureString();
                    String localName = readStructureString();
                    String qname = readStructureString();
                    int pIndex = qname.indexOf(':');
                    String prefix = "";
                    if(pIndex != -1 ){
                        prefix = qname.substring(0,pIndex);
                    }
                    if(prefix.length() >0){
                        writer.writeStartElement(prefix,localName,uri);
                    }else{
                        writer.writeStartElement(uri,localName);
                    }
                    break;
                }
                case STATE_ELEMENT_P_U_LN:{
                    index ++;
                    String prefix = readStructureString();
                    String uri = readStructureString();
                    String localName = readStructureString();
                    writer.writeStartElement(prefix,localName,uri);
                    break;
                }
                case STATE_ELEMENT_U_LN: {
                    index ++;
                    String uri = readStructureString();
                    String localName = readStructureString();
                    writer.writeStartElement(uri,localName);
                    break;
                }
                case STATE_ELEMENT_LN: {
                    index ++;
                    String localName = readStructureString();
                    writer.writeStartElement(localName);
                    break;
                }
                case STATE_NAMESPACE_ATTRIBUTE_P_U:{
                    String prefix = readStructureString();
                    String uri = readStructureString();
                    writer.writeNamespace(prefix,uri);
                    break;
                }
                case STATE_NAMESPACE_ATTRIBUTE_U:{
                    String uri = readStructureString();
                    writer.writeDefaultNamespace(uri);
                    break;
                }
                case STATE_ATTRIBUTE_U_LN_QN:{
                    String prefix = readStructureString();
                    String uri = readStructureString();
                    String localName = readStructureString();
                    writer.writeAttribute(prefix,uri,localName,readContentString());
                    break;
                }
                case STATE_ATTRIBUTE_P_U_LN:{
                    
                    String prefix = readStructureString();
                    String uri = readStructureString();
                    String localName = readStructureString();
                    writer.writeAttribute(prefix,uri,localName,readContentString());
                    break;
                }
                case STATE_ATTRIBUTE_U_LN : {
                    String uri = readStructureString();
                    String localName = readStructureString();
                    writer.writeAttribute(uri,localName,readContentString());
                    break;
                }
                case STATE_ATTRIBUTE_LN: {
                    String localName = readStructureString();
                    writer.writeAttribute(localName,readContentString());
                    break;
                }
                case STATE_TEXT_AS_CHAR_ARRAY:{
                    final int length = readStructure();
                    int start = readContentCharactersBuffer(length);
                    writer.writeCharacters(_contentCharactersBuffer,start,length);
                    break;
                }
                case STATE_TEXT_AS_STRING:{
                    String s = readStructureString();
                    writer.writeCharacters(s);
                    break;
                }
                case STATE_COMMENT_AS_CHAR_ARRAY:{
                    final int length = readStructure();
                    final int start = readContentCharactersBuffer(length);
                    char [] data = new char[length];
                    System.arraycopy(_contentCharactersBuffer,start,data,0,length);
                    String comment = new String(data);
                    writer.writeComment(comment);
                    break;
                }
                case STATE_COMMENT_AS_CHAR_ARRAY_COPY:{
                    final char[] ch = readContentCharactersCopy();
                    writer.writeComment(new String(ch));
                    break;
                }
                case STATE_PROCESSING_INSTRUCTION:{
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
        }while(index == 0);
        
    }
    
    public void write(XMLStreamWriter writer) throws XMLStreamException{
        int item = 0;
        while(item != STATE_END_DOCUMENT) {
            
            //while(item != T_END_OF_BUFFER) {
            item = _stateTable[readStructure()];
            writer.flush();
            
            switch(item) {
                case STATE_DOCUMENT:{
                    writer.writeStartDocument();
                    break;
                }
                case T_END_OF_BUFFER:{
                    return;
                }
                case STATE_ELEMENT_U_LN_QN:{
                    String uri = readStructureString();
                    String localName = readStructureString();
                    String qname = readStructureString();
                    int index = qname.indexOf(':');
                    String prefix = "";
                    if(index != -1 ){
                        prefix = qname.substring(0,index);
                    }
                    if(prefix.length() >0){
                        writer.writeStartElement(prefix,localName,uri);
                    }else{
                        writer.writeStartElement(uri,localName);
                    }
                    break;
                }
                case STATE_ELEMENT_P_U_LN:{
                    String prefix = readStructureString();
                    String uri = readStructureString();
                    String localName = readStructureString();
                    writer.writeStartElement(prefix,localName,uri);
                    break;
                }
                case STATE_ELEMENT_U_LN: {
                    String uri = readStructureString();
                    String localName = readStructureString();
                    writer.writeStartElement(uri,localName);
                    break;
                }
                case STATE_ELEMENT_LN: {
                    String localName = readStructureString();
                    writer.writeStartElement(localName);
                    break;
                }
                case STATE_NAMESPACE_ATTRIBUTE_P_U:{
                    String prefix = readStructureString();
                    String uri = readStructureString();
                    writer.writeNamespace(prefix,uri);
                    break;
                }
                case STATE_NAMESPACE_ATTRIBUTE_U:{
                    String uri = readStructureString();
                    writer.writeDefaultNamespace(uri);
                    break;
                }
                case STATE_ATTRIBUTE_U_LN_QN:{
                    String prefix = readStructureString();
                    String uri = readStructureString();
                    String localName = readStructureString();
                    writer.writeAttribute(prefix,uri,localName,readContentString());
                    break;
                }
                case STATE_ATTRIBUTE_P_U_LN:{
                    
                    String prefix = readStructureString();
                    String uri = readStructureString();
                    String localName = readStructureString();
                    writer.writeAttribute(prefix,uri,localName,readContentString());
                    break;
                }
                case STATE_ATTRIBUTE_U_LN : {
                    String uri = readStructureString();
                    String localName = readStructureString();
                    writer.writeAttribute(uri,localName,readContentString());
                    break;
                }
                case STATE_ATTRIBUTE_LN: {
                    String localName = readStructureString();
                    writer.writeAttribute(localName,readContentString());
                    break;
                }
                case STATE_TEXT_AS_CHAR_ARRAY:{
                    final int length = readStructure();
                    int start = readContentCharactersBuffer(length);
                    writer.writeCharacters(_contentCharactersBuffer,start,length);
                    break;
                }
                case STATE_TEXT_AS_STRING:{
                    String s = readStructureString();
                    writer.writeCharacters(s);
                    break;
                }
                case STATE_COMMENT_AS_CHAR_ARRAY:{
                    final int length = readStructure();
                    final int start = readContentCharactersBuffer(length);
                    char [] data = new char[length];
                    System.arraycopy(_contentCharactersBuffer,start,data,0,length);
                    String comment = new String(data);
                    writer.writeComment(comment);
                    break;
                }
                case STATE_COMMENT_AS_CHAR_ARRAY_COPY:{
                    final char[] ch = readContentCharactersCopy();
                    writer.writeComment(new String(ch));
                    break;
                }
                case STATE_PROCESSING_INSTRUCTION:{
                    break;
                }
                case STATE_END:{
                    writer.writeEndElement();
                    break;
                }
                default:{
                    throw new XMLStreamException("Invalid State "+item);
                    
                }
            }
        }
        writer.writeEndDocument();
        
    }
    
    
    
}
