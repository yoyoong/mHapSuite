package com;

import com.args.GenomeWideArgs;
import com.bean.*;
import com.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class GenomeWide {
    public final Logger log = LoggerFactory.getLogger(GenomeWide.class);

    GenomeWideArgs args = new GenomeWideArgs();
    Util util = new Util();

    public void genomeWide(GenomeWideArgs genomeWideArgs) throws Exception {
        log.info("GenomeWide start!");
        args = genomeWideArgs;

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        // get the metric list
        String[] metrics = args.getMetrics().trim().split(" ");
        for (String metric : metrics) {
            BufferedWriter bufferedWriter = util.createOutputFile(args.getOutputDir(), args.getTag() + "." + metric + ".bedGraph");
            if (args.getRegion() != null && !args.getRegion().equals("")) {
                Region region = util.parseRegion(args.getRegion());
                // parse cpg file in region
                List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, 500);
                List<Integer> cpgPosListInRegion = util.getCpgPosListInRegion(cpgPosList, region);

                boolean getGenomeWideResult = getGenomeWide(metric, cpgPosListInRegion, region, bufferedWriter);
                if (!getGenomeWideResult) {
                    log.error("getGenomeWide fail, please check the command.");
                    return;
                }
            } else if (args.getBedPath() != null && !args.getBedPath().equals("")) {
                // get region list from bed file
                List<Region> regionList = util.getBedRegionList(args.getBedPath());;

                for (Region region : regionList) {
                    // parse cpg file in region
                    List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, 500);
                    List<Integer> cpgPosListInRegion = util.getCpgPosListInRegion(cpgPosList, region);

                    boolean getGenomeWideResult = getGenomeWide(metric, cpgPosListInRegion, region, bufferedWriter);
                    if (!getGenomeWideResult) {
                        log.error("getGenomeWide fail, please check the command.");
                        return;
                    }
                }
            } else {
                // parse whole cpg file
                Map<String, List<Integer>> cpgPosListMapRaw = util.parseWholeCpgFile(args.getCpgPath());

                // sort the cpgPosListMap
                List<Map.Entry<String, List<Integer>>> cpgPosListMapList = new ArrayList<Map.Entry<String, List<Integer>>>(cpgPosListMapRaw.entrySet());
                Collections.sort(cpgPosListMapList, (o1, o2) -> o1.getKey().compareTo(o2.getKey()));
                cpgPosListMapList.sort(new Comparator<Map.Entry<String, List<Integer>>>() {
                    @Override
                    public int compare(Map.Entry<String, List<Integer>> o1, Map.Entry<String, List<Integer>> o2) {
                        String chromNum1 = o1.getKey().substring(3, o1.getKey().length());
                        String chromNum2 = o2.getKey().substring(3, o2.getKey().length());
                        if (util.isNumeric(chromNum1) && util.isNumeric(chromNum2)) {
                            return Integer.valueOf(chromNum1) - Integer.valueOf(chromNum2);//o1减o2是升序，反之是降序
                        } else {
                            return chromNum1.compareTo(chromNum2);
                        }
                    }
                });

                for (Map.Entry<String, List<Integer>> cpgPosListMap : cpgPosListMapList) {
                    List<Integer> cpgPosListInRegion = cpgPosListMap.getValue();

                    // get the whole region of this chrom
                    Region region = new Region();
                    region.setChrom(cpgPosListMap.getKey());
                    region.setStart(cpgPosListInRegion.get(0));
                    region.setEnd(cpgPosListInRegion.get(cpgPosListInRegion.size() - 1));

                    boolean getGenomeWideResult = getGenomeWide(metric, cpgPosListInRegion, region, bufferedWriter);
                    if (!getGenomeWideResult) {
                        log.error("getGenomeWide fail, please check the command.");
                        return;
                    }
                    log.info("calculate " + metric + " " + cpgPosListMap.getKey() + " end!");
                }
            }

            bufferedWriter.close();
            log.info("calculate " + metric + " succeed!");
        }

        log.info("GenomeWide end!");
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
        if (args.getMetrics().equals("")) {
            log.error("metrics can not be null.");
            return false;
        }
        if (!args.getRegion().equals("") && !args.getBedPath().equals("")) {
            log.error("Can not input region and bedPath at the same time.");
            return false;
        }

        return true;
    }

    private boolean getGenomeWide(String metric, List<Integer> cpgPosListInRegion, Region region, BufferedWriter bufferedWriter) throws Exception {
        Integer getCnt = 0;
        for (Integer cpgPos : cpgPosListInRegion) {
            getCnt++;
            if (getCnt % 10000 == 0) {
                log.info("calculate " + metric + " complete " + + getCnt + " positions.");
            }

            //获取时间戳
//            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
//            long timeStramp = System.currentTimeMillis();
//            log.info(format.format(timeStramp));

            Region thisSiteRegion = new Region();
            thisSiteRegion.setChrom(region.getChrom());
            thisSiteRegion.setStart(cpgPos);
            thisSiteRegion.setEnd(cpgPos);
            // parse the mhap file
            List<MHapInfo> mHapInfoListWithSite = util.parseMhapFile(args.getMhapPath(), thisSiteRegion, args.getStrand(), true);
            if (mHapInfoListWithSite.size() < 1) {
                continue;
            }

            // get the region with site
            Region regionWithSite = new Region();
            Integer startPos = Integer.MAX_VALUE;
            Integer endPos = 0;
            for (MHapInfo mHapInfo : mHapInfoListWithSite) {
                if (mHapInfo.getStart() < startPos) {
                    startPos = mHapInfo.getStart();
                }
                if (mHapInfo.getEnd() > endPos) {
                    endPos = mHapInfo.getEnd();
                }
            }
            regionWithSite.setChrom(mHapInfoListWithSite.get(0).getChrom());
            regionWithSite.setStart(startPos);
            regionWithSite.setEnd(endPos);
            List<Integer> cpgPosListWithSite = util.parseCpgFileWithShift(args.getCpgPath(), regionWithSite, 500);

            // calculate some base calue
            Integer nReads = 0; // 总read个数
            Integer mBase = 0; // 甲基化位点个数
            Integer cBase = 0; // 存在甲基化的read中的未甲基化位点个数
            Integer tBase = 0; // 总位点个数
            Integer K4plus = 0; // 长度大于等于K个位点的read个数
            Integer nDR = 0; // 长度大于等于K个位点且同时含有甲基化和未甲基化位点的read个数
            Integer nMR = 0; // 长度大于等于K个位点且含有甲基化位点的read个数
            if (metric.equals("MM") || metric.equals("PDR") || metric.equals("CHALM") || metric.equals("MHL") ||
                    metric.equals("MCR") || metric.equals("MBS") || metric.equals("Entropy")) {
                for (int i = 0; i < mHapInfoListWithSite.size(); i++) {
                    MHapInfo mHapInfo = mHapInfoListWithSite.get(i);
                    String cpg = mHapInfo.getCpg();
                    Integer cnt = mHapInfo.getCnt();
                    nReads += cnt;
                    tBase += cpg.length() * cnt;

                    // 计算该位点的MM
                    Integer pos = cpgPosListInRegion.indexOf(cpgPos) - cpgPosListInRegion.indexOf(mHapInfo.getStart());
                    if (mHapInfo.getCpg().charAt(pos) == '1') {
                        mBase += cnt;
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
            }

            if (metric.equals("MM") && nReads < args.getCpgCov()) {
                continue;
            }
            if (metric.equals("PDR") || metric.equals("CHALM") || metric.equals("MHL") ||
                    metric.equals("MCR") || metric.equals("MBS") || metric.equals("Entropy")) {
                if (K4plus < args.getK4Plus()) {
                    continue;
                }
            }

            BedGraphInfo bedGraphInfo = new BedGraphInfo();
            bedGraphInfo.setChrom(region.getChrom());
            bedGraphInfo.setStart(cpgPos - 1);
            bedGraphInfo.setEnd(cpgPos);
            if (metric.equals("MM")) {
                Double MM = mBase.doubleValue() / nReads.doubleValue();
                if (MM.isNaN() || MM.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setValue(MM.floatValue());
            } else if (metric.equals("PDR")) {
                Double PDR = nDR.doubleValue() / K4plus.doubleValue();
                if (PDR.isNaN() || PDR.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setValue(PDR.floatValue());
            } else if (metric.equals("CHALM")) {
                Double CHALM = nMR.doubleValue() / K4plus.doubleValue();
                if (CHALM.isNaN() || CHALM.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setValue(CHALM.floatValue());
            } else if (metric.equals("MHL")) {
                Double MHL = util.calculateMHL(mHapInfoListWithSite, args.getMinK(), args.getMaxK());
                if (MHL.isNaN() || MHL.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setValue(MHL.floatValue());
            } else if (metric.equals("MCR")) {
                Double MCR = cBase.doubleValue() / tBase.doubleValue();
                if (MCR.isNaN() || MCR.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setValue(MCR.floatValue());
            } else if (metric.equals("MBS")) {
                Double MBS = util.calculateMBS(mHapInfoListWithSite, args.getK());
                if (MBS.isNaN() || MBS.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setValue(MBS.floatValue());
            } else if (metric.equals("Entropy")) {
                Double Entropy = util.calculateEntropy(mHapInfoListWithSite, args.getK());
                if (Entropy.isNaN() || Entropy.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setValue(Entropy.floatValue());
            } else if (metric.equals("R2")) {
                Double R2 = calculateR2(mHapInfoListWithSite, cpgPosListWithSite, cpgPos);
                if (R2.isNaN() || R2.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setValue(R2.floatValue());
            }

            String line = bedGraphInfo.getChrom() + "\t" + bedGraphInfo.getStart() + "\t" + bedGraphInfo.getEnd() + "\t" + bedGraphInfo.getValue() + "\n";
            bufferedWriter.write(line);
        }

        return true;
    }

    public Double calculateR2(List<MHapInfo> mHapInfoList, List<Integer> cpgPosList, Integer cpgPos) {
        Double r2Sum = 0.0;
        List<Double> r2List = new ArrayList<>();

        Integer pos = cpgPosList.indexOf(cpgPos);
        for (int j = pos - 2; j < pos + 3; j++) {
            if (j < 0 || j == pos || j >= cpgPosList.size()) {
                continue;
            }

            R2Info r2Info = util.getR2FromList(mHapInfoList, cpgPosList, cpgPos, cpgPosList.get(j), args.getR2Cov());
            if (r2Info != null && !r2Info.getR2().isNaN()) {
                r2List.add(r2Info.getR2());
            }
        }

        for (int i = 0; i < r2List.size(); i++) {
            r2Sum += r2List.get(i);
        }

        return r2Sum / r2List.size();
    }
}
