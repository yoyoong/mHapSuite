package com;

import com.args.StatArgs;
import com.bean.MHapInfo;
import com.bean.R2Info;
import com.bean.Region;
import com.bean.StatInfo;
import com.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.*;

public class Stat {
    public static final Logger log = LoggerFactory.getLogger(Stat.class);

    StatArgs args = new StatArgs();
    Util util = new Util();

    public void stat(StatArgs statArgs) throws Exception {
        log.info("Stat start!");
        args = statArgs;

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

        BufferedWriter bufferedWriter = util.createOutputFile("", args.getOutputFile());

        String[] metrics = args.getMetrics().split(" ");
        List<String> metricsList = new ArrayList<>();
        for (String metric : metrics) {
            metricsList.add(metric);
        }
        bufferedWriter.write(printHead(metricsList));

        // get the metric list
        for (Region region : regionList) {
            // parse the mhap file
            List<MHapInfo> mHapInfoListMerged = util.parseMhapFile(args.getMhapPath(), region, args.getStrand(), true);
            if (mHapInfoListMerged.size() < 1) {
                continue;
            }

            // parse the cpg file
            List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, 500);
            if (cpgPosList.size() < 1) {
                continue;
            }

            boolean getStatResult = getStat(mHapInfoListMerged, cpgPosList, region, metricsList, bufferedWriter);
            if (!getStatResult) {
                log.error("getStat fail, please check the command.");
                return;
            }
            log.info("Region: " + region.toHeadString() + " calculate end!");
        }
        bufferedWriter.close();

        log.info("Stat end!");
    }

    private boolean checkArgs() {
        if (args.getMhapPath().equals("")) {
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
        if (!args.getStrand().equals("plus") && !args.getStrand().equals("minus") && !args.getStrand().equals("both")) {
            log.error("The strand must be one of plus, minus or both");
            return false;
        }
        return true;
    }

    public String printHead(List<String> metricsList) {
        String line = "chr" + "\t" + "start" + "\t" + "end" + "\t" + "nReads" + "\t" + "mBase" + "\t" + "cBase" + "\t"
                + "tBase" + "\t" + "K4plus" + "\t" + "nDR" + "\t" + "nMR" + "\t" + "nCPG" + "\t" + "nPairs";
        for (int i = 0; i < metricsList.size(); i++) {
            if (metricsList.get(i).equals("MM")) {
                line += "\t" + "MM";
            } else if (metricsList.get(i).equals("CHALM")) {
                line += "\t" + "CHALM";
            } else if (metricsList.get(i).equals("PDR")) {
                line += "\t" + "PDR";
            } else if (metricsList.get(i).equals("MHL")) {
                line += "\t" + "MHL";
            } else if (metricsList.get(i).equals("MBS")) {
                line += "\t" + "MBS";
            } else if (metricsList.get(i).equals("MCR")) {
                line += "\t" + "MCR";
            } else if (metricsList.get(i).equals("Entropy")) {
                line += "\t" + "Entropy";
            } else if (metricsList.get(i).equals("R2")) {
                line += "\t" + "R2";
            }
        }
        line += "\n";
        return line;
    }

    private boolean getStat(List<MHapInfo> mHapInfoListMerged, List<Integer> cpgPosList, Region region,
                            List<String> metricsList, BufferedWriter bufferedWriter) throws Exception {

        // get cpg site list in region
        List<Integer> cpgPosListInRegion = util.getCpgPosListInRegion(cpgPosList, region);

        if (args.getCutReads()) { // cut the cpg out of region
            for (int i = 0; i < mHapInfoListMerged.size(); i++) {
                MHapInfo mHapInfo = mHapInfoListMerged.get(i);
                String cpg = util.cutReads(mHapInfo, cpgPosList, cpgPosListInRegion);
                mHapInfo.setCpg(cpg);
            }
        }

        // calculate the default columns
        Integer nReads = 0; // 总read个数
        Integer mBase = 0; // 甲基化位点个数
        Integer cBase = 0; // 存在甲基化的read中的未甲基化位点个数
        Integer tBase = 0; // 总位点个数
        Integer K4plus = 0; // 长度大于等于K个位点的read个数
        Integer nDR = 0; // 长度大于等于K个位点且同时含有甲基化和未甲基化位点的read个数
        Integer nMR = 0; // 长度大于等于K个位点且含有甲基化位点的read个数
        for (int i = 0; i < mHapInfoListMerged.size(); i++) {
            MHapInfo mHapInfo = mHapInfoListMerged.get(i);
            String cpg = mHapInfo.getCpg();
            Integer cnt = mHapInfo.getCnt();
            nReads += cnt;
            tBase += cpg.length() * cnt;
            for (int j = 0; j < cpg.length(); j++) {
                if (cpg.charAt(j) == '1') {
                    mBase += cnt;
                }
            }
            if (cpg.contains("1")) {
                for (int j = 0; j < cpg.length(); j++) {
                    if (cpg.charAt(j) == '0') {
                        cBase += cnt;
                    }
                }
            }
            if (cpg.length() >= args.getK()) {
                K4plus += cnt;
                if (cpg.contains("1")) {
                    nMR += cnt;
                    if (cpg.contains("0")) {
                        nDR += cnt;
                    }
                }
            }
        }

        StatInfo statInfo = new StatInfo();
        statInfo.setChr(region.getChrom());
        statInfo.setStart(region.getStart());
        statInfo.setEnd(region.getEnd());
        statInfo.setnReads(nReads);
        statInfo.setmBase(mBase);
        statInfo.setcBase(cBase);
        statInfo.settBase(tBase);
        statInfo.setK4plus(K4plus);
        statInfo.setnDR(nDR);
        statInfo.setnMR(nMR);
        statInfo.setnCPG(cpgPosListInRegion.size());
        Double[] nPairsAndR2 = calculateNPairsAndR2(mHapInfoListMerged, cpgPosList, cpgPosListInRegion, args.getR2Cov());
        statInfo.setnPairs(nPairsAndR2[0].intValue());
        for (int i = 0; i < metricsList.size(); i++) {
            if (metricsList.get(i).equals("MM")) {
                if (nReads < args.getCpgCov()) {
                    statInfo.setMM(Double.NaN);
                } else {
                    statInfo.setMM(mBase.doubleValue() / tBase.doubleValue());
                }
            } else if (metricsList.get(i).equals("CHALM")) {
                if (K4plus < args.getK4Plus()) {
                    statInfo.setCHALM(Double.NaN);
                } else {
                    statInfo.setCHALM(nMR.doubleValue() / K4plus.doubleValue());
                }
            } else if (metricsList.get(i).equals("PDR")) {
                if (K4plus < args.getK4Plus()) {
                    statInfo.setPDR(Double.NaN);
                } else {
                    statInfo.setPDR(nDR.doubleValue() / K4plus.doubleValue());
                }
            } else if (metricsList.get(i).equals("MHL")) {
                if (K4plus < args.getK4Plus()) {
                    statInfo.setMHL(Double.NaN);
                } else {
                    statInfo.setMHL(util.calculateMHL(mHapInfoListMerged, args.getMinK(), args.getMaxK()));
                }
            } else if (metricsList.get(i).equals("MBS")) {
                if (K4plus < args.getK4Plus()) {
                    statInfo.setMBS(Double.NaN);
                } else {
                    statInfo.setMBS(util.calculateMBS(mHapInfoListMerged, args.getK()));
                }
            } else if (metricsList.get(i).equals("MCR")) {
                if (nReads < args.getCpgCov()) {
                    statInfo.setMCR(Double.NaN);
                } else {
                    statInfo.setMCR(cBase.doubleValue() / tBase.doubleValue());
                }
            } else if (metricsList.get(i).equals("Entropy")) {
                if (K4plus < args.getK4Plus()) {
                    statInfo.setEntropy(Double.NaN);
                } else {
                    statInfo.setEntropy(util.calculateEntropy(mHapInfoListMerged, args.getK()));
                }
            } else if (metricsList.get(i).equals("R2")) {
                statInfo.setR2(nPairsAndR2[1]);
            }
        }

        bufferedWriter.write(statInfo.print(metricsList));

        return true;
    }

    public Double[] calculateNPairsAndR2(List<MHapInfo> mHapInfoList, List<Integer> cpgPosList, List<Integer> cpgPosListInRegion, Integer r2Cov) {
        Double[] nPairsAndR2 = new Double[2];

        Double nPairs = 0.0;
        Double R2 = 0.0;
        Integer R2Cnt = 0;
        for (int i = 0; i < cpgPosListInRegion.size(); i++) {
            for (int j = i + 1; j < cpgPosListInRegion.size(); j++) {
                R2Info r2Info = util.getR2FromList(mHapInfoList, cpgPosList, cpgPosListInRegion.get(i), cpgPosListInRegion.get(j), r2Cov);
                if (r2Info != null) {
                    nPairs++;
                    if (!r2Info.getR2().isNaN()) {
                        R2Cnt++;
                        R2 += r2Info.getR2();
                    }
                }
            }
        }

        nPairsAndR2[0] = nPairs;
        nPairsAndR2[1] = R2 / R2Cnt;
        return nPairsAndR2;
    }

}
