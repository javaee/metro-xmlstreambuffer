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

package com.sun.xml.stream.buffer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
/**
 * 
 * @author K.Venugopal@sun.com
 */
public class NamespaceContextImpl implements NamespaceContext{
    private boolean debug = true;
    protected AttributeNS nsDecl = new AttributeNS();
    protected HashMap<String,Stack> prefixMappings = new HashMap<String,Stack>();
    protected ArrayList<UsedNSList> clearDepth  = new ArrayList<UsedNSList>(10);
    protected int nsDepth;
    protected static final int resizeBy = 10;
    
    
    //TODO ::  Recycle AttributeNS
    public NamespaceContextImpl(){
        //change this
        for(int i=0;i<10;i++){
            clearDepth.add(null);
        }
        
    }
    
    public AttributeNS getNamespaceDeclaration(String prefix){
        Stack stack = prefixMappings.get(prefix);
        if(stack == null || stack.empty() ){
            return null;
        }
        AttributeNS attrNS  = (AttributeNS)stack.peek();
        
        
        return attrNS;
    }
    
    
    public void declareNamespace(String prefix, String uri){
        Stack<AttributeNS> nsDecls = prefixMappings.get(prefix);
        nsDecl.setPrefix(prefix);
        nsDecl.setUri(uri);
        if(nsDecls == null){
            nsDecls = new Stack<AttributeNS>();
            try {
                nsDecls.push((AttributeNS)nsDecl.clone());
                prefixMappings.put(prefix,nsDecls);
            } catch (CloneNotSupportedException ex) {
                throw new RuntimeException(ex);
            }
        }else if(!nsDecls.contains(nsDecl)){//peek should do.
            try {
                nsDecls.push((AttributeNS)nsDecl.clone());
            } catch (CloneNotSupportedException ex) {
                throw new RuntimeException(ex);
            }
        }else{
            return;
        }
        
        
        UsedNSList uList = null;
        uList = (UsedNSList)clearDepth.get(nsDepth);
        if(uList == null){
            uList = new UsedNSList();
            clearDepth.set(nsDepth,uList);
        }
        ArrayList prefixList = uList.getPopList();
        prefixList.add(prefix);
    }
    
    public void push(){
        nsDepth++;
        if(debug){
            System.out.println("--------------------Push depth----------------"+nsDepth);
        }
        if(nsDepth > clearDepth.size()){
            clearDepth.ensureCapacity(clearDepth.size()+resizeBy);
        }
    }
    
    public void pop(){
        if(nsDepth <=0){
            return;
        }
        UsedNSList ul = clearDepth.get(nsDepth);
        if(debug){
            System.out.println("---------------------pop depth----------------------"+nsDepth);
        }
        nsDepth--;
        if(ul == null ){
            return;
        }
        ArrayList<String> pList  = ul.getPopList();
        for(int i=0;i<pList.size();i++){
            String prefix = pList.get(i);
            Stack stack = prefixMappings.get(prefix);
            if(debug){
                System.out.println("clear prefix"+prefix);
            }
            if(!stack.isEmpty()){
                stack.pop();
            }
        }
        pList.clear();
        
    }
    
    public void reset(){
        nsDepth =0;
        for(int i=0;i<clearDepth.size();i++){
            UsedNSList ul = (UsedNSList)clearDepth.get(i);
            if(ul == null){
                continue;
            }
            ul.clear();
        }
    }
    
    public String getNamespaceURI(String prefix) {
         if(prefix == null){
            throw new IllegalArgumentException("Prefix cannot be null");
        }
        if(prefix == XMLConstants.XML_NS_PREFIX){
            return XMLConstants.XML_NS_URI;//xml
        }else if(prefix == XMLConstants.XMLNS_ATTRIBUTE ){
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;//xmlns
        }
        Stack<AttributeNS> stack = prefixMappings.get(prefix);
        if(stack == null){
            return null;
        }
        if(stack.isEmpty()){
            return XMLConstants.NULL_NS_URI;
        }
        AttributeNS attrNS  = stack.peek();
        
        return attrNS.getUri();
    }
    
    public String getPrefix(String namespaceURI) {
        if(namespaceURI == null){
            throw new IllegalArgumentException("NamespaceURI cannot be null");
        }
        if(namespaceURI == XMLConstants.XML_NS_URI){
            return XMLConstants.XML_NS_PREFIX;//xml
        }else if(namespaceURI == XMLConstants.XMLNS_ATTRIBUTE_NS_URI){
            return XMLConstants.XMLNS_ATTRIBUTE;//xmlns
        }
        
        for(Stack<AttributeNS> st : prefixMappings.values()){
            if(st.isEmpty()){
                continue;
            }
            AttributeNS attrNS = st.peek();
            
            if(attrNS.getUri().equals(namespaceURI)){
                return attrNS.getPrefix();
            }
        }
        return null;
    }
    
    public Iterator getPrefixes(String namespaceURI) {
        throw new UnsupportedOperationException("Not supported");
    }
}

class UsedNSList {
//    ArrayList usedPrefixList = new ArrayList();
    ArrayList <String> popPrefixList = new ArrayList<String>();
    
    public ArrayList<String> getPopList(){
        return popPrefixList;
    }
    
//    public ArrayList getUsedPrefixList(){
//        return usedPrefixList;
//    }
    
    public void clear(){
//        usedPrefixList.clear();
        popPrefixList.clear();
    }
}
