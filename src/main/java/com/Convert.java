package com;

import com.args.ConvertArgs;
import com.bean.MHapInfo;
import com.bean.Region;
import com.bean.RegionType;
import com.bean.StrandType;
import com.common.Util;
import htsjdk.samtools.*;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.tribble.bed.BEDCodec;
import htsjdk.tribble.bed.BEDFeature;
import htsjdk.tribble.readers.AsciiLineReader;
import htsjdk.tribble.readers.LineIteratorImpl;
import htsjdk.tribble.readers.TabixReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static htsjdk.samtools.SamFiles.findIndex;

public class Convert {
    public static final Logger log = LoggerFactory.getLogger(Convert.class);

    ConvertArgs args = new ConvertArgs();
    Util util = new Util();
    public RegionType regionType; // 区域类型
    public File inputFile; // sam/bam文件

    public void convert(ConvertArgs convertArgs) throws Exception {
        log.info("command.Convert start!");
        args = convertArgs;

        // 校验命令正确性
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        // 数据初始化
        boolean initResult = initData();
        if (!initResult) {
            log.error("InitData fail, please check the command.");
            return;
        }

        String mhapFileName = "";
        if (args.getOutPutFile() == null || args.getOutPutFile().equals("")) {
            mhapFileName = "out.mhap";
        } else {
            mhapFileName = args.getOutPutFile().substring(0, args.getOutPutFile().length() - 3);
        }
        BufferedWriter bufferedWriter = util.createOutputFile("", mhapFileName);

        if (args.isPat()) {
            boolean convertPatResult = convertPat(bufferedWriter);
            if (!convertPatResult) {
                log.error("convertPat fail, please check the command.");
                return;
            }
        } else {
            boolean convertBamResult = convertBam(bufferedWriter);
            if (!convertBamResult) {
                log.error("convertBam fail, please check the command.");
                return;
            }
        }

        bufferedWriter.close();

        // convert mhap file to .gz file
        log.info("Start generate .gz file...");
        String gzFileName = mhapFileName + ".gz";
        InputStream inputStream = new FileInputStream(mhapFileName);
        OutputStream outputStream = new BlockCompressedOutputStream(new File(gzFileName));
        byte[] b = new byte[1024];
        int len = inputStream.read(b);
        while (len > 0) {
            outputStream.write(b, 0, len);
            len = inputStream.read(b);
        }
        inputStream.close();
        outputStream.close();
        new File(mhapFileName).delete();

        log.info("command.Convert end! ");
    }

    private boolean convertBam(BufferedWriter bufferedWriter) throws Exception {
        if (regionType == RegionType.SINGLE_REGION) { // 单区间
            // 解析region
            Region region = util.parseRegion(args.getRegion());
            boolean getSingleRegionDataResult = getSingleRegionData(region, bufferedWriter);
            if (!getSingleRegionDataResult) {
                log.error("getSingleRegionData fail, please check the command.");
                return false;
            }
        } else if (regionType == RegionType.WHOLE_FILE) { // 整个文件
            boolean getWholeFileDataResult = getWholeFileData(bufferedWriter);
            if (!getWholeFileDataResult) {
                log.error("getWholeFileData fail, please check the command.");
                return false;
            }
        } else if (regionType == RegionType.MULTI_REGION) { // bed文件的基因
            boolean getWholeFileDataResult = getMultiRegionData(bufferedWriter);
            if (!getWholeFileDataResult) {
                log.error("getWholeFileData fail, please check the command.");
                return false;
            }
        }

        return true;
    }

    private boolean convertPat(BufferedWriter bufferedWriter) throws Exception {

        // get whole cpg position list
        List<Integer> cpgPosList = new ArrayList<>();
        TabixReader tabixReader = new TabixReader(args.getCpgPath());
        String cpgLine = tabixReader.readLine();
        while(cpgLine != null && !cpgLine.equals("")) {
            if (cpgLine.split("\t").length < 3) {
                continue;
            } else {
                cpgPosList.add(Integer.valueOf(cpgLine.split("\t")[1]));
                cpgLine = tabixReader.readLine();
            }
        }
        log.info("Read cpg reference file: " + args.getCpgPath() + " end.");

        FileInputStream fileInputStream = new FileInputStream(args.getInputFile());
        GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
        InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String patLine = "";
        long completeLine = 0l;
        while ((patLine = bufferedReader.readLine()) != null && !patLine.equals("")) {
            completeLine++;
            if (completeLine % 1000000 == 0) { // 每一百万条打印进度
                log.info("Complete convert pat file "  + completeLine + " lines.");
            }

            if(patLine == null || patLine.split("\t").length < 4) {
                continue;
            }

            String thisChrom = patLine.split("\t")[0];
            Integer startLine = Integer.valueOf(patLine.split("\t")[1]);
            String cpgInfo = patLine.split("\t")[2];
            Integer readNum = Integer.valueOf(patLine.split("\t")[3]);

            if (cpgInfo.contains(".")) {
                continue;
            }

            Integer startPos = cpgPosList.get(startLine - 1);
            Integer endPos = cpgPosList.get(startLine + cpgInfo.length() - 2);

            String cpgStr = ""; // cpg string in format of 0/1
            for (char cpg : cpgInfo.toCharArray()) {
                if (cpg == 'C') {
                    cpgStr += "1";
                } else if (cpg == 'T') {
                    cpgStr += "0";
                }
            }

            bufferedWriter.write(thisChrom + "\t" + startPos + "\t" + endPos + "\t" + cpgStr + "\t" + readNum + "\t" + "+" + "\n");
        }

        return true;
    }

    private boolean checkArgs() {
        if (args.getInputFile().isEmpty() || args.getCpgPath().isEmpty()) {
            log.error("Please specify -i and -c options.");
            return false;
        }
        if (args.getMode() != "BS" && args.getMode() != "TAPS") {
            log.error("opt error: -m should be specified as BS or TAPS.");
            return false;
        }
        if (!args.getCpgPath().isEmpty() && !args.getCpgPath().endsWith(".gz")) {
            log.error("-c opt should be followed by a .gz file.");
            return false;
        }
        if (!args.getOutPutFile().isEmpty() && !args.getOutPutFile().endsWith(".mhap.gz")) {
            log.error("-o opt should be followed by a .mhap.gz file.");
            return false;
        }
        if (!args.getBedFile().isEmpty() && !args.getBedFile().endsWith(".bed")) {
            log.error("-b opt should be followed by a .bed file.");
            return false;
        }

        return true;
    }

    private boolean initData() {
        if (args.getRegion() != null && !args.getRegion().isEmpty()) {
            regionType = RegionType.SINGLE_REGION;
        } else if (args.getBedFile() != null && !args.getBedFile().isEmpty()) {
            regionType = RegionType.MULTI_REGION;
        } else if (args.getInputFile() != null && !args.getInputFile().isEmpty()
                && args.getCpgPath() != null && !args.getCpgPath().isEmpty()) {
            regionType = RegionType.WHOLE_FILE;
        } else {
            return false;
        }

        // 打开sam/bam文件
        inputFile = new File(args.getInputFile());
        if (inputFile == null || !inputFile.exists()) {
            log.error("The sam/bam file do not exist.");
            return false;
        }

        return true;
    }

    private boolean getWholeFileData(BufferedWriter bufferedWriter) throws Exception {
        SamReader samReader = SamReaderFactory.makeDefault().open(inputFile);
        SAMFileHeader samFileHeader = samReader.getFileHeader(); // sam文件头
        List<SAMSequenceRecord> samFileHeaderList = samFileHeader.getSequenceDictionary().getSequences();

        for (int i = 0; i < samFileHeaderList.size(); i++) {
            SAMSequenceRecord samSequenceRecord = samFileHeaderList.get(i); // sam每个染色体的信息
            Region region = new Region();
            region.setChrom(samSequenceRecord.getSequenceName());
            region.setStart(samSequenceRecord.getStart());
            region.setEnd(samSequenceRecord.getEnd());

            boolean getSingleRegionDataResult = getSingleRegionData(region, bufferedWriter);
            if (!getSingleRegionDataResult) {
                log.error("getSingleRegionData fail, please check the command.");
                return false;
            }
            log.info("Convert " + region.getChrom() + " completed!");
        }

        return true;
    }

    private boolean getMultiRegionData(BufferedWriter bufferedWriter) throws Exception {
        File bedFile = new File(args.getBedFile()); // 打开bed文件
        InputStream inputStream = new FileInputStream(bedFile);  // 文件流
        AsciiLineReader asciiLineReader = new AsciiLineReader(inputStream); // 行阅读器
        LineIteratorImpl lineIterator = new LineIteratorImpl(asciiLineReader);
        BEDCodec bedCodec = new BEDCodec(); // bed行解析器
        while (!bedCodec.isDone(lineIterator)) {
            BEDFeature bedFeature = bedCodec.decode(lineIterator);
            Region region = new Region();
            region.setChrom(bedFeature.getContig());
            region.setStart(bedFeature.getStart());
            region.setEnd(bedFeature.getEnd());
            boolean getSingleRegionDataResult = getSingleRegionData(region, bufferedWriter);
            if (!getSingleRegionDataResult) {
                log.info("getSingleRegionData fail, please check the command.");
                return false;
            }
        }
        return true;
    }

    private boolean getSingleRegionData(Region region, BufferedWriter bufferedWriter) throws Exception {

        // get cpg position list
        List<Integer> cpgPosList = util.parseCpgFile(args.getCpgPath(), region);
        if (cpgPosList.size() < 1) {
            log.info("remove chrome:" + region.getChrom() + " due to the size of cpg is 0.");
            return true;
        }
        Integer cpgStartIndex = 0;
        SamReader samReader = SamReaderFactory.makeDefault().open(inputFile);
        SAMRecordIterator samRecordIterator = samReader.query(region.getChrom(), region.getStart(), region.getEnd(),true);
        Map<String, MHapInfo> mHapMap = new HashMap<>();
        long samCnt = 0l; // 处理的sam数量
        while (samRecordIterator.hasNext()) {
            samCnt++;
            if (samCnt % 1000000 == 0) { // 每一百万条打印进度
                log.info("Read sam/bam "  + region.getChrom() + " completed "+ samCnt + " reads.");
            }

            // 获取sam下一行
            SAMRecord samRecord = samRecordIterator.next();
            if (samRecord.getReadLength() == 0) {
                log.error("The read length is 0.");
                continue;
            }

            // 获取sam的甲基化信息
//            String samHaploInfo = "";
//            List<SAMRecord.SAMTagAndValue> samTagAndValueList = samRecord.getAttributes();
//            for (SAMRecord.SAMTagAndValue samTagAndValue : samTagAndValueList) {
//                if (samTagAndValue.tag.equals("Z") || samTagAndValue.tag.equals("H")) {
//                    samHaploInfo = (String) samTagAndValue.tag;
//                }
//            }
//            if (samHaploInfo.equals("")) {
//                //log.error("The XM string is null.");
//                continue;
//            }
//            for (int i = 0; i < samHaploInfo.length(); i++) {
//                if (samHaploInfo.charAt(i) == 'X' || samHaploInfo.charAt(i) == 'H' || samHaploInfo.charAt(i) == 'U') {
//                    continue;
//                }
//            }

            // 获取正负链信息
            StrandType strand = StrandType.UNKNOWN;
            if (args.isNonDirectional()) { // 无方向
                strand = StrandType.UNKNOWN;
            } else {
                if (samRecord.getReadPairedFlag() && samRecord.getProperPairFlag()) { // 同时含1和2
                    if (samRecord.getFirstOfPairFlag()) { // 含64
                        if (samRecord.getReadNegativeStrandFlag()) { // 含16
                            strand = StrandType.MINUS;
                        } else if (samRecord.getMateNegativeStrandFlag()) { // 含32
                            strand = StrandType.PLUS;
                        }
                    } else if (samRecord.getSecondOfPairFlag()) { // 含128
                        if (samRecord.getReadNegativeStrandFlag()) { // 含16
                            strand = StrandType.MINUS;
                        } else if (samRecord.getMateNegativeStrandFlag()) { // 含32
                            strand = StrandType.PLUS;
                        }
                    }
//                } else if (!samRecord.getReadPairedFlag() && !samRecord.getProperPairFlag()) { // 同时不含1和2
                } else {
                    if (samRecord.getReadNegativeStrandFlag()) { // 含16
                        strand = StrandType.MINUS;
                    } else { // 其他
                        strand = StrandType.PLUS;
                    }
                }
            }

            List<Integer> cpgPosListInRegion = new ArrayList<>();
            while (cpgStartIndex < cpgPosList.size() - 1 && cpgPosList.get(cpgStartIndex) < samRecord.getStart()) {
                cpgStartIndex++;
            }
            Integer cpgCnt = 0;
            while (cpgStartIndex + cpgCnt < cpgPosList.size() &&
                    cpgPosList.get(cpgStartIndex + cpgCnt) < samRecord.getEnd()) {
                cpgPosListInRegion.add(cpgPosList.get(cpgStartIndex + cpgCnt));
                cpgCnt++;
            }
            if (cpgCnt == 0) {
                log.error("remove read:" + samRecord.getReadName() + " due to the size of cpg is 0.");
                continue;
            }

            // 获取甲基化位点信息
            String haplotype = getHaplotype(samRecord, cpgPosListInRegion, cpgCnt, strand);
            if (haplotype.matches(".*[a-zA-z].*")) {
                continue;
            }

            // mHap数据赋值
            MHapInfo mHapLine = new MHapInfo(region.getChrom(), cpgPosListInRegion.get(0),
                    cpgPosListInRegion.get(cpgPosListInRegion.size() -1), haplotype, 1, strand.getStrandFlag());

            // 合并索引相同的行
            if (mHapMap.containsKey(mHapLine.indexByReadAndStrand())) {
                mHapMap.get(mHapLine.indexByReadAndStrand()).setCnt(mHapMap.get(mHapLine.indexByReadAndStrand()).getCnt() + 1);
            } else {
                mHapMap.put(mHapLine.indexByReadAndStrand(), mHapLine);
            }
        }

        // 对mHap数据进行排序
        List<Map.Entry<String, MHapInfo>> mHapList = new ArrayList<Map.Entry<String, MHapInfo>>(mHapMap.entrySet());
        Collections.sort(mHapList, new Comparator<Map.Entry<String, MHapInfo>>() { //升序排序
            public int compare(Map.Entry<String, MHapInfo> o1, Map.Entry<String, MHapInfo> o2) {
                return o1.getValue().getChrom().compareTo(o2.getValue().getChrom())
                        + o1.getValue().getStart().compareTo(o2.getValue().getStart())
                        + o1.getValue().getEnd().compareTo(o2.getValue().getEnd());
            }
        });

        for(Map.Entry<String, MHapInfo> mHapInfo : mHapList) {
            bufferedWriter.write(mHapInfo.getValue().print() + "\n");
        }

        return true;
    }

    private String getHaplotype(SAMRecord samRecord, List<Integer> cpgPosList, Integer cpgCnt, StrandType strand) {
        // 获取read甲基化位点的碱基序列
        String haploString = ""; // read甲基化位点的碱基序列
        String readString = samRecord.getReadString();
        for (int i = 0; i < cpgCnt; i++) {
            Integer pos = 0; // 偏移量
            if (strand == StrandType.UNKNOWN || strand == StrandType.PLUS) {
                if (cpgPosList.get(i) < samRecord.getStart()) {
                    continue;
                } else if (cpgPosList.get(i) > samRecord.getEnd()) {
                    break;
                }
                pos = cpgPosList.get(i) - samRecord.getStart();
            } else {
                if (cpgPosList.get(i) < samRecord.getStart() - 1) {
                    continue;
                } else if (cpgPosList.get(i) > samRecord.getEnd() - 1) {
                    break;
                }
                pos = cpgPosList.get(i) - samRecord.getStart() + 1;
            }

            if (pos >= samRecord.getReadLength() || pos < 0) {
                continue;
            }

            haploString += String.valueOf(readString.charAt(pos));
        }

        // 获取甲基化状态信息
        String haplotype = ""; // 甲基化状态信息
        for (int i = 0; i < haploString.length(); i++) {
            if (strand == StrandType.UNKNOWN || strand == StrandType.PLUS) {
                if (haploString.charAt(i) == 'C') {
                    if (args.getMode().equals("BS")) {
                        haplotype += "1";
                    } else {
                        haplotype += "0";
                    }
                } else if (haploString.charAt(i) == 'T') {
                    if (args.getMode().equals("BS")) {
                        haplotype += "0";
                    } else {
                        haplotype += "1";
                    }
                } else {
                    haplotype += haploString.charAt(i);
                    log.info("Direction + or * : beg: " + samRecord.getStart() + ", end:" + samRecord.getEnd()
                            + " nucleobases error:" + haplotype);
                }
            } else {
                if (haploString.charAt(i) == 'G') {
                    if (args.getMode().equals("BS")) {
                        haplotype += "1";
                    } else {
                        haplotype += "0";
                    }
                } else if (haploString.charAt(i) == 'A') {
                    if (args.getMode().equals("BS")) {
                        haplotype += "0";
                    } else {
                        haplotype += "1";
                    }
                } else {
                    haplotype += haploString.charAt(i);
                    log.info("Direction - : beg: " + samRecord.getStart() + ", end:" + samRecord.getEnd()
                            + " nucleobases error:" + haplotype);
                }
            }
        }

        return haplotype;
    }
    

}
