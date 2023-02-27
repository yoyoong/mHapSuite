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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/**
 * @author jrobinso
 */
public class ParsingUtils {

    public static final Logger log = LoggerFactory.getLogger(ParsingUtils.class);

    public static InputStream openInputStream(String path) throws IOException {
        return openInputStreamGZ(new ResourceLocator(path));
    }

    /**
     * Open an InputStream on the resource.  Wrap it in a GZIPInputStream if necessary.
     *
     * @param locator
     * @return
     * @throws IOException
     */
    public static InputStream openInputStreamGZ(ResourceLocator locator) throws IOException {

        InputStream inputStream = null;

        if (isDataURL(locator.getPath())) {
            String dataURL = locator.getPath();
            int commaIndex = dataURL.indexOf(',');
            if (commaIndex < 0) {
                throw new Error("dataURL missing commas");
            }
            // TODO -- check optional media type - only text/plain supported
            String contents = URLDecoder.decode(dataURL.substring(commaIndex + 1), String.valueOf(StandardCharsets.UTF_8));
            return new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8));
        } else {
            String path = locator.getPath();
            if (path.startsWith("file://")) {
                path = path.substring(7);
            }
            File file = new File(path);
            inputStream = new FileInputStream(file);
        }

        if (locator.getPath().endsWith("gz")) {
            return new GZIPInputStream(inputStream);
        } else {
            return inputStream;
        }
    }

    public static boolean isDataURL(String url) {
        return url != null && url.startsWith("data:") && !((new File(url)).exists());
    }
}
