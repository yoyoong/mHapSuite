package com;

import com.args.GenomeWideArgs;
import com.bean.*;
import com.common.Util;
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

        // get the metric list
        String[] metrics = args.getMetrics().trim().split(" ");

        // 线程数
        int threadNum = metrics.length;
        // 计数器
        CountDownLatch countDownLatch = new CountDownLatch(threadNum);
        // 创建一个线程池
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        // 定义一个任务集合
        List<Callable<Boolean>> tasks = Lists.newArrayList();
        // 定义一个任务
        Callable<Boolean> task;

        if (args.getRegion() != null && !args.getRegion().equals("")) {
            for (String metric : metrics) {
                task = new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        log.info("calculate " + metric + " begin!");
                        BufferedWriter bufferedWriter = util.createOutputFile(args.getOutputDir(), args.getTag() + "." + metric + ".bedGraph");
                        Region region = util.parseRegion(args.getRegion());
                        // parse cpg file in region
                        List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, 500);
                        List<Integer> cpgPosListInRegion = util.getCpgPosListInRegion(cpgPosList, region);

                        List<BedGraphInfo> calculateResult = calculateOneThread(metric, cpgPosListInRegion, region);
                        if (calculateResult.size() > 0) {
                            for (BedGraphInfo bedGraphInfo : calculateResult) {
                                String line = bedGraphInfo.getChrom() + "\t" + bedGraphInfo.getStart() + "\t" + bedGraphInfo.getEnd() + "\t" + bedGraphInfo.getValue() + "\n";
                                bufferedWriter.write(line);
                            }
                        }
                        bufferedWriter.close();
                        log.info("calculate " + metric + " succeed!");

                        return true;
                    }
                };
                // 减少计数器的计数，如果计数达到零，则释放所有等待线程。
                // 如果当前计数大于零，则递减。如果新计数为零，则重新启用所有等待线程以进行线程调度。
                countDownLatch.countDown();
                // 任务处理完加入集合
                tasks.add(task);
            }
        } else if (args.getBedPath() != null && !args.getBedPath().equals("")) {
            // get region list from bed file
            List<Region> regionList = util.getBedRegionList(args.getBedPath());

            for (String metric : metrics) {
                task = new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        log.info("calculate " + metric + " begin!");
                        BufferedWriter bufferedWriter = util.createOutputFile(args.getOutputDir(), args.getTag() + "." + metric + ".bedGraph");
                        for (Region region : regionList) {
                            // parse cpg file in region
                            List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, 500);
                            List<Integer> cpgPosListInRegion = util.getCpgPosListInRegion(cpgPosList, region);

                            List<BedGraphInfo> calculateResult = calculateOneThread(metric, cpgPosListInRegion, region);
                            if (calculateResult.size() > 0) {
                                for (BedGraphInfo bedGraphInfo : calculateResult) {
                                    String line = bedGraphInfo.getChrom() + "\t" + bedGraphInfo.getStart() + "\t" + bedGraphInfo.getEnd() + "\t" + bedGraphInfo.getValue() + "\n";
                                    bufferedWriter.write(line);
                                }
                            }
                        }
                        bufferedWriter.close();
                        log.info("calculate " + metric + " succeed!");

                        return true;
                    }
                };
                // 减少计数器的计数，如果计数达到零，则释放所有等待线程。
                // 如果当前计数大于零，则递减。如果新计数为零，则重新启用所有等待线程以进行线程调度。
                countDownLatch.countDown();
                // 任务处理完加入集合
                tasks.add(task);
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

            for (String metric : metrics) {
                BufferedWriter bufferedWriter = util.createOutputFile(args.getOutputDir(), args.getTag() + "." + metric + ".bedGraph");
                task = new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        log.info("calculate " + metric + " begin!");

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
                            List<Integer> cpgPosListInRegion = cpgPosListMap.getValue();
                            // get the whole region of this chrom
                            Region region = new Region();
                            region.setChrom(cpgPosListMap.getKey());
                            region.setStart(cpgPosListInRegion.get(0));
                            region.setEnd(cpgPosListInRegion.get(cpgPosListInRegion.size() - 1));

//                            boolean calculateResult = calculateMultiThread(metric, cpgPosListInRegion, region, bufferedWriter, 100000);
//                            if (!calculateResult) {
//                                log.error("calculate fail, please check the command.");
//                                return false;
//                            }
                            task = new Callable<Boolean>() {
                                @Override
                                public Boolean call() throws Exception {
                                    log.info("calculate " + metric + " " + cpgPosListMap.getKey() + " start!");
                                    List<BedGraphInfo> calculateResult = calculateOneThread(metric, cpgPosListInRegion, region);
                                    if (calculateResult.size() > 0) {
                                        for (BedGraphInfo bedGraphInfo : calculateResult) {
                                            String line = bedGraphInfo.getChrom() + "\t" + bedGraphInfo.getStart() + "\t" + bedGraphInfo.getEnd() + "\t" + bedGraphInfo.getValue() + "\n";
                                            bufferedWriter.write(line);
                                        }
                                    }
                                    log.info("calculate " + metric + " " + cpgPosListMap.getKey() + " end!");
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

                        bufferedWriter.close();
                        log.info("calculate " + metric + " succeed!");

                        return true;
                    }
                };
                // 减少计数器的计数，如果计数达到零，则释放所有等待线程。
                // 如果当前计数大于零，则递减。如果新计数为零，则重新启用所有等待线程以进行线程调度。
                countDownLatch.countDown();
                // 任务处理完加入集合
                tasks.add(task);

            }
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
            List<Integer> cpgPosListInRegionLoop = loopDataList;
            task = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    if (cpgPosListInRegionLoop.size() > 0) {
                        List<BedGraphInfo> calculateResult = calculateOneThread(metric, cpgPosListInRegionLoop, region);
                        if (calculateResult.size() > 0) {
                            for (BedGraphInfo bedGraphInfo : calculateResult) {
                                String line = bedGraphInfo.getChrom() + "\t" + bedGraphInfo.getStart() + "\t" + bedGraphInfo.getEnd() + "\t" + bedGraphInfo.getValue() + "\n";
                                bufferedWriter.write(line);
                            }
                        }
                    }
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

    private List<BedGraphInfo> calculateOneThread(String metric, List<Integer> cpgPosListInRegion, Region region) throws Exception {
        List<BedGraphInfo> bedGraphInfoList = Lists.newArrayList();
        Integer getCnt = 0;
        for (Integer cpgPos : cpgPosListInRegion) {
            getCnt++;
            if (getCnt % 1000 == 0) {
                log.info("calculate " + metric + " complete " + region.getChrom() + " " + getCnt + " positions.");
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

            bedGraphInfoList.add(bedGraphInfo);

        }

        return bedGraphInfoList;
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
