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
 * Date: Jan 13, 2010
 * Time: 11:33:41 AM
 * To change this template use File | Settings | File Templates.
 */

/*
*   Container class for B+ Tree Child (Non-Leaf) Node.
*
*   Note: Key is a property of node items and BPTreeNode methods
*       getLowestKeyItem() and getHighestKeyItem() can be used
*       to check node key range.
*
* */
public class BPTreeChildNode implements BPTreeNode{

    public static final Logger log = LoggerFactory.getLogger(BPTreeChildNode.class);
    private final boolean isLeafNode = false;

    private long nodeIndex;    // index for node in B+ tree organization
    String lowestChromKey;     // lowest chromosome/contig key name
    String highestChromKey;         // highest chromosome/contig key name
    int lowestChromID;         // lowest chromosome ID corresponds to lowest key
    int highestChromID;        // highest chromosome ID corresponds to highest key
    private ArrayList<BPTreeChildNodeItem> childItems; // child node items

    /*
    *   Constructor for the B+ tree child (non-leaf) node.
    *
    *   Parameters:
    *       nodeIndex - index assigned to the node
    *       parent - parent node (object)
    *
    *   Note: Inserted child items contain child/leaf nodes assigned.
    * */
    public BPTreeChildNode(long nodeIndex){

        this.nodeIndex = nodeIndex;
        childItems = new ArrayList<BPTreeChildNodeItem>();
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
        childItems.add((BPTreeChildNodeItem)item );

        BPTreeNode childNode = ((BPTreeChildNodeItem)item).getChildNode();

        // Note: assumes rank order insertions
        if(childItems.size() == 1 ){
            lowestChromKey = childNode.getLowestChromKey();
            lowestChromID = childNode.getLowestChromID();
        }
        else {
            highestChromKey = childNode.getHighestChromKey();
            highestChromID = childNode.getHighestChromID();
        }


        return true;    // success
    }

    /*
    *   Method returns the number of items assigned to the node.
    *
    *   Returns:
    *       Count of node items contained in the node
    * */
    public int getItemCount() {
        return childItems.size();
    }

    /*
    *   Method returns the lowest chromosome key value belonging to the node.
    *
    *   Returns:
    *       Lowest contig/chromosome name key value; or null if no node items
    * */
    public  String getLowestChromKey(){
        if(childItems.size() > 0)
            return lowestChromKey;
        else
            return null;
    }
    
    /*
    *   Method returns the highest chromosome key value belonging to the node.
    *
    *   Returns:
    *       Highest contig/chromosome name key value; or null if no node items
    * */
    public  String getHighestChromKey(){
        if(childItems.size() > 0)
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
        if(childItems.size() > 0)
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
        if(childItems.size() > 0)
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

        log.debug("Child node " + nodeIndex + " contains " + itemCount + " child items:");
        for(int item = 0; item < itemCount; ++item){

            // recursively will print all node items below this node
            childItems.get(item).print();
        }
    }

    // *********** BPTreeChildNode specific methods *************
    /*
    *   Method returns all child items mContained by this child node.
    *
    *   Returns:
    *       List of child items contained by this node
    * */
    public ArrayList<BPTreeChildNodeItem> getChildItems(){
        return childItems;
    }

}
