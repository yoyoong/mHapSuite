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

/**
 * Created by IntelliJ IDEA.
 * User: martind
 * Date: Dec 20, 2009
 * Time: 10:37:43 PM
 * To change this template use File | Settings | File Templates.
 */
package com.common.bigwigTool;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 *   Container class for B+ tree leaf node.
* */
public class BPTreeLeafNodeItem implements BPTreeNodeItem {

    public static final Logger log = LoggerFactory.getLogger(BPTreeLeafNodeItem.class);
    private final boolean isLeafItem = true;
    private long leafIndex;    // leaf index in B+ tree item list

    // B+ Tree Leaf Node Item entities - BBFile Table G
    private String chromKey; // B+ tree node item is associated by key
    private int chromID;      // numeric mChromosome/contig ID
    private int chromSize;    // number of bases in mChromosome/contig

    /*
    *   Constructs a B+ tree leaf node item with the supplied information.
    *
    *   Parameters:
    *       leafIndex - leaf item index
    *       chromKey - chromosome/contig name key
    *       chromID - chromosome ID assigned to the chromosome name key
    *       chromsize - number of bases in the chromosome/contig
    * */
    public BPTreeLeafNodeItem(long leafIndex, String chromKey, int chromID, int chromSize) {

        this.leafIndex = leafIndex;
        this.chromKey = chromKey;
        this.chromID = chromID;
        this.chromSize = chromSize;
    }

    /*
    *   Method returns the chromosome name key  assigned to this node item.
    *
    *   Returns:
    *       chromosome name key assigned to this node item
    * */
    public String getChromKey() {
        return chromKey;
    }


    public void print() {

        log.debug("B+ tree leaf node item number " + leafIndex);
        log.debug("Key value = " + chromKey);
        log.debug("ChromID = " + chromID);
        log.debug("Chromsize = " + chromSize);
    }

    // *** BPTreeLeafNodeItem specific methods ***
    public int getChromID() {
        return chromID;
    }
}
