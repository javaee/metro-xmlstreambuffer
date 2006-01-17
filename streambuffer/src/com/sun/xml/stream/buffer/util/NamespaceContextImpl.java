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
import java.util.Stack;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class NamespaceContextImpl {
    boolean debug = false;
    AttributeNS nsDecl = new AttributeNS();
    HashMap prefixMappings = new HashMap();
    ArrayList clearDepth  = new ArrayList(10);
    int nsDepth;
    int resizeBy = 10;
    
    
    
    public NamespaceContextImpl(){
        //change this
        for(int i=0;i<10;i++){
            clearDepth.add(null);
        }
        
    }
    
    public AttributeNS getNamespaceDeclaration(String prefix){
        Stack stack = (Stack)prefixMappings.get(prefix);
        if(stack == null || stack.empty() ){
            return null;
        }
        AttributeNS attrNS  = (AttributeNS)stack.peek();
        
        
        return attrNS;
    }
    
    
    public void declareNamespace(String prefix, String uri){
        Stack nsDecls = (Stack)prefixMappings.get(prefix);
        nsDecl.setPrefix(prefix);
        nsDecl.setUri(uri);
        if(nsDecls == null){
            nsDecls = new Stack();
            try {
                nsDecls.add(nsDecl.clone());
                prefixMappings.put(prefix,nsDecls);
            } catch (CloneNotSupportedException ex) {
                throw new RuntimeException(ex);
            }
        }else if(!nsDecls.contains(nsDecl)){
            try {
                nsDecls.add(nsDecl.clone());
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
        UsedNSList ul = (UsedNSList)clearDepth.get(nsDepth);
        if(debug){
            System.out.println("---------------------pop depth----------------------"+nsDepth);
        }
        nsDepth--;
        if(ul == null ){
            return;
        }
        ArrayList pList  = ul.getPopList();
        for(int i=0;i<pList.size();i++){
            String prefix = (String)pList.get(i);
            Stack stack = (Stack)prefixMappings.get(prefix);
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
}

class UsedNSList {
    ArrayList usedPrefixList = new ArrayList();
    ArrayList popPrefixList = new ArrayList();
    
    public ArrayList getPopList(){
        return popPrefixList;
    }
    
    public ArrayList getUsedPrefixList(){
        return usedPrefixList;
    }
    
    public void clear(){
        usedPrefixList.clear();
        popPrefixList.clear();
    }
}
