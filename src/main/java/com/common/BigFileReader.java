package com.common;

import com.args.GenomeWideArgs;
import com.bean.MHapInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class BigFileReader {
    public final Logger log = LoggerFactory.getLogger(BigFileReader.class);

    private int threadPoolSize;
    private Charset charset;
    private int bufferSize;
    private ExecutorService executorService;
    private CountDownLatch countDownLatch;
    private long fileLength;
    private RandomAccessFile rAccessFile;
    private Set<StartEndPair> startEndPairs;
    private CyclicBarrier cyclicBarrier;
    private AtomicLong counter = new AtomicLong(0);

    GenomeWideArgs args = new GenomeWideArgs();
    Map<String, List<Integer>> cpgPosListMapRaw = new HashMap<>();
    Map<String, int[]> nReadsListMap = new HashMap<>();
    Map<String, int[]> mReadListMap = new HashMap<>();
    Map<String, int[]> cBaseListMap = new HashMap<>();
    Map<String, int[]> tBaseListMap = new HashMap<>();
    Map<String, int[]> K4plusListMap = new HashMap<>();
    Map<String, int[]> nDRListMap = new HashMap<>();
    Map<String, int[]> nMRListMap = new HashMap<>();
    Map<String, int[][]> methKmersListMap = new HashMap<>();
    Map<String, int[][]> totalKmersListMap = new HashMap<>();
    Map<String, Double[]> mbsNumListMap = new HashMap<>();
    Map<String, int[][]> kmerListMap = new HashMap<>();
    Map<String, int[]> kmerAllListMap = new HashMap<>();
    Map<String, int[][]> N00ListMap = new HashMap<>();
    Map<String, int[][]> N01ListMap = new HashMap<>();
    Map<String, int[][]> N10ListMap = new HashMap<>();
    Map<String, int[][]> N11ListMap = new HashMap<>();

    public BigFileReader(File file, Charset charset, int bufferSize, int threadPoolSize, Map<String, List<Integer>> cpgPosListMap2,
                         GenomeWideArgs args, List<Map.Entry<String, List<Integer>>> cpgPosListMapList) {
        this.fileLength = file.length();
        this.charset = charset;
        this.bufferSize = bufferSize;
        this.threadPoolSize = threadPoolSize;
        try {
            this.rAccessFile = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("文件不存在！");
        }
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.countDownLatch = new CountDownLatch(this.threadPoolSize);
        startEndPairs = new HashSet<StartEndPair>();

        this.args = args;
        this.cpgPosListMapRaw = cpgPosListMap2;
        for (Map.Entry<String, List<Integer>> cpgPosListMap : cpgPosListMapList) {
            log.info("Calculate " + cpgPosListMap.getKey() + " start!");
            List<Integer> cpgPosList = cpgPosListMap.getValue();

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

            this.nReadsListMap.put(cpgPosListMap.getKey(), nReadsList);
            this.mReadListMap.put(cpgPosListMap.getKey(), mReadList);
            this.cBaseListMap.put(cpgPosListMap.getKey(), cBaseList);
            this.tBaseListMap.put(cpgPosListMap.getKey(), tBaseList);
            this.K4plusListMap.put(cpgPosListMap.getKey(), K4plusList);
            this.nDRListMap.put(cpgPosListMap.getKey(), nDRList);
            this.nMRListMap.put(cpgPosListMap.getKey(), nMRList);
            this.methKmersListMap.put(cpgPosListMap.getKey(), methKmersList);
            this.totalKmersListMap.put(cpgPosListMap.getKey(), totalKmersList);
            this.mbsNumListMap.put(cpgPosListMap.getKey(), mbsNumList);
            this.kmerListMap.put(cpgPosListMap.getKey(), kmerList);
            this.kmerAllListMap.put(cpgPosListMap.getKey(), kmerAllList);
            this.N00ListMap.put(cpgPosListMap.getKey(), N00List);
            this.N01ListMap.put(cpgPosListMap.getKey(), N01List);
            this.N10ListMap.put(cpgPosListMap.getKey(), N10List);
            this.N11ListMap.put(cpgPosListMap.getKey(), N11List);
        }
    }

    public void start() {
        long everySize = this.fileLength / this.threadPoolSize;
        try {
            calculateStartEnd(0, everySize);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("calculateStartEnd error", e);
            return;
        }

        final long startTime = System.currentTimeMillis();
        cyclicBarrier = new CyclicBarrier(startEndPairs.size(), () -> {
            log.info("use time: " + (System.currentTimeMillis() - startTime));
            log.info("all line: " + counter.get());
            shutdown();
        });
        for (StartEndPair pair : startEndPairs) {
            log.info("分配分片：" + pair);
            this.executorService.execute(new SliceReaderTask(pair));
        }

        try {
            countDownLatch.await();//阻塞当前线程，直到计数器的值为0
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();// 关闭线程池

    }

    private void calculateStartEnd(long start, long size) throws IOException {
        if (start > fileLength - 1) {
            return;
        }
        StartEndPair pair = new StartEndPair();
        pair.start = start;
        long endPosition = start + size - 1;
        if (endPosition >= fileLength - 1) {
            pair.end = fileLength - 1;
            startEndPairs.add(pair);
            return;
        }

        rAccessFile.seek(endPosition);
        byte tmp = (byte) rAccessFile.read();
        while (tmp != '\n' && tmp != '\r') {
            endPosition++;
            if (endPosition >= fileLength - 1) {
                endPosition = fileLength - 1;
                break;
            }
            rAccessFile.seek(endPosition);
            tmp = (byte) rAccessFile.read();
        }
        pair.end = endPosition;
        startEndPairs.add(pair);

        calculateStartEnd(endPosition + 1, size);

    }

    public void shutdown() {
        try {
            this.rAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.executorService.shutdown();
    }

    private void handle(byte[] bytes) throws UnsupportedEncodingException {
        String mHapLine = null;
        if (this.charset == null) {
            mHapLine = new String(bytes);
        } else {
            mHapLine = new String(bytes, charset);
        }
        if (mHapLine != null && !"".equals(mHapLine)) {
            if ((args.getStrand().equals("plus") && mHapLine.split("\t")[5].equals("-")) ||
                    (args.getStrand().equals("minus") && mHapLine.split("\t")[5].equals("+"))) {
                return;
            }

            Long lineCnt = counter.incrementAndGet();
            if (lineCnt % 1000000 == 0) {
                log.info("Read mhap file complete " + lineCnt + " lines.");
            }

            MHapInfo mHapInfo = new MHapInfo(mHapLine.split("\t")[0], Integer.valueOf(mHapLine.split("\t")[1]),
                    Integer.valueOf(mHapLine.split("\t")[2]), mHapLine.split("\t")[3],
                    Integer.valueOf(mHapLine.split("\t")[4]), mHapLine.split("\t")[5]);

            Integer cpgPosIndex = this.cpgPosListMapRaw.get(mHapInfo.getChrom()).indexOf(mHapInfo.getStart());
            String cpgStr = mHapInfo.getCpg();
            Integer cpgLen = cpgStr.length();
            Integer readCnt = mHapInfo.getCnt();

            for (int i = 0; i < cpgLen; i++) {
                if (cpgStr.charAt(i) == '1') {
                    mReadListMap.get(mHapInfo.getChrom())[cpgPosIndex + i] += readCnt;
                }
                nReadsListMap.get(mHapInfo.getChrom())[cpgPosIndex + i] += readCnt;
                tBaseListMap.get(mHapInfo.getChrom())[cpgPosIndex + i] += cpgLen * readCnt;
                if (cpgStr.contains("1")) {
                    long noMethCnt = cpgStr.chars().filter(ch -> ch == '0').count();
                    cBaseListMap.get(mHapInfo.getChrom())[cpgPosIndex + i] += noMethCnt * readCnt;
                }
            }

            if (cpgStr.length() >= args.getK()) {
                for (int i = 0; i < cpgLen; i++) {
                    K4plusListMap.get(mHapInfo.getChrom())[cpgPosIndex + i] += readCnt;
                    if (cpgStr.contains("1")) {
                        nMRListMap.get(mHapInfo.getChrom())[cpgPosIndex + i] += readCnt;
                        if (cpgStr.contains("0")) {
                            nDRListMap.get(mHapInfo.getChrom())[cpgPosIndex + i] += readCnt;
                        }
                    }
                }
            }

            if (args.getMetrics().contains("MHL")) {
                if (args.getMinK() > cpgLen) {
                    log.error("calculate MHL Error: minK is too large.");
                    return;
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
                        methKmersListMap.get(mHapInfo.getChrom())[row][cpgPosIndex + j] += subMethKmersList[i];
                        totalKmersListMap.get(mHapInfo.getChrom())[row][cpgPosIndex + j] += subTotalKmersList[i];
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
                        mbsNumListMap.get(mHapInfo.getChrom())[cpgPosIndex + i] += mbsNum;
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
                        Iterator<String> iterator1 = kmerMap.keySet().iterator();
                        while (iterator1.hasNext()) {
                            String key = iterator1.next();
                            Double index = 0.0;
                            for (int j = 0; j < key.length(); j++) {
                                if (key.charAt(key.length() - 1 - j) == '1') {
                                    index += Math.pow(2, j);
                                }
                            }
                            kmerListMap.get(mHapInfo.getChrom())[index.intValue()][cpgPosIndex + i] += kmerMap.get(key);
                            kmerAllListMap.get(mHapInfo.getChrom())[cpgPosIndex + i] += kmerMap.get(key);
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
                            N00ListMap.get(mHapInfo.getChrom())[index][cpgPosIndex + i] += readCnt;
                        } else if (mHapInfo.getCpg().charAt(i) == '0' && mHapInfo.getCpg().charAt(j) == '1') {
                            N01ListMap.get(mHapInfo.getChrom())[index][cpgPosIndex + i] += readCnt;
                        } else if (mHapInfo.getCpg().charAt(i) == '1' && mHapInfo.getCpg().charAt(j) == '0') {
                            N10ListMap.get(mHapInfo.getChrom())[index][cpgPosIndex + i] += readCnt;
                        } else if (mHapInfo.getCpg().charAt(i) == '1' && mHapInfo.getCpg().charAt(j) == '1') {
                            N11ListMap.get(mHapInfo.getChrom())[index][cpgPosIndex + i] += readCnt;
                        }
                    }
                }
            }
        }
    }


    private static class StartEndPair {
        public long start;
        public long end;

        @Override
        public String toString() {
            return "star=" + start + ";end=" + end;
        }
    }

    private class SliceReaderTask implements Runnable {
        private long start;
        private long sliceSize;
        private byte[] readBuff;

        public SliceReaderTask(StartEndPair pair) {
            this.start = pair.start;
            this.sliceSize = pair.end - pair.start + 1;
            this.readBuff = new byte[bufferSize];
        }

        @Override
        public void run() {
            try {
                MappedByteBuffer mapBuffer = rAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, start, this.sliceSize);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                for (int offset = 0; offset < sliceSize; offset += bufferSize) {
                    int readLength;
                    if (offset + bufferSize <= sliceSize) {
                        readLength = bufferSize;
                    } else {
                        readLength = (int) (sliceSize - offset);
                    }
                    mapBuffer.get(readBuff, 0, readLength);
                    for (int i = 0; i < readLength; i++) {
                        byte tmp = readBuff[i];
                        //碰到换行符
                        if (tmp == '\n' || tmp == '\r') {
                            handle(bos.toByteArray());
                            bos.reset();
                        } else {
                            bos.write(tmp);
                        }
                    }
                }
                if (bos.size() > 0) {
                    handle(bos.toByteArray());
                }
                cyclicBarrier.await();//测试性能用
                countDownLatch.countDown();//当前线程调用此方法，则计数减一
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, int[]> getnReadsListMap() {
        return nReadsListMap;
    }

    public void setnReadsListMap(Map<String, int[]> nReadsListMap) {
        this.nReadsListMap = nReadsListMap;
    }

    public Map<String, int[]> getmReadListMap() {
        return mReadListMap;
    }

    public void setmReadListMap(Map<String, int[]> mReadListMap) {
        this.mReadListMap = mReadListMap;
    }

    public Map<String, int[]> getcBaseListMap() {
        return cBaseListMap;
    }

    public void setcBaseListMap(Map<String, int[]> cBaseListMap) {
        this.cBaseListMap = cBaseListMap;
    }

    public Map<String, int[]> gettBaseListMap() {
        return tBaseListMap;
    }

    public void settBaseListMap(Map<String, int[]> tBaseListMap) {
        this.tBaseListMap = tBaseListMap;
    }

    public Map<String, int[]> getK4plusListMap() {
        return K4plusListMap;
    }

    public void setK4plusListMap(Map<String, int[]> k4plusListMap) {
        K4plusListMap = k4plusListMap;
    }

    public Map<String, int[]> getnDRListMap() {
        return nDRListMap;
    }

    public void setnDRListMap(Map<String, int[]> nDRListMap) {
        this.nDRListMap = nDRListMap;
    }

    public Map<String, int[]> getnMRListMap() {
        return nMRListMap;
    }

    public void setnMRListMap(Map<String, int[]> nMRListMap) {
        this.nMRListMap = nMRListMap;
    }

    public Map<String, int[][]> getMethKmersListMap() {
        return methKmersListMap;
    }

    public void setMethKmersListMap(Map<String, int[][]> methKmersListMap) {
        this.methKmersListMap = methKmersListMap;
    }

    public Map<String, int[][]> getTotalKmersListMap() {
        return totalKmersListMap;
    }

    public void setTotalKmersListMap(Map<String, int[][]> totalKmersListMap) {
        this.totalKmersListMap = totalKmersListMap;
    }

    public Map<String, Double[]> getMbsNumListMap() {
        return mbsNumListMap;
    }

    public void setMbsNumListMap(Map<String, Double[]> mbsNumListMap) {
        this.mbsNumListMap = mbsNumListMap;
    }

    public Map<String, int[][]> getKmerListMap() {
        return kmerListMap;
    }

    public void setKmerListMap(Map<String, int[][]> kmerListMap) {
        this.kmerListMap = kmerListMap;
    }

    public Map<String, int[]> getKmerAllListMap() {
        return kmerAllListMap;
    }

    public void setKmerAllListMap(Map<String, int[]> kmerAllListMap) {
        this.kmerAllListMap = kmerAllListMap;
    }

    public Map<String, int[][]> getN00ListMap() {
        return N00ListMap;
    }

    public void setN00ListMap(Map<String, int[][]> n00ListMap) {
        N00ListMap = n00ListMap;
    }

    public Map<String, int[][]> getN01ListMap() {
        return N01ListMap;
    }

    public void setN01ListMap(Map<String, int[][]> n01ListMap) {
        N01ListMap = n01ListMap;
    }

    public Map<String, int[][]> getN10ListMap() {
        return N10ListMap;
    }

    public void setN10ListMap(Map<String, int[][]> n10ListMap) {
        N10ListMap = n10ListMap;
    }

    public Map<String, int[][]> getN11ListMap() {
        return N11ListMap;
    }

    public void setN11ListMap(Map<String, int[][]> n11ListMap) {
        N11ListMap = n11ListMap;
    }

}
