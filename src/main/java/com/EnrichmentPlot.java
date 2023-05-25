package com;

import com.args.EnrichmentPlotArgs;
import com.bean.Region;
import com.common.Util;
import com.common.bigwigTool.BBFileReader;
import com.common.bigwigTool.BigWigIterator;
import com.common.bigwigTool.WigItem;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EnrichmentPlot {
    public static final Logger log = LoggerFactory.getLogger(EnrichmentPlot.class);
    EnrichmentPlotArgs args = new EnrichmentPlotArgs();
    Util util = new Util();

    public void enrichmentPlot(EnrichmentPlotArgs enrichmentPlotArgs) throws Exception {
        log.info("EnrichmentPlot start!");
        args = enrichmentPlotArgs;

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        List<Region> openChromatin = util.getBedRegionList(args.getOpenChromatin());
        if (openChromatin.size() < 1) {
            log.info("The openChromatin is null, please check.");
            return;
        }

        // get bedfile list
        String[] bedPaths = args.getBedPaths().split(" ");
        BBFileReader reader = new BBFileReader(args.getBigwig());
        DefaultXYDataset xyDataset = new DefaultXYDataset();
        for (String bedPath : bedPaths) {
            // region list total length and cover length array list which split by group,
            // e.g groupNum = 10, so the region which bigwig average value range [0, 0.1) in group 1,
            // range [0.1, 0.2) in group 2, ..., range [0.9, 1.0] in group 10
            double[] totalLengthArray = new double[args.getGroupNum()];
            double[] coverLengthArray = new double[args.getGroupNum()];
            double[] regionNumArray = new double[args.getGroupNum()];

            String bedFileName = new File(bedPath).getName();
            String bedFileLabel = bedFileName.substring(0, bedFileName.lastIndexOf("."));
            List<Region> regionList = util.getBedRegionList(bedPath);
            if (regionList.size() < 1) {
                log.info("The bed file:" + bedPath + " is null, please check.");
                continue;
            }

            for (Region region : regionList) {
                BigWigIterator iter = reader.getBigWigIterator(region.getChrom(), region.getStart(), region.getChrom(), region.getEnd(), true);
                Double sumInRegion = 0.0;
                Integer numInRegion = 0;
                while (iter.hasNext()) {
                    WigItem wigItem = iter.next();
                    sumInRegion += wigItem.getWigValue();
                    numInRegion++;
                }
                Double average = sumInRegion / numInRegion;
                Integer groupIndex = (int) (average >= 1 ? args.getGroupNum() - 1 : average * args.getGroupNum());
                Integer regionLength = region.getEnd() - region.getStart() + 1;
                Integer coverLength = getRegionCoverLength(openChromatin, 0, openChromatin.size() - 1, region);
                totalLengthArray[groupIndex] += regionLength;
                coverLengthArray[groupIndex] += coverLength;
                regionNumArray[groupIndex] += 1;
            }

            double[][] xyData = new double[2][args.getGroupNum()];
            for (int i = 0; i < args.getGroupNum(); i++) {
                if (regionNumArray[i] < args.getGroupCutoff()) {
                    xyData[0][i] = Double.NaN;
                    xyData[1][i] = Double.NaN;
                } else {
                    double coverRate = coverLengthArray[i] / totalLengthArray[i] * 100;
                    double xAxisPos =  ((double) (i + 1) / args.getGroupNum()) - 1.0 / (args.getGroupNum() * 2); // to the middle of group
                    xyData[0][i] = xAxisPos;
                    xyData[1][i] = coverRate;
                }
            }
            xyDataset.addSeries(bedFileLabel, xyData);
        }

        Integer width = args.getGroupNum() * 100 < 500 ? 500 : args.getGroupNum() * 100;
        JFreeChart jfreechart = generateLinePlot(xyDataset, width);
        Integer height = width;

        String outputFilename = "";
        if (args.getOutFormat().equals("png")) {
            outputFilename = args.getTag() + ".enrichmentPlot.png";
            util.saveAsPng(jfreechart, outputFilename, width, height);
        } else {
            outputFilename = args.getTag() + ".enrichmentPlot.pdf";
            util.saveAsPdf(jfreechart, outputFilename, width, height);
        }

        log.info("EnrichmentPlot end!");
    }

    private boolean checkArgs() {
        if (args.getBigwig() == null || args.getBigwig().equals("")) {
            log.error("The bigwig file can not be null.");
            return false;
        }
        if (args.getBedPaths() == null || args.getBedPaths().equals("")) {
            log.error("The bed file can not be null.");
            return false;
        }
        if (args.getOpenChromatin() == null || args.getOpenChromatin().equals("")) {
            log.error("The open chromatin file can not be null.");
            return false;
        }
        if (args.getTag() == null || args.getTag().equals("")) {
            log.error("The tag can not be null.");
            return false;
        }

        return true;
    }

    private Integer getRegionCoverLength(List<Region> regionList, Integer start, Integer end, Region target) {
        if(start <= end){
            Integer middle = (start + end) / 2;
            Region middleRegion = regionList.get(middle);//中间值
            if (target.getChrom().equals(middleRegion.getChrom())) {
                if (target.getEnd() < middleRegion.getStart()) {
                    return getRegionCoverLength(regionList, start, middle - 1, target);
                } else if (target.getStart() > middleRegion.getEnd()) {
                    return getRegionCoverLength(regionList, middle + 1, end, target);
                } else {
                    Integer coverStart = middleRegion.getStart() > target.getStart() ? middleRegion.getStart() : target.getStart();
                    Integer coverEnd = middleRegion.getEnd() > target.getEnd() ? target.getEnd() : middleRegion.getEnd();
//                    log.info("target: " + target.toHeadString() + " in "  + middleRegion.toHeadString() + ", cover: " + (coverEnd - coverStart + 1));
                    return coverEnd - coverStart + 1;
                }
            } else if (target.getChrom().compareTo(middleRegion.getChrom()) < 0) {
                return getRegionCoverLength(regionList, start, middle - 1, target);
            } else {
                return getRegionCoverLength(regionList, middle + 1, end, target);
            }
        }
        return 0;
    }

    private JFreeChart generateLinePlot(XYDataset dataset, Integer width) {
        String bigwigName = new File(args.getBigwig()).getName();
        String title = bigwigName.substring(0, bigwigName.lastIndexOf("."));
        JFreeChart jFreeChart = ChartFactory.createXYLineChart(
                title,//图名字
                "",//横坐标
                "",//纵坐标
                dataset,//数据集
                PlotOrientation.VERTICAL,
                true, // 显示图例
                true, // 采用标准生成器
                false);// 是否生成超链接
        TextTitle textTitle = new TextTitle(title, new Font("", 0, width / 30));
        jFreeChart.setTitle(textTitle);

        LegendTitle legendTitle = jFreeChart.getLegend();
        legendTitle.setBorder(1, 1, 1, 2);
        legendTitle.setItemFont(new Font("", 0, width / 50));

        XYPlot xyPlot = (XYPlot) jFreeChart.getPlot();
        xyPlot.setBackgroundPaint(Color.WHITE);
        xyPlot.setRangeGridlinesVisible(false);
        xyPlot.setOutlinePaint(Color.BLACK);

        // xy轴
        NumberAxis xAxis = new NumberAxis();
        xAxis.setTickUnit(new NumberTickUnit(0.1));
        xAxis.setTickLabelFont(new Font("", 0, width / 75));
        xAxis.setRange(new Range(0, 1));
        xyPlot.setDomainAxis(xAxis);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("The percentage of genomic feature overlap with TCGA-ATAC peaks(%)");
        yAxis.setLabelFont(new Font("", 0, width / 50));
        yAxis.setTickUnit(new NumberTickUnit(20));
        yAxis.setTickLabelFont(new Font("", 0, width / 75));
        yAxis.setRange(new Range(0, 100));
        xyPlot.setRangeAxis(yAxis);

        return jFreeChart;
    }

}
