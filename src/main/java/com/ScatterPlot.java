package com;

import com.args.ScatterPlotArgs;
import com.bean.Region;
import com.common.bigwigTool.BBFileReader;
import com.common.bigwigTool.BigWigIterator;
import com.common.Util;
import com.common.bigwigTool.WigItem;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ScatterPlot {
    public static final Logger log = LoggerFactory.getLogger(ScatterPlot.class);
    ScatterPlotArgs args = new ScatterPlotArgs();
    Util util = new Util();
    public static final Integer MAXSIZE = 10000;

    public void scatterPlot(ScatterPlotArgs scatterPlotArgs) throws Exception {
        log.info("ScatterPlot start!");
        args = scatterPlotArgs;

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        // get regionList from region bedfile
        List<Region> regionList = util.getBedRegionList(args.getBedPath());
        if (regionList.size() < 1) {
            log.error("The bed file is null, please check.");
            return;
        }

        DefaultXYDataset dataset = new DefaultXYDataset();
        double[][] xyData = new double[2][regionList.size()];
        int index = 0;
        for (Region region : regionList) {
            String startChr = region.getChrom();
            Integer startBase = region.getStart();
            String endChr = region.getChrom();
            Integer endBase = region.getEnd();

            // parse bigwig file get wig item list
            // get x axis and y axis value from wig item list
            double[] xValue = new double[MAXSIZE];
            double[] yValue = new double[MAXSIZE];
            Integer realIndex = 0;
            BBFileReader reader1 = new BBFileReader(args.getBigwig1());
            BigWigIterator iter1 = reader1.getBigWigIterator(startChr, startBase, endChr, endBase, true);
            BBFileReader reader2 = new BBFileReader(args.getBigwig2());
            BigWigIterator iter2 = reader2.getBigWigIterator(startChr, startBase, endChr, endBase, true);
            while(iter1.hasNext() && iter2.hasNext()) {
                WigItem wigItem1 = iter1.next();
                WigItem wigItem2 = iter2.next();
                if (wigItem1.getStartBase() > wigItem2.getStartBase()) {
                    iter2.next();
                    continue;
                } else if (wigItem1.getStartBase() < wigItem2.getStartBase()) {
                    iter1.next();
                    continue;
                }
                xValue[realIndex] = wigItem1.getWigValue();
                yValue[realIndex] = wigItem2.getWigValue();
                realIndex++;
            }
            reader1.close();
            reader2.close();

            double[] xValueWithoutZero = Arrays.stream(xValue).filter(value -> value != 0).toArray();
            double[] yValueWithoutZero = Arrays.stream(yValue).filter(value -> value != 0).toArray();
            double xValueOfThisRegion = Arrays.stream(xValueWithoutZero).sum() / xValueWithoutZero.length;
            double yValueOfThisRegion = Arrays.stream(yValueWithoutZero).sum() / yValueWithoutZero.length;
            xyData[0][index] = xValueOfThisRegion;
            xyData[1][index] = yValueOfThisRegion;
            index++;
        }
        dataset.addSeries("xyData", xyData);

        Integer width = 1000;
        Integer height = width;

        // 绘制XY图
        String bedName = new File(args.getBedPath()).getName();
        String title = bedName.substring(0, bedName.lastIndexOf("."));
        String bigwig1Name = new File(args.getBigwig1()).getName();
        String bigwig2Name = new File(args.getBigwig2()).getName();
        String xAxisLabel = bigwig1Name.substring(0, bigwig1Name.lastIndexOf("."));
        String yAxisLabel = bigwig2Name.substring(0, bigwig2Name.lastIndexOf("."));
        JFreeChart jfreechart = ChartFactory.createScatterPlot(title, // 标题
                xAxisLabel, // X轴标签
                yAxisLabel, // Y轴的标签
                dataset, // dataset
                PlotOrientation.VERTICAL,
                false, // legend
                false, // tooltips
                false); // URLs
        TextTitle textTitle = new TextTitle(title, new Font("", 0, width / 25));
        jfreechart.setTitle(textTitle);

        XYPlot xyPlot = jfreechart.getXYPlot( );
        xyPlot.setBackgroundPaint(Color.WHITE); // 背景色
        xyPlot.setDomainGridlinesVisible(false); // 不显示X轴网格线
        xyPlot.setRangeGridlinesVisible(false); // 不显示Y轴网格线
        xyPlot.setOutlineVisible(true); // 不显示数据区的边界线条

        XYItemRenderer renderer = xyPlot.getRenderer();
        //renderer.setSeriesShape(0, ShapeUtils.createDiamond(1));
        renderer.setSeriesShape(0, new Ellipse2D.Double(0, 0, 2, 2));
        renderer.setSeriesPaint(0, Color.red);

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);
        xAxis.setLabelFont(new Font("", Font.PLAIN, width / 30));
        xAxis.setRange(new Range(0, 1));
        xAxis.setVisible(true);
        xAxis.setTickUnit(new NumberTickUnit(0.1));
        xAxis.setTickLabelFont(new Font("", 0, width / 40));
        xAxis.setTickMarksVisible(false);
        xAxis.setAxisLineVisible(false);
        xyPlot.setDomainAxis(xAxis); // x axis

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisLabel);
        yAxis.setLabelFont(new Font("", Font.PLAIN, width / 30));
        yAxis.setRange(new Range(0, 1));
        yAxis.setVisible(true);
        yAxis.setTickUnit(new NumberTickUnit(0.1));
        yAxis.setTickLabelFont(new Font("", 0, width / 40));
        yAxis.setTickMarksVisible(false);
        yAxis.setAxisLineVisible(false);
        xyPlot.setRangeAxis(yAxis); // y axis

        String outputFilename = "";
        if (args.getOutFormat().equals("png")) {
            outputFilename = args.getTag() +  ".scatterPlot.png";
            util.saveAsPng(jfreechart, outputFilename, width, height);
        } else {
            outputFilename = args.getTag() + ".scatterPlot.pdf";
            util.saveAsPdf(jfreechart, outputFilename, width, height);
        }

        log.info("ScatterPlot end!");
    }

    private boolean checkArgs() {
        if (args.getBigwig1() == null || args.getBigwig1().equals("")) {
            log.error("The first bigwig file can not be null.");
            return false;
        }
        if (args.getBigwig2() == null || args.getBigwig2().equals("")) {
            log.error("The second bigwig file can not be null.");
            return false;
        }
        if (args.getTag() == null || args.getTag().equals("")) {
            log.error("The tag can not be null.");
            return false;
        }
        if (!args.getOutFormat().equals("png") && !args.getOutFormat().equals("pdf")) {
            log.error("The output format must be pdf or png");
            return false;
        }
        return true;
    }

}
