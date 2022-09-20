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
            File bedFile = new File(args.getBedPath());
            BufferedReader bufferedReader = new BufferedReader(new FileReader(bedFile));
            String bedLine = "";
            while ((bedLine = bufferedReader.readLine()) != null && !bedLine.equals("")) {
                Region region = new Region();
                if (bedLine.split("\t").length < 3) {
                    log.error("Interval not in correct format.");
                    break;
                }
                region.setChrom(bedLine.split("\t")[0]);
                region.setStart(Integer.valueOf(bedLine.split("\t")[1]) + 1);
                region.setEnd(Integer.valueOf(bedLine.split("\t")[2]));
                regionList.add(region);
            }
        }

        BufferedWriter bufferedWriter = util.createOutputFile("", args.getOutputFile());

        // get the metric list
        String[] metrics = args.getMetrics().split(" ");
        for (Region region : regionList) {
            List<String> metricsList = new ArrayList<>();
            for (String metric : metrics) {
                metricsList.add(metric);
            }

            // parse the mhap file
            List<MHapInfo> mHapInfoList = util.parseMhapFile(args.getMhapPath(), region, args.getStrand(), false);
            List<MHapInfo> mHapInfoListMerged = util.parseMhapFile(args.getMhapPath(), region, args.getStrand(), true);

            // parse the cpg file
            List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, 500);

            bufferedWriter.write(printHead(metricsList));
            boolean getStatResult = getStat(mHapInfoList, mHapInfoListMerged, cpgPosList, region, metricsList, bufferedWriter);
            if (!getStatResult) {
                log.error("getStat fail, please check the command.");
                return;
            }
        }
        bufferedWriter.close();

        log.info("Stat end!");
    }

    private boolean checkArgs() {

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

    private boolean getStat(List<MHapInfo> mHapInfoList, List<MHapInfo> mHapInfoListMerged, List<Integer> cpgPosList, Region region,
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
        Double[] nPairsAndR2 = calculateNPairsAndR2(mHapInfoList, cpgPosList, cpgPosListInRegion);
        statInfo.setnPairs(nPairsAndR2[0].intValue());
        for (int i = 0; i < metricsList.size(); i++) {
            if (metricsList.get(i).equals("MM")) {
                statInfo.setMM(mBase.doubleValue() / tBase.doubleValue());
            } else if (metricsList.get(i).equals("CHALM")) {
                statInfo.setCHALM(nMR.doubleValue() / K4plus.doubleValue());
            } else if (metricsList.get(i).equals("PDR")) {
                statInfo.setPDR(nDR.doubleValue() / K4plus.doubleValue());
            } else if (metricsList.get(i).equals("MHL")) {
                statInfo.setMHL(calculateMHL(mHapInfoListMerged));
            } else if (metricsList.get(i).equals("MBS")) {
                statInfo.setMBS(calculateMBS(mHapInfoListMerged));
            } else if (metricsList.get(i).equals("MCR")) {
                statInfo.setMCR(cBase.doubleValue() / tBase.doubleValue());
            } else if (metricsList.get(i).equals("Entropy")) {
                statInfo.setEntropy(calculateEntropy(mHapInfoListMerged));
            } else if (metricsList.get(i).equals("R2")) {
                statInfo.setR2(nPairsAndR2[1]);
            }
        }

        bufferedWriter.write(statInfo.print(metricsList));

        return true;
    }

    public Double calculateMHL(List<MHapInfo> mHapInfoListMerged) {
        Double MHL = 0.0;
        Integer maxCpgLength = 0;
        for (int i = 0; i < mHapInfoListMerged.size(); i++) {
            MHapInfo mHapInfo = mHapInfoListMerged.get(i);
            if (args.getMinK() > mHapInfo.getCpg().length()) {
                log.error("calculate MHL Error: startK is too large.");
                return 0.0;
            }
            if (maxCpgLength < mHapInfo.getCpg().length()) {
                maxCpgLength = mHapInfo.getCpg().length();
            }
        }
        if (args.getMaxK() > maxCpgLength) {
            args.setMaxK(maxCpgLength);
        }


        Double temp = 0.0;
        Integer w = 0;
        String fullMethStr = "";
        for (int i = 0; i < args.getMinK(); i++) {
            fullMethStr += "1";
        }
        for (Integer kmer = args.getMinK(); kmer < args.getMaxK() + 1; kmer++) {
            Map<String, Integer> kmerMap = new HashMap<>();
            Integer kmerNum = 0;
            Integer mKmerNum = 0;
            w += kmer;
            for (int j = 0; j < mHapInfoListMerged.size(); j++) {
                MHapInfo mHapInfo = mHapInfoListMerged.get(j);
                if (mHapInfo.getCpg().length() >= kmer) {
                    for (int k = 0; k < mHapInfo.getCpg().length() - kmer + 1; k++) {
                        String kmerStr = mHapInfo.getCpg().substring(k, k + kmer);
                        if (kmerMap.containsKey(kmerStr)) {
                            kmerMap.put(kmerStr, kmerMap.get(kmerStr) + mHapInfo.getCnt());
                        } else {
                            kmerMap.put(kmerStr, mHapInfo.getCnt());
                        }
                    }
                }
            }

            Iterator<String> iterator = kmerMap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                kmerNum += kmerMap.get(key);
                if (key.substring(0, kmer).equals(fullMethStr)) {
                    mKmerNum += kmerMap.get(key);
                }
            }

            fullMethStr += "1";
            temp += kmer.doubleValue() * mKmerNum.doubleValue() / kmerNum.doubleValue();
        }
        MHL = temp / w;

        return MHL;
    }

    public Double calculateMBS(List<MHapInfo> mHapInfoListMerged) {
        Double MBS = 0.0;
        Integer kmerNum = 0;
        Double temp1 = 0.0;
        for (int i = 0; i < mHapInfoListMerged.size(); i++) {
            MHapInfo mHapInfo = mHapInfoListMerged.get(i);
            if (mHapInfo.getCpg().length() >= args.getK()) {
                String[] cpgStrList = mHapInfo.getCpg().split("0");
                Double temp2 = 0.0;
                for (String cpg : cpgStrList) {
                    temp2 += Math.pow(cpg.length(), 2);
                }
                temp1 += temp2 / Math.pow(mHapInfo.getCpg().length(), 2) * mHapInfo.getCnt();
                kmerNum += mHapInfo.getCnt();
            }
        }
        MBS = temp1 / kmerNum.doubleValue();

        return MBS;
    }

    public Double calculateEntropy(List<MHapInfo> mHapInfoListMerged) {
        Double Entropy = 0.0;
        Map<String, Integer> kmerMap = new HashMap<>();
        Integer kmerAll = 0;
        for (int i = 0; i < mHapInfoListMerged.size(); i++) {
            MHapInfo mHapInfo = mHapInfoListMerged.get(i);
            if (mHapInfo.getCpg().length() >= args.getK()) {
                for (int j = 0; j < mHapInfo.getCpg().length() - args.getK() + 1; j++) {
                    kmerAll += mHapInfo.getCnt();
                    String kmerStr = mHapInfo.getCpg().substring(j, j + args.getK());
                    if (kmerMap.containsKey(kmerStr)) {
                        kmerMap.put(kmerStr, kmerMap.get(kmerStr) + mHapInfo.getCnt());
                    } else {
                        kmerMap.put(kmerStr, mHapInfo.getCnt());
                    }
                }
            }
        }

        Iterator<String> iterator = kmerMap.keySet().iterator();
        Double temp = 0.0;
        while (iterator.hasNext()) {
            Integer cnt = kmerMap.get(iterator.next());
            temp += cnt.doubleValue() / kmerAll.doubleValue() * Math.log(cnt.doubleValue() / kmerAll.doubleValue()) / Math.log(2);
        }
        Entropy = - 1 / args.getK().doubleValue() * temp;

        return Entropy;
    }

    public Double[] calculateNPairsAndR2(List<MHapInfo> mHapInfoList, List<Integer> cpgPosList, List<Integer> cpgPosListInRegion) {
        Double[] nPairsAndR2 = new Double[2];

        // get cpg status matrix in region
        Integer[][] cpgHpMatInRegion = util.getCpgHpMat(mHapInfoList, cpgPosList, cpgPosListInRegion);

        List<Integer> posList = new ArrayList<>(); // the index list of matrix which cpg site coverage count greater than cutoff
        for (int i = 0; i < cpgPosListInRegion.size(); i++) {
            Integer coverCnt = 0;
            for (int j = 0; j < cpgHpMatInRegion.length; j++) {
                if (cpgHpMatInRegion[j][i] != null) {
                    coverCnt++;
                }
            }
            if (coverCnt > args.getCutOff()) {
                posList.add(i);
            }
        }

        Double nPairs = 0.0;
        Double R2 = 0.0;
        for (int i = 0; i < posList.size(); i++) {
            for (int j = i + 1; j < posList.size(); j++) {
                R2Info r2Info = util.getR2Info(cpgHpMatInRegion, posList.get(i), posList.get(j));
                nPairs++;
                R2 += r2Info.getR2();
            }
        }

        nPairsAndR2[0] = nPairs;
        nPairsAndR2[1] = R2 / nPairs;
        return nPairsAndR2;
    }
}
