/*
 * NamespaceTest.java
 *
 * Created on January 19, 2006, 10:22 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.stream.buffer.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class NamespaceTest  extends TestCase {
    
    /** Creates a new instance of NamespaceTest */
    public NamespaceTest() {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(NamespaceTest.class);
        
        return suite;
    }
    public void testNamespaceCTX() throws Exception {
        
        NamespaceContextImpl impl  = new NamespaceContextImpl();
        impl.push();
        impl.declareNamespace("p1","http://prefix1.com");
        impl.declareNamespace("p2","http://prefix2.com");
        impl.declareNamespace("","http://defaultns.com");
        impl.push();
        String prefix = impl.getPrefix("http://prefix1.com");
        assertEquals("p1",prefix);
        String uri;
        uri = impl.getNamespaceURI("p1");
        assertEquals("http://prefix1.com",uri);
        uri = impl.getNamespaceURI("");
        assertEquals("http://defaultns.com",uri);
        
        impl.declareNamespace("p3","http://prefix3.com");
        impl.declareNamespace("p4","http://prefix4.com");
        impl.declareNamespace("","http://defaultns2.com");
        prefix = impl.getPrefix("http://prefix3.com");
        assertEquals("p3",prefix);
        
        uri = impl.getNamespaceURI("p3");
        assertEquals("http://prefix3.com",uri);
        uri = impl.getNamespaceURI("p4");
        assertEquals("http://prefix4.com",uri);
        uri = impl.getNamespaceURI("");
        assertEquals("http://defaultns2.com",uri);
        
        impl.pop();
        prefix = impl.getPrefix("http://prefix1.com");
        assertEquals("p1",prefix);
        
        uri = impl.getNamespaceURI("p1");
        assertEquals("http://prefix1.com",uri);
        uri = impl.getNamespaceURI("");
        assertEquals("http://defaultns.com",uri);
        impl.pop();
        impl.pop();
        
    }
}
