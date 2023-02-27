/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2007-2015 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.common.bigwigTool;

import com.google.common.primitives.Ints;
import htsjdk.samtools.seekablestream.SeekableStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static java.lang.System.arraycopy;

/**
 * A wrapper class to provide buffered read access to a SeekableStream.  Just wrapping such a stream with
 * a BufferedInputStream will not work as it does not support seeking.  In this implementation,
 * we attempt to reuse the buffer if there is overlap between the newly requested range and where
 * the buffer contains data for.
 */
public class IGVSeekableBufferedStream extends SeekableStream {

    public static final Logger log = LoggerFactory.getLogger(IGVSeekableBufferedStream.class);

    public static final int DEFAULT_BUFFER_SIZE = 512000;

    final private int maxBufferSize;
    final SeekableStream wrappedStream;
    long position;
    /**
     * Can't make the assumption that length is a valid value.
     * May be -1, which means we don't know.
     */

    int markpos;

    byte[] buffer;
    long bufferStartPosition; // Position in file corresponding to start of buffer
    int bufferSize;

    public IGVSeekableBufferedStream(final SeekableStream stream, final int bsize) {
        this.maxBufferSize = bsize;
        this.wrappedStream = stream;
        this.position = 0;
        this.buffer = new byte[maxBufferSize];
        this.bufferStartPosition = -1;
        this.bufferSize = 0;

    }

    public long length() {
        return wrappedStream.length();
    }

    @Override
    public long skip(final long skipLength) throws IOException {
        long maxSkip = Long.MAX_VALUE;
        long length = wrappedStream.length();
        if (length >= 0) maxSkip = length - position - 1;
        long actualSkip = Math.min(maxSkip, skipLength);
        position += actualSkip;
        return actualSkip;
    }

    @Override
    public synchronized void reset() throws IOException {

        if (markpos < 0) {
            throw new IOException("Resetting to invalid mark");
        }
        position = markpos;

    }

    public void seek(final long position) throws IOException {
        this.position = position;
    }

    public void close() throws IOException {
        wrappedStream.close();
    }

    public boolean eof() throws IOException {
        long length = wrappedStream.length();
        return length >= 0 && position >= length;
    }

    @Override
    public String getSource() {
        return wrappedStream.getSource();
    }

    @Override
    public long position() throws IOException {
        return position;
    }

    /**
     * Return true iff the buffer needs to be refilled for the given
     * amount of data requested
     *
     * @param len Number of bytes from {@code position} one plans on reading
     * @return
     */
    private boolean needFillBuffer(int len) {
        return bufferSize == 0 || position < bufferStartPosition || (position + len) > bufferStartPosition + bufferSize;
    }


    public int read() throws IOException {

        if (needFillBuffer(1)) {
            fillBuffer();
        }

        int offset = (int) (position - bufferStartPosition);
        int b = buffer[offset];
        position++;
        return (b >= 0 ? b : 256+b);
    }

    public int read(final byte[] b, final int off, final int len) throws IOException {

        long contentLength = wrappedStream.length();
        if (contentLength >= 0 && position >= contentLength) return -1;

        if (len > maxBufferSize) {
            // Buffering not useful here.  Don't bother trying to use any (possible) overlapping buffer contents
            wrappedStream.seek(position);
            int nBytes = wrappedStream.read(b, off, len);
            position += nBytes;
            return nBytes;
        } else {

            long end = position + len;
            long bufferEnd = bufferStartPosition + bufferSize;

            if (position > bufferStartPosition && position < bufferEnd && end > bufferEnd) {

                // We have part of the requested range.  Return what we have, the read contract does not guarantee
                // all bytes requested.
                int bufferOffset = (int) (position - bufferStartPosition);
                int bytesCopied = (int) (bufferEnd - position);
                arraycopy(buffer, bufferOffset, b, off, bytesCopied);
                position += bytesCopied;
                return bytesCopied;

            } else {
                if (!(position >= bufferStartPosition && end <= bufferEnd)) {
                    fillBuffer();
                }

                int bufferOffset = (int) (position - bufferStartPosition);
                int bytesCopied = Math.min(len, bufferSize - bufferOffset);
                arraycopy(buffer, bufferOffset, b, off, bytesCopied);
                position += bytesCopied;
                return bytesCopied;
            }
        }
    }

    private void fillBuffer() throws IOException {

        long longRem = maxBufferSize;
        long length = wrappedStream.length();

        if (length >= 0) longRem = Math.min((long) maxBufferSize, length - position);

        //This shouldn't actually be necessary as long as maxBufferSize is
        //an int, but we leave it here to stress the fact that
        //we need to watch for overflow
        int bytesRemaining = Ints.saturatedCast(longRem);
        //Number of bytes to skip at beginning when reading from stream later
        //Number of bytes known to be stored in the buffer, which are valid
        int tmpBufferSize = 0;

        if (bytesRemaining > 0) {
            int curOffset = 0;
            wrappedStream.seek(position);
            while (bytesRemaining > 0) {
                int count = wrappedStream.read(buffer, curOffset, bytesRemaining);
                if (count < 0) {
                    break;  // EOF.
                }
                curOffset += count;
                bytesRemaining -= count;
                tmpBufferSize += count;
            }
            bufferStartPosition = position;
            bufferSize = tmpBufferSize;
        }
    }
}