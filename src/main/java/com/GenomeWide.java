package com;

import com.args.GenomeWideArgs;
import com.bean.*;
import com.common.Util;
import htsjdk.samtools.util.StringUtil;
import htsjdk.tribble.readers.TabixReader;
import org.apache.commons.compress.utils.Lists;
import org.jfree.chart.util.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static java.lang.Thread.sleep;

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

        BufferedWriter bufferedWriterMM = util.createOutputFile(args.getOutputDir(), args.getTag() + ".MM.bedGraph");
        BufferedWriter bufferedWriterPDR = util.createOutputFile(args.getOutputDir(), args.getTag() + ".PDR.bedGraph");
        BufferedWriter bufferedWriterCHALM = util.createOutputFile(args.getOutputDir(), args.getTag() + ".CHALM.bedGraph");
        BufferedWriter bufferedWriterMHL = util.createOutputFile(args.getOutputDir(), args.getTag() + ".MHL.bedGraph");
        BufferedWriter bufferedWriterMCR = util.createOutputFile(args.getOutputDir(), args.getTag() + ".MCR.bedGraph");
        BufferedWriter bufferedWriterMBS = util.createOutputFile(args.getOutputDir(), args.getTag() + ".MBS.bedGraph");
        BufferedWriter bufferedWriterEntropy = util.createOutputFile(args.getOutputDir(), args.getTag() + ".Entropy.bedGraph");
        BufferedWriter bufferedWriterR2 = util.createOutputFile(args.getOutputDir(), args.getTag() + ".R2.bedGraph");

//        if (args.getMetrics().contains("MM")) {
//            bufferedWriterMM = util.createOutputFile(args.getOutputDir(), args.getTag() + ".MM.bedGraph");
//        }
//        if (args.getMetrics().contains("PDR")) {
//            bufferedWriterPDR = util.createOutputFile(args.getOutputDir(), args.getTag() + ".PDR.bedGraph");
//        }
//        if (args.getMetrics().contains("CHALM")) {
//            bufferedWriterCHALM = util.createOutputFile(args.getOutputDir(), args.getTag() + ".CHALM.bedGraph");
//        }
//        if (args.getMetrics().contains("MHL")) {
//            bufferedWriterMHL = util.createOutputFile(args.getOutputDir(), args.getTag() + ".MHL.bedGraph");
//        }
//        if (args.getMetrics().contains("MCR")) {
//            bufferedWriterMCR = util.createOutputFile(args.getOutputDir(), args.getTag() + ".MCR.bedGraph");
//        }
//        if (args.getMetrics().contains("MBS")) {
//            bufferedWriterMBS = util.createOutputFile(args.getOutputDir(), args.getTag() + ".MBS.bedGraph");
//        }
//        if (args.getMetrics().contains("Entropy")) {
//            bufferedWriterEntropy = util.createOutputFile(args.getOutputDir(), args.getTag() + ".Entropy.bedGraph");
//        }
//        if (args.getMetrics().contains("R2")) {
//            bufferedWriterR2 = util.createOutputFile(args.getOutputDir(), args.getTag() + ".R2.bedGraph");
//        }

        if (args.getRegion() != null && !args.getRegion().equals("")) {
            Region region = util.parseRegion(args.getRegion());
            List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, 500);

            List<BedGraphInfo> calculateResult = calculate1(cpgPosList, region);
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

            bufferedWriterMM.close();
            bufferedWriterPDR.close();
            bufferedWriterCHALM.close();
            bufferedWriterMHL.close();
            bufferedWriterMCR.close();
            bufferedWriterMBS.close();
            bufferedWriterEntropy.close();
            bufferedWriterR2.close();
        } else if (args.getBedPath() != null && !args.getBedPath().equals("")) {
            // get region list from bed file
            List<Region> regionList = util.getBedRegionList(args.getBedPath());

            for (Region region : regionList) {
                List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, 500);
                List<BedGraphInfo> calculateResult = calculate1(cpgPosList, region);
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
            }

            bufferedWriterMM.close();
            bufferedWriterPDR.close();
            bufferedWriterCHALM.close();
            bufferedWriterMHL.close();
            bufferedWriterMCR.close();
            bufferedWriterMBS.close();
            bufferedWriterEntropy.close();
            bufferedWriterR2.close();
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

            // 线程数
            int threadNum = cpgPosListMapList.size();
            // 计数器
            CountDownLatch countDownLatch = new CountDownLatch(threadNum);
            // 创建一个线程池
            ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
            // 定义一个任务集合
            List<Callable<Boolean>> tasks = Lists.newArrayList();
            // 定义一个任务
            Callable<Boolean> task;

            for (Map.Entry<String, List<Integer>> cpgPosListMap : cpgPosListMapList) {
                List<Integer> cpgPosList = cpgPosListMap.getValue();
                // get the whole region of this chrom
                Region region = new Region();
                region.setChrom(cpgPosListMap.getKey());
                region.setStart(cpgPosList.get(0));
                region.setEnd(cpgPosList.get(cpgPosList.size() - 1));

//                            boolean calculateResult = calculateMultiThread(metric, cpgPosListInRegion, region, bufferedWriter, 100000);
//                            if (!calculateResult) {
//                                log.error("calculate fail, please check the command.");
//                                return false;
//                            }

                task = new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        log.info("Calculate " + cpgPosListMap.getKey() + " start!");
                        List<BedGraphInfo> calculateResult = calculate1(cpgPosList, region);
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
                        log.info("Calculate " + cpgPosListMap.getKey() + " end!");
                        return true;
                    }
                };

                bufferedWriterMM.close();
                bufferedWriterPDR.close();
                bufferedWriterCHALM.close();
                bufferedWriterMHL.close();
                bufferedWriterMCR.close();
                bufferedWriterMBS.close();
                bufferedWriterEntropy.close();
                bufferedWriterR2.close();
                // 减少计数器的计数，如果计数达到零，则释放所有等待线程。
                // 如果当前计数大于零，则递减。如果新计数为零，则重新启用所有等待线程以进行线程调度。
                countDownLatch.countDown();
                // 任务处理完加入集合
                tasks.add(task);

            }

            try {
                // 执行给定的任务，返回一个 Futures 列表，在所有完成时保存它们的状态和结果。
                // Future.isDone对于返回列表的每个元素都是true 。
                // 请注意，已完成的任务可能已经正常终止，也可能通过引发异常终止。
                // 如果在此操作进行时修改了给定的集合，则此方法的结果是不确定的
                executorService.invokeAll(tasks);
                // 等待计数器归零
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 关闭线程池
            executorService.shutdown();
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

    private boolean calculateMultiThread(String metric, List<Integer> cpgPosListInRegion, Region region, BufferedWriter bufferedWriter, Integer threadSize) {
        // 返回的数据
        List<BedGraphInfo> bedGraphInfoList = Lists.newArrayList();
        // 总数据条数
        Integer dataSize = cpgPosListInRegion.size();
        // 线程数
        Integer threadNum = dataSize / threadSize + 1;
        // 计数器
        CountDownLatch countDownLatch = new CountDownLatch(threadNum);
        // 创建一个线程池
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        // 定义标记,过滤threadNum为整数
        boolean special = dataSize % threadSize == 0;
        // 定义一个任务集合
        List<Callable<Boolean>> tasks = Lists.newArrayList();
        // 定义一个任务
        Callable<Boolean> task;
        // 定义循环处理的i批次数据
        List<Integer> loopDataList;
        // 确定每条线程的数据
        for (int i = 0; i < threadNum; i++) {
            if (i == threadNum - 1) {
                if (special) {
                    countDownLatch.countDown();
                    break;
                }
                loopDataList = cpgPosListInRegion.subList(threadSize * i, dataSize);
            } else {
                loopDataList = cpgPosListInRegion.subList(threadSize * i, threadSize * (i + 1));
            }
            // 当前循环处理的数据集
            List<Integer> cpgPosListLoop = loopDataList;
            task = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
//                    if (cpgPosListLoop.size() > 0) {
//                        List<BedGraphInfo> calculateResult = calculate1(metric, cpgPosListLoop, region);
//                        if (calculateResult.size() > 0) {
//                            for (BedGraphInfo bedGraphInfo : calculateResult) {
//                                String line = bedGraphInfo.getChrom() + "\t" + bedGraphInfo.getStart() + "\t" + bedGraphInfo.getEnd() + "\t" + bedGraphInfo.getValue() + "\n";
//                                bufferedWriter.write(line);
//                            }
//                        }
//                    }
                    return true;
                }
            };
            // 减少计数器的计数，如果计数达到零，则释放所有等待线程。
            // 如果当前计数大于零，则递减。如果新计数为零，则重新启用所有等待线程以进行线程调度。
            countDownLatch.countDown();
            // 任务处理完加入集合
            tasks.add(task);
        }

        try {
            // 执行给定的任务，返回一个 Futures 列表，在所有完成时保存它们的状态和结果。
            // Future.isDone对于返回列表的每个元素都是true 。
            // 请注意，已完成的任务可能已经正常终止，也可能通过引发异常终止。
            // 如果在此操作进行时修改了给定的集合，则此方法的结果是不确定的
            executorService.invokeAll(tasks);
            // 等待计数器归零
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 关闭线程池
        executorService.shutdown();
        //return bedGraphInfoList;
        return true;
    }

    private List<BedGraphInfo> calculate1(List<Integer> cpgPosList, Region region) throws Exception {
        List<BedGraphInfo> bedGraphInfoList = Lists.newArrayList();

        int[] nReadsList = new int[cpgPosList.size()]; // 该位点的总read个数
        int[] mReadList = new int[cpgPosList.size()]; // 该位点为甲基化的read个数
        int[] cBaseList = new int[cpgPosList.size()]; // 该位点为甲基化的read中的未甲基化cpg位点个数
        int[] tBaseList = new int[cpgPosList.size()]; // 该位点的总cpg位点个数
        int[] K4plusList = new int[cpgPosList.size()]; // 长度大于等于K个位点的read个数
        int[] nDRList = new int[cpgPosList.size()]; // 长度大于等于K个位点且同时含有甲基化和未甲基化位点的read个数
        int[] nMRList = new int[cpgPosList.size()]; // 长度大于等于K个位点且含有甲基化位点的read个数
        int[] minReadList = new int[cpgPosList.size()]; // 最小cpg长度
        for (Integer i = 0; i < cpgPosList.size(); i++) {
            minReadList[i] = Integer.MAX_VALUE;
        }
        int[] maxReadList = new int[cpgPosList.size()]; // 最大cpg长度
        int[][] methKmersList = new int[100][cpgPosList.size()]; // Number of fully methylated k-mers
        int[][] totalKmersList = new int[100][cpgPosList.size()]; // Number of fully methylated k-mers

        TabixReader tabixReader = new TabixReader(args.getMhapPath());
        TabixReader.Iterator mhapIterator = tabixReader.query(region.getChrom(), region.getStart() - 1, region.getEnd());
        List<MHapInfo> mHapInfoList = new ArrayList<>();
        String mHapLine = "";
        Integer lineCnt = 0;
        while((mHapLine = mhapIterator.next()) != null) {
            lineCnt++;
            if (lineCnt % 100000 == 0) {
                log.info("Calculate complete " + region.getChrom() + " " + lineCnt + " mhap lines.");
            }
            if ((args.getStrand().equals("plus") && mHapLine.split("\t")[5].equals("-")) ||
                    (args.getStrand().equals("minus") && mHapLine.split("\t")[5].equals("+"))) {
                continue;
            }
            MHapInfo mHapInfo = new MHapInfo(mHapLine.split("\t")[0], Integer.valueOf(mHapLine.split("\t")[1]),
                    Integer.valueOf(mHapLine.split("\t")[2]), mHapLine.split("\t")[3],
                    Integer.valueOf(mHapLine.split("\t")[4]), mHapLine.split("\t")[5]);
            mHapInfoList.add(mHapInfo);

            Integer cpgPosIndex = cpgPosList.indexOf(mHapInfo.getStart());
            String cpgStr = mHapInfo.getCpg();
            Integer cpgLen = cpgStr.length();
            Integer readCnt = mHapInfo.getCnt();

            if (args.getMetrics().contains("PDR") || args.getMetrics().contains("CHALM") || args.getMetrics().contains("MHL") ||
                    args.getMetrics().contains("MCR") || args.getMetrics().contains("MBS") || args.getMetrics().contains("Entropy")) {
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
            }

            if (args.getMetrics().contains("MHL")) {
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
                for (int i = 0; i < cpgLen; i++) {
                    for (int j = 0; j < cpgLen; j++) {
                        methKmersList[i][cpgPosIndex + j] += subMethKmersList[i];
                        totalKmersList[i][cpgPosIndex + j] += subTotalKmersList[i];
                    }
                    if (cpgLen < minReadList[cpgPosIndex + i]) {
                        minReadList[cpgPosIndex + i] = cpgLen;
                    }
                    if (cpgLen > maxReadList[cpgPosIndex + i]) {
                        maxReadList[cpgPosIndex + i] = cpgLen;
                    }
                }
            }
        }
        tabixReader.close();

        List<Integer> cpgPosListInRegion = util.getCpgPosListInRegion(cpgPosList, region);
        for (Integer i = 0; i < cpgPosListInRegion.size(); i++) {
            Integer nReads = nReadsList[i];
            Integer mBase = mReadList[i];
            Integer cBase = cBaseList[i];
            Integer tBase = tBaseList[i];
            Integer K4plus = K4plusList[i];
            Integer nDR = nDRList[i];
            Integer nMR = nMRList[i];
            Integer minRead = minReadList[i];
            Integer maxRead = maxReadList[i];

            if (args.getMetrics().contains("MM") && nReads < args.getCpgCov()) {
                continue;
            }
            if (args.getMetrics().contains("PDR") || args.getMetrics().contains("CHALM") || args.getMetrics().contains("MHL") ||
                    args.getMetrics().contains("MCR") || args.getMetrics().contains("MBS") || args.getMetrics().contains("Entropy")) {
                if (K4plus < args.getK4Plus()) {
                    continue;
                }
            }
            if (args.getMetrics().contains("MHL")) {
                if (args.getMinK() > minRead) {
                    log.error("calculate MHL Error: minK is too large.");
                    continue;
                }
                if (args.getMaxK() > maxRead) {
                    args.setMaxK(maxRead);
                }
            }

            BedGraphInfo bedGraphInfo = new BedGraphInfo();
            bedGraphInfo.setChrom(region.getChrom());
            bedGraphInfo.setStart(cpgPosList.get(i) - 1);
            bedGraphInfo.setEnd(cpgPosList.get(i));
            if (args.getMetrics().contains("MM")) {
                Double MM = mBase.doubleValue() / nReads.doubleValue();
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
                    Integer methKmers = methKmersList[j][i];
                    Integer totalKmers = totalKmersList[j][i];
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

            }
            if (args.getMetrics().contains("Entropy")) {

            }
            if (args.getMetrics().contains("R2")) {

            }
            bedGraphInfoList.add(bedGraphInfo);
        }

        return bedGraphInfoList;
    }

    private List<BedGraphInfo> calculate2(List<Integer> cpgPosList, Region region) throws Exception {
        List<BedGraphInfo> bedGraphInfoList = Lists.newArrayList();
        
        List<Integer> cpgPosListInRegion = util.getCpgPosListInRegion(cpgPosList, region);
        Integer getCnt = 0;
        for (Integer cpgPos : cpgPosListInRegion) {
            getCnt++;
            if (getCnt % 10000 == 0) {
                log.info("calculate complete " + region.getChrom() + " " + getCnt + " positions.");
            }

            Region thisSiteRegion = new Region();
            thisSiteRegion.setChrom(region.getChrom());
            thisSiteRegion.setStart(cpgPos);
            thisSiteRegion.setEnd(cpgPos);
            // parse the mhap file
            List<MHapInfo> mHapInfoListWithSite = util.parseMhapFile(args.getMhapPath(), thisSiteRegion, args.getStrand(), true);
            if (mHapInfoListWithSite.size() < 1) {
                continue;
            }

            // calculate some base calue
            Integer nReads = 0; // 总read个数
            Integer mBase = 0; // 甲基化位点个数
            Integer cBase = 0; // 存在甲基化的read中的未甲基化位点个数
            Integer tBase = 0; // 总位点个数
            Integer K4plus = 0; // 长度大于等于K个位点的read个数
            Integer nDR = 0; // 长度大于等于K个位点且同时含有甲基化和未甲基化位点的read个数
            Integer nMR = 0; // 长度大于等于K个位点且含有甲基化位点的read个数

            for(MHapInfo mHapInfo : mHapInfoListWithSite) {
                if (args.getMetrics().contains("MM") || args.getMetrics().contains("PDR") || args.getMetrics().contains("CHALM") || args.getMetrics().contains("MHL") ||
                        args.getMetrics().contains("MCR") || args.getMetrics().contains("MBS") || args.getMetrics().contains("Entropy")) {
                    String cpg = mHapInfo.getCpg();
                    Integer cnt = mHapInfo.getCnt();
                    nReads += cnt;
                    tBase += cpg.length() * cnt;

                    // 计算该位点的MM
                    Integer pos = cpgPosList.indexOf(cpgPos) - cpgPosList.indexOf(mHapInfo.getStart());
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

            if (args.getMetrics().contains("MM")) {
                log.info("cpgPos:" + cpgPos + " | nReads:" + nReads + " mBase:" + mBase + " cBase:" + cBase + " tBase:" + tBase + " K4plus:" + K4plus + " nDR:" + nDR + " nMR:" + nMR);
            }

            BedGraphInfo bedGraphInfo = new BedGraphInfo();
            bedGraphInfo.setChrom(region.getChrom());
            bedGraphInfo.setStart(cpgPos - 1);
            bedGraphInfo.setEnd(cpgPos);
            if (args.getMetrics().contains("MM")) {
                Double MM = mBase.doubleValue() / nReads.doubleValue();
                if (MM.isNaN() || MM.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setMM(MM.floatValue());
            } else if (args.getMetrics().contains("PDR")) {
                Double PDR = nDR.doubleValue() / K4plus.doubleValue();
                if (PDR.isNaN() || PDR.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setPDR(PDR.floatValue());
            } else if (args.getMetrics().contains("CHALM")) {
                Double CHALM = nMR.doubleValue() / K4plus.doubleValue();
                if (CHALM.isNaN() || CHALM.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setCHALM(CHALM.floatValue());
            } else if (args.getMetrics().contains("MHL")) {
                Double MHL = util.calculateMHL(mHapInfoListWithSite, args.getMinK(), args.getMaxK());
                if (MHL.isNaN() || MHL.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setMHL(MHL.floatValue());
            } else if (args.getMetrics().contains("MCR")) {
                Double MCR = cBase.doubleValue() / tBase.doubleValue();
                if (MCR.isNaN() || MCR.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setMCR(MCR.floatValue());
            } else if (args.getMetrics().contains("MBS")) {
                Double MBS = util.calculateMBS(mHapInfoListWithSite, args.getK());
                if (MBS.isNaN() || MBS.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setMBS(MBS.floatValue());
            } else if (args.getMetrics().contains("Entropy")) {
                Double Entropy = util.calculateEntropy(mHapInfoListWithSite, args.getK());
                if (Entropy.isNaN() || Entropy.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setEntropy(Entropy.floatValue());
            } else if (args.getMetrics().contains("R2")) {
                Double R2 = calculateR2(mHapInfoListWithSite, cpgPosListInRegion, cpgPos);
                if (R2.isNaN() || R2.isInfinite()) {
                    continue;
                }
                bedGraphInfo.setR2(R2.floatValue());
            }

            bedGraphInfoList.add(bedGraphInfo);
        }

        return bedGraphInfoList;
    }

    public Double calculateR2(List<MHapInfo> mHapInfoList, List<Integer> cpgPosList, Integer cpgPos) {
        Double r2Sum = 0.0;
        Integer r2Num = 0;
        Integer pos = cpgPosList.indexOf(cpgPos);
        for (int j = pos - 2; j < pos + 3; j++) {
            if (j < 0 || j == pos || j >= cpgPosList.size()) {
                continue;
            }

            R2Info r2Info = util.getR2FromList(mHapInfoList, cpgPosList, cpgPos, cpgPosList.get(j), args.getR2Cov());
            if (r2Info != null && !r2Info.getR2().isNaN()) {
                r2Num++;
                r2Sum += r2Info.getR2();
            }
        }

        return r2Sum / r2Num;
    }
}
