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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/*
*   Broad Institute Interactive Genome Viewer Big Binary File (BBFile) Reader
*   -   File reader for UCSC BigWig and BigBed file types.
*
*   Notes:   Table entries refer to Jim Kent of UCSC's document description:
*           "BigWig and BigBed: Enabling Browsing of Large Distributed Data Sets",
*           November 2009.
*
*           The overall binary file layout is defined in Table B of the document.
*
*   BBFile Reader sequences through this binary file layout:
*
*   1) Reads in BBFile Header Table C and determine if file is a valid Big Bed or Big Wig
*      binary file type.
*
*   2) Reads in Zoom Header Tables D if zoom data is present, as defined by zoomLevels
*       in Table C, one for each zoom level.
*
*   3) Reads in  the AutoSQL block Table B if present, as referenced by autoSqlOffset in Table C.
*
*   4) Reads in Total Summary Block Table DD if present, as referenced by
*       TotalSummaryOffset in Table C.
*
*   5) Reads in B+ Tree Header Chromosome Index Header Table E, as referenced
*       by chromosomeTreeOffset in Table C.
*
*   6)Reads in B+ Tree Nodes indexing mChromosome ID's for mChromosome regions;
*       Table F for node type (leaf/child), Table G for leaf items,
*       Table H for child node items.
*
*   7) Reads in R+ Tree Chromosome ID Index Header Table K.
*
*   8) Reads in R+ Tree Nodes indexing of data arranged by mChromosome ID's;
*       Table L for node type (leaf or child), Table M for leaf items,
*       Table N for child node items.
*
*   9) Verifies Data Count of data records, as referenced by fullDataOffset in Table C
*
*   10) References data count records of data size defined in Table M of R+ Tree index
*       for all leaf items in the tree.
*
*   11) Reads in zoom level format Table O for each zoom level comprised of
*       zoom record count followed by that many Table P zoom statistics records,
*       followed by an R+ tree of zoom data locations indexed as in Tables L, M, and N.
*
*   12) Returns information on chromosome name keys and chromosome data regions.
*
*   13) Provides iterators using chromosome names and data regions to extract
*       zoom data, Wig data, and Bed data.
* 
* */

public class BBFileReader {

    public static final long BBFILE_HEADER_OFFSET = 0;

    public static final Logger log = LoggerFactory.getLogger(BBFileReader.class);

    private SeekableStream fis;      // BBFile input stream handle
    private long fileOffset;           // file offset for next item to be read

    private BBFileHeader fileHeader; // Big Binary file header
    private boolean isLowToHigh;       // BBFile binary data format: low to high or high to low
    private int uncompressBufSize;     // buffer byte size for data decompression; 0 for uncompressed

    // AutoSQL String defines custom BigBed formats

    private String autoSqlFormat;

    // This section defines the zoom items if zoom data exists
    private int zoomLevelCount;       // number of zoom levels defined
    private long zoomLevelOffset;      // file offset to zoom level headers
    private BBZoomLevels zoomLevels;   // zoom level headers and data locations

    // Total Summary Block - file statistical info
    private BBTotalSummaryBlock totalSummaryBlock;

    // B+ tree
    private long chromIDTreeOffset; // file offset to mChromosome index B+ tree
    private BPTree chromosomeIDTree;     // Container for the mChromosome index B+ tree

    // R+ tree
    private long chromDataTreeOffset;  // file offset to mChromosome data R+ tree
    private RPTree chromosomeDataTree;     // Container for the mChromosome data R+ tree
    private String autoSql;


    public BBFileReader(String path) throws IOException {

        log.debug("Opening BBFile source  " + path);

        fis = new IGVSeekableBufferedStream(IGVSeekableStreamFactory.getInstance().getStreamFor(path), 128000);

        // read in file header
        fileOffset = BBFILE_HEADER_OFFSET;
        fileHeader = new BBFileHeader(path, fis, fileOffset);
        //fileHeader.print();

        if (!fileHeader.isHeaderOK()) {
            log.error("BBFile header is unrecognized type, header magic = " + fileHeader.getMagic());
            throw new RuntimeException("Error reading BBFile header for: " + path);
        }

        // get data characteristics
        isLowToHigh = fileHeader.isLowToHigh();
        uncompressBufSize = fileHeader.getUncompressBuffSize();

        // update file offset past BBFile header
        fileOffset += BBFileHeader.BBFILE_HEADER_SIZE;

        // get zoom level count from file header
        zoomLevelCount = fileHeader.getZoomLevels();

        // extract zoom level headers and zoom data records
        // Note: zoom headers Table D immediately follow the BBFile Header
        if (zoomLevelCount > 0) {
            zoomLevelOffset = fileOffset;
            zoomLevels = new BBZoomLevels(fis, zoomLevelOffset, zoomLevelCount, isLowToHigh, uncompressBufSize);

            // end of zoom level headers - compare with next BBFile item location
            fileOffset += zoomLevelCount * BBZoomLevelHeader.ZOOM_LEVEL_HEADER_SIZE;
        }

        // get the AutoSQL custom BigBed fields
        long autoSqlOffset = fileHeader.getAutoSqlOffset();
        if (autoSqlOffset != 0) {
            fis.seek(autoSqlOffset);
            autoSql = readNullTerminatedString(fis);
        }

        // get the Total Summary Block (Table DD)
        fileOffset = fileHeader.getTotalSummaryOffset();
        if (fileHeader.getVersion() >= 2 && fileOffset > 0) {
            totalSummaryBlock = new BBTotalSummaryBlock(fis, fileOffset, isLowToHigh);
            fileOffset += BBTotalSummaryBlock.TOTAL_SUMMARY_BLOCK_SIZE;
        }

        // get Chromosome Data B+ Tree (Table E, F, G, H) : should always exist
        chromIDTreeOffset = fileHeader.getChromosomeTreeOffset();
        if (chromIDTreeOffset != 0) {
            fileOffset = chromIDTreeOffset;
            chromosomeIDTree = new BPTree(fis, fileOffset, isLowToHigh);
        }

        // get R+ chromosome data location tree (Tables K, L, M, N)
        chromDataTreeOffset = fileHeader.getFullIndexOffset();
        if (chromDataTreeOffset != 0) {
            fileOffset = chromDataTreeOffset;
            chromosomeDataTree = new RPTree(fis, fileOffset, isLowToHigh, uncompressBufSize);
        }


        // get number of data records indexed by the R+ chromosome data location tree
        fileOffset = fileHeader.getFullDataOffset();
    }



    public void close() {
        try {
            fis.close();
        } catch (IOException e) {
            log.error("Error closing bigwig stream", e);
        }
    }

    public boolean isBigWigFile() {
        return fileHeader.isBigWig();
    }


    /*
    *   Method returns if the Big Binary File is written with a low to high byte
    *   order for formatted data.
    *
    *   Returns:
    *       Boolean identifies if Big Binary File is low to high byte order
    *       (recognized from magic number in file header Table C); else is
    *       high to low byte order if false.
    * */

    public boolean isLowToHigh() {
        return isLowToHigh;
    }

    /*
    *   Method returns the total summary block for the Big Binary File.
    *
    *   Returns:
    *       Total summary block data statistics for the whole file (Table DD)
    * */
    /*
    *   Method finds chromosome names in the B+ chromosome index tree.
    *
    *   Returns:
    *       LIst of all chromosome key names in the B+ tree.
    * */

    public ArrayList<String> getChromosomeNames() {

        ArrayList<String> chromosomeList = chromosomeIDTree.getChromosomeNames();
        return chromosomeList;
    }

    /**
     * Returns an iterator for BigWig values which occupy the specified startChromosome region.
     * <p/>
     * Note: the BBFile type should be BigWig; else a null iterator is returned.
     * <p/>
     * Parameters:
     * startChromosome  - name of start chromosome
     * startBase    - starting base position for features
     * endChromosome  - name of end chromosome
     * endBase      - ending base position for feature
     * contained    - flag specifies bed features must be contained in the specified
     * base region if true; else can intersect the region if false
     * <p/>
     * Returns:
     * Iterator to provide BedFeature(s) for the requested chromosome region.
     * Error conditions:
     * 1) An empty iterator is returned if region has no data available
     * 2) A null object is returned if the file is not BigWig.(see isBigWigFile method)
     */
    synchronized public BigWigIterator getBigWigIterator(String startChromosome, int startBase,
                                                         String endChromosome, int endBase, boolean contained) {


        if (!isBigWigFile())
            return null;

        // go from chromosome names to chromosome ID region
        RPChromosomeRegion selectionRegion = getChromosomeBounds(startChromosome, startBase,
                endChromosome, endBase);

        // check for valid selection region, return empty iterator if null
        if (selectionRegion == null)
            return new BigWigIterator();

        // compose an iterator
        BigWigIterator wigIterator = new BigWigIterator(fis, chromosomeIDTree, chromosomeDataTree,
                selectionRegion, contained);

        return wigIterator;
    }

    private RPChromosomeRegion getChromosomeBounds(String startChromosome, int startBase,
                                                   String endChromosome, int endBase) {

        // If the chromosome name length is > the key size we can't distinguish it
        if (startChromosome.length() > chromosomeIDTree.getKeySize()) {
            return null;
        }

        // find the chromosome ID's using the name to get a valid name key, then associated ID
        String startChromKey = chromosomeIDTree.getChromosomeKey(startChromosome);
        int startChromID = chromosomeIDTree.getChromosomeID(startChromKey);
        if (startChromID < 0)       // mChromosome not in data?
            return null;

        String endChromKey = chromosomeIDTree.getChromosomeKey(endChromosome);
        int endChromID = chromosomeIDTree.getChromosomeID(endChromKey);
        if (endChromID < 0)       // mChromosome not in data?
            return null;

        // create the bounding mChromosome region
        RPChromosomeRegion chromBounds = new RPChromosomeRegion(startChromID, startBase,
                endChromID, endBase);

        return chromBounds;
    }


    private static String readNullTerminatedString(InputStream fis) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(100);
        byte b;
        while ((b = (byte) fis.read()) != 0) {
            bos.write(b);
        }
        return new String(bos.toByteArray());
    }
} // end of BBFileReader
