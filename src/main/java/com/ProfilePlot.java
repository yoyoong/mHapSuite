package com;

import com.args.ProfilePlotArgs;
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
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.util.List;

public class ProfilePlot {
    public static final Logger log = LoggerFactory.getLogger(ProfilePlot.class);
    ProfilePlotArgs args = new ProfilePlotArgs();
    Util util = new Util();
    public static final Integer MAXSIZE = 10000;

    public void profilePlot(ProfilePlotArgs profilePlotArgs) throws Exception {
        log.info("ProfilePlot start!");
        args = profilePlotArgs;

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        Integer coreWindowNum = args.getWindowNum();
        Integer upWindowNum = coreWindowNum / 2;
        Integer upWindowLength = args.getUpLength() / upWindowNum;
        Integer downWindowNum = coreWindowNum / 2;
        Integer downWindowLength = args.getDownLength() / downWindowNum;

        // get bedfile list
        String[] bedPaths = args.getBedPaths().split(" ");
        BBFileReader reader = new BBFileReader(args.getBigwig());
        DefaultCategoryDataset lineDataset = new DefaultCategoryDataset();
        for (String bedPath : bedPaths) {
            String bedFileName = new File(bedPath).getName();
            String bedFileLabel = bedFileName.substring(0, bedFileName.lastIndexOf("."));
            List<Region> regionList = util.getBedRegionList(bedPath);
            if (regionList.size() < 1) {
                log.info("The bed file:" + bedPath + " is null, please check.");
                continue;
            }

            for (int i = 0; i < upWindowNum + coreWindowNum + downWindowNum + 1; i++) {
                Double allSumOfWindow = 0.0;
                Integer allNumOfWindow = 0;
                String xAxisPos = "";
                for (Region region : regionList) {
                    Integer regionLength = region.getEnd() - region.getStart() + 1;
                    Integer coreWindowLength = regionLength / (coreWindowNum - 1);
                    Integer startSiteOfWindow = 0;
                    Integer endSiteOfWindow = 0;
                    if (i < upWindowNum) {
                        startSiteOfWindow = region.getStart() - coreWindowLength / 2 - args.getUpLength() + upWindowLength * i;
                        endSiteOfWindow = startSiteOfWindow + upWindowLength;
                    } else if (i >= upWindowNum && i < upWindowNum + coreWindowNum) {
                        startSiteOfWindow = region.getStart() - coreWindowLength / 2 + coreWindowLength * (i - upWindowNum);
                        endSiteOfWindow = startSiteOfWindow + coreWindowLength;
                    } else if (i >= upWindowNum + coreWindowNum) {
                        startSiteOfWindow = region.getEnd() + coreWindowLength / 2 + downWindowLength * (i - upWindowNum - coreWindowNum);
                        endSiteOfWindow = startSiteOfWindow + downWindowLength;
                    }

                    BigWigIterator iter = reader.getBigWigIterator(region.getChrom(), startSiteOfWindow, region.getChrom(), endSiteOfWindow, true);
                    Double sumOfWindowOfRegion = 0.0;
                    Integer numOfWindowOfRegion = 0;
                    while(iter.hasNext()) {
                        WigItem wigItem = iter.next();
                        sumOfWindowOfRegion += wigItem.getWigValue();
                        numOfWindowOfRegion ++;
                    }
                    allSumOfWindow += sumOfWindowOfRegion;
                    allNumOfWindow += numOfWindowOfRegion;
                }
                Double average = allNumOfWindow > 0 ? allSumOfWindow / allNumOfWindow : 0;
                if (i < upWindowNum) {
                    xAxisPos = String.valueOf(-args.getUpLength() + upWindowLength * i);
                } else if (i == upWindowNum) {
                    xAxisPos = "START";
                } else if (i > upWindowNum && i < upWindowNum + coreWindowNum) {
                    xAxisPos = String.valueOf( i - upWindowNum);
                } else if (i == upWindowNum + coreWindowNum) {
                    xAxisPos = "END";
                } else if (i > upWindowNum + coreWindowNum) {
                    xAxisPos = String.valueOf(downWindowLength * (i - upWindowNum - coreWindowNum));
                }
                lineDataset.addValue(average, bedFileLabel, xAxisPos);
            }
            log.info("Read " + bedPath + " end!");
        }

        Integer width = coreWindowNum * 100 < 500 ? 500 : coreWindowNum * 100;
        JFreeChart jfreechart = generateLinePlot(lineDataset, width);
        Integer height = width / 2;

        String outputFilename = "";
        if (args.getOutFormat().equals("png")) {
            outputFilename = args.getTag() +  ".profilePlot.png";
            util.saveAsPng(jfreechart, outputFilename, width, height);
        } else {
            outputFilename = args.getTag() + ".profilePlot.pdf";
            util.saveAsPdf(jfreechart, outputFilename, width, height);
        }

        log.info("ProfilePlot end!");
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

    private JFreeChart generateLinePlot(CategoryDataset dataset, Integer width) {
        String bigwigName = new File(args.getBigwig()).getName();
        String title = bigwigName.substring(0, bigwigName.lastIndexOf("."));
        JFreeChart jFreeChart = ChartFactory.createLineChart(
                title,//图名字
                "",//横坐标
                "",//纵坐标
                dataset,//数据集
                PlotOrientation.VERTICAL,
                true, // 显示图例
                true, // 采用标准生成器
                false);// 是否生成超链接
        LegendTitle legendTitle = jFreeChart.getLegend();
        legendTitle.setBorder(1, 1, 1, 2);
        legendTitle.setItemFont(new Font("", 0, width / 60));

        CategoryPlot categoryPlot = (CategoryPlot) jFreeChart.getPlot();
        categoryPlot.setBackgroundPaint(Color.WHITE);
        categoryPlot.setRangeGridlinesVisible(false);
        categoryPlot.setOutlinePaint(Color.BLACK);

        // xy轴
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabelFont(new Font("", 0, width / 75));
        xAxis.setTickLabelFont(new Font("", 0, width / 100));
        xAxis.setLowerMargin(0);
        xAxis.setUpperMargin(0);
        categoryPlot.setDomainAxis(xAxis);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickUnit(new NumberTickUnit(0.2));
        yAxis.setTickLabelFont(new Font("", 0, width / 100));
        yAxis.setLabelFont(new Font("", 0, width / 75));
        yAxis.setRange(new Range(0, 1));
        categoryPlot.setRangeAxis(yAxis);

        return jFreeChart;
    }

}
