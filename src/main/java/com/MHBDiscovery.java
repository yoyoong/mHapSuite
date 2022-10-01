package com;

import com.args.MHBDiscoveryArgs;
import com.bean.MHBInfo;
import com.bean.MHapInfo;
import com.bean.R2Info;
import com.bean.Region;
import com.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MHBDiscovery {
    public static final Logger log = LoggerFactory.getLogger(MHBDiscovery.class);

    Util util = new Util();
    MHBDiscoveryArgs args = new MHBDiscoveryArgs();

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
            regionList.add(region);
        } else {
            regionList = util.getBedRegionList(args.getBedPath());
        }

        // create the output directory and file
        BufferedWriter bufferedWriter = util.createOutputFile(args.getOutputDir(), args.getTag() + ".bed");
        for (Region region : regionList) {
            // parse the cpg file
            List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, 500);

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
            List<Integer> cpgPosListInRegion = cpgPosList.subList(cpgStartPos, cpgEndPos + 2); // end site add 1

            List<MHBInfo> mhbInfoList = new ArrayList<>();
            Integer startIndex = 0; // start mhb position index in cpgPosListInRegion
            Integer endIndex = 0; // end mhb position index in cpgPosListInRegion
            while (endIndex < cpgPosListInRegion.size() - 1) {
                MHBInfo mhbInfo = new MHBInfo();
                endIndex++;
                Boolean extendFlag = true;
                Integer index = 0;
                for (int i = 1; i < args.getWindow(); i++) {
                    index = endIndex - i; // cpg site index in cpgPosListInRegion for loop
                    if (index < 0) {
                        break;
                    }

                    // parse the mhap file
                    List<MHapInfo> mHapInfoList = util.parseMhapFile(args.getmHapPath(), region, "both", true);

                    // get r2 and pvalue of startIndex
                    R2Info r2Info= util.getR2FromList(mHapInfoList, cpgPosList, cpgPosListInRegion.get(index), cpgPosListInRegion.get(endIndex), 0);
//                    System.out.println("startIndex: " + startIndex + " index: " + index + " endIndex: " + endIndex);
//                    System.out.println(cpgPosListInRegion.get(index) + "\t" + cpgPosListInRegion.get(endIndex) + "\t"
//                            + r2Info.getR2() + "\t" + r2Info.getPvalue());
                    if (r2Info == null || r2Info.getR2() < args.getR2() || r2Info.getPvalue() > args.getPvalue()) {
                        extendFlag = false;
                        break;
                    }
                }

                if (!extendFlag) {
                    Integer mhbSize = endIndex - startIndex;
                    Integer mhbStart = startIndex;
                    Integer mhbEnd = endIndex - 1;
                    startIndex = endIndex;
                    if (mhbSize >= args.getWindow()) {
                        mhbInfo.setChrom(region.getChrom());
                        mhbInfo.setStart(cpgPosListInRegion.get(mhbStart));
                        mhbInfo.setEnd(cpgPosListInRegion.get(mhbEnd));
                        mhbInfoList.add(mhbInfo);
                        log.info("discovery a mhb in : " + mhbInfo.getChrom() + ":" + mhbInfo.getStart() + "-" + mhbInfo.getEnd());
                        bufferedWriter.write(mhbInfo.getChrom() + "\t" + mhbInfo.getStart() + "\t" + mhbInfo.getEnd() + "\n");
                    }
                }
            }
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
