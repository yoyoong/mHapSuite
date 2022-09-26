package com;

import com.args.GenomeWideArgs;
import com.bean.*;
import com.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
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

        // cpgPosList‘s map, a chrom corresponds to one list, map by chr
        List<Map.Entry<String, List<Integer>>> cpgPosListMapList = new ArrayList<>();
        Map<String, List<Integer>> cpgPosListMapRaw = new LinkedHashMap<>();
        if (args.getRegion() != null && !args.getRegion().equals("")) {
            Region region = util.parseRegion(args.getRegion());

            // parse cpg file in region
            List<Integer> cpgPosListInRegion = util.parseCpgFile(args.getCpgPath(), region);
            cpgPosListMapRaw.put(region.getChrom(), cpgPosListInRegion);
        } else {
            // parse whole cpg file
            cpgPosListMapRaw = util.parseWholeCpgFile(args.getCpgPath());
        }

        // sort the cpgPosListMap
        cpgPosListMapList = new ArrayList<Map.Entry<String, List<Integer>>>(cpgPosListMapRaw.entrySet());
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

        // get the metric list
        String[] metrics = args.getMetrics().split(" ");
        for (String metric : metrics) {
            if (!metric.equals("")) {
                BufferedWriter bufferedWriter = util.createOutputFile(args.getOutputDir(), args.getTag() + "." + metric + ".bedGraph");
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
                }
                bufferedWriter.close();
                log.info("calculate " + metric + " succeed!");
            }
        }

        log.info("GenomeWide end!");
    }

    private boolean checkArgs() {
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
            if (metric.equals("MM") || metric.equals("PDR") || metric.equals("CHALM") || metric.equals("MCR")) {
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
                bedGraphInfo.setValue(mBase.doubleValue() / nReads.doubleValue());
            } else if (metric.equals("PDR")) {
                bedGraphInfo.setValue(nDR.doubleValue() / K4plus.doubleValue());
            } else if (metric.equals("CHALM")) {
                bedGraphInfo.setValue(nMR.doubleValue() / K4plus.doubleValue());
            } else if (metric.equals("MHL")) {
                bedGraphInfo.setValue(util.calculateMHL(mHapInfoListWithSite, args.getMinK(), args.getMaxK()));
            } else if (metric.equals("MCR")) {
                bedGraphInfo.setValue(cBase.doubleValue() / tBase.doubleValue());
            } else if (metric.equals("MBS")) {
                bedGraphInfo.setValue(util.calculateMBS(mHapInfoListWithSite, args.getK()));
            } else if (metric.equals("Entropy")) {
                bedGraphInfo.setValue(util.calculateEntropy(mHapInfoListWithSite, args.getK()));
            } else if (metric.equals("R2")) {
                bedGraphInfo.setValue(calculateR2(mHapInfoListWithSite, cpgPosListWithSite, cpgPos));
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

            R2Info r2Info = util.getR2FromList(mHapInfoList, cpgPosList, cpgPos, cpgPosList.get(j), args.getCpgCov());
            if (r2Info != null && r2Info.getR2() != Double.NaN) {
                r2List.add(r2Info.getR2());
            }
        }

        for (int i = 0; i < r2List.size(); i++) {
            r2Sum += r2List.get(i);
        }

        return r2Sum / r2List.size();
    }
}
