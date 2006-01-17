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



/**
 *
 * @author K.Venugopal@sun.com
 */
public class AttributeNS implements Cloneable , Comparable {
    private String uri;
    private String prefix;
   
    int code = 0;
    /** Creates a new instance of AttributeNS */
    public AttributeNS() {
    }
    
    public String getUri() {
        return uri;
    }
    
    public void setUri(String uri) {
        this.uri = uri;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
           
    public Object clone() throws CloneNotSupportedException {
        AttributeNS attrNS = new AttributeNS();
        attrNS.setPrefix(this.prefix);
        attrNS.setUri(this.uri);
        return attrNS;
    }
    
    public boolean equals(Object obj) {
        if(!(obj instanceof AttributeNS)){
            return false;
        }
        AttributeNS attrNS = (AttributeNS)obj;
        if(this.uri == null || this.prefix == null){
            return false;
        }
        if(this.prefix.equals(attrNS.getPrefix()) && this.uri.equals(attrNS.getUri())){
            return true;
        }
        return false;
    }
    
    public int hashCode(){
        if(code ==0){
            if(uri!=null){
                code =uri.hashCode();
            }
            if(prefix !=null){
                code =code+prefix.hashCode();
            }
        }
        return code;
    }
    
      
    public int compareTo(Object cmp) {
        return sortNamespaces(cmp, this);
    }
    
    protected int sortNamespaces(Object object, Object object0) {
        AttributeNS attr = (AttributeNS)object;
        AttributeNS attr0 = (AttributeNS)object0;
        //assume namespace processing is on.
        String lN = attr.getPrefix();
        String lN0 = attr0.getPrefix();
        return lN.compareTo(lN0);
    }
    
    public void reset(){
        
        prefix = null;
      
        uri = null;
    }
}
