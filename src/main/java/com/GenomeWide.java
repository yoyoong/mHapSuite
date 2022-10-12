package com;

import com.args.GenomeWideArgs;
import com.bean.BedGraphInfo;
import com.bean.MHapInfo;
import com.bean.Region;
import com.common.Util;
import com.common.BigFileReader;
import htsjdk.tribble.readers.TabixReader;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class GenomeWide {
    public final Logger log = LoggerFactory.getLogger(GenomeWide.class);

    GenomeWideArgs args = new GenomeWideArgs();
    Util util = new Util();

    BufferedWriter bufferedWriterMM = null;
    BufferedWriter bufferedWriterPDR = null;
    BufferedWriter bufferedWriterCHALM = null;
    BufferedWriter bufferedWriterMHL = null;
    BufferedWriter bufferedWriterMCR = null;
    BufferedWriter bufferedWriterMBS = null;
    BufferedWriter bufferedWriterEntropy = null;
    BufferedWriter bufferedWriterR2 = null;

    public void genomeWide(GenomeWideArgs genomeWideArgs) throws Exception {
        log.info("GenomeWide start!");
        args = genomeWideArgs;

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        if (args.getMetrics().contains("MM")) {
            bufferedWriterMM = util.createOutputFile(args.getOutputDir(), args.getTag() + ".MM.bedGraph");
        }
        if (args.getMetrics().contains("PDR")) {
            bufferedWriterPDR = util.createOutputFile(args.getOutputDir(), args.getTag() + ".PDR.bedGraph");
        }
        if (args.getMetrics().contains("CHALM")) {
            bufferedWriterCHALM = util.createOutputFile(args.getOutputDir(), args.getTag() + ".CHALM.bedGraph");
        }
        if (args.getMetrics().contains("MHL")) {
            bufferedWriterMHL = util.createOutputFile(args.getOutputDir(), args.getTag() + ".MHL.bedGraph");
        }
        if (args.getMetrics().contains("MCR")) {
            bufferedWriterMCR = util.createOutputFile(args.getOutputDir(), args.getTag() + ".MCR.bedGraph");
        }
        if (args.getMetrics().contains("MBS")) {
            bufferedWriterMBS = util.createOutputFile(args.getOutputDir(), args.getTag() + ".MBS.bedGraph");
        }
        if (args.getMetrics().contains("Entropy")) {
            bufferedWriterEntropy = util.createOutputFile(args.getOutputDir(), args.getTag() + ".Entropy.bedGraph");
        }
        if (args.getMetrics().contains("R2")) {
            bufferedWriterR2 = util.createOutputFile(args.getOutputDir(), args.getTag() + ".R2.bedGraph");
        }

        if (args.getRegion() != null && !args.getRegion().equals("")) {
            Region region = util.parseRegion(args.getRegion());
            List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, 500);
            Boolean calculateRegionResult = calculateRegion(cpgPosList, region);
            if (!calculateRegionResult) {
                log.error("Calculate " + region.toHeadString() + " fail!");
                return;
            }
        } else if (args.getBedPath() != null && !args.getBedPath().equals("")) {
            // get region list from bed file
            List<Region> regionList = util.getBedRegionList(args.getBedPath());
            for (Region region : regionList) {
                List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, 500);
                Boolean calculateRegionResult = calculateRegion(cpgPosList, region);
                if (!calculateRegionResult) {
                    log.error("Calculate " + region.toHeadString() + " fail!");
                    return;
                }
            }
        } else {
            Boolean calculateResult = calculateWholeGenome();
            if (!calculateResult) {
                log.error("calculateResult fail!");
                return;
            }
        }

        if (args.getMetrics().contains("MM")) {
            bufferedWriterMM.close();
        }
        if (args.getMetrics().contains("PDR")) {
            bufferedWriterPDR.close();
        }
        if (args.getMetrics().contains("CHALM")) {
            bufferedWriterCHALM.close();
        }
        if (args.getMetrics().contains("MHL")) {
            bufferedWriterMHL.close();
        }
        if (args.getMetrics().contains("MCR")) {
            bufferedWriterMCR.close();
        }
        if (args.getMetrics().contains("MBS")) {
            bufferedWriterMBS.close();
        }
        if (args.getMetrics().contains("Entropy")) {
            bufferedWriterEntropy.close();
        }
        if (args.getMetrics().contains("R2")) {
            bufferedWriterR2.close();
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

    private boolean calculateRegion(List<Integer> cpgPosList, Region region) {
        try {
            List<BedGraphInfo> calculateResult = genomeWide(cpgPosList, region);
            if (calculateResult.size() > 0) {
                for (BedGraphInfo bedGraphInfo : calculateResult) {
                    if (args.getMetrics().contains("MM")) {
                        bufferedWriterMM.write(bedGraphInfo.printMM());
                    }
                    if (args.getMetrics().contains("PDR")) {
                        bufferedWriterPDR.write(bedGraphInfo.printPDR());
                    }
                    if (args.getMetrics().contains("CHALM")) {
                        bufferedWriterCHALM.write(bedGraphInfo.printCHALM());
                    }
                    if (args.getMetrics().contains("MHL")) {
                        bufferedWriterMHL.write(bedGraphInfo.printMHL());
                    }
                    if (args.getMetrics().contains("MCR")) {
                        bufferedWriterMCR.write(bedGraphInfo.printMCR());
                    }
                    if (args.getMetrics().contains("MBS")) {
                        bufferedWriterMBS.write(bedGraphInfo.printMBS());
                    }
                    if (args.getMetrics().contains("Entropy")) {
                        bufferedWriterEntropy.write(bedGraphInfo.printEntropy());
                    }
                    if (args.getMetrics().contains("R2")) {
                        bufferedWriterR2.write(bedGraphInfo.printR2());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private boolean calculateWholeGenome() throws Exception {
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

        AtomicLong counter = new AtomicLong(0);
        String bigFilePath = "esophagus_T.mhap";
        BigFileReader bigFileReader = new BigFileReader(new File(bigFilePath), StandardCharsets.UTF_8, 1024 * 1024,
                24, cpgPosListMapRaw, args, cpgPosListMapList);
        bigFileReader.start();

        Map<String, int[]> nReadsListMap = bigFileReader.getnReadsListMap();
        Map<String, int[]> mReadListMap = bigFileReader.getmReadListMap();
        Map<String, int[]> cBaseListMap = bigFileReader.getcBaseListMap();
        Map<String, int[]> tBaseListMap = bigFileReader.gettBaseListMap();
        Map<String, int[]> K4plusListMap = bigFileReader.getK4plusListMap();
        Map<String, int[]> nDRListMap = bigFileReader.getnDRListMap();
        Map<String, int[]> nMRListMap = bigFileReader.getnMRListMap();
        Map<String, int[][]> methKmersListMap = bigFileReader.getMethKmersListMap();
        Map<String, int[][]> totalKmersListMap = bigFileReader.getTotalKmersListMap();
        Map<String, Double[]> mbsNumListMap = bigFileReader.getMbsNumListMap();
        Map<String, int[][]> kmerListMap = bigFileReader.getKmerListMap();
        Map<String, int[]> kmerAllListMap = bigFileReader.getKmerAllListMap();
        Map<String, int[][]> N00ListMap = bigFileReader.getN00ListMap();
        Map<String, int[][]> N01ListMap = bigFileReader.getN01ListMap();
        Map<String, int[][]> N10ListMap = bigFileReader.getN10ListMap();
        Map<String, int[][]> N11ListMap = bigFileReader.getN11ListMap();

        for (Map.Entry<String, List<Integer>> cpgPosListMap : cpgPosListMapList) {
            log.info("Calculate " + cpgPosListMap.getKey() + " start!");
            List<Integer> cpgPosList = cpgPosListMap.getValue();

//            int[] nReadsList = new int[cpgPosList.size()]; // 该位点的总read个数
//            int[] mReadList = new int[cpgPosList.size()]; // 该位点为甲基化的read个数
//            int[] cBaseList = new int[cpgPosList.size()]; // 该位点为甲基化的read中的未甲基化cpg位点个数
//            int[] tBaseList = new int[cpgPosList.size()]; // 该位点的总cpg位点个数
//            int[] K4plusList = new int[cpgPosList.size()]; // 长度大于等于K个位点的read个数
//            int[] nDRList = new int[cpgPosList.size()]; // 长度大于等于K个位点且同时含有甲基化和未甲基化位点的read个数
//            int[] nMRList = new int[cpgPosList.size()]; // 长度大于等于K个位点且含有甲基化位点的read个数
//            // MHL
//            int[][] methKmersList = new int[0][0]; // Number of fully methylated k-mers
//            int[][] totalKmersList = new int[0][0]; // Number of fully methylated k-mers
//            if (args.getMetrics().contains("MHL")) {
//                methKmersList = new int[args.getMaxK() - args.getMinK() + 1][cpgPosList.size()]; // Number of fully methylated k-mers
//                totalKmersList = new int[args.getMaxK() - args.getMinK() + 1][cpgPosList.size()]; // Number of fully methylated k-mers
//            }
//            // MBS
//            Double[] mbsNumList = new Double[0]; // MBS list
//            if (args.getMetrics().contains("MBS")) {
//                mbsNumList = new Double[cpgPosList.size()]; // MBS list
//                for (Integer i = 0; i < cpgPosList.size(); i++) {
//                    mbsNumList[i] = 0.0;
//                }
//            }
//            // Entropy
//            int[][] kmerList = new int[0][0];
//            int[] kmerAllList = new int[0];
//            if (args.getMetrics().contains("Entropy")) {
//                kmerList = new int[args.getK() * args.getK()][cpgPosList.size()];
//                kmerAllList = new int[cpgPosList.size()];
//            }
//            // R2
//            int[][] N00List = new int[0][0];
//            int[][] N01List = new int[0][0];
//            int[][] N10List = new int[0][0];
//            int[][] N11List = new int[0][0];
//            if (args.getMetrics().contains("R2")) {
//                N00List = new int[4][cpgPosList.size()];
//                N01List = new int[4][cpgPosList.size()];
//                N10List = new int[4][cpgPosList.size()];
//                N11List = new int[4][cpgPosList.size()];
//            }

//            Region region = new Region();
//            region.setChrom(cpgPosListMap.getKey());
//            region.setStart(cpgPosList.get(0));
//            region.setEnd(cpgPosList.get(cpgPosList.size() - 1));
//
//            TabixReader tabixReader = new TabixReader(args.getMhapPath());
//            TabixReader.Iterator mhapIterator = tabixReader.query(region.getChrom(), region.getStart() - 1, region.getEnd());
//            List<MHapInfo> mHapInfoList = new ArrayList<>();
//            String mHapLine = "";
//            Integer lineCnt = 0;
//            while((mHapLine = mhapIterator.next()) != null) {
//                if ((args.getStrand().equals("plus") && mHapLine.split("\t")[5].equals("-")) ||
//                        (args.getStrand().equals("minus") && mHapLine.split("\t")[5].equals("+"))) {
//                    continue;
//                }
//                lineCnt++;
//                if (lineCnt % 1000000 == 0) {
//                    log.info("Read mhap file complete " + lineCnt + " lines.");
//                }
//
//                MHapInfo mHapInfo = new MHapInfo(mHapLine.split("\t")[0], Integer.valueOf(mHapLine.split("\t")[1]),
//                        Integer.valueOf(mHapLine.split("\t")[2]), mHapLine.split("\t")[3],
//                        Integer.valueOf(mHapLine.split("\t")[4]), mHapLine.split("\t")[5]);
//
//                Integer cpgPosIndex = cpgPosListMapRaw.get(mHapInfo.getChrom()).indexOf(mHapInfo.getStart());
//                String cpgStr = mHapInfo.getCpg();
//                Integer cpgLen = cpgStr.length();
//                Integer readCnt = mHapInfo.getCnt();
//
//                for (int i = 0; i < cpgLen; i++) {
//                    if (cpgStr.charAt(i) == '1') {
//                        mReadList[cpgPosIndex + i] += readCnt;
//                    }
//                    nReadsList[cpgPosIndex + i] += readCnt;
//                    tBaseList[cpgPosIndex + i] += cpgLen * readCnt;
//                    if (cpgStr.contains("1")) {
//                        long noMethCnt = cpgStr.chars().filter(ch -> ch == '0').count();
//                        cBaseList[cpgPosIndex + i] += noMethCnt * readCnt;
//                    }
//                }
//
//                if (cpgStr.length() >= args.getK()) {
//                    for (int i = 0; i < cpgLen; i++) {
//                        K4plusList[cpgPosIndex + i] += readCnt;
//                        if (cpgStr.contains("1")) {
//                            nMRList[cpgPosIndex + i] += readCnt;
//                            if (cpgStr.contains("0")) {
//                                nDRList[cpgPosIndex + i] += readCnt;
//                            }
//                        }
//                    }
//                }
//
//                if (args.getMetrics().contains("MHL")) {
//                    if (args.getMinK() > cpgLen) {
//                        log.error("calculate MHL Error: minK is too large.");
//                        continue;
//                    }
//                    Integer maxK = args.getMaxK() > cpgLen ? cpgLen : args.getMaxK();
//
//                    int[] subMethKmersList = new int[cpgLen];
//                    int[] subTotalKmersList = new int[cpgLen];
//                    String fullMethStr = "1";
//                    for (int i = 0; i < cpgLen; i++) {
//                        Integer noMethCnt = 0;
//                        int start = 0;
//                        while (cpgStr.indexOf(fullMethStr, start) >= 0 && start < cpgStr.length()) {
//                            noMethCnt++;
//                            start = cpgStr.indexOf(fullMethStr, start) + 1;
//                        }
//
//                        subMethKmersList[i] = noMethCnt * readCnt;
//                        subTotalKmersList[i] = (cpgLen - i) * readCnt;
//                        fullMethStr += "1";
//                    }
//
//                    for (int i = args.getMinK() - 1; i < maxK; i++) {
//                        Integer row = i - args.getMinK() + 1;
//                        for (int j = 0; j < cpgLen; j++) {
//                            methKmersList[row][cpgPosIndex + j] += subMethKmersList[i];
//                            totalKmersList[row][cpgPosIndex + j] += subTotalKmersList[i];
//                        }
//                    }
//                }
//
//                if (args.getMetrics().contains("MBS")) {
//                    Double mbsNum = 0.0;
//                    if (cpgLen >= args.getK()) {
//                        for (int i = 0; i < cpgLen; i++) {
//                            String[] cpgStrList = cpgStr.split("0");
//                            Double temp = 0.0;
//                            for (String cpg : cpgStrList) {
//                                temp += Math.pow(cpg.length(), 2);
//                            }
//                            mbsNum = temp / Math.pow(cpgLen, 2) * readCnt;
//                            mbsNumList[cpgPosIndex + i] += mbsNum;
//                        }
//                    }
//                }
//
//                if (args.getMetrics().contains("Entropy")) {
//                    if (cpgLen >= args.getK()) {
//                        Map<String, Integer> kmerMap = new HashMap<>();
//                        if (cpgLen >= args.getK()) {
//                            for (int i = 0; i < cpgLen - args.getK() + 1; i++) {
//                                String kmerStr = cpgStr.substring(i, i + args.getK());
//                                if (kmerMap.containsKey(kmerStr)) {
//                                    kmerMap.put(kmerStr, kmerMap.get(kmerStr) + readCnt);
//                                } else {
//                                    kmerMap.put(kmerStr, readCnt);
//                                }
//                            }
//                        }
//
//                        for (int i = 0; i < cpgLen; i++) {
//                            Iterator<String> iterator1 = kmerMap.keySet().iterator();
//                            while (iterator1.hasNext()) {
//                                String key = iterator1.next();
//                                Double index = 0.0;
//                                for (int j = 0; j < key.length(); j++) {
//                                    if (key.charAt(key.length() - 1 - j) == '1') {
//                                        index += Math.pow(2, j);
//                                    }
//                                }
//                                kmerList[index.intValue()][cpgPosIndex + i] += kmerMap.get(key);
//                                kmerAllList[cpgPosIndex + i] += kmerMap.get(key);
//                            }
//                        }
//                    }
//                }
//
//                if (args.getMetrics().contains("R2")) {
//                    for (int i = 0; i < cpgLen; i++) {
//                        for (int j = i - 2; j < i + 3; j++) {
//                            if (j < 0 || j == i || j >= cpgLen) {
//                                continue;
//                            }
//                            Integer index = j - i > 0 ? j - i + 1 : j - i + 2;
//                            if (mHapInfo.getCpg().charAt(i) == '0' && mHapInfo.getCpg().charAt(j) == '0') {
//                                N00List[index][cpgPosIndex + i] += readCnt;
//                            } else if (mHapInfo.getCpg().charAt(i) == '0' && mHapInfo.getCpg().charAt(j) == '1') {
//                                N01List[index][cpgPosIndex + i] += readCnt;
//                            } else if (mHapInfo.getCpg().charAt(i) == '1' && mHapInfo.getCpg().charAt(j) == '0') {
//                                N10List[index][cpgPosIndex + i] += readCnt;
//                            } else if (mHapInfo.getCpg().charAt(i) == '1' && mHapInfo.getCpg().charAt(j) == '1') {
//                                N11List[index][cpgPosIndex + i] += readCnt;
//                            }
//                        }
//                    }
//                }
//            }

            Integer cpgCnt = 0;
            for (Integer i = 0; i < cpgPosList.size(); i++) {
                cpgCnt++;
                if (cpgCnt % 100000 == 0) {
                    log.info("Calculate complete " + cpgCnt + " cpg positions.");
                }

                Integer nReads = nReadsListMap.get(cpgPosListMap.getKey())[i];
                Integer mRead = mReadListMap.get(cpgPosListMap.getKey())[i];
                Integer cBase = cBaseListMap.get(cpgPosListMap.getKey())[i];
                Integer tBase = tBaseListMap.get(cpgPosListMap.getKey())[i];
                Integer K4plus = K4plusListMap.get(cpgPosListMap.getKey())[i];
                Integer nDR = nDRListMap.get(cpgPosListMap.getKey())[i];
                Integer nMR = nMRListMap.get(cpgPosListMap.getKey())[i];

                if (args.getMetrics().contains("MM") && nReads < args.getCpgCov()) {
                    continue;
                }
                if (args.getMetrics().contains("PDR") || args.getMetrics().contains("CHALM") || args.getMetrics().contains("MHL") ||
                        args.getMetrics().contains("MCR") || args.getMetrics().contains("MBS") || args.getMetrics().contains("Entropy")) {
                    if (K4plus < args.getK4Plus()) {
                        continue;
                    }
                }

                BedGraphInfo bedGraphInfo = new BedGraphInfo();
                bedGraphInfo.setChrom(cpgPosListMap.getKey());
                bedGraphInfo.setStart(cpgPosList.get(i) - 1);
                bedGraphInfo.setEnd(cpgPosList.get(i));
                if (args.getMetrics().contains("MM")) {
                    Double MM = mRead.doubleValue() / nReads.doubleValue();
                    if (MM.isNaN() || MM.isInfinite()) {
                        continue;
                    }
                    bedGraphInfo.setMM(MM.floatValue());
                    bufferedWriterMM.write(bedGraphInfo.printMM());
                }
                if (args.getMetrics().contains("PDR")) {
                    Double PDR = nDR.doubleValue() / K4plus.doubleValue();
                    if (PDR.isNaN() || PDR.isInfinite()) {
                        continue;
                    }
                    bedGraphInfo.setPDR(PDR.floatValue());
                    bufferedWriterPDR.write(bedGraphInfo.printPDR());
                }
                if (args.getMetrics().contains("CHALM")) {
                    Double CHALM = nMR.doubleValue() / K4plus.doubleValue();
                    if (CHALM.isNaN() || CHALM.isInfinite()) {
                        continue;
                    }
                    bedGraphInfo.setCHALM(CHALM.floatValue());
                    bufferedWriterCHALM.write(bedGraphInfo.printCHALM());
                }
                if (args.getMetrics().contains("MHL")) {
                    Double temp = 0.0;
                    Integer w = 0;
                    for (int j = args.getMinK() - 1; j < args.getMaxK(); j++) {
                        Integer row = j - args.getMinK() + 1;
                        Integer methKmers = methKmersListMap.get(cpgPosListMap.getKey())[row][i];
                        Integer totalKmers = totalKmersListMap.get(cpgPosListMap.getKey())[row][i];
                        temp += methKmers.doubleValue() / totalKmers.doubleValue() * (j + 1);
                        w += (j + 1);
                    }
                    Double MHL = temp / w;
                    if (MHL.isNaN() || MHL.isInfinite()) {
                        continue;
                    }
                    bedGraphInfo.setMHL(MHL.floatValue());
                    bufferedWriterMHL.write(bedGraphInfo.printMHL());
                }
                if (args.getMetrics().contains("MCR")) {
                    Double MCR = cBase.doubleValue() / tBase.doubleValue();
                    if (MCR.isNaN() || MCR.isInfinite()) {
                        continue;
                    }
                    bedGraphInfo.setMCR(MCR.floatValue());
                    bufferedWriterMCR.write(bedGraphInfo.printMCR());
                }
                if (args.getMetrics().contains("MBS")) {
                    Double mbsNum = mbsNumListMap.get(cpgPosListMap.getKey())[i];
                    Double MBS = mbsNum / K4plus;
                    if (MBS.isNaN() || MBS.isInfinite()) {
                        continue;
                    }
                    bedGraphInfo.setMBS(MBS.floatValue());
                    bufferedWriterMBS.write(bedGraphInfo.printMBS());
                }
                if (args.getMetrics().contains("Entropy")) {
                    Integer kmerAll = kmerAllListMap.get(cpgPosListMap.getKey())[i];
                    Double temp = 0.0;
                    for (int j = 0; j < args.getK() * args.getK(); j++) {
                        if (kmerListMap.get(cpgPosListMap.getKey())[j][i] > 0) {
                            Integer cnt = kmerListMap.get(cpgPosListMap.getKey())[j][i];
                            temp += cnt.doubleValue() / kmerAll.doubleValue() * Math.log(cnt.doubleValue() / kmerAll.doubleValue()) / Math.log(2);
                        }
                    }

                    Double entropy = - 1 / args.getK().doubleValue() * temp;
                    if (entropy.isNaN() || entropy.isInfinite()) {
                        continue;
                    }
                    bedGraphInfo.setEntropy(entropy.floatValue());
                    bufferedWriterEntropy.write(bedGraphInfo.printEntropy());
                }
                if (args.getMetrics().contains("R2")) {
                    Double r2Sum = 0.0;
                    Double r2Num = 0.0;
                    for (int j = i - 2; j < i + 3; j++) {
                        if (j < 0 || j == i || j >= cpgPosList.size()) {
                            continue;
                        }
                        Integer index = j - i > 0 ? j - i + 1 : j - i + 2;
                        Integer N00 = N00ListMap.get(cpgPosListMap.getKey())[index][i];
                        Integer N01 = N01ListMap.get(cpgPosListMap.getKey())[index][i];
                        Integer N10 = N10ListMap.get(cpgPosListMap.getKey())[index][i];
                        Integer N11 = N11ListMap.get(cpgPosListMap.getKey())[index][i];

                        if ((N00 + N01 + N10 + N11) < args.getR2Cov()) {
                            continue;
                        }

                        // 计算r2
                        Double r2 = 0.0;
                        Double pvalue = 0.0;
                        Double N = N00 + N01 + N10 + N11 + 0.0;
                        if(N == 0) {
                            r2 = Double.NaN;
                            pvalue = Double.NaN;
                        }
                        Double PA = (N10 + N11) / N;
                        Double PB = (N01 + N11) / N;
                        Double D = N11 / N - PA * PB;
                        Double Num = D * D;
                        Double Den = PA * (1 - PA) * PB * (1 - PB);
                        if (Den == 0.0) {
                            r2 = Double.NaN;
                        } else {
                            r2 = Num / Den;
                            if (D < 0) {
                                r2 = -1 * r2;
                            }
                        }

                        if (!r2.isNaN()) {
                            r2Num++;
                            r2Sum += r2;
                        }
                    }

                    Double meanR2 = r2Sum / r2Num;
                    if (meanR2.isNaN() || meanR2.isInfinite()) {
                        continue;
                    }
                    bedGraphInfo.setR2(meanR2.floatValue());
                    bufferedWriterR2.write(bedGraphInfo.printR2());
                }
            }

            log.info("Calculate " + cpgPosListMap.getKey() + " end!");
        }

        return true;
    }

    private List<BedGraphInfo> genomeWide(List<Integer> cpgPosList, Region region) throws Exception {
        List<BedGraphInfo> bedGraphInfoList = Lists.newArrayList();

        int[] nReadsList = new int[cpgPosList.size()]; // 该位点的总read个数
        int[] mReadList = new int[cpgPosList.size()]; // 该位点为甲基化的read个数
        int[] cBaseList = new int[cpgPosList.size()]; // 该位点为甲基化的read中的未甲基化cpg位点个数
        int[] tBaseList = new int[cpgPosList.size()]; // 该位点的总cpg位点个数
        int[] K4plusList = new int[cpgPosList.size()]; // 长度大于等于K个位点的read个数
        int[] nDRList = new int[cpgPosList.size()]; // 长度大于等于K个位点且同时含有甲基化和未甲基化位点的read个数
        int[] nMRList = new int[cpgPosList.size()]; // 长度大于等于K个位点且含有甲基化位点的read个数
        // MHL
        int[][] methKmersList = new int[0][0]; // Number of fully methylated k-mers
        int[][] totalKmersList = new int[0][0]; // Number of fully methylated k-mers
        if (args.getMetrics().contains("MHL")) {
            methKmersList = new int[args.getMaxK() - args.getMinK() + 1][cpgPosList.size()]; // Number of fully methylated k-mers
            totalKmersList = new int[args.getMaxK() - args.getMinK() + 1][cpgPosList.size()]; // Number of fully methylated k-mers
        }
        // MBS
        Double[] mbsNumList = new Double[0]; // MBS list
        if (args.getMetrics().contains("MBS")) {
            mbsNumList = new Double[cpgPosList.size()]; // MBS list
            for (Integer i = 0; i < cpgPosList.size(); i++) {
                mbsNumList[i] = 0.0;
            }
        }
        // Entropy
        int[][] kmerList = new int[0][0];
        int[] kmerAllList = new int[0];
        if (args.getMetrics().contains("Entropy")) {
            kmerList = new int[args.getK() * args.getK()][cpgPosList.size()];
            kmerAllList = new int[cpgPosList.size()];
        }
        // R2
        int[][] N00List = new int[0][0];
        int[][] N01List = new int[0][0];
        int[][] N10List = new int[0][0];
        int[][] N11List = new int[0][0];
        if (args.getMetrics().contains("R2")) {
            N00List = new int[4][cpgPosList.size()];
            N01List = new int[4][cpgPosList.size()];
            N10List = new int[4][cpgPosList.size()];
            N11List = new int[4][cpgPosList.size()];
        }

        TabixReader tabixReader = new TabixReader(args.getMhapPath());
        TabixReader.Iterator mhapIterator = tabixReader.query(region.getChrom(), region.getStart() - 1, region.getEnd());
        List<MHapInfo> mHapInfoList = new ArrayList<>();
        String mHapLine = "";
        Integer lineCnt = 0;
        while((mHapLine = mhapIterator.next()) != null) {
            if ((args.getStrand().equals("plus") && mHapLine.split("\t")[5].equals("-")) ||
                    (args.getStrand().equals("minus") && mHapLine.split("\t")[5].equals("+"))) {
                continue;
            }

            lineCnt++;
            if (lineCnt % 100000 == 0) {
                log.info("Calculate complete " + region.getChrom() + " " + lineCnt + " mhap lines.");
            }

            MHapInfo mHapInfo = new MHapInfo(mHapLine.split("\t")[0], Integer.valueOf(mHapLine.split("\t")[1]),
                    Integer.valueOf(mHapLine.split("\t")[2]), mHapLine.split("\t")[3],
                    Integer.valueOf(mHapLine.split("\t")[4]), mHapLine.split("\t")[5]);
            mHapInfoList.add(mHapInfo);

            Integer cpgPosIndex = cpgPosList.indexOf(mHapInfo.getStart());
            String cpgStr = mHapInfo.getCpg();
            Integer cpgLen = cpgStr.length();
            Integer readCnt = mHapInfo.getCnt();

            for (int i = 0; i < cpgLen; i++) {
                if (cpgStr.charAt(i) == '1') {
                    mReadList[cpgPosIndex + i] += readCnt;
                }
                nReadsList[cpgPosIndex + i] += readCnt;
                tBaseList[cpgPosIndex + i] += cpgLen * readCnt;
                if (cpgStr.contains("1")) {
                    long noMethCnt = cpgStr.chars().filter(ch -> ch == '0').count();
                    cBaseList[cpgPosIndex + i] += noMethCnt * readCnt;
                }
            }

            if (cpgStr.length() >= args.getK()) {
                for (int i = 0; i < cpgLen; i++) {
                    K4plusList[cpgPosIndex + i] += readCnt;
                    if (cpgStr.contains("1")) {
                        nMRList[cpgPosIndex + i] += readCnt;
                        if (cpgStr.contains("0")) {
                            nDRList[cpgPosIndex + i] += readCnt;
                        }
                    }
                }
            }

            if (args.getMetrics().contains("MHL")) {
                if (args.getMinK() > cpgLen) {
                    log.error("calculate MHL Error: minK is too large.");
                    continue;
                }
                Integer maxK = args.getMaxK() > cpgLen ? cpgLen : args.getMaxK();

                int[] subMethKmersList = new int[cpgLen];
                int[] subTotalKmersList = new int[cpgLen];
                String fullMethStr = "1";
                for (int i = 0; i < cpgLen; i++) {
                    Integer noMethCnt = 0;
                    int start = 0;
                    while (cpgStr.indexOf(fullMethStr, start) >= 0 && start < cpgStr.length()) {
                        noMethCnt++;
                        start = cpgStr.indexOf(fullMethStr, start) + 1;
                    }

                    subMethKmersList[i] = noMethCnt * readCnt;
                    subTotalKmersList[i] = (cpgLen - i) * readCnt;
                    fullMethStr += "1";
                }

                for (int i = args.getMinK() - 1; i < maxK; i++) {
                    Integer row = i - args.getMinK() + 1;
                    for (int j = 0; j < cpgLen; j++) {
                        methKmersList[row][cpgPosIndex + j] += subMethKmersList[i];
                        totalKmersList[row][cpgPosIndex + j] += subTotalKmersList[i];
                    }
                }
            }

            if (args.getMetrics().contains("MBS")) {
                Double mbsNum = 0.0;
                if (cpgLen >= args.getK()) {
                    for (int i = 0; i < cpgLen; i++) {
                        String[] cpgStrList = cpgStr.split("0");
                        Double temp = 0.0;
                        for (String cpg : cpgStrList) {
                            temp += Math.pow(cpg.length(), 2);
                        }
                        mbsNum = temp / Math.pow(cpgLen, 2) * readCnt;
                        mbsNumList[cpgPosIndex + i] += mbsNum;
                    }
                }
            }

            if (args.getMetrics().contains("Entropy")) {
                if (cpgLen >= args.getK()) {
                    Map<String, Integer> kmerMap = new HashMap<>();
                    if (cpgLen >= args.getK()) {
                        for (int i = 0; i < cpgLen - args.getK() + 1; i++) {
                            String kmerStr = cpgStr.substring(i, i + args.getK());
                            if (kmerMap.containsKey(kmerStr)) {
                                kmerMap.put(kmerStr, kmerMap.get(kmerStr) + readCnt);
                            } else {
                                kmerMap.put(kmerStr, readCnt);
                            }
                        }
                    }

                    for (int i = 0; i < cpgLen; i++) {
                        Iterator<String> iterator = kmerMap.keySet().iterator();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            Double index = 0.0;
                            for (int j = 0; j < key.length(); j++) {
                                if (key.charAt(key.length() - 1 - j) == '1') {
                                    index += Math.pow(2, j);
                                }
                            }
                            kmerList[index.intValue()][cpgPosIndex + i] += kmerMap.get(key);
                            kmerAllList[cpgPosIndex + i] += kmerMap.get(key);
                        }
                    }
                }
            }

            if (args.getMetrics().contains("R2")) {
                for (int i = 0; i < cpgLen; i++) {
                    for (int j = i - 2; j < i + 3; j++) {
                        if (j < 0 || j == i || j >= cpgLen) {
                            continue;
                        }
                        Integer index = j - i > 0 ? j - i + 1 : j - i + 2;
                        if (mHapInfo.getCpg().charAt(i) == '0' && mHapInfo.getCpg().charAt(j) == '0') {
                            N00List[index][cpgPosIndex + i] += readCnt;
                        } else if (mHapInfo.getCpg().charAt(i) == '0' && mHapInfo.getCpg().charAt(j) == '1') {
                            N01List[index][cpgPosIndex + i] += readCnt;
                        } else if (mHapInfo.getCpg().charAt(i) == '1' && mHapInfo.getCpg().charAt(j) == '0') {
                            N10List[index][cpgPosIndex + i] += readCnt;
                        } else if (mHapInfo.getCpg().charAt(i) == '1' && mHapInfo.getCpg().charAt(j) == '1') {
                            N11List[index][cpgPosIndex + i] += readCnt;
                        }
                    }
                }
            }
        }
        tabixReader.close();

        List<Integer> cpgPosListInRegion = util.getCpgPosListInRegion(cpgPosList, region);
        Integer start = cpgPosList.indexOf(cpgPosListInRegion.get(0));
        for (Integer i = 0; i < cpgPosListInRegion.size(); i++) {
            Integer nReads = nReadsList[start + i];
            Integer mRead = mReadList[start + i];
            Integer cBase = cBaseList[start + i];
            Integer tBase = tBaseList[start + i];
            Integer K4plus = K4plusList[start + i];
            Integer nDR = nDRList[start + i];
            Integer nMR = nMRList[start + i];

            if (args.getMetrics().contains("MM") && nReads < args.getCpgCov()) {
                continue;
            }
            if (args.getMetrics().contains("PDR") || args.getMetrics().contains("CHALM") || args.getMetrics().contains("MHL") ||
                    args.getMetrics().contains("MCR") || args.getMetrics().contains("MBS") || args.getMetrics().contains("Entropy")) {
                if (K4plus < args.getK4Plus()) {
                    continue;
                }
            }

            BedGraphInfo bedGraphInfo = new BedGraphInfo();
            bedGraphInfo.setChrom(region.getChrom());
            bedGraphInfo.setStart(cpgPosListInRegion.get(i) - 1);
            bedGraphInfo.setEnd(cpgPosListInRegion.get(i));
            if (args.getMetrics().contains("MM")) {
                Double MM = mRead.doubleValue() / nReads.doubleValue();
                if (MM.isNaN() || MM.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setMM(MM.floatValue());
            }
            if (args.getMetrics().contains("PDR")) {
                Double PDR = nDR.doubleValue() / K4plus.doubleValue();
                if (PDR.isNaN() || PDR.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setPDR(PDR.floatValue());
            }
            if (args.getMetrics().contains("CHALM")) {
                Double CHALM = nMR.doubleValue() / K4plus.doubleValue();
                if (CHALM.isNaN() || CHALM.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setCHALM(CHALM.floatValue());
            }
            if (args.getMetrics().contains("MHL")) {
                Double temp = 0.0;
                Integer w = 0;
                for (int j = args.getMinK() - 1; j < args.getMaxK(); j++) {
                    Integer row = j - args.getMinK() + 1;
                    Integer methKmers = methKmersList[row][start + i];
                    Integer totalKmers = totalKmersList[row][start + i];
                    temp += methKmers.doubleValue() / totalKmers.doubleValue() * (j + 1);
                    w += (j + 1);
                }
                Double MHL = temp / w;
                if (MHL.isNaN() || MHL.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setMHL(MHL.floatValue());
            }
            if (args.getMetrics().contains("MCR")) {
                Double MCR = cBase.doubleValue() / tBase.doubleValue();
                if (MCR.isNaN() || MCR.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setMCR(MCR.floatValue());
            }
            if (args.getMetrics().contains("MBS")) {
                Double mbsNum = mbsNumList[start + i];
                Double MBS = mbsNum / K4plus;
                if (MBS.isNaN() || MBS.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setMBS(MBS.floatValue());
            }
            if (args.getMetrics().contains("Entropy")) {
                Integer kmerAll = kmerAllList[start + i];
                Double temp = 0.0;
                for (int j = 0; j < args.getK() * args.getK(); j++) {
                    if (kmerList[j][i] > 0) {
                        Integer cnt = kmerList[j][start + i];
                        temp += cnt.doubleValue() / kmerAll.doubleValue() * Math.log(cnt.doubleValue() / kmerAll.doubleValue()) / Math.log(2);
                    }
                }

                Double entropy = - 1 / args.getK().doubleValue() * temp;
                if (entropy.isNaN() || entropy.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setEntropy(entropy.floatValue());
            }
            if (args.getMetrics().contains("R2")) {
                Double r2Sum = 0.0;
                Double r2Num = 0.0;
                for (int j = i - 2; j < i + 3; j++) {
                    if (j < 0 || j == i || j >= cpgPosListInRegion.size()) {
                        continue;
                    }
                    Integer index = j - i > 0 ? j - i + 1 : j - i + 2;
                    Integer N00 = N00List[index][start + i];
                    Integer N01 = N01List[index][start + i];
                    Integer N10 = N10List[index][start + i];
                    Integer N11 = N11List[index][start + i];

                    if ((N00 + N01 + N10 + N11) < args.getR2Cov()) {
                        continue;
                    }

                    // 计算r2
                    Double r2 = 0.0;
                    Double pvalue = 0.0;
                    Double N = N00 + N01 + N10 + N11 + 0.0;
                    if(N == 0) {
                        r2 = Double.NaN;
                        pvalue = Double.NaN;
                    }
                    Double PA = (N10 + N11) / N;
                    Double PB = (N01 + N11) / N;
                    Double D = N11 / N - PA * PB;
                    Double Num = D * D;
                    Double Den = PA * (1 - PA) * PB * (1 - PB);
                    if (Den == 0.0) {
                        r2 = Double.NaN;
                    } else {
                        r2 = Num / Den;
                        if (D < 0) {
                            r2 = -1 * r2;
                        }
                    }

                    if (!r2.isNaN()) {
                        r2Num++;
                        r2Sum += r2;
                    }
                }

                Double meanR2 = r2Sum / r2Num;
                if (meanR2.isNaN() || meanR2.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setR2(meanR2.floatValue());
            }
            bedGraphInfoList.add(bedGraphInfo);
        }

        return bedGraphInfoList;
    }
}