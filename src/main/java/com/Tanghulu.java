package com;

import com.args.TanghuluArgs;
import com.bean.MHapInfo;
import com.bean.MHapInfo;
import com.bean.Region;
import com.common.Util;
import com.rewrite.CustomXYLineAndShapeRenderer;
import com.rewrite.CustomXYLineAndShapeRenderer2;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.random;

public class Tanghulu {
    public static final Logger log = LoggerFactory.getLogger(Tanghulu.class);

    TanghuluArgs args = new TanghuluArgs();
    Util util = new Util();

    public void tanghulu(TanghuluArgs tanghuluArgs) throws Exception {
        log.info("Tanghulu start!");
        args = tanghuluArgs;

        // parse the region
        Region region = util.parseRegion(args.getRegion());
        if (region.getEnd() - region.getStart() > args.getMaxLength()) {
            log.info("The region is larger than " + args.getMaxLength()
                    + ", it's not recommand to do tanghulu plotting and we will cut the superfluous region.");
            region.setEnd(region.getStart() + args.getMaxLength());
        }

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        // parse the cpg file
        List<Integer> cpgPosList = util.parseCpgFileWithShift(args.getCpgPath(), region, 500);

        // parse the mhap file
        List<MHapInfo> mHapInfoList = util.parseMhapFile(args.getMhapPath(), region, args.getStrand(), args.getMerge());
        if (mHapInfoList.size() > args.getMaxReads()) {
            log.info("The reads is larger than " + args.getMaxReads()
                    + ", it's not recommand to do tanghulu plotting and we will cut the superfluous reads.");
            mHapInfoList = mHapInfoList.subList(0, args.getMaxReads());
        }

        // merge the same mhap after cut read
        if (args.getCutReads()) {
            List<MHapInfo> mHapInfoListCutReadsMerged = new ArrayList<>();
            for (MHapInfo mHapInfo : mHapInfoList) {
                // get cpg site list in region
                List<Integer> cpgPosListInRegion = util.getCpgPosListInRegion(cpgPosList, region);
                String cpg = util.cutReads(mHapInfo, cpgPosList, cpgPosListInRegion);
                mHapInfo.setCpg(cpg);
                if (mHapInfo.getStart() < cpgPosListInRegion.get(0)) {
                    mHapInfo.setStart(cpgPosListInRegion.get(0));
                }
                if (mHapInfo.getEnd() > cpgPosListInRegion.get(cpgPosListInRegion.size() - 1)) {
                    mHapInfo.setEnd(cpgPosListInRegion.get(cpgPosListInRegion.size() - 1));
                }
            }

            mHapInfoList.parallelStream().collect(Collectors.groupingBy(o -> (o.index()), Collectors.toList())).forEach(
                    (id, transfer) -> {transfer.stream().reduce((a, b) ->
                         new MHapInfo(a.getChrom(), a.getStart(), a.getEnd(), a.getCpg(), a.getCnt() + b.getCnt(), a.getStrand()))
                            .ifPresent(mHapInfoListCutReadsMerged::add);
                    }
            );
            mHapInfoListCutReadsMerged.sort(Comparator.comparing(MHapInfo::getStart));

            mHapInfoList = mHapInfoListCutReadsMerged;
        }

        if (args.getSimulation()) {
            boolean simulationResult = simulation(mHapInfoList, cpgPosList, region);
            if (!simulationResult) {
                log.error("simulation fail, please check the command.");
                return;
            }
        } else {
            boolean tanghuluResult = paintTanghulu(mHapInfoList, cpgPosList, region);
            if (!tanghuluResult) {
                log.error("tanghulu fail, please check the command.");
                return;
            }
        }

        log.info("Tanghulu end!");
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
        if (!args.getOutFormat().equals("png") && !args.getOutFormat().equals("pdf")) {
            log.error("The output format must be pdf or png");
            return false;
        }
        if (!args.getStrand().equals("plus") && !args.getStrand().equals("minus") && !args.getStrand().equals("both")) {
            log.error("The strand must be one of plus, minus or both");
            return false;
        }
        if (args.getSimulation() && args.getMerge()) {
            log.error("Can not enter both simulation and merge");
            return false;
        }
        if (args.getSimulation() && args.getCutReads()) {
            log.info("The cutReads parameter is invalid when input simulation command");
        }
        if (args.getMaxReads() > 50) {
            log.error("The reads is larger than 50, it's not recommand to do tanghulu plotting, please re-enter the maxReads");
            return false;
        }
        if (args.getMaxLength() > 2000) {
            log.error("The region is larger than 2000, it's not recommand to do tanghulu plotting, please re-enter the maxLength");
            return false;
        }

        return true;
    }

    private boolean paintTanghulu(List<MHapInfo> mHapInfoList, List<Integer> cpgPosList, Region region) throws Exception {
        // create the dataset
        XYSeriesCollection dataset = new XYSeriesCollection();
        Integer startPos = Integer.MAX_VALUE; // 最近的甲基化位点
        Integer endPos = 0; // 最远的甲基化位点

        for (int i = 0; i < mHapInfoList.size(); i++) {
            MHapInfo mHapInfo = mHapInfoList.get(i);
            String cpg = mHapInfo.getCpg();
            Integer startCpgPos = mHapInfo.getStart();

            if (mHapInfo.getStart() < startPos) { // 找出最远的甲基化位点
                startPos = mHapInfo.getStart();
            }
            if (mHapInfo.getEnd() > endPos) { // 找出最远的甲基化位点
                endPos = mHapInfo.getEnd();
            }

            XYSeries allSeries = new XYSeries(i + mHapInfo.indexByPos()
                    + "_" + cpg + "*" + mHapInfo.getCnt()); // 将mhap中的cpg和cnt存入，合并时显示合并read数用
            XYSeries cpgSeries = new XYSeries("cpg" + i + mHapInfo.indexByRead());
            XYSeries unCpgSeries = new XYSeries("unCpg" + i + mHapInfo.indexByRead());
            Integer pos = cpgPosList.indexOf(startCpgPos); // mhap某行起点在cpgPosList的位置
            for (int j = 0; j < cpg.length(); j++) {
                allSeries.add(cpgPosList.get(pos + j), Integer.valueOf(i + 1));
                if (cpg.charAt(j) == '1') {
                    cpgSeries.add(cpgPosList.get(pos + j), Integer.valueOf(i + 1));
                } else {
                    unCpgSeries.add(cpgPosList.get(pos + j), Integer.valueOf(i + 1));
                }
            }
            // 全部节点和甲基化节点交替加入
            dataset.addSeries(allSeries);
            dataset.addSeries(cpgSeries);
            dataset.addSeries(unCpgSeries);
        }
        XYSeries alignSeries = new XYSeries("Align series");
        for (Integer i = cpgPosList.indexOf(startPos); i < cpgPosList.indexOf(endPos) + 1; i++) {
            alignSeries.add(cpgPosList.get(i), Integer.valueOf(0));
        }
        dataset.addSeries(alignSeries);

        // 绘制XY图
        File tempFile = new File(args.getMhapPath());
        String title = args.getRegion() + "(" + tempFile.getName() + ")";
        JFreeChart jfreechart = ChartFactory.createXYLineChart(title, // 标题
                "Genomic position", // categoryAxisLabel （category轴，横轴，X轴标签）
                "", // valueAxisLabel（value轴，纵轴，Y轴的标签）
                dataset, // dataset
                PlotOrientation.VERTICAL,
                true, // legend
                false, // tooltips
                false); // URLs

        XYPlot xyPlot = jfreechart.getXYPlot( );
        xyPlot.setBackgroundPaint(Color.WHITE); // 背景色
        xyPlot.setDomainGridlinesVisible(false); // 不显示X轴网格线
        xyPlot.setRangeGridlinesVisible(false); // 不显示Y轴网格线
        xyPlot.setOutlineVisible(false); // 不显示数据区的边界线条

        // 画布大小设置
        Integer width = (region.getEnd() - region.getStart()) * 15;
        width = width > 14400 ? 14400 : width;
        Integer height = dataset.getSeriesCount() * 15;
        height = height > 14400 ? 14400 : height;

        CustomXYLineAndShapeRenderer xyLineAndShapeRenderer = new CustomXYLineAndShapeRenderer();
        xyLineAndShapeRenderer.setDefaultItemLabelsVisible(args.getMerge() ? true : false); // 是否显示合并个数

        // 普通糖葫芦格式设置
        Double circleSize = 20.0;
        Shape circle = new Ellipse2D.Double(-circleSize / 2, -circleSize / 2, circleSize, circleSize);
        for (int i = 0; i < dataset.getSeriesCount() - 1; i++) { // 糖葫芦样式设置
            if (i % 3 == 0) { // 全部节点画空心圆
                xyLineAndShapeRenderer.setSeriesShape(i, circle);
                xyLineAndShapeRenderer.setSeriesShapesFilled(i, false);
                MHapInfo mHapInfo = mHapInfoList.get(i / 3);
                if (mHapInfo.getStrand().equals("+")) {
                    xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
                } else {
                    xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLUE);
                }

                ItemLabelPosition itemLabelPosition1 = new ItemLabelPosition(ItemLabelAnchor.INSIDE6,
                        TextAnchor.CENTER, TextAnchor.CENTER, 0);
                xyLineAndShapeRenderer.setSeriesPositiveItemLabelPosition(i, itemLabelPosition1);
            } else if (i % 3 == 1) { // 甲基化节点画实心圆
                xyLineAndShapeRenderer.setSeriesShape(i, circle);
                xyLineAndShapeRenderer.setSeriesShapesFilled(i, true);
                xyLineAndShapeRenderer.setSeriesLinesVisible(i, false);
                MHapInfo mHapInfo = mHapInfoList.get(i / 3);
                if (mHapInfo.getStrand().equals("+")) {
                    xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
                } else {
                    xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLUE);
                }
            } else { // 非甲基化节点以白色填充
                Shape circle1 = new Ellipse2D.Double(-circleSize / 2, -circleSize / 2, circleSize - 2, circleSize - 2);
                xyLineAndShapeRenderer.setSeriesShape(i, circle1);
                xyLineAndShapeRenderer.setSeriesShapesFilled(i, true);
                xyLineAndShapeRenderer.setSeriesLinesVisible(i, false);
                xyLineAndShapeRenderer.setSeriesPaint(i, Color.WHITE);
            }
        }
        // cpg位点刻度形状设置
        xyLineAndShapeRenderer.setSeriesShape(dataset.getSeriesCount() - 1, circle);
        xyLineAndShapeRenderer.setSeriesShapesFilled(dataset.getSeriesCount() - 1, true);
        xyLineAndShapeRenderer.setSeriesLinesVisible(dataset.getSeriesCount() - 1, true);
        xyLineAndShapeRenderer.setSeriesPaint(dataset.getSeriesCount() - 1, Color.GRAY);
        xyLineAndShapeRenderer.setSeriesItemLabelsVisible(dataset.getSeriesCount() - 1, true);
        xyLineAndShapeRenderer.setDefaultLegendShape(circle);

        xyPlot.setRenderer(xyLineAndShapeRenderer);

        // cpg位点刻度数字设置
        XYItemRenderer xyItemRenderer = xyPlot.getRenderer();
        DecimalFormat decimalformat = new DecimalFormat("############"); // 显示数据值的格式
        xyItemRenderer.setDefaultItemLabelGenerator(new StandardXYItemLabelGenerator("{1}",
                decimalformat, decimalformat)); // 显示X轴的值（cpg位点位置）
        ItemLabelPosition itemLabelPosition = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6,
                TextAnchor.TOP_CENTER, TextAnchor.CENTER, -0.5D); // 显示数据值的位置
        xyItemRenderer.setSeriesPositiveItemLabelPosition(dataset.getSeriesCount() - 1, itemLabelPosition);
        xyItemRenderer.setSeriesItemLabelPaint(dataset.getSeriesCount() - 1, Color.GRAY);
        // Double fontSize = circleSize < 10 ? 10 : (circleSize > 20 ? 20 : circleSize); // 字体大小
        Double fontSize = 20.0;
        xyItemRenderer.setSeriesItemLabelFont(dataset.getSeriesCount() - 1, new Font("", Font.PLAIN, fontSize.intValue()));

        xyPlot.setRenderer(xyItemRenderer);


        // X轴设置
        ValueAxis domainAxis = xyPlot.getDomainAxis();
        domainAxis.setVisible(false);

        // Y轴设置
        NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
        NumberTickUnit numberTickUnit = new NumberTickUnit(1);
        rangeAxis.setTickUnit(numberTickUnit);
        Range range = new Range(-2, mHapInfoList.size() + 2);
        rangeAxis.setRange(range);

        // 输出到文件
        String outputFile = "";
        if (args.getOutFormat().equals("png")) {
            outputFile = args.getOutputFile() + ".tanghulu.png";
            util.saveAsPng(jfreechart, outputFile, width, height);
        } else {
            outputFile = args.getOutputFile() + ".tanghulu.pdf";
            util.saveAsPdf(jfreechart, outputFile, width, height);
        }

        return true;
    }

    private boolean simulation(List<MHapInfo> mHapInfoList, List<Integer> cpgPosList, Region region) throws Exception {
        // 提取查询区域内的甲基化位点列表
        List<Integer> cpgPosListInRegion = util.getCpgPosListInRegion(cpgPosList, region);
        if (cpgPosListInRegion.size() > 20) {
            log.error("nums of cpg are larger than 20 " + cpgPosListInRegion.size());
            return false;
        }

        // 取区域内的甲基化状态列表
        Integer[][] cpgHpMatInRegion = new Integer[mHapInfoList.size()][cpgPosListInRegion.size()];
        for (int i = 0; i < mHapInfoList.size(); i++) {
            for (int j = 0; j < cpgPosListInRegion.size(); j++) {
                cpgHpMatInRegion[i][j] = 0;
            }
        }
        for (int i = 0; i < mHapInfoList.size(); i++) {
            MHapInfo mHapInfo = mHapInfoList.get(i);
            Integer pos = cpgPosList.indexOf(cpgPosListInRegion.get(0)) - cpgPosList.indexOf(mHapInfo.getStart());
            for (int k = pos > 0 ? pos : 0; k < mHapInfo.getCpg().length(); k++) {
                if (k - pos < cpgPosListInRegion.size()) {
                    if (mHapInfo.getCpg().charAt(k) == '0') {
                        cpgHpMatInRegion[i][k - pos] = -1;
                    } else {
                        cpgHpMatInRegion[i][k - pos] = 1;
                    }
                }
            }
        }

        // 求每个甲基化位点的甲基化比率和平均甲基化率
        List<Double> cpgRateList = new ArrayList<>();
        for (int i = 0; i < cpgPosListInRegion.size(); i++) {
            Double cpgRate = 0.0;
            Double cpgCnt = 0.0;
            Double allCnt = 0.0;
            for (int j = 0; j < mHapInfoList.size(); j++) {
                if (cpgHpMatInRegion[j][i] != 0) {
                    if (cpgHpMatInRegion[j][i] == 1) {
                        cpgCnt++;
                    }
                    allCnt++;
                }
            }
            cpgRate = cpgCnt / allCnt;
            cpgRateList.add(cpgRate);
        }

        // 求平均甲基化率
        Double sumCpgRate = 0.0;
        Double sumCpgCnt = 0.0;
        Double sumAllCnt = 0.0;
        for (int i = 0; i < mHapInfoList.size(); i++) {
            MHapInfo mHapInfo = mHapInfoList.get(i);
            for (int j = 0; j < mHapInfo.getCpg().length(); j++) {
                String cpg = mHapInfo.getCpg();
                if (cpg.charAt(j) == '1') {
                    sumCpgCnt += mHapInfo.getCnt();
                }
                sumAllCnt += mHapInfo.getCnt();
            }
        }
        sumCpgRate = sumCpgCnt / sumAllCnt;

        // TODO
        List<Double[][]> toLeftList = new ArrayList<>();
        List<Double[][]> toRightList = new ArrayList<>();
        for (int i = 0; i < cpgPosListInRegion.size(); i++) {
            Double[][] toLeft = new Double[2][2];
            Double toLeft_1Not0 = 0.0;
            Double toLeft1Not0 = 0.0;
            Double toLeft_1_1 = 0.0;
            Double toLeft_11 = 0.0;
            Double toLeft1_1 = 0.0;
            Double toLeft11 = 0.0;
            if (i == 0) {
                toLeft = new Double[][]{{0.0, 0.0}, {0.0, 0.0}};
            } else {
                for (int j = 0; j < mHapInfoList.size(); j++) {
                    if (cpgHpMatInRegion[j][i] != 0 && cpgHpMatInRegion[j][i - 1] != 0) {
                        if (cpgHpMatInRegion[j][i] == -1) {
                            if (cpgHpMatInRegion[j][i - 1] == -1) {
                                toLeft_1_1++;
                            } else if (cpgHpMatInRegion[j][i - 1] == 1) {
                                toLeft_11++;
                            }
                            toLeft_1Not0++;
                        } else if (cpgHpMatInRegion[j][i] == 1) {
                            if (cpgHpMatInRegion[j][i - 1] == -1) {
                                toLeft1_1++;
                            } else if (cpgHpMatInRegion[j][i - 1] == 1) {
                                toLeft11++;
                            }
                            toLeft1Not0++;
                        }
                    }
                }
                toLeft[0][0] = toLeft_1Not0 == 0.0 ? 0.0 : toLeft_1_1 / toLeft_1Not0;
                toLeft[0][1] = toLeft_1Not0 == 0.0 ? 0.0 : toLeft_11 / toLeft_1Not0;
                toLeft[1][0] = toLeft1Not0 == 0.0 ? 0.0 : toLeft1_1 / toLeft1Not0;
                toLeft[1][1] = toLeft1Not0 == 0.0 ? 0.0 : toLeft11 / toLeft1Not0;
            }
            toLeftList.add(toLeft);

            Double[][] toRight = new Double[2][2];
            Double toRight_1Not0 = 0.0;
            Double toRight1Not0 = 0.0;
            Double toRight_1_1 = 0.0;
            Double toRight_11 = 0.0;
            Double toRight1_1 = 0.0;
            Double toRight11 = 0.0;
            if (i == cpgPosListInRegion.size() - 1) {
                toRight = new Double[][]{{0.0, 0.0}, {0.0, 0.0}};
            } else {
                for (int j = 0; j < mHapInfoList.size(); j++) {
                    if (cpgHpMatInRegion[j][i] != 0 && cpgHpMatInRegion[j][i + 1] != 0) {
                        if (cpgHpMatInRegion[j][i] == -1) {
                            if (cpgHpMatInRegion[j][i + 1] == -1) {
                                toRight_1_1++;
                            } else if (cpgHpMatInRegion[j][i + 1] == 1) {
                                toRight_11++;
                            }
                            toRight_1Not0++;
                        } else if (cpgHpMatInRegion[j][i] == 1) {
                            if (cpgHpMatInRegion[j][i + 1] == -1) {
                                toRight1_1++;
                            } else if (cpgHpMatInRegion[j][i + 1] == 1) {
                                toRight11++;
                            }
                            toRight1Not0++;
                        }
                    }
                }
                toRight[0][0] = toRight_1Not0 == 0.0 ? 0.0 : toRight_1_1 / toRight_1Not0;
                toRight[0][1] = toRight_1Not0 == 0.0 ? 0.0 : toRight_11 / toRight_1Not0;
                toRight[1][0] = toRight1Not0 == 0.0 ? 0.0 : toRight1_1 / toRight1Not0;
                toRight[1][1] = toRight1Not0 == 0.0 ? 0.0 : toRight11 / toRight1Not0;
            }
            toRightList.add(toRight);
        }

        // TODO
        for (int i = 0; i < mHapInfoList.size(); i++) {
            for (int j = 0; j < cpgPosListInRegion.size(); j++) {
                dfs(i, j, cpgHpMatInRegion, toLeftList, toRightList, cpgPosListInRegion);
            }
        }

        // 将二维列表中的-1改成0
        for (int i = 0; i < mHapInfoList.size(); i++) {
            for (int j = 0; j < cpgPosListInRegion.size(); j++) {
                cpgHpMatInRegion[i][j] = cpgHpMatInRegion[i][j] == -1 ? 0 : 1;
            }
        }

        // 取10个样本
        Random random =new Random();
        Integer[][] cpgHpMatSample = new Integer[10][cpgPosList.size()];
        for (int i = 0; i < 10; i++) {
            Integer randomNum = random.nextInt(mHapInfoList.size());
            cpgHpMatSample[i] = cpgHpMatInRegion[randomNum];
        }

        // 求样本中每列的甲基化比率
        List<Double> cpgRateListInSample = new ArrayList<>();
        for (int j = 0; j < cpgPosListInRegion.size(); j++) {
            Double cpgNum = 0.0;
            for (int i = 0; i < cpgHpMatSample.length; i++) {
                if (cpgHpMatSample[i][j] == 1) {
                    cpgNum++;
                }
            }
            cpgRateListInSample.add(cpgNum / cpgHpMatSample.length);
        }

        // 求样本与原甲基化比率的损失值
        Double loss = 0.0;
        for (int i = 0; i < cpgPosListInRegion.size(); i++) {
            loss += Math.pow((cpgRateList.get(i) - cpgRateListInSample.get(i)), 2);
        }

        // 模拟退火算法取损失值最小的样本
        while (cpgHpMatSample.length < 20) {
            Integer randomNum = random.nextInt(mHapInfoList.size());
            Integer[][] newCpgHpMatSample = new Integer[cpgHpMatSample.length + 1][cpgPosList.size()];
            for (int i = 0; i < cpgHpMatSample.length; i++) {
                newCpgHpMatSample[i] = cpgHpMatSample[i];
            }
            newCpgHpMatSample[cpgHpMatSample.length] = cpgHpMatInRegion[randomNum];

            // 求新样本中每列的甲基化比率
            List<Double> newCpgRateListInSample = new ArrayList<>();
            for (int j = 0; j < cpgPosListInRegion.size(); j++) {
                Double cpgNum = 0.0;
                for (int i = 0; i < newCpgHpMatSample.length; i++) {
                    if (newCpgHpMatSample[i][j] == 1) {
                        cpgNum++;
                    }
                }
                newCpgRateListInSample.add(cpgNum / newCpgHpMatSample.length);
            }

            // 求新样本与原甲基化比率的损失值
            Double newLoss = 0.0;
            for (int i = 0; i < cpgPosListInRegion.size(); i++) {
                newLoss += Math.pow((cpgRateList.get(i) - newCpgRateListInSample.get(i)), 2);
            }

            if (newLoss <= loss) {
                cpgHpMatSample = newCpgHpMatSample;
                loss = newLoss;
            } else {
                if (random() < 0.1) {
                    cpgHpMatSample = newCpgHpMatSample;
                }
            }
        }

        // 按甲基化个数递增排序
        Arrays.sort(cpgHpMatSample, new Comparator<Integer[]>() {
            public int compare(Integer[] a, Integer[] b){
                Integer cpgNumA = 0;
                for (int i = 0; i < a.length; i++) {
                    if (a[i] == 1) {
                        cpgNumA++;
                    }
                }
                Integer cpgNumB = 0;
                for (int i = 0; i < b.length; i++) {
                    if (b[i] == 1) {
                        cpgNumB++;
                    }
                }
                return cpgNumA - cpgNumB;
            }
        });

        // 画布大小设置
        Integer width = cpgPosListInRegion.size() * 100;
        Integer height = width / (2 * cpgPosListInRegion.size() + 1) * 40;

        // 创建数据集
        XYSeriesCollection dataset = new XYSeriesCollection();
        for (int i = 0; i < cpgHpMatSample.length; i++) {
            XYSeries allSeries = new XYSeries(i + String.valueOf(cpgHpMatSample)); // 将mhap中的cpg和cnt存入，合并时显示合并read数用
            XYSeries cpgSeries = new XYSeries("cpg" + i + String.valueOf(cpgHpMatSample));
            XYSeries unCpgSeries = new XYSeries("unCpg" + i + String.valueOf(cpgHpMatSample));
            for (int j = 0; j < cpgHpMatSample[i].length; j++) {
                Integer xPos = width / (cpgPosListInRegion.size() + 1) * (j + 1);
                allSeries.add(xPos, Integer.valueOf(cpgHpMatSample.length - i));
                if (cpgHpMatSample[i][j] == 1) {
                    cpgSeries.add(xPos, Integer.valueOf(cpgHpMatSample.length - i));
                } else {
                    unCpgSeries.add(xPos, Integer.valueOf(cpgHpMatSample.length - i));
                }
            }
            // 全部节点和甲基化节点交替加入
            dataset.addSeries(allSeries);
            dataset.addSeries(cpgSeries);
            dataset.addSeries(unCpgSeries);
        }
        XYSeries alignSeries = new XYSeries("Align series");
        for (Integer i = 0; i < cpgPosListInRegion.size(); i++) {
            Integer xPos = width / (cpgPosListInRegion.size() + 1) * (i + 1);
            alignSeries.add(xPos, Integer.valueOf(0));
        }
        dataset.addSeries(alignSeries);

        // 绘制折线图
        String title = "Average methylation:" + String.format("%1.8f" , sumCpgRate);
        JFreeChart jfreechart = ChartFactory.createXYLineChart(title, // 标题
                "Genomic position", // categoryAxisLabel （category轴，横轴，X轴标签）
                "", // valueAxisLabel（value轴，纵轴，Y轴的标签）
                dataset, // dataset
                PlotOrientation.VERTICAL,
                false, // legend
                false, // tooltips
                false); // URLs

        jfreechart.setTitle(new TextTitle(title, new Font("", 0, width / (2 * cpgPosListInRegion.size() + 1))));
        XYPlot xyPlot = jfreechart.getXYPlot( );
        xyPlot.setBackgroundPaint(Color.WHITE); // 背景色
        xyPlot.setDomainGridlinesVisible(false); // 不显示X轴网格线
        xyPlot.setRangeGridlinesVisible(false); // 不显示Y轴网格线
        xyPlot.setOutlineVisible(true); // 显示数据区的边界线条

        CustomXYLineAndShapeRenderer2 xyLineAndShapeRenderer = new CustomXYLineAndShapeRenderer2();
        xyLineAndShapeRenderer.setLabelList(cpgPosListInRegion);

        // 普通糖葫芦格式设置
        Double circleSize = (double) width / (2 * cpgPosListInRegion.size() + 1);  // 糖葫芦大小
        Shape circle = new Ellipse2D.Double(-circleSize / 2, -circleSize / 2, circleSize, circleSize);
        for (int i = 0; i < dataset.getSeriesCount() - 1; i++) { // 糖葫芦样式设置
            if (i % 3 == 0) { // 全部节点画空心圆
                xyLineAndShapeRenderer.setSeriesShape(i, circle);
                xyLineAndShapeRenderer.setSeriesShapesFilled(i, false);
                xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
            } else if (i % 3 == 1) { // 甲基化节点画实心圆
                xyLineAndShapeRenderer.setSeriesShape(i, circle);
                xyLineAndShapeRenderer.setSeriesShapesFilled(i, true);
                xyLineAndShapeRenderer.setSeriesLinesVisible(i, false);
                xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
            } else { // 非甲基化节点以白色填充
                Shape circle1 = new Ellipse2D.Double(-circleSize / 2, -circleSize / 2, circleSize - 2, circleSize - 2);
                xyLineAndShapeRenderer.setSeriesShape(i, circle1);
                xyLineAndShapeRenderer.setSeriesShapesFilled(i, true);
                xyLineAndShapeRenderer.setSeriesLinesVisible(i, false);
                xyLineAndShapeRenderer.setSeriesPaint(i, Color.WHITE);
            }
        }
        // cpg位点刻度形状设置
//        xyLineAndShapeRenderer.setSeriesShape(dataset.getSeriesCount() - 1, circle);
//        xyLineAndShapeRenderer.setSeriesShapesFilled(dataset.getSeriesCount() - 1, true);
        xyLineAndShapeRenderer.setSeriesLinesVisible(dataset.getSeriesCount() - 1, false);
//        xyLineAndShapeRenderer.setSeriesPaint(dataset.getSeriesCount() - 1, Color.GRAY);
//        xyLineAndShapeRenderer.setDefaultLegendShape(circle);
        xyLineAndShapeRenderer.setSeriesShapesVisible(dataset.getSeriesCount() - 1, false);
        xyLineAndShapeRenderer.setSeriesItemLabelsVisible(dataset.getSeriesCount() - 1, true);


        xyPlot.setRenderer(xyLineAndShapeRenderer);

        // cpg位点刻度数字设置
        XYItemRenderer xyItemRenderer = xyPlot.getRenderer();
        DecimalFormat decimalformat = new DecimalFormat("############"); // 显示数据值的格式
        xyItemRenderer.setDefaultItemLabelGenerator(new StandardXYItemLabelGenerator("{1}",
                decimalformat, decimalformat)); // 显示X轴的值（cpg位点位置）
        ItemLabelPosition itemLabelPosition = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6,
                TextAnchor.TOP_CENTER, TextAnchor.CENTER, -0.8D); // 显示数据值的位置
        xyItemRenderer.setSeriesPositiveItemLabelPosition(dataset.getSeriesCount() - 1, itemLabelPosition);
        xyItemRenderer.setSeriesItemLabelPaint(dataset.getSeriesCount() - 1, Color.GRAY);
        xyItemRenderer.setSeriesItemLabelFont(dataset.getSeriesCount() - 1, new Font("", Font.PLAIN, circleSize.intValue() / 2));

        xyPlot.setRenderer(xyItemRenderer);

        // X轴设置
        ValueAxis domainAxis = xyPlot.getDomainAxis();
        domainAxis.setLowerMargin(0.1);
        domainAxis.setUpperMargin(0.1);
        domainAxis.setVisible(false);

        // Y轴设置
        NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
        Range range = new Range(0, 20);
        rangeAxis.setRangeWithMargins(range);
        rangeAxis.setVisible(false);

        // 输出到文件
        String outputFile = "";
        if (args.getOutFormat().equals("png")) {
            outputFile = args.getOutputFile() + ".simulation.png";
            util.saveAsPng(jfreechart, outputFile, width, height);
        } else {
            outputFile = args.getOutputFile() + ".simulation.pdf";
            util.saveAsPdf(jfreechart, outputFile, width, height);
        }

        return true;
    }

    private void dfs(Integer x, Integer y, Integer[][] cpgHpMatInRegion, List<Double[][]> toLeftList,
                     List<Double[][]> toRightList, List<Integer> cpgPosListInRegion) {
        if (y < 0 || y >= cpgPosListInRegion.size()) {
            return;
        }
        if (cpgHpMatInRegion[x][y] != 0) {
            return;
        }
        if (y - 1 >= 0 && cpgHpMatInRegion[x][y - 1] != 0) {
            Integer check = cpgHpMatInRegion[x][y - 1] == -1 ? 0 : 1;
            if (toRightList.get(y - 1)[check][0] > random()) {
                cpgHpMatInRegion[x][y] = -1;
            } else {
                cpgHpMatInRegion[x][y] = 1;
            }
            dfs(x, y + 1, cpgHpMatInRegion, toLeftList, toRightList, cpgPosListInRegion);
        }
        if (y + 1 < cpgPosListInRegion.size() && cpgHpMatInRegion[x][y + 1] != 0) {
            Integer check = cpgHpMatInRegion[x][y + 1] == -1 ? 0 : 1;
            if (toLeftList.get(y + 1)[check][0] > random()) {
                cpgHpMatInRegion[x][y] = -1;
            } else {
                cpgHpMatInRegion[x][y] = 1;
            }
            dfs(x, y - 1, cpgHpMatInRegion, toLeftList, toRightList, cpgPosListInRegion);
        }
    }
}
