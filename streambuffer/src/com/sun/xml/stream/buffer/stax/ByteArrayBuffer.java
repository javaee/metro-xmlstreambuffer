package com.sun.xml.stream.buffer.stax;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
 * Read/write buffer that stores a sequence of bytes.
 *
 * <p>
 * It works in a way similar to {@link java.io.ByteArrayOutputStream} but
 * this class works better in the following ways:
 *
 * <ol>
 *  <li>no synchronization
 *  <li>offers a {@link #newInputStream()} that creates a new {@link java.io.InputStream}
 *      that won't cause buffer reallocation.
 *  <li>less parameter correctness checking
 *  <li>offers a {@link #write(java.io.InputStream)} method that reads the entirety of the
 *      given {@link java.io.InputStream} without using a temporary buffer.
 * </ol>
 *
 * @author Kohsuke Kawaguchi
 */
class ByteArrayBuffer extends OutputStream {
    /**
     * The buffer where data is stored.
     */
    protected byte[] buf;

    /**
     * The number of valid bytes in the buffer.
     */
    private int count;

    /**
     * Creates a new byte array output stream. The buffer capacity is
     * initially 32 bytes, though its size increases if necessary.
     */
    public ByteArrayBuffer() {
        this(32);
    }

    /**
     * Creates a new byte array output stream, with a buffer capacity of
     * the specified size, in bytes.
     *
     * @param size the initial size.
     * @throws IllegalArgumentException if size is negative.
     */
    public ByteArrayBuffer(int size) {
        if (size <= 0)
            throw new IllegalArgumentException();
        buf = new byte[size];
    }

    public ByteArrayBuffer(byte[] data) {
        this.buf = data;
    }

    /**
     * Reads all the data of the given {@link java.io.InputStream} and appends them
     * into this buffer.
     *
     * @throws java.io.IOException
     *      if the read operation fails with an {@link java.io.IOException}.
     */
    public final void write(InputStream in) throws IOException {
        while(true) {
            int cap = buf.length-count;     // the remaining buffer space
            int sz = in.read(buf,count,cap);
            if(sz<0)    return;     // hit EOS
            count += sz;


            if(cap==sz)
                ensureCapacity(buf.length*2);   // buffer filled up.
        }
    }

    public final void write(int b) {
        int newcount = count + 1;
        ensureCapacity(newcount);
        buf[count] = (byte) b;
        count = newcount;
    }

    public final void write(byte b[], int off, int len) {
        int newcount = count + len;
        ensureCapacity(newcount);
        System.arraycopy(b, off, buf, count, len);
        count = newcount;
    }

    private void ensureCapacity(int newcount) {
        if (newcount > buf.length) {
            byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
            System.arraycopy(buf, 0, newbuf, 0, count);
            buf = newbuf;
        }
    }

    public final void writeTo(OutputStream out) throws IOException {
        out.write(buf, 0, count);
    }

    public final void reset() {
        count = 0;
    }

    /**
     * Gets the <b>copy</b> of exact-size byte[] that represents the written data.
     *
     * <p>
     * Since this method needs to allocate a new byte[], this method will be costly.
     *
     * @deprecated
     *      this method causes a buffer reallocation. Use it only when
     *      you have to.
     */
    public final byte[] toByteArray() {
        byte newbuf[] = new byte[count];
        System.arraycopy(buf, 0, newbuf, 0, count);
        return newbuf;
    }

    public final int size() {
        return count;
    }

    /**
     * Gets the underlying buffer that this {@link ByteArrayBuffer} uses.
     * It's never small than its {@link #size()}.
     *
     * Use with caution.
     */
    public final byte[] getRawData() {
        return buf;
    }

    public void close() throws IOException {
    }

    /**
     * Creates a new {@link InputStream} that reads from this buffer.
     */
    public final InputStream newInputStream() {
        return new ByteArrayInputStream(buf,0,count);
    }

    /**
     * Creates a new {@link InputStream} that reads a part of this bfufer.
     */
    public final InputStream newInputStream(int start, int length) {
        return new ByteArrayInputStream(buf,start,length);
    }

    /**
     * Decodes the contents of this buffer by the default encoding
     * and returns it as a string.
     *
     * <p>
     * Meant to aid debugging, but no more.
     */
    public String toString() {
        return new String(buf, 0, count);
    }
}
