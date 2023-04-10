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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Created by IntelliJ IDEA.
 * User: jrobinso, mdecautis
 * Date: Dec 13, 2009
 * Time: 4:16:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompressionUtils {

    public static final Logger log = LoggerFactory.getLogger(CompressionUtils.class);

    private Deflater deflater;
    private Inflater decompressor;

    public CompressionUtils() {
        decompressor = new Inflater();
        deflater = new Deflater();
        deflater.setLevel(Deflater.DEFAULT_COMPRESSION);
    }
    /**
     * @param data                  -- the data to decompress
     * @param uncompressedChunkSize -- an estimate of the uncompressed chunk size.  This need not be exact.
     * @return
     */
    public synchronized byte[] decompress(byte[] data, int uncompressedChunkSize) {

        // mpd: new code
        int rem = data.length;

        // Create an expandable byte array to hold the decompressed data
        ByteArrayOutputStream bos = new ByteArrayOutputStream(uncompressedChunkSize);

        // Decompress the data
        byte[] outbuf = new byte[uncompressedChunkSize];

        decompressor.reset();
        decompressor.setInput(data);
        while (rem > 0) {

            // If we are finished with the current chunk start a new one
            if (decompressor.finished()) {
                decompressor = new Inflater();
                int offset = data.length - rem;
                decompressor.setInput(data, offset, rem);
            }

            try {
                int count = decompressor.inflate(outbuf, 0, outbuf.length);
                rem = decompressor.getRemaining();
                bos.write(outbuf, 0, count);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            bos.close();
        } catch (IOException e) {
            // Ignore -- no resources open
        }

        // Return the decompressed data
        return bos.toByteArray();
    }

}
