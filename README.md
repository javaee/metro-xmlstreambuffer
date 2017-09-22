#  Metro XML Stream Buffer

Welcome to the Metro XML Stream Buffer project.  This project defines a stream-based representation, a stream buffer, of an XML infoset in Java and mechanisms to create and processs stream buffers using standard XML APIs. Supports SAX and StAX. 

# Introduction

A stream buffer is a stream-based representation of an XML infoset in Java. Stream buffers are designed to provide very efficient stream-based memory representations of XML infosets, and be created and processed using any Java-based XML API.

Conceptually a stream buffer is similar to the representation used in the Xerces deferred DOM implementation, with the crucial difference that a stream buffer does not store hierarchal information like parent and sibling information. The deferred DOM implementation reduces memory usage when large XML documents are parsed but only a subset of the document needs to be processed. (Note that using deferred DOM will be more expensive than non-deferred DOM in terms of memory and processing if all the document is traversed.)

Stream buffers may be used as an efficient alternative to DOM where:
* most or all of an XML infoset will eventually get traversed and/or
* targeted access to certain parts of an XML infoset are required and need to be efficiently processed using stream-based APIs like SAX or StAX.

# Goals

* Design a very efficient stream-based memory representation of XML infosets;
* Create and process stream buffers using the standard Java-based XML APIs SAX, StAX and DOM;
* Mark fragments of an XML infoset when creating a stream buffer for targeted processing of fragments. Fragments can be accessed directly rather than sequentially and can be streamed independently of the whole stream. Such marking could be achieved for elements that have attributes of type ID, explicitly specified elements or simple linear XPath expressions.
* Remove or replace marked fragments;



# IMPORTANT!

* By contributing to this project, you are agreeing to the terms of use described in [CONTRIBUTING.md](./CONTRIBUTING.md)

