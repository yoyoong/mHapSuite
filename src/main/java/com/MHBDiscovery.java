package com;

import com.args.MHBDiscoveryArgs;
import com.bean.MHBInfo;
import com.bean.MHapInfo;
import com.bean.R2Info;
import com.bean.Region;
import com.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class MHBDiscovery {
    public static final Logger log = LoggerFactory.getLogger(MHBDiscovery.class);

    Util util = new Util();
    MHBDiscoveryArgs args = new MHBDiscoveryArgs();

    long completeCpgCnt = 0;

    public void MHBDiscovery(MHBDiscoveryArgs mhbDiscoveryArgs) throws Exception {
        log.info("MHBDiscovery start!");
        args = mhbDiscoveryArgs;

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        // get regionList, from region or bedfile
        List<Region> regionList = new ArrayList<>();
        if (args.getRegion() != null && !args.getRegion().equals("")) {
            Region region = util.parseRegion(args.getRegion());
            regionList.addAll(util.splitRegionToSmallRegion(region, 1000000));
        } else if (args.getBedPath() != null && !args.getBedPath().equals("")) {
            List<Region> regionListInBed = util.getBedRegionList(args.getBedPath());
            // merge adjacent regions
            List<Region> regionListMerged = new ArrayList<>();
            Map<String, List<Integer>> cpgPosListMap = util.parseWholeCpgFile(args.getCpgPath());
            Iterator<String> iterator = cpgPosListMap.keySet().iterator();
            while (iterator.hasNext()) {
                String chrom = iterator.next();
                List<Region> regionListInChrom = regionListInBed.stream().filter(region -> region.getChrom().equals(chrom)).collect(Collectors.toList());
                if (regionListInChrom.size() > 1) {
                    for (Integer i = 0; i < regionListInChrom.size() - 1;) {
                        Region thisRegion = regionListInChrom.get(i);
                        Integer start = thisRegion.getStart();
                        Region nextRegion = regionListInChrom.get(i + 1);
                        Integer end = nextRegion.getEnd();
                        List<Integer> cpgPosList = cpgPosListMap.get(thisRegion.getChrom());
                        List<Integer> cpgPosListInThisRegion = util.getCpgPosListInRegion(cpgPosList, thisRegion);
                        List<Integer> cpgPosListInNextRegion = util.getCpgPosListInRegion(cpgPosList, nextRegion);
                        Integer thisRegionEndCpgIndex = util.indexOfList(cpgPosList, 0, cpgPosList.size() - 1,
                                cpgPosListInThisRegion.get(cpgPosListInThisRegion.size() - 1));
                        Integer nextRegionStartCpgIndex = util.indexOfList(cpgPosList, 0, cpgPosList.size() - 1,
                                cpgPosListInNextRegion.get(0));
                        if (nextRegionStartCpgIndex <= thisRegionEndCpgIndex + 1) {
                            int nextNum = 2;
                            while (i + nextNum < regionListInChrom.size() && nextRegionStartCpgIndex <= thisRegionEndCpgIndex + 1) {
                                thisRegion = regionListInChrom.get(i + nextNum - 1);
                                cpgPosListInThisRegion = util.getCpgPosListInRegion(cpgPosList, thisRegion);
                                thisRegionEndCpgIndex = util.indexOfList(cpgPosList, 0, cpgPosList.size() - 1,
                                        cpgPosListInThisRegion.get(cpgPosListInThisRegion.size() - 1));
                                nextRegion = regionListInChrom.get(i + nextNum);
                                cpgPosListInNextRegion = util.getCpgPosListInRegion(cpgPosList, nextRegion);
                                nextRegionStartCpgIndex = util.indexOfList(cpgPosList, 0, cpgPosList.size() - 1,
                                        cpgPosListInNextRegion.get(0));
                                nextNum++;
                            }
                            if (i + nextNum == regionListInChrom.size()) {
                                end = nextRegion.getEnd();
                            } else {
                                end = thisRegion.getEnd();
                            }

                            Region mergeRegion = new Region();
                            mergeRegion.setChrom(chrom);
                            mergeRegion.setStart(start);
                            mergeRegion.setEnd(end);
                            regionListMerged.add(mergeRegion);
                            i += (nextNum - 1);
                        } else {
                            i++;
                            regionListMerged.add(thisRegion);
                        }
                    }
                } else {
                    regionListMerged.add(regionListInChrom.get(0));
                }
            }
            for (Region region : regionListMerged) {
                regionList.addAll(util.splitRegionToSmallRegion(region, 1000000));
            }
        } else {
//            List<Region> wholeRegionList = util.getWholeRegionFromMHapFile(args.getmHapPath());
            List<Region> wholeRegionList = new ArrayList<>();
            Map<String, List<Integer>> cpgPostListMap = util.parseWholeCpgFile(args.getCpgPath());
            Iterator<String> iterator = cpgPostListMap.keySet().iterator();
            while (iterator.hasNext()) {
                String chrom = iterator.next();
                List<Integer> cpgPostList = cpgPostListMap.get(chrom);
                Region region = new Region();
                region.setChrom(chrom);
                region.setStart(cpgPostList.get(0));
                region.setEnd(cpgPostList.get(cpgPostList.size() - 1));
                wholeRegionList.add(region);
            }

            for (Region region : wholeRegionList) {
                regionList.addAll(util.splitRegionToSmallRegion(region, 1000000));
            }
        }

        // create the output directory and file
        BufferedWriter bufferedWriter = util.createOutputFile(args.getOutputDir(), args.getTag() + ".bed");

        List<MHBInfo> mhbInfoList = new ArrayList<>();
        for (Region region : regionList) {
            // parse the mhap file
            //List<MHapInfo> mHapInfoList = util.parseMhapFile(args.getmHapPath(), region, "both", true);
            List<MHapInfo> mHapInfoList = util.parseMhapFileWithEndShift(args.getmHapPath(), region, "both", 500);

            // parse the cpg file
            List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, 2000);

            // get cpg site list in region
            Integer cpgStartPos = 0;
            Integer cpgEndPos = cpgPosList.size() - 1;
            for (int i = 0; i < cpgPosList.size(); i++) {
                if (cpgPosList.get(i) < region.getStart() && cpgPosList.get(i + 1) >= region.getStart()) {
                    cpgStartPos = i + 1;
                    break;
                }
            }
            for (int i = 0; i < cpgPosList.size(); i++) {
                if (cpgPosList.get(i) >= region.getEnd()) {
                    cpgEndPos = i;
                    break;
                }
            }
            List<Integer> cpgPosListInRegion = cpgPosList.subList(cpgStartPos,
                    cpgEndPos + 2 > cpgPosList.size() ? cpgPosList.size() : cpgEndPos + 2); // end site add 1
            if (cpgPosListInRegion.size() < 1) {
                continue;
            }

            // get mhap index list map to cpg positions
            Map<Integer, List<Integer>> mHapIndexListMapToCpg = util.getMhapIndexMapToCpg(mHapInfoList, cpgPosListInRegion);

            Integer startIndex = 0; // start mhb position index in cpgPosListInRegion
            Integer endIndex = 0; // end mhb position index in cpgPosListInRegion
            Integer index = 0;
            while (endIndex < cpgPosListInRegion.size() - 1) {
                completeCpgCnt++;
                if (completeCpgCnt % 10000 == 0) {
                    log.info("Read completed " + completeCpgCnt + " cpg positions.");
                }
                endIndex++;
                Boolean extendFlag = true;
                for (int i = 1; i < args.getWindow(); i++) {
                    index = endIndex - i; // cpg site index in cpgPosListInRegion for loop
                    if (index < 0) {
                        break;
                    }

                    // get r2 and pvalue of startIndex
                    Integer cpgPos1 = cpgPosListInRegion.get(index);
                    Integer cpgPos2 = cpgPosListInRegion.get(endIndex);
                    List<Integer> mHapIndexList1 = mHapIndexListMapToCpg.get(cpgPos1);
                    List<Integer> mHapIndexList2 = mHapIndexListMapToCpg.get(cpgPos2);
                    List<MHapInfo> mHapList1 = util.getMHapListFromIndex(mHapInfoList, mHapIndexList1);
                    List<MHapInfo> mHapList2 = util.getMHapListFromIndex(mHapInfoList, mHapIndexList2);
                    R2Info r2Info = util.getR2FromMap(mHapList1, cpgPosList, cpgPos1, cpgPos2, 0);
                    //log.info("start:" + startIndex + "\t" + "index:" + index + "\t" + "end:" + endIndex);
//                    log.info(cpgPosListInRegion.get(index) + "\t" + cpgPosListInRegion.get(endIndex) + "\t"
//                            + r2Info.getR2() + "\t" + r2Info.getPvalue());
                    if (r2Info == null || r2Info.getR2() < args.getR2() || r2Info.getPvalue() > args.getPvalue()) {
                        extendFlag = false;
                        break;
                    }
                }

                if (!extendFlag) {
                    MHBInfo mhbInfo = new MHBInfo();
                    Integer mhbSize = endIndex - startIndex;
                    mhbInfo.setChrom(region.getChrom());
                    mhbInfo.setStart(cpgPosListInRegion.get(startIndex));
                    mhbInfo.setEnd(cpgPosListInRegion.get(endIndex - 1));
                    startIndex = index + 1 > startIndex ? index + 1 : startIndex;
                    if (mhbSize >= args.getWindow() && !mhbInfoList.contains(mhbInfo)) {
                        mhbInfoList.add(mhbInfo);
                        //log.info("discovery a mhb in : " + mhbInfo.getChrom() + ":" + mhbInfo.getStart() + "-" + mhbInfo.getEnd());
                        bufferedWriter.write(mhbInfo.getChrom() + "\t" + mhbInfo.getStart() + "\t" + mhbInfo.getEnd() + "\n");
                    }
                }
            }

            if (endIndex - startIndex >= args.getWindow()) {
                MHBInfo mhbInfo = new MHBInfo();
                mhbInfo.setChrom(region.getChrom());
                mhbInfo.setStart(cpgPosListInRegion.get(startIndex));
                mhbInfo.setEnd(cpgPosListInRegion.get(endIndex - 1));
                if (!mhbInfoList.contains(mhbInfo)) {
                    mhbInfoList.add(mhbInfo);
                    bufferedWriter.write(mhbInfo.getChrom() + "\t" + mhbInfo.getStart() + "\t" + mhbInfo.getEnd() + "\n");
                }
            }
            log.info("Get MHB from region: " + region.toHeadString() + " end!");
        }
        bufferedWriter.close();

        log.info("MHBDiscovery end!");
    }

    private boolean checkArgs() {
        if (args.getmHapPath().equals("")) {
            log.error("mhapPath can not be null.");
            return false;
        }
        if (args.getCpgPath().equals("")) {
            log.error("cpgPath can not be null.");
            return false;
        }
        if (!args.getRegion().equals("") && !args.getBedPath().equals("")) {
            log.error("Can not input region and bedPath at the same time.");
            return false;
        }
        return true;
    }

    private Integer[][] getMC(List<MHapInfo> mHapInfoList, List<Integer> cpgPosListInRegion) {
        Integer[][] MC = new Integer[mHapInfoList.size()][cpgPosListInRegion.size()];

        return MC;
    }

    private Integer[][] getM1(List<MHapInfo> mHapInfoList, List<Integer> cpgPosListInRegion) {
        Integer[][] M1 = new Integer[mHapInfoList.size()][cpgPosListInRegion.size()];

        return M1;
    }

    private Integer[][] getM0(List<MHapInfo> mHapInfoList, List<Integer> cpgPosListInRegion) {
        Integer[][] M0 = new Integer[mHapInfoList.size()][cpgPosListInRegion.size()];

        return M0;
    }
}
