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

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: martind
 * Date: Dec 17, 2009
 * Time: 12:28:30 PM
 * To change this template use File | Settings | File Templates.
 */
/*
*   Container class for B+ Tree Leaf Node.
*
*   Note: Key is a property of node items and BPTreeNode methods
*       getLowestKeyItem() and getHighestKeyItem() can be used
*       to check node key range.
* */
public class BPTreeLeafNode implements BPTreeNode{

    public static final Logger log = LoggerFactory.getLogger(BPTreeLeafNode.class);
    private final boolean isLeafNode = true;

    private long nodeIndex;    // index for node in B+ tree organization
    String lowestChromKey;     // lowest chromosome/contig key name
    String highestChromKey;    // highest chromosome/contig key name
    int lowestChromID;         // lowest chromosome ID corresponds to lowest key
    int highestChromID;        // highest chromosome ID corresponds to highest key
    private ArrayList<BPTreeLeafNodeItem> leafItems; // array for leaf items

    /*
    *   Constructor for the B+ tree leaf (terminal) node.
    *
    *   Parameters:
    *       nodeIndex - index assigned to the node
    *       parent - parent node (object)
    *
    *   Note: Inserted leaf items contain associated name key/chromosome ID.
    * */
    public BPTreeLeafNode(long nodeIndex){

        this.nodeIndex = nodeIndex;
        leafItems = new ArrayList<BPTreeLeafNodeItem>();
    }

    /*
    *   Method identifies the node as a leaf node or a child (non-leaf) node.
    *
    *   Returns:
    *       true, if leaf node; false if child node
    * */
    public boolean isLeaf() {
        return isLeafNode;
    }

    /*
    *   Method inserts the node item appropriate to the item's key value.
    *
    *   Returns:
    *       Node item inserted successfully.
    * */
    public boolean insertItem(BPTreeNodeItem item){

         // Quick implementation: assumes all keys are inserted in rank order
        // todo: verify if need to compare key and insert at rank location
        leafItems.add((BPTreeLeafNodeItem)item );

        // Note: assumes rank order insertions
        if(leafItems.size() == 1 ){
            lowestChromKey = item.getChromKey();
            lowestChromID = ((BPTreeLeafNodeItem)item).getChromID();
        }
        else {
           highestChromKey = item.getChromKey();
           highestChromID = ((BPTreeLeafNodeItem)item).getChromID();
        }

        // success
        return true;
    }

    /*
    *   Method returns the number of items assigned to the node.
    *
    *   Returns:
    *       Count of node items contained in the node
    * */
    public int getItemCount() {
        return leafItems.size();
    }

    /*
    *   Method returns the lowest key value belonging to the node.
    *
    *   Returns:
    *       Lowest key contig/chromosome name value
    * */
    public  String getLowestChromKey(){
       if(leafItems.size() > 0)
           return lowestChromKey;
       else
           return null;
    }

    /*
    *   Method returns the highest key value belonging to the node.
    *
    *   Returns:
    *       Highest key contig/chromosome name value
    * */
    public  String getHighestChromKey(){
       if(leafItems.size() > 0)
           return highestChromKey;
       else
           return null;
    }

     /*
    *   Method returns the lowest chromosome ID belonging to the node.
    *
    *   Returns:
    *       Lowest key contig/chromosome ID; or -1 if no node items
    * */
    public  int getLowestChromID(){
        if(leafItems.size() > 0)
            return lowestChromID;
        else
            return -1;
    }

    /*
    *   Method returns the highest chromosome ID belonging to the node.
    *
    *   Returns:
    *       Highest key contig/chromosome ID; or -1 if no node items
    * */
    public  int getHighestChromID(){
        if(leafItems.size() > 0)
            return highestChromID;
        else
            return -1;
    }

    /*
    *   Method prints the nodes items and sub-node items.
    *       Node item deleted successfully.
    * */
    public void printItems(){
        int  itemCount = getItemCount();

        log.debug("Leaf node " + nodeIndex +  "contains " + itemCount + " leaf items:");
        for(int item = 0; item < itemCount; ++item){
            leafItems.get(item).print();
        }
    }
}