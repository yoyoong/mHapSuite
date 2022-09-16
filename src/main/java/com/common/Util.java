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
        // create the output directory
        File outputDir = new File(directory);
        if (!outputDir.exists()){
            if (!outputDir.mkdirs()){
                log.error("create" + outputDir.getAbsolutePath() + "fail");
                return null;
            }
        }

        // create the output file
        File file = new File(directory + "/" + fileName);
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

    public List<Integer> getcpgPosListInRegion(List<Integer> cpgPosList, Region region) throws Exception {
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
                for (int i = 0; i < mHapInfo.getCnt(); i++) {
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

    public R2Info getR2Info(Integer[][] cpgHpMat, Integer col1, Integer col2, Integer rowNum) {
        R2Info r2Info = new R2Info();

        Integer N00 = 0;
        Integer N01 = 0;
        Integer N10 = 0;
        Integer N11 = 0;

        for (int i = 0; i < rowNum; i++) {
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

}
