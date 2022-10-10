package com;

import com.args.ConvertArgs;
import com.bean.MHapInfo;
import com.bean.Region;
import com.bean.RegionType;
import com.bean.StrandType;
import com.common.Util;
import htsjdk.samtools.*;
import htsjdk.tribble.bed.BEDCodec;
import htsjdk.tribble.bed.BEDFeature;
import htsjdk.tribble.readers.AsciiLineReader;
import htsjdk.tribble.readers.LineIteratorImpl;
import htsjdk.tribble.readers.TabixReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

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

        // 生成mHap文件数据
        if (regionType == RegionType.SINGLE_REGION) { // 单区间
            // 解析region
            Region region = util.parseRegion(args.getRegion());
            boolean getSingleRegionDataResult = getSingleRegionData(region);
            if (!getSingleRegionDataResult) {
                log.error("getSingleRegionData fail, please check the command.");
                return;
            }
        } else if (regionType == RegionType.WHOLE_FILE) { // 整个文件
            boolean getWholeFileDataResult = getWholeFileData();
            if (!getWholeFileDataResult) {
                log.error("getWholeFileData fail, please check the command.");
                return;
            }
        } else if (regionType == RegionType.MULTI_REGION) { // bed文件的基因
            boolean getWholeFileDataResult = getMultiRegionData();
            if (!getWholeFileDataResult) {
                log.error("getWholeFileData fail, please check the command.");
                return;
            }
        }
        
        log.info("command.Convert end! ");
    }

    private boolean checkArgs() {
        if (args.getInputFile().isEmpty() || args.getCpgPath().isEmpty()) {
            log.error("Please specify -i and -c options.");
            return false;
        }
        if (!args.getRegion().isEmpty() && !args.getBedFile().isEmpty()) {
            log.error("You can not specify both -r and -b options.");
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

    private boolean getWholeFileData() throws IOException {
        SamReader samReader = SamReaderFactory.makeDefault().open(inputFile);
        SAMFileHeader samFileHeader = samReader.getFileHeader(); // sam文件头
        List<SAMSequenceRecord> samFileHeaderList = samFileHeader.getSequenceDictionary().getSequences();

        for (int i = 0; i < samFileHeaderList.size(); i++) {
            SAMSequenceRecord samSequenceRecord = samFileHeaderList.get(i); // sam每个染色体的信息
            Region region = new Region();
            region.setChrom(samSequenceRecord.getSequenceName());
            region.setStart(samSequenceRecord.getStart());
            region.setEnd(samSequenceRecord.getEnd());
            boolean getSingleRegionDataResult = getSingleRegionData(region);
            if (!getSingleRegionDataResult) {
                log.error("getSingleRegionData fail, please check the command.");
                return false;
            }
        }

        return true;
    }

    private boolean getMultiRegionData() throws IOException {
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
            boolean getSingleRegionDataResult = getSingleRegionData(region);
            if (!getSingleRegionDataResult) {
                log.error("getSingleRegionData fail, please check the command.");
                return false;
            }
        }
        return true;
    }

    private boolean getSingleRegionData(Region region) throws IOException {
        String outputFileName = "";
        if (args.getOutPutFile() == null || args.getOutPutFile().equals("")) {
            outputFileName = "out.mhap";
        } else {
            outputFileName = args.getOutPutFile().substring(0, args.getOutPutFile().length() - 3);
        }
        BufferedWriter bufferedWriter = util.createOutputFile("", outputFileName);

        SamReader samReader = SamReaderFactory.makeDefault().open(inputFile);
        SAMRecordIterator samRecordIterator = samReader.query(region.getChrom(), region.getStart(), region.getEnd(),true);
        Map<String, MHapInfo> mHapMap = new HashMap<>();
        MHapInfo mHapNewLine = new MHapInfo("", 0, 0, "", 0, "");
        MHapInfo mHapFrontLine = new MHapInfo("", 0, 0, "", 0, "");
        long samCnt = 0l; // 处理的sam数量
        while (samRecordIterator.hasNext()) {
            // 保存上一行的数据
            mHapFrontLine.setChrom(mHapNewLine.getChrom());
            mHapFrontLine.setStart(mHapNewLine.getStart());
            mHapNewLine = new MHapInfo("", 0, 0, "", 0, ""); // 清空新行

            samCnt++;
            if (samCnt % 10000 == 0) { // 每一万条打印进度
                log.info(samCnt + " reads processed.");
            }

            // 获取sam下一行
            SAMRecord samRecord = samRecordIterator.next();
            if (samRecord.getReadLength() == 0) {
                log.error("The read length is 0.");
                continue;
            }

            // 获取sam的甲基化信息
            String samHaploInfo = "";
            List<SAMRecord.SAMTagAndValue> samTagAndValueList = samRecord.getAttributes();
            for (SAMRecord.SAMTagAndValue samTagAndValue : samTagAndValueList) {
                if (samTagAndValue.tag.equals("XM")) {
                    samHaploInfo = (String) samTagAndValue.value;
                }
            }
            if (samHaploInfo.equals("")) {
                log.error("The XM string is null.");
                continue;
            }
            for (int i = 0; i < samHaploInfo.length(); i++) {
                if (samHaploInfo.charAt(i) == 'C' || samHaploInfo.charAt(i) == 'H' || samHaploInfo.charAt(i) == 'U') {
                    continue;
                }
            }

            // 获取正负链信息
            StrandType strand = StrandType.UNKNOWN;
            if (args.isNon_directional()) { // 无方向
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

            // 查询cpg文件的甲基化位点信息
            TabixReader tabixReader = new TabixReader(args.getCpgPath());
            TabixReader.Iterator iterator = tabixReader.query(region.getChrom(), samRecord.getStart(), samRecord.getEnd());
            String cpgPosition = ""; // 每一个甲基化位点最左侧位置
            Integer[] cpgPosList = new Integer[samRecord.getReadLength()]; // 在该read内所有甲基化位点最左侧位置（初始化长度为read的长度）
            Integer cpgCnt = 0;
            while((cpgPosition = iterator.next()) != null) {
                cpgPosList[cpgCnt] = Integer.valueOf(cpgPosition.split("\t")[1]);
                cpgCnt++;
            }
            if (cpgCnt == 0) {
                log.info("remove read:" + samRecord.getReadName() + " due to the size of cpg is 0.");
                continue;
            }

            // 获取甲基化位点信息
            String haplotype = getHaplotype(samRecord, cpgPosList, cpgCnt, strand);
            if (haplotype.matches(".*[a-zA-z].*")) {
                continue;
            }

            // mHap数据赋值
            mHapNewLine.setChrom(region.getChrom());
            mHapNewLine.setStart(cpgPosList[0]);
            mHapNewLine.setEnd(cpgPosList[cpgCnt - 1]);
            mHapNewLine.setCpg(haplotype);
            mHapNewLine.setCnt(1);
            mHapNewLine.setStrand(strand.getStrandFlag());

            // 合并索引相同的行
            if (mHapMap.containsKey(mHapNewLine.indexByRead())) {
                mHapMap.get(mHapNewLine.indexByRead()).setCnt(mHapMap.get(mHapNewLine.indexByRead()).getCnt() + 1);
            } else {
                mHapMap.put(mHapNewLine.indexByRead(), mHapNewLine);
            }
        }

        // 对mHap数据进行排序
        List<Map.Entry<String, MHapInfo>> mHapList = new ArrayList<Map.Entry<String, MHapInfo>>(mHapMap.entrySet());
        Collections.sort(mHapList, new Comparator<Map.Entry<String, MHapInfo>>() { //升序排序
            public int compare(Map.Entry<String, MHapInfo> o1, Map.Entry<String, MHapInfo> o2) {
                return o1.getValue().getChrom().compareTo(o2.getValue().getChrom())
                        + o1.getValue().getStart().compareTo(o2.getValue().getStart());
            }
        });

        for(int i = 0; i < mHapList.size(); i++) {
            bufferedWriter.write(mHapList.get(i).getValue().print() + "\n");
        }

        bufferedWriter.close();

        return true;
    }

    private String getHaplotype(SAMRecord samRecord, Integer[] cpgPosList, Integer cpgCnt, StrandType strand) {
        // 获取read甲基化位点的碱基序列
        String haploString = ""; // read甲基化位点的碱基序列
        String readString = samRecord.getReadString();
        for (int i = 0; i < cpgCnt; i++) {
            if (cpgPosList[0].equals(102988)) {
                Integer pos = 0; // 偏移量
                if (strand == StrandType.UNKNOWN || strand == StrandType.PLUS) {
                    if (cpgPosList[i] < samRecord.getStart()) {
                        continue;
                    } else if (cpgPosList[i] > samRecord.getEnd()) {
                        break;
                    }
                    pos = cpgPosList[i] - samRecord.getStart();
                } else {
                    if (cpgPosList[i] < samRecord.getStart() - 1) {
                        continue;
                    } else if (cpgPosList[i] > samRecord.getEnd() - 1) {
                        break;
                    }
                    pos = cpgPosList[i] - samRecord.getStart() + 1;
                }

                if (pos >= samRecord.getReadLength() || pos < 0) {
                    continue;
                }
            }
            Integer pos = 0; // 偏移量
            if (strand == StrandType.UNKNOWN || strand == StrandType.PLUS) {
                if (cpgPosList[i] < samRecord.getStart()) {
                    continue;
                } else if (cpgPosList[i] > samRecord.getEnd()) {
                    break;
                }
                pos = cpgPosList[i] - samRecord.getStart();
            } else {
                if (cpgPosList[i] < samRecord.getStart() - 1) {
                    continue;
                } else if (cpgPosList[i] > samRecord.getEnd() - 1) {
                    break;
                }
                pos = cpgPosList[i] - samRecord.getStart() + 1;
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
