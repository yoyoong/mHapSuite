package com.common;

import com.bean.*;
import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import htsjdk.tribble.readers.TabixReader;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.*;
import java.util.List;

public class Util {
    public static final Logger log = LoggerFactory.getLogger(Util.class);

    public Region parseRegion(String regionStr) {
        Region region = new Region();
        region.setChrom(regionStr.split(":")[0]);
        region.setStart(Integer.valueOf(regionStr.split(":")[1].split("-")[0]));
        region.setEnd(Integer.valueOf(regionStr.split(":")[1].split("-")[1]));
        return region;
    }

    public List<Integer> parseCpgFile(String cpgPath, Region region) throws Exception {
        List<Integer> cpgPosList = new ArrayList<>();
        TabixReader cpgTabixReader = new TabixReader(cpgPath);
        TabixReader.Iterator cpgIterator = cpgTabixReader.query(region.getChrom(), region.getStart(), region.getEnd());
        String cpgLine = "";
        while((cpgLine = cpgIterator.next()) != null) {
            if (cpgLine.split("\t").length < 3) {
                continue;
            } else {
                cpgPosList.add(Integer.valueOf(cpgLine.split("\t")[1]));
            }
        }

        return cpgPosList;
    }

    public Map<String, List<Integer>> parseWholeCpgFile(String cpgPath) throws Exception {
        Map<String, List<Integer>> cpgPosListMap = new HashMap<>();

        List<Integer> cpgPosList = new ArrayList<>();
        TabixReader cpgTabixReader = new TabixReader(cpgPath);
        String cpgLine = cpgTabixReader.readLine();
        String lastChr = cpgLine.split("\t")[0];
        while(cpgLine != null && !cpgLine.equals("")) {
            if (cpgLine.split("\t").length < 3) {
                continue;
            } else {
                if (lastChr.equals(cpgLine.split("\t")[0])) {
                    cpgPosList.add(Integer.valueOf(cpgLine.split("\t")[1]));
                } else {
                    log.info("Read cpg file " + lastChr + " end");
                    cpgPosListMap.put(lastChr, cpgPosList);
                    lastChr = cpgLine.split("\t")[0];
                    cpgPosList = new ArrayList<>();
                    cpgPosList.add(Integer.valueOf(cpgLine.split("\t")[1]));
                }
                cpgLine = cpgTabixReader.readLine();
            }
        }
        log.info("Read cpg file " + lastChr + " end");

        return cpgPosListMap;
    }

    public List<Integer> parseCpgFileWithShift(String cpgPath, Region region, Integer shift) throws Exception {
        List<Integer> cpgPosList = new ArrayList<>();
        TabixReader cpgTabixReader = new TabixReader(cpgPath);
        TabixReader.Iterator cpgIterator = cpgTabixReader.query(region.getChrom(), region.getStart() - shift, region.getEnd() + shift);
        String cpgLine = "";
        while((cpgLine = cpgIterator.next()) != null) {
            if (cpgLine.split("\t").length < 3) {
                continue;
            } else {
                cpgPosList.add(Integer.valueOf(cpgLine.split("\t")[1]));
            }
        }

        return cpgPosList;
    }

    public List<BedInfo> parseBedFile(String bedFile, Region region) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(bedFile)));
        String bedLine = "";
        List<BedInfo> bedInfoList = new ArrayList<>();
        while ((bedLine = bufferedReader.readLine()) != null) {
            BedInfo bedInfo = new BedInfo();
            if (bedLine.split("\t").length >= 3) {
                bedInfo.setChrom(bedLine.split("\t")[0]);
                bedInfo.setStart(Integer.valueOf(bedLine.split("\t")[1]));
                bedInfo.setEnd(Integer.valueOf(bedLine.split("\t")[2]));
                if (bedInfo.getEnd() >= region.getStart() && bedInfo.getStart() <= region.getEnd()) {
                    if (bedInfo.getEnd() >= region.getStart() && bedInfo.getStart() <= region.getStart()) {
                        bedInfo.setStart(region.getStart());
                    }
                    if (bedInfo.getEnd() >= region.getEnd() && bedInfo.getStart() <= region.getEnd()) {
                        bedInfo.setEnd(region.getEnd());
                    }
                    bedInfoList.add(bedInfo);
                }
            }
        }
        return bedInfoList;
    }

    public BufferedWriter createOutputFile(String directory, String fileName) throws IOException {
        String filePath = "";
        if (directory != null && !directory.equals("")) {
            // create the output directory
            File outputDir = new File(directory);
            if (!outputDir.exists()){
                if (!outputDir.mkdirs()){
                    log.error("create" + outputDir.getAbsolutePath() + "fail");
                    return null;
                }
            }
            filePath = directory + "/" + fileName;
        } else {
            filePath = fileName;
        }

        // create the output file
        File file = new File(filePath);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                log.error("create" + file.getAbsolutePath() + "fail");
                return null;
            }
        } else {
            FileWriter fileWriter =new FileWriter(file.getAbsoluteFile());
            fileWriter.write("");  //写入空
            fileWriter.flush();
            fileWriter.close();
        }
        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(), true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        return bufferedWriter;
    }

    public List<Integer> getCpgPosListInRegion(List<Integer> cpgPosList, Region region) throws Exception {
        Integer cpgStartPos = 0;
        Integer cpgEndPos = cpgPosList.size() - 1;
        for (int i = 0; i < cpgPosList.size(); i++) {
            if (cpgPosList.get(i) < region.getStart() && cpgPosList.get(i + 1) >= region.getStart()) {
                cpgStartPos = i + 1;
                break;
            }
        }
        for (int i = 0; i < cpgPosList.size(); i++) {
            if (cpgPosList.get(i) >= region.getEnd()) {
                cpgEndPos = i;
                break;
            }
        }
        List<Integer> cpgPosListInRegion = cpgPosList.subList(cpgStartPos, cpgEndPos + 1);

        return cpgPosListInRegion;
    }

    public List<MHapInfo> parseMhapFile(String mhapPath, Region region, String strand, Boolean isMerge) throws IOException {
        TabixReader mhapTabixReader = new TabixReader(mhapPath);
        TabixReader.Iterator mhapIterator = mhapTabixReader.query(region.getChrom(), region.getStart() - 1, region.getEnd());
        List<MHapInfo> mergedMHapInfoList = new ArrayList<>(); // mhap数据列表（原始值）
        List<MHapInfo> unMergedMHapInfoList = new ArrayList<>(); // mhap数据列表（未合并的值）
        String mHapLine = "";
        while((mHapLine = mhapIterator.next()) != null) {
            if ((strand.equals("plus") && mHapLine.split("\t")[5].equals("-")) ||
                    (strand.equals("minus") && mHapLine.split("\t")[5].equals("+"))) {
                continue;
            }
            MHapInfo mHapInfo = new MHapInfo();
            mHapInfo.setChrom(mHapLine.split("\t")[0]);
            mHapInfo.setStart(Integer.valueOf(mHapLine.split("\t")[1]));
            mHapInfo.setEnd(Integer.valueOf(mHapLine.split("\t")[2]));
            mHapInfo.setCpg(mHapLine.split("\t")[3]);
            mHapInfo.setCnt(Integer.valueOf(mHapLine.split("\t")[4]));
            mHapInfo.setStrand(mHapLine.split("\t")[5]);
            if (mHapInfo.getCnt() > 1) {
                mergedMHapInfoList.add(mHapInfo);
                Integer cnt = mHapInfo.getCnt();
                for (int i = 0; i < cnt; i++) {
                    unMergedMHapInfoList.add(mHapInfo);
                }
            } else {
                mergedMHapInfoList.add(mHapInfo);
                unMergedMHapInfoList.add(mHapInfo);
            }
        }

        List<MHapInfo> mHapInfoList = new ArrayList<>();
        if (isMerge) {
            mHapInfoList = mergedMHapInfoList;
        } else {
            mHapInfoList = unMergedMHapInfoList;
        }

        return mHapInfoList;
    }

    public Integer[][] getCpgHpMat(List<MHapInfo> mHapInfoList, List<Integer> cpgPosList, List<Integer> cpgPosListInRegion) {
        Integer[][] cpgHpMatInRegion = new Integer[mHapInfoList.size()][cpgPosListInRegion.size()];

        for (int i = 0; i < cpgPosListInRegion.size(); i++) {
            for (int j = 0; j < mHapInfoList.size(); j++) {
                MHapInfo mHapInfo = mHapInfoList.get(j);
                if (cpgPosListInRegion.get(i) >= mHapInfo.getStart() && cpgPosListInRegion.get(i) <= mHapInfo.getEnd()) {
                    // 获取某个在区域内的位点在mhap的cpg中的相对位置
                    Integer pos = cpgPosList.indexOf(cpgPosListInRegion.get(i)) - cpgPosList.indexOf(mHapInfo.getStart());
                    for (int k = pos; k < mHapInfo.getCpg().length(); k++) {
                        if (i + k - pos < cpgPosListInRegion.size()) {
                            if (mHapInfo.getCpg().charAt(k) == '0') {
                                cpgHpMatInRegion[j][i + k - pos] = 0;
                            } else {
                                cpgHpMatInRegion[j][i + k - pos] = 1;
                            }
                        }
                    }
                }
            }
        }
        return cpgHpMatInRegion;
    }

    public String cutReads(MHapInfo mHapInfo, List<Integer> cpgPosList, List<Integer> cpgPosListInRegion) {
        String cpg = mHapInfo.getCpg();
        Integer cpgStart = cpgPosList.get(0);
        Integer cpgEnd = cpgPosList.get(cpgPosListInRegion.size() - 1);

        if (mHapInfo.getStart() < cpgStart) { // mhap.start在region.start左边
            if (mHapInfo.getEnd() < cpgEnd) { // mhap.end在region.end左边
                int pos = 0;
                for (int j = cpgPosList.indexOf(mHapInfo.getStart()); j < cpgPosList.indexOf(cpgStart); j++) {
                    pos++;
                }
                cpg = cpg.substring(pos);
            } else { // mhap.end在region.end右边
                int pos = cpgPosList.indexOf(mHapInfo.getStart());
                int pos1 = cpgPosList.indexOf(cpgStart);
                int pos2 = cpgPosList.indexOf(cpgEnd);
                cpg = cpg.substring(pos1 - pos, pos2 - pos);
            }
        } else { // mhap.start在region.start右边
            if (mHapInfo.getEnd() > cpgEnd) { // mhap.end在region.end右边
                int pos = 0;
                for (int j = cpgPosList.indexOf(mHapInfo.getStart()); j <= cpgPosList.indexOf(cpgEnd); j++) {
                    pos++;
                }
                cpg = cpg.substring(0, pos);
            }
        }

        return cpg;
    }

    public R2Info getR2FromMat(Integer[][] cpgHpMat, Integer col1, Integer col2, Integer r2Cov) {
        R2Info r2Info = new R2Info();

        Integer N00 = 0;
        Integer N01 = 0;
        Integer N10 = 0;
        Integer N11 = 0;

        for (int i = 0; i < cpgHpMat.length; i++) {
            if (cpgHpMat[i][col1] != null && cpgHpMat[i][col2] != null) {
                if (cpgHpMat[i][col1] == 0 && cpgHpMat[i][col2] == 0) {
                    N00++;
                } else if (cpgHpMat[i][col1] == 0 && cpgHpMat[i][col2] == 1) {
                    N01++;
                } else if (cpgHpMat[i][col1] == 1 && cpgHpMat[i][col2] == 0) {
                    N10++;
                } else if (cpgHpMat[i][col1] == 1 && cpgHpMat[i][col2] == 1) {
                    N11++;
                }
            }
        }

        if ((N00 + N01 + N10 + N11) < r2Cov) {
            return null;
        }

        /// 计算r2
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

        // 计算pvalue
        BinomialDistribution binomialDistribution = new BinomialDistribution(N.intValue(), PA * PB);
        Double pGreater = 1 - binomialDistribution.cumulativeProbability(N11);
        Double pEqual = binomialDistribution.probability(N11);
        pvalue = pGreater + pEqual;

        r2Info.setN00(N00);
        r2Info.setN01(N01);
        r2Info.setN10(N10);
        r2Info.setN11(N11);
        r2Info.setR2(r2);
        r2Info.setPvalue(pvalue);

        return r2Info;
    }

    public R2Info getR2FromList(List<MHapInfo> mHapInfoList, List<Integer> cpgPosList, Integer cpgPos1, Integer cpgPos2, Integer r2Cov) {
        R2Info r2Info = new R2Info();

        Integer N00 = 0;
        Integer N01 = 0;
        Integer N10 = 0;
        Integer N11 = 0;

        for (int i = 0; i < mHapInfoList.size(); i++) {
            MHapInfo mHapInfo = mHapInfoList.get(i);
            if (cpgPos2 < cpgPos1) {
                Integer temp = cpgPos2;
                cpgPos2 = cpgPos1;
                cpgPos1 = temp;
            }
            if (mHapInfo.getStart() <= cpgPos1 && cpgPos2 <= mHapInfo.getEnd()) {
                Integer pos1 = cpgPosList.indexOf(cpgPos1) - cpgPosList.indexOf(mHapInfo.getStart());
                Integer pos2 = cpgPosList.indexOf(cpgPos2) - cpgPosList.indexOf(mHapInfo.getStart());
                if (mHapInfo.getCpg().charAt(pos1) == '0' && mHapInfo.getCpg().charAt(pos2) == '0') {
                    N00++;
                } else if (mHapInfo.getCpg().charAt(pos1) == '0' && mHapInfo.getCpg().charAt(pos2) == '1') {
                    N01++;
                } else if (mHapInfo.getCpg().charAt(pos1) == '1' && mHapInfo.getCpg().charAt(pos2) == '0') {
                    N10++;
                } else if (mHapInfo.getCpg().charAt(pos1) == '1' && mHapInfo.getCpg().charAt(pos2) == '1') {
                    N11++;
                }
            }
        }

        if ((N00 + N01 + N10 + N11) < r2Cov) {
            return null;
        }

        /// 计算r2
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

        // 计算pvalue
        BinomialDistribution binomialDistribution = new BinomialDistribution(N.intValue(), PA * PB);
        Double pGreater = 1 - binomialDistribution.cumulativeProbability(N11);
        Double pEqual = binomialDistribution.probability(N11);
        pvalue = pGreater + pEqual;

        r2Info.setN00(N00);
        r2Info.setN01(N01);
        r2Info.setN10(N10);
        r2Info.setN11(N11);
        r2Info.setR2(r2);
        r2Info.setPvalue(pvalue);

        return r2Info;
    }

    // 保存为文件
    public void saveAsPdf(JFreeChart chart, String outputPath, int width, int height) throws DocumentException, IOException {
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputPath));
        // 设置文档大小
        Rectangle pagesize = new Rectangle(width, height);
        // 创建一个文档
        Document document = new Document(pagesize, 50, 50, 50, 50);
        // document.setPageSize(PageSize.A4); // 设置大小
        // document.setMargins(50, 50, 50, 50); // 设置边距
        // 创建writer，通过writer将文档写入磁盘
        PdfWriter pdfWriter = PdfWriter.getInstance(document, outputStream);
        // 打开文档，只有打开后才能往里面加东西
        document.open();
        // 加入统计图
        PdfContentByte pdfContentByte = pdfWriter.getDirectContent();
        PdfTemplate pdfTemplate = pdfContentByte.createTemplate(width, height);
        Graphics2D graphics2D = pdfTemplate.createGraphics(width, height, new DefaultFontMapper());
        Rectangle2D rectangle2D = new Rectangle2D.Double(0, 0, width, height);
        chart.draw(graphics2D, rectangle2D);
        graphics2D.dispose();
        pdfContentByte.addTemplate(pdfTemplate, 0, 0);
        // 关闭文档，才能输出
        document.close();
        pdfWriter.close();
    }

    public void saveAsPng(JFreeChart chart, String outputPath, int weight, int height) {
        FileOutputStream out = null;
        try {
            File outFile = new File(outputPath);
            out = new FileOutputStream(outputPath);
            // 保存为JPEG
            ChartUtils.writeChartAsJPEG(out, chart, weight, height);
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
    }

    public Double calculateMHL(List<MHapInfo> mHapInfoListMerged, Integer minK, Integer maxK) {
        Double MHL = 0.0;
        Integer maxCpgLength = 0;
        for (int i = 0; i < mHapInfoListMerged.size(); i++) {
            MHapInfo mHapInfo = mHapInfoListMerged.get(i);
            if (minK > mHapInfo.getCpg().length()) {
                log.error("calculate MHL Error: startK is too large.");
                return 0.0;
            }
            if (maxCpgLength < mHapInfo.getCpg().length()) {
                maxCpgLength = mHapInfo.getCpg().length();
            }
        }
        if (maxK > maxCpgLength) {
            maxK = maxCpgLength;
        }


        Double temp = 0.0;
        Integer w = 0;
        String fullMethStr = "";
        for (int i = 0; i < minK; i++) {
            fullMethStr += "1";
        }
        for (Integer kmer = minK; kmer < maxK + 1; kmer++) {
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

    public Double calculateMBS(List<MHapInfo> mHapInfoListMerged, Integer K) {
        Double MBS = 0.0;
        Integer kmerNum = 0;
        Double temp1 = 0.0;
        for (int i = 0; i < mHapInfoListMerged.size(); i++) {
            MHapInfo mHapInfo = mHapInfoListMerged.get(i);
            if (mHapInfo.getCpg().length() >= K) {
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

    public Double calculateEntropy(List<MHapInfo> mHapInfoListMerged, Integer K) {
        Double Entropy = 0.0;
        Map<String, Integer> kmerMap = new HashMap<>();
        Integer kmerAll = 0;
        for (int i = 0; i < mHapInfoListMerged.size(); i++) {
            MHapInfo mHapInfo = mHapInfoListMerged.get(i);
            if (mHapInfo.getCpg().length() >= K) {
                for (int j = 0; j < mHapInfo.getCpg().length() - K + 1; j++) {
                    kmerAll += mHapInfo.getCnt();
                    String kmerStr = mHapInfo.getCpg().substring(j, j + K);
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
        Entropy = - 1 / K.doubleValue() * temp;

        return Entropy;
    }



    public boolean isNumeric(String str){
        for (int i = str.length(); --i>=0 ;){
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }
}
