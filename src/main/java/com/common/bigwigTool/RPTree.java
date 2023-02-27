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
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: martind
 * Date: Jan 6, 2010
 * Time: 4:05:31 PM
 * To change this template use File | Settings | File Templates.
 */
/*
*   RPTree class will construct a R+ tree from a binary Bed/Wig BBFile.
*   (or by insertion of tree nodes - TBD see insert method)
*
*   1) RPTree will first read in the R+ tree header with RPTreeHeader class.
*
*   2) Starting with the root node, the readRPTreeNode method will read in the
*   node format, determine if the node contains child nodes (isLeaf = false)
*   or leaf items (isLeaf = true).
*
*   3) If the node is a leaf node, all leaf items are read in to the node's leaf array.
*
*   4) If node is a child node, readRPTreeNode will be called recursively,
*   until a leaf node is encountered, where step 3 is performed.
*
*   5) The child nodes will be populated with their child node items in reverse order
*   of recursion from step 4, until the tree is completely populated
*   back up to the root node.
*
*   6) The getChromosomeKey is provided to construct a valid key for B+
*   chromosome tree searches, and getChromosomeID returns a chromosome ID for
*   searches in the R+ index tree.
*
**/
public class RPTree {

    public static final Logger log = LoggerFactory.getLogger(RPTree.class);

    public static final int RPTREE_NODE_FORMAT_SIZE = 4;       // node format size
    public static final int RPTREE_NODE_LEAF_ITEM_SIZE = 32;   // leaf item size
    public static final int RPTREE_NODE_CHILD_ITEM_SIZE = 24;  // child item size

    // R+ tree access variables   - for reading in R+ tree nodes from a file
    private int uncompressBuffSize;    // decompression buffer size; or 0 for uncompressed data
    private boolean isLowToHigh;       // binary data low to high if true; else high to low
    private long rpTreeOffset;         // file offset to the R+ tree

    // R+ tree index header - Table K
    private RPTreeHeader rpTreeHeader; // R+ tree header (Table K for BBFile)

    // R+ tree bounds
    private RPChromosomeRegion chromosomeBounds;  // R+ tree's bounding chromosome region

    // R+ tree nodal variables
    private int order;         // R+ tree order: maximum number of leaves per node
    private RPTreeNode rootNode;  // root node for R+ tree
    private long nodeCount;        // number of nodes defined in the R+ tree
    private long leafCount;        // number of leaves in the R+ tree


    /*
   * Constructor for reading in a B+ tree from a BBFile/input stream.
   * */
    /*
    *   Constructor for R+ chromosome data locator tree
    *
    *   Parameters:
    *       fis - file input stream handle
    *       fileOffset - location for R+ tree header
    *       isLowToHigh - binary values are low to high if true; else high to low
    *       uncompressBuffSize - buffer size for decompression; else 0 for uncompressed data
    * */

    public RPTree(SeekableStream fis, long fileOffset, boolean isLowToHigh, int uncompressBuffSize) {

        // save the seekable file handle  and B+ Tree file offset
        // Note: the offset is the file position just after the B+ Tree Header
        // mBBFis = fis;
        rpTreeOffset = fileOffset;
        this.uncompressBuffSize = uncompressBuffSize;
        this.isLowToHigh = isLowToHigh;

        // read in R+ tree header - verify the R+ tree info exits
        rpTreeHeader = new RPTreeHeader(fis, rpTreeOffset, isLowToHigh);

        // log error if header not found and throw exception
        if (!rpTreeHeader.isHeaderOK()) {
            int badMagic = rpTreeHeader.getMagic();
            log.error("Error reading R+ tree header: bad magic = " + badMagic);
            throw new RuntimeException("Error reading R+ tree header: bad magic = " + badMagic);
        }

        // assigns R+ tree organization from the header
        order = rpTreeHeader.getBlockSize();
        chromosomeBounds = new RPChromosomeRegion(rpTreeHeader.getStartChromID(), rpTreeHeader.getStartBase(),
                rpTreeHeader.getEndChromID(), rpTreeHeader.getEndBase());

        // populate the tree - read in the nodes
        long nodeOffset = rpTreeOffset + rpTreeHeader.getHeaderSize();
        RPTreeNode parentNode = null;      // parent node of the root is itself, or null

        // start constructing the R+ tree - get the root node
        boolean forceDescend = false;
        rootNode = readRPTreeNode(fis, nodeOffset, isLowToHigh, forceDescend);
    }

    /*
    *   Method returns size of buffer required for data decompression.
    *
    *   Returns:
    *       Data decompression buffer size in bytes; else 0 for uncompressed data in file.
    * */

    public int getUncompressBuffSize() {
        return uncompressBuffSize;
    }

    /*
    *   Method returns if file contains formatted data in low to high byte order.
    *
    *   Returns:
    *       Returns true if data ordered in low to high byte order; false if high to low.
    * */

    public boolean isIsLowToHigh() {
        return isLowToHigh;
    }

    /*
    *   Method returns the R+ tree order, the maximum number of leaf items per node.
    *
    *   Returns:
    *       Maximum number of leaf items per node..
    * */

    public int getOrder() {
        return order;
    }

    /*
    *   Method returns the R+ tree index header.
    *
    *   Returns:
    *       R+ tree index header.
    * */

    public ArrayList<RPTreeLeafNodeItem> getChromosomeDataHits(RPChromosomeRegion selectionRegion,
                                                               boolean contained) {

        ArrayList<RPTreeLeafNodeItem> leafHitItems = new ArrayList<RPTreeLeafNodeItem>();

        // check for valid selection region - return empty collection if null
        if (selectionRegion == null)
            return leafHitItems;

        // limit the hit list size
        /*
        if(maxLeafHits > 0)
            mMaxLeafHits = maxLeafHits;
        else
            mMaxLeafHits = mRPTreeHeader.getBlockSize();
        */


        findChromosomeRegionItems(rootNode, selectionRegion, leafHitItems);

        return leafHitItems;
    }

    // prints out the R+ tree  header, nodes, and leaves

    public void print() {

        // check if read in
        if (!rpTreeHeader.isHeaderOK()) {
            int badMagic = rpTreeHeader.getMagic();
            log.error("Error reading R+ tree header: bad magic = " + badMagic);
            return;
        }

        // print R+ tree header
        rpTreeHeader.print();

        // print  R+ tree node and leaf items - recursively
        if (rootNode != null)
            rootNode.printItems();

    }

    private void findChromosomeRegionItems(RPTreeNode thisNode, RPChromosomeRegion selectionRegion,
                                           ArrayList<RPTreeLeafNodeItem> leafHitItems) {

        int hitValue;

        // check for valid selection region - ignore request if null
        if (selectionRegion == null)
            return;

        // check if node is disjoint
        hitValue = thisNode.compareRegions(selectionRegion);
        if (Math.abs(hitValue) >= 2)
            return;

        // search down the tree recursively starting with the root node
        if (thisNode.isLeaf()) {
            int nLeaves = thisNode.getItemCount();
            for (int index = 0; index < nLeaves; ++index) {
                RPTreeLeafNodeItem leafItem = (RPTreeLeafNodeItem) thisNode.getItem(index);

                // compute the region hit value
                hitValue = leafItem.compareRegions(selectionRegion);

                // select contained or intersected leaf regions - item selection is by iterator
                if (Math.abs(hitValue) < 2) {
                    leafHitItems.add(leafItem);
                }

                // ascending regions will continue to be disjoint so terminate nodal search
                else if (hitValue > 1)
                    break;

                // check next leaf
            }
        } else {
            // check all child nodes
            int nNodes = thisNode.getItemCount();
            for (int index = 0; index < nNodes; ++index) {
                RPTreeChildNodeItem childItem = (RPTreeChildNodeItem) thisNode.getItem(index);

                // check for region intersection at the node level
                hitValue = childItem.compareRegions(selectionRegion);

                // test this node and get any leaf hits; intersections and containing
                if (Math.abs(hitValue) < 2) {
                    RPTreeNode childNode = childItem.getChildNode();
                    findChromosomeRegionItems(childNode, selectionRegion, leafHitItems);
                }

                // ascending regions will continue to be disjoint so terminate nodal search
                else if (hitValue > 1)
                    break;
            }
        }
    }

    /*
    *   Method reads in the R+ tree nodes recursively.
    *
    *   Note: If node is a child node, the node is examined recursively,
    *       until the leaves are found.
    *
    *   Parameters:
    *       fis - file input stream handle
    *       fileOffset - file location for node specification (Table L)
    *       parent - parent node of this node
    *       isLowToHigh - indicates formatted data is low to high byte order if true;
    *           else is high to low byte order
    *
    *   Returns:
    *       A tree node, for success, or null for failure to find the node information.

    * */

    static RPTreeNode readRPTreeNode(SeekableStream fis, long fileOffset, boolean isLowToHigh, boolean forceDescend) {

        LittleEndianInputStream lbdis = null; // low o high byte stream reader
        DataInputStream bdis = null;    // high to low byte stream reader

        byte[] buffer = new byte[RPTREE_NODE_FORMAT_SIZE];
        RPTreeNode thisNode = null;

        try {

            // Read node format into a buffer
            fis.seek(fileOffset);
            fis.readFully(buffer);

            if (isLowToHigh) {
                lbdis = new LittleEndianInputStream(new ByteArrayInputStream(buffer));
            } else {
                bdis = new DataInputStream(new ByteArrayInputStream(buffer));
            }

            // find node type
            byte type;
            if (isLowToHigh) {
                type = lbdis.readByte();
            } else {
                type = bdis.readByte();
            }

            boolean isLeaf;
            int itemSize;
            if (type == 1) {
                isLeaf = true;
                itemSize = RPTREE_NODE_LEAF_ITEM_SIZE;
                thisNode = new RPTreeNode(true);
            } else {
                isLeaf = false;
                itemSize = RPTREE_NODE_CHILD_ITEM_SIZE;
                thisNode = new RPTreeNode(false);
            }
            //nodeCount++;

            int itemCount;
            if (isLowToHigh) {
                lbdis.readByte();          // reserved - not currently used
                itemCount = lbdis.readUShort();
            } else {
                bdis.readByte();          // reserved - not currently used
                itemCount = bdis.readUnsignedShort();
            }

            int itemBlockSize = itemCount * itemSize;
            buffer = new byte[itemBlockSize];            // allocate buffer for item sisze
            fis.readFully(buffer);
            if (isLowToHigh)
                lbdis = new LittleEndianInputStream(new ByteArrayInputStream(buffer));
            else
                bdis = new DataInputStream(new ByteArrayInputStream(buffer));

            // get the node items - leaves or child nodes
            int startChromID, endChromID;
            int startBase, endBase;
            for (int item = 0; item < itemCount; ++item) {

                // always extract the bounding rectangle
                if (isLowToHigh) {
                    startChromID = lbdis.readInt();
                    startBase = lbdis.readInt();
                    endChromID = lbdis.readInt();
                    endBase = lbdis.readInt();
                } else {
                    startChromID = bdis.readInt();
                    startBase = bdis.readInt();
                    endChromID = bdis.readInt();
                    endBase = bdis.readInt();
                }

                if (isLeaf) {
                    long dataOffset;
                    long dataSize;
                    if (isLowToHigh) {
                        dataOffset = lbdis.readLong();
                        dataSize = lbdis.readLong();
                    } else {
                        dataOffset = bdis.readLong();
                        dataSize = bdis.readLong();
                    }

                    thisNode.insertItem(new RPTreeLeafNodeItem(startChromID, startBase, endChromID, endBase,
                            dataOffset, dataSize));
                } else {
                    // get the child node pointed to in the node item
                    long nodeOffset;
                    if (isLowToHigh) {
                        nodeOffset = lbdis.readLong();
                    } else {
                        nodeOffset = bdis.readLong();
                    }

                    // Recursive call to read next child node
                    // The test on chromIds is designed to stop the descent when the tree reaches the level of an
                    // individual chromosome.  These are loaded later on demand.

                    RPTreeChildNodeItem childNodeItem;
                    if (startChromID != endChromID || forceDescend) {
                        RPTreeNode childNode = readRPTreeNode(fis, nodeOffset, isLowToHigh, forceDescend);
                        childNodeItem = new RPTreeChildNodeItem(startChromID, startBase, endChromID,
                                endBase, childNode);
                    } else {
                        RPTreeNodeProxy proxy = new RPTreeNodeProxy(fis, nodeOffset, isLowToHigh, startChromID);
                        childNodeItem = new RPTreeChildNodeItem(startChromID, startBase, endChromID,
                                endBase, proxy);
                    }
                    thisNode.insertItem(childNodeItem);
                }

                fileOffset += itemSize;
            }

        } catch (IOException ex) {
            log.error("Error reading in R+ tree nodes: " + ex);
            throw new RuntimeException("Error reading R+ tree nodes: \n", ex);
        }

        // return success
        return thisNode;
    }
}
