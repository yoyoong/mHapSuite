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

import htsjdk.samtools.seekablestream.SeekableStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: martind
 * Date: Dec 17, 2009
 * Time: 4:36:21 PM
 *
 * To change this template use File | Settings | File Templates.
 */
/*
*   Container class for holding zoom level header information, BBFile Table D.
*
*   Constructed either from BBFile read or by load of header values.
*
* */
public class BBZoomLevelHeader {

    public static final Logger log = LoggerFactory.getLogger(BBZoomLevelHeader.class);

    static public final int ZOOM_LEVEL_HEADER_SIZE = 24;

    // Defines the Big Binary File (BBFile) access
    private SeekableStream fis;          // BBFile input stream handle
    private long zoomLevelHeaderOffset;    // file location for zoom level header
    int zoomLevel;     // the zoom level for this information

    // zoom level header information - BBFile Table D
    private int reductionLevel;   // number of bases summerized
    private int reserved;         // reserved, currently 0
    private long dataOffset;      // file position of zoom data
    private long indexOffset;     // file position for index of zoomed data

    /*
    *   Constructor reads zoom level header
    *
    *   Parameters:
    *       fis - File input stream handle
    *       fileOffset - file byte position for zoom header
    *       zoomLevel - level of zoom
    *       isLowToHigh - indicates byte order is low to high, else is high to low
    * */
    public BBZoomLevelHeader(SeekableStream fis, long fileOffset, int zoomLevel,
                             boolean isLowToHigh){

        this.fis = fis;
        zoomLevelHeaderOffset = fileOffset;
        this.zoomLevel = zoomLevel;

        readZoomLevelHeader(zoomLevelHeaderOffset, this.zoomLevel, isLowToHigh);
    }

    /*
    *   Method returns the zoom level data file location.
    *
    *   Returns:
    *       zoom level data file location
    * */
    public long getDataOffset() {
        return dataOffset;
    }

    /*
    *   Method returns the zoom level R+ index tree file location.
    *
    *   Returns:
    *       R+ index tree file location
    * */
    public long getIndexOffset() {
        return indexOffset;
    }

    /*
    *   Method prints the zoom level header info.
    * */
    public void print(){

        // Table D - Zoom Level Header information
        System.out.println("Zoom level " + zoomLevel + " header Table D: ");
        System.out.println("Number of zoom level bases = " + reductionLevel);
        System.out.println("Reserved = " + reserved);
        System.out.println("Zoom data offset = " + dataOffset);
        System.out.println("Zoom index offset = " + indexOffset);
    }

    /*
    *   Reads zoom level header information into class data members.
    *
    *   Parameters:
    *       fileOffset - Byte position in fle for zoom header
    *       zoomLevel - level of zoom
    *       isLowToHigh - indicate byte order is low to high, else is high to low
    * */
    private void readZoomLevelHeader(long fileOffset, int zoomLevel, boolean isLowToHigh) {

       LittleEndianInputStream lbdis = null;
       DataInputStream bdis = null;

        byte[] buffer = new byte[ZOOM_LEVEL_HEADER_SIZE];

            try {

            // Read zoom header into a buffer
            fis.seek(fileOffset);
            fis.readFully(buffer);

            // decode header
            if(isLowToHigh)
                lbdis = new LittleEndianInputStream(new ByteArrayInputStream(buffer));
            else
                bdis = new DataInputStream(new ByteArrayInputStream(buffer));

            // Get zoom level information
            if(isLowToHigh){
                reductionLevel = lbdis.readInt();
                reserved = lbdis.readInt();
                dataOffset = lbdis.readLong();
                indexOffset = lbdis.readLong();
            }
            else {
                reductionLevel = bdis.readInt();
                reserved = bdis.readInt();
                dataOffset = bdis.readLong();
                indexOffset = bdis.readLong();
            }

        }catch(IOException ex) {
            log.error("Error reading zoom level header: " + zoomLevel, ex);
            throw new RuntimeException("Error reading zoom header " + zoomLevel, ex);
        }
    }


}
