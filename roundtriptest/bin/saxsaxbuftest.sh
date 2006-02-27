#!/bin/sh
java -Xms384m -Xmx384m -cp ${XSB_HOME}/roundtriptest/lib/xom-1.1b4.jar:${XSB_HOME}/roundtriptest/lib/sjsxp.jar:${XSB_HOME}/streambuffer/lib/jsr173_api.jar:${XSB_HOME}/streambuffer/dist/streambuffer.jar:${XSB_HOME}/streambuffer/lib/stax-ex.jar:${XSB_HOME}/roundtriptest/dist/roundtriptest.jar com.sun.xml.stream.buffer.test.SAXcreatedSAXprocessedComparator $1
