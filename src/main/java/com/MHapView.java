package com;

import com.args.MHapViewArgs;
import com.bean.BedInfo;
import com.bean.MHapInfo;
import com.bean.R2Info;
import com.bean.Region;
import com.common.Util;
import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.rewrite.CustomXYBlockRenderer;
import com.rewrite.CustomXYBlockRenderer2;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.DefaultXYZDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class MHapView {
    public static final Logger log = LoggerFactory.getLogger(MHapView.class);

    Util util = new Util();
    MHapViewArgs args = new MHapViewArgs();
    Region region = new Region();
    Integer width = 0;

    public void mHapView(MHapViewArgs r2Args) throws Exception {
        log.info("MHapView start!");
        args = r2Args;

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        // parse the region
        region = util.parseRegion(args.getRegion());

        // parse the mhap file
        List<MHapInfo> mHapList = util.parseMhapFile(args.getMhapPath(), region, args.getStrand(), false);

        // parse the cpg file
        List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, 500);

        boolean getMhapViewResult = getMhapView(mHapList, cpgPosList);
        if (!getMhapViewResult) {
            log.error("getMhapView fail, please check the command.");
            return;
        }

        log.info("MHapView end!");
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
        if (args.getRegion().equals("")) {
            log.error("region can not be null.");
            return false;
        }
        if (args.getTag().equals("")) {
            log.error("tag can not be null.");
            return false;
        }
        if (!args.getOutFormat().equals("png") && !args.getOutFormat().equals("pdf")) {
            log.error("The output format must be pdf or png");
            return false;
        }
        if (!args.getStrand().equals("plus") && !args.getStrand().equals("minus") && !args.getStrand().equals("both")) {
            log.error("The strand must be one of plus, minus or both");
            return false;
        }
        return true;
    }

    private boolean getMhapView(List<MHapInfo> mHapList, List<Integer> cpgPosList) throws Exception {

        // 提取查询区域内的甲基化位点列表
        List<Integer> cpgPosListInRegion = util.getCpgPosListInRegion(cpgPosList, region);

        // 甲基化状态矩阵 0-未甲基化 1-甲基化
        Integer[][] cpgHpMatInRegion = util.getCpgHpMat(mHapList, cpgPosList, cpgPosListInRegion);

        // 画布大小设置
        width = cpgHpMatInRegion[0].length * 50;

        CategoryPlot cellCntPlot = createReadCntPlot(mHapList, cpgPosListInRegion);
        CategoryPlot mmPlot = createMMPlot(cpgPosListInRegion, cpgHpMatInRegion);
        XYPlot whiteBlackPlot = createWhiteBlackPlot(cpgHpMatInRegion);
        XYPlot bedRegionPlot = new XYPlot();
        if (args.getBedPath() != null && !args.getBedPath().equals("")) {
            bedRegionPlot = createBedRegionPlot(cpgPosListInRegion);
        }
        XYPlot MHapViewHeatMapPlot = createHeatMapPlot(cpgHpMatInRegion, cpgPosListInRegion);

        List<Plot> plotList = new ArrayList<>();
        List<Integer> heightList = new ArrayList<>();
        plotList.add(cellCntPlot);
        heightList.add(width * 7 / 50);
        plotList.add(mmPlot);
        heightList.add(width / 10);
        plotList.add(whiteBlackPlot);
        heightList.add(width * 2 / 5);
        if (args.getBedPath() != null && !args.getBedPath().equals("")) {
            plotList.add(bedRegionPlot);
            heightList.add(width * 3 / 50);
        }
        plotList.add(MHapViewHeatMapPlot);
        heightList.add(width * 3 / 10);

        // 输出到文件
        String outputPath = args.getTag() + "_" + region.toFileString() + ".mHapView." + args.getOutFormat();
        if (args.getOutFormat().equals("pdf")) {
            saveAsPdf(plotList, outputPath, width, heightList, cpgPosListInRegion);
        } else if (args.getOutFormat().equals("png")) {
            saveAsPng(plotList, outputPath, width, heightList, cpgPosListInRegion);
        }

        return true;
    }

    private CategoryPlot createReadCntPlot(List<MHapInfo> mHapList, List<Integer> cpgPosListInRegion) {
        // get cell count of every cpg site
        List<Integer> readCntList = new ArrayList<>();
        for (int i = 0; i < cpgPosListInRegion.size(); i++) {
            Integer readCnt = 0;
            for (int j = 0; j < mHapList.size(); j++) {
                MHapInfo mHapInfo = mHapList.get(j);
                if (mHapInfo.getStart() <= cpgPosListInRegion.get(i) && cpgPosListInRegion.get(i) <= mHapInfo.getEnd()) {
                    readCnt += 1;
                }
            }
            readCntList.add(readCnt);
        }

        // create the barchart dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Integer maxCellCnt = 0;
        for (int i = 0; i < readCntList.size(); i++) {
            dataset.addValue(readCntList.get(i), cpgPosListInRegion.get(i), "");
            maxCellCnt = readCntList.get(i) > maxCellCnt ? readCntList.get(i) : maxCellCnt;
        }

        // X axis
        CategoryAxis categoryAxis = new CategoryAxis();
        categoryAxis.setUpperMargin(0);
        categoryAxis.setLowerMargin(0);

        // Y axis
        NumberAxis valueAxis = new NumberAxis();
        valueAxis.setRange(new Range(1, maxCellCnt * 1.1));
        valueAxis.setVisible(true);
        valueAxis.setTickUnit(new NumberTickUnit(maxCellCnt / 2));
        valueAxis.setTickLabelFont(new Font("", Font.PLAIN, width / 150));
        valueAxis.setLabel("read count");
        valueAxis.setLabelFont(new Font("", Font.PLAIN, width / 100));

        // renderer
        BarRenderer barRenderer = new BarRenderer();
        barRenderer.setBarPainter(new StandardBarPainter()); // 设置柱子为平面图不是立体的
        barRenderer.setShadowVisible(false);
        barRenderer.setDrawBarOutline(false);
        barRenderer.setMaximumBarWidth(0.015);
        barRenderer.setDefaultItemLabelsVisible(true);
        for (int i = 0; i < dataset.getRowCount(); i++) {
            barRenderer.setSeriesPaint(i, new Color(70, 130, 180));
            barRenderer.setSeriesItemLabelsVisible(i, true);
        }

        // paint bar plot
        CategoryPlot categoryPlot = new CategoryPlot(dataset, categoryAxis, valueAxis, barRenderer);
        categoryPlot.setDomainGridlinesVisible(false);
        categoryPlot.setRangeGridlinesVisible(false);

        return categoryPlot;
    }

    private CategoryPlot createMMPlot(List<Integer> cpgPosListInRegion, Integer[][] cpgHpMatInRegion) {
        // get mean methylation of every cpg site
        List<Double> mmList = new ArrayList<>();
        for (int i = 0; i < cpgPosListInRegion.size(); i++) {
            Double mm = 0.0;
            Double cpgCnt = 0.0;
            Double unCpgCnt = 0.0;
            for (int j = 0; j < cpgHpMatInRegion.length; j++) {
                if (cpgHpMatInRegion[j][i] != null) {
                    if (cpgHpMatInRegion[j][i] == 1) {
                        cpgCnt++;
                    } else {
                        unCpgCnt++;
                    }
                }
            }
            mm = cpgCnt / (cpgCnt + unCpgCnt);
            mmList.add(mm);
        }

        // create the barchart dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < mmList.size(); i++) {
            dataset.addValue(mmList.get(i), cpgPosListInRegion.get(i), "");
        }

        // X axis
        CategoryAxis categoryAxis = new CategoryAxis();
        categoryAxis.setUpperMargin(0);
        categoryAxis.setLowerMargin(0);

        // Y axis
        NumberAxis valueAxis = new NumberAxis();
        valueAxis.setRange(new Range(0, 1.1));
        valueAxis.setVisible(true);
        valueAxis.setTickUnit(new NumberTickUnit(0.5));
        valueAxis.setTickLabelFont(new Font("", Font.PLAIN, width / 150));
        valueAxis.setLabel("mean methylation");
        valueAxis.setLabelFont(new Font("", Font.PLAIN, width / 100));

        // renderer
        BarRenderer barRenderer = new BarRenderer();
        barRenderer.setBarPainter(new StandardBarPainter()); // 设置柱子为平面图不是立体的
        barRenderer.setShadowVisible(false);
        barRenderer.setDrawBarOutline(false);
        barRenderer.setMaximumBarWidth(0.015);
        barRenderer.setDefaultItemLabelsVisible(true);
        for (int i = 0; i < dataset.getRowCount(); i++) {
            barRenderer.setSeriesPaint(i, new Color(70, 130, 180));
            barRenderer.setSeriesItemLabelsVisible(i, true);
        }

        // paint bar plot
        CategoryPlot categoryPlot = new CategoryPlot(dataset, categoryAxis, valueAxis, barRenderer);
        categoryPlot.setDomainGridlinesVisible(false);
        categoryPlot.setRangeGridlinesVisible(false);

        return categoryPlot;
    }

    private XYPlot createWhiteBlackPlot(Integer[][] cpgHpMatInRegion) {

        // 创建数据集
        DefaultXYZDataset dataset = new DefaultXYZDataset();
        double x[] = new double[cpgHpMatInRegion.length * cpgHpMatInRegion[0].length];
        double y[] = new double[cpgHpMatInRegion.length * cpgHpMatInRegion[0].length];
        double z[] = new double[cpgHpMatInRegion.length * cpgHpMatInRegion[0].length];
        for (int i = 0; i < cpgHpMatInRegion.length; i++) {
            for (int j = 0; j < cpgHpMatInRegion[0].length; j++) {
                x[cpgHpMatInRegion[0].length * i + j] = j;
                y[cpgHpMatInRegion[0].length * i + j] = i;
                if (cpgHpMatInRegion[i][j] != null) {
                    if (cpgHpMatInRegion[i][j] == 0) {
                        z[cpgHpMatInRegion[0].length * i + j] = -1;
                    } else {
                        z[cpgHpMatInRegion[0].length * i + j] = 1;
                    }
                } else {
                    z[cpgHpMatInRegion[0].length * i + j] = 0;
                }
            }
        }
        double pos[][] = {x, y, z};
        dataset.addSeries( "Series" , pos);

        // xy轴
        NumberAxis xAxis = new NumberAxis();
        xAxis.setUpperMargin(0);
        xAxis.setLowerMargin(0);
        xAxis.setVisible(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickUnit(new NumberTickUnit(cpgHpMatInRegion.length * 2)); // 不让它显示y轴
        yAxis.setRange(new Range(1, cpgHpMatInRegion.length));
        yAxis.setVisible(true);
        yAxis.setLabel("cpg");
        yAxis.setLabelFont(new Font("", Font.PLAIN, width / 100));

        LookupPaintScale paintScale = new LookupPaintScale(-1, 2, Color.black);
        paintScale.add(-1, Color.white);
        paintScale.add(0, new Color(220, 220, 220));
        paintScale.add(1, Color.black);

        // 绘制色块图
        XYPlot xyPlot = new XYPlot(dataset, xAxis, yAxis, new XYBlockRenderer());
        XYBlockRenderer xyBlockRenderer = new XYBlockRenderer();
        xyBlockRenderer.setPaintScale(paintScale);
        xyBlockRenderer.setBlockHeight(1.0f);
        xyBlockRenderer.setBlockWidth(1.0f);
        xyPlot.setRenderer(xyBlockRenderer);
        xyPlot.setDomainGridlinesVisible(false); // 不显示X轴网格线
        xyPlot.setRangeGridlinesVisible(false); // 不显示Y轴网格线
        xyPlot.setOutlineVisible(false);

        return xyPlot;
    }

    private XYPlot createBedRegionPlot(List<Integer> cpgPosListInRegion) throws Exception {

        // parse the bed file
        List<BedInfo> bedInfoList = util.parseBedFile(args.getBedPath(), region);
        if (bedInfoList.size() <= 0) {
            return null;
        }
        // 创建数据集
        DefaultXYZDataset dataset = new DefaultXYZDataset();
        double x[] = new double[cpgPosListInRegion.size() * (bedInfoList.size() * 2 + 1)];
        double y[] = new double[cpgPosListInRegion.size() * (bedInfoList.size() * 2 + 1)];
        double z[] = new double[cpgPosListInRegion.size() * (bedInfoList.size() * 2 + 1)];
        for (int i = 0; i < cpgPosListInRegion.size(); i++) {
            Integer cnt = 2;
            for (int j = 0; j < bedInfoList.size(); j++) {
                BedInfo bedInfo = bedInfoList.get(j);
                if (bedInfo.getStart() <= cpgPosListInRegion.get(i) && cpgPosListInRegion.get(i) <= bedInfo.getEnd()) {
                    x[cpgPosListInRegion.size() * cnt + i] = i;
                    y[cpgPosListInRegion.size() * cnt + i] = cnt;
                    z[cpgPosListInRegion.size() * cnt + i] = 1;
                }
                cnt += 2;
            }
        }

        double pos[][] = {x, y, z};
        dataset.addSeries( "Series" , pos);

        // xy轴
        NumberAxis xAxis = new NumberAxis();
        xAxis.setVisible(false);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickUnit(new NumberTickUnit(bedInfoList.size() * 5));
        yAxis.setRange(new Range(1, bedInfoList.size() * 2 + 1));
        yAxis.setVisible(true);
        yAxis.setLabel("bed file");
        yAxis.setLabelFont(new Font("", Font.PLAIN, width / 100));

        LookupPaintScale paintScale = new LookupPaintScale(0, 2, Color.WHITE);
        paintScale.add(0, Color.WHITE);
        paintScale.add(1, new Color(70, 130, 180));

        // 绘制色块图
        XYPlot xyPlot = new XYPlot(dataset, xAxis, yAxis, new XYBlockRenderer());
        CustomXYBlockRenderer2 xyBlockRenderer = new CustomXYBlockRenderer2();
        xyBlockRenderer.setPaintScale(paintScale);
        xyBlockRenderer.setBlockHeight(0.5f);
        xyBlockRenderer.setBlockWidth(1.0f);
        xyBlockRenderer.setxBlockNum(cpgPosListInRegion.size());
        xyBlockRenderer.setyBlockNum(bedInfoList.size() * 2 + 1);
        xyBlockRenderer.setSeriesItemLabelsVisible(0, true);
        xyBlockRenderer.setSeriesItemLabelFont(0, new Font("", Font.PLAIN,
                cpgPosListInRegion.size() * 3 / (bedInfoList.size() * 2 + 1)));
        xyBlockRenderer.setSeriesItemLabelPaint(0, Color.BLACK);
        xyPlot.setRenderer(xyBlockRenderer);
        xyPlot.setDomainGridlinesVisible(false); // 不显示X轴网格线
        xyPlot.setRangeGridlinesVisible(false); // 不显示Y轴网格线
        xyPlot.setOutlineVisible(false);

        return  xyPlot;
    }

    private XYPlot createHeatMapPlot(Integer[][] cpgHpMatInRegion, List<Integer> cpgPosListInRegion) throws IOException {
        List<R2Info> r2List = new ArrayList<>();
        for (int i = 0; i < cpgPosListInRegion.size(); i++) {
            for (int j = i + 1; j < cpgPosListInRegion.size(); j++) {
                R2Info r2Info = util.getR2FromMat(cpgHpMatInRegion, i, j, 0);
                r2Info.setChrom(region.getChrom());
                r2Info.setStart(cpgPosListInRegion.get(i));
                r2Info.setEnd(cpgPosListInRegion.get(j));
                r2List.add(r2Info);
            }
        }

        // MHapView data List group by start and sorted
        Map<Integer, List<R2Info>> r2ListMap = r2List.stream().collect(Collectors.groupingBy(R2Info::getStart));
        List<Map.Entry<Integer, List<R2Info>>> r2ListMapSorted = new ArrayList<Map.Entry<Integer, List<R2Info>>>(r2ListMap.entrySet());
        Collections.sort(r2ListMapSorted, new Comparator<Map.Entry<Integer, List<R2Info>>>() { //升序排序
            public int compare(Map.Entry<Integer, List<R2Info>> o1, Map.Entry<Integer, List<R2Info>> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        // 创建数据集
        DefaultXYZDataset dataset = new DefaultXYZDataset();
        double x[] = new double[r2List.size()];
        double y[] = new double[r2List.size()];
        double z[] = new double[r2List.size()];
        int next = 0;
        for (int i = 0; i < r2ListMapSorted.size(); i++) {
            List<R2Info> r2InfoList = r2ListMapSorted.get(i).getValue();
            for (int j = 0; j < r2InfoList.size(); j++) {
                R2Info r2Info = r2InfoList.get(j);
                x[next + j] = i;
                y[next + j] = j;
                Double r2 = 0.0;
                if (r2Info != null && !r2Info.getR2().isNaN()) {
                    r2 = (r2Info.getR2() < -1 ? -1 : r2Info.getR2()) > 1 ? 1 : r2Info.getR2();
                }
                z[next + j] = r2 * r2;
            }
            next += r2InfoList.size();
        }
        double pos[][] = {x , y , z};
        dataset.addSeries( "Series" , pos);

        // xy轴
        NumberAxis xAxis = new NumberAxis();
        xAxis.setVisible(false);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickUnit(new NumberTickUnit(cpgHpMatInRegion.length * 2)); // 不让它显示y轴
        yAxis.setRange(new Range(1, cpgHpMatInRegion.length));
        yAxis.setVisible(true);
        yAxis.setLabel("MHapView HeatMap");
        yAxis.setLabelFont(new Font("", Font.PLAIN, width / 100));

        // 颜色定义
        LookupPaintScale paintScale = new LookupPaintScale(-1, 1, Color.black);
        for (Double j = 0.0; j < 255.0; j++) {
            paintScale.add((j - 255) / 255, new Color(j.intValue(), j.intValue(), 255));
        }
        for (Double j = 255.0; j < 510.0; j++) {
            paintScale.add((j - 255) / 255, new Color(255, 510 - j.intValue(), 510 - j.intValue()));
        }

        XYPlot xyPlot = new XYPlot(dataset, xAxis, yAxis, new CustomXYBlockRenderer());
        CustomXYBlockRenderer xyBlockRenderer = new CustomXYBlockRenderer();
        xyBlockRenderer.setPaintScale(paintScale);
        xyBlockRenderer.setBlockHeight(1.0f);
        xyBlockRenderer.setBlockWidth(1.0f);
        xyBlockRenderer.setBlockNum(r2ListMapSorted.size());
        xyPlot.setRenderer(xyBlockRenderer);
        xyPlot.setDomainGridlinesVisible(false); // 不显示X轴网格线
        xyPlot.setRangeGridlinesVisible(false); // 不显示Y轴网格线
        xyPlot.setOutlineVisible(false);

        return  xyPlot;
    }

    // 保存为文件
    public void saveAsPdf(List<Plot> plotList, String outputPath, Integer width, List<Integer> heightList,
                           List<Integer> cpgPosListInRegion) throws FileNotFoundException, DocumentException {
        width = width > 14400 ? 14400 : width;
        Integer sumHeight = 0;
        for (int i = 0; i < heightList.size(); i++) {
            sumHeight += heightList.get(i);
        }
        if (sumHeight > 14400) {
            width = width / sumHeight * 14400;
            sumHeight = 14400;
        }

        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputPath));
        // 设置文档大小
        com.itextpdf.text.Rectangle pagesize = new com.itextpdf.text.Rectangle(width, sumHeight);
        // 创建一个文档
        Document document = new Document(pagesize, 50, 50, 50, 50);
        // 创建writer，通过writer将文档写入磁盘
        PdfWriter pdfWriter = PdfWriter.getInstance(document, outputStream);
        // 打开文档，只有打开后才能往里面加东西
        document.open();
        // 加入统计图
        PdfContentByte pdfContentByte = pdfWriter.getDirectContent();
        PdfTemplate pdfTemplate = pdfContentByte.createTemplate(width, sumHeight);
        Graphics2D graphics2D = pdfTemplate.createGraphics(width, sumHeight, new DefaultFontMapper());

        Integer nextHeight = 0;
        for (int i = 0; i < plotList.size(); i++) {
            JFreeChart jFreeChart = new JFreeChart("", null, plotList.get(i), false);
            if (i == 0) {
                jFreeChart = new JFreeChart(region.toHeadString(), new Font("", Font.PLAIN, sumHeight / 30), plotList.get(i), false);
            } else if (i == plotList.size() - 1) {
                // 颜色定义
                LookupPaintScale paintScale = new LookupPaintScale(-1, 1, Color.black);
                for (Double j = 0.0; j < 255.0; j++) {
                    paintScale.add((j - 255) / 255, new Color(j.intValue(), j.intValue(), 255));
                }
                for (Double j = 255.0; j < 510.0; j++) {
                    paintScale.add((j - 255) / 255, new Color(255, 510 - j.intValue(), 510 - j.intValue()));
                }

                // 颜色示意图
                PaintScaleLegend paintScaleLegend = new PaintScaleLegend(paintScale, new NumberAxis());
                paintScaleLegend.setStripWidth(width / cpgPosListInRegion.size() / 5);
                paintScaleLegend.setPosition(RectangleEdge.RIGHT);
                paintScaleLegend.setAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
                paintScaleLegend.setMargin(heightList.get(i) * 1 / 3, 0, heightList.get(i) * 1 / 3, 0);
                jFreeChart.addSubtitle(paintScaleLegend);
            }
            jFreeChart.setBackgroundPaint(Color.WHITE);

            Rectangle2D rectangle2D0 = new Rectangle2D.Double(0, nextHeight, width, heightList.get(i));
            jFreeChart.draw(graphics2D, rectangle2D0);
            pdfContentByte.addTemplate(pdfTemplate, 0, 0);
            nextHeight += heightList.get(i);
        }
        graphics2D.dispose();

        // 关闭文档，才能输出
        document.close();
        pdfWriter.close();
    }

    public void saveAsPng(List<Plot> plotList, String outputPath, Integer width, List<Integer> heightList,
                          List<Integer> cpgPosListInRegion) throws IOException {
        File outFile = new File(outputPath);
        Integer sumHeight = 0;
        for (int i = 0; i < heightList.size(); i++) {
            sumHeight += heightList.get(i);
        }
        BufferedImage bufferedImage = new BufferedImage(width, sumHeight, BufferedImage.TYPE_INT_RGB);

        Integer nextHeight = 0;
        for (int i = 0; i < plotList.size(); i++) {
            Graphics2D graphics2D = bufferedImage.createGraphics();

            JFreeChart jFreeChart = new JFreeChart("", null, plotList.get(i), false);
            if (i == 0) {
                jFreeChart = new JFreeChart(region.toHeadString(), new Font("", Font.PLAIN, sumHeight / 30), plotList.get(i), false);
            } else if (i == plotList.size() - 1) {
                // 颜色定义
                LookupPaintScale paintScale = new LookupPaintScale(-1, 1, Color.black);
                for (Double j = 0.0; j < 255.0; j++) {
                    paintScale.add((j - 255) / 255, new Color(j.intValue(), j.intValue(), 255));
                }
                for (Double j = 255.0; j < 510.0; j++) {
                    paintScale.add((j - 255) / 255, new Color(255, 510 - j.intValue(), 510 - j.intValue()));
                }

                // 颜色示意图
                PaintScaleLegend paintScaleLegend = new PaintScaleLegend(paintScale, new NumberAxis());
                paintScaleLegend.setStripWidth(width / cpgPosListInRegion.size() / 5);
                paintScaleLegend.setPosition(RectangleEdge.RIGHT);
                paintScaleLegend.setAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
                paintScaleLegend.setMargin(heightList.get(i) * 1 / 3, 0, heightList.get(i) * 1 / 3, 0);
                jFreeChart.addSubtitle(paintScaleLegend);
            }
            jFreeChart.setBackgroundPaint(Color.WHITE);
            Rectangle2D rectangle2D0 = new Rectangle2D.Double(0, nextHeight, width, heightList.get(i));
            jFreeChart.draw(graphics2D, rectangle2D0);
            nextHeight += heightList.get(i);
            graphics2D.dispose();
        }

        RenderedImage rendImage = bufferedImage;
        ImageIO.write(rendImage, "png", outFile);
    }
}
