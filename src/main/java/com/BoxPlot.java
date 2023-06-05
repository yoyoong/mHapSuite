package com;

import com.args.BoxPlotArgs;
import com.bean.Region;
import com.common.Util;
import com.common.bigwigTool.BBFileReader;
import com.common.bigwigTool.BigWigIterator;
import com.common.bigwigTool.WigItem;
import com.rewrite.CustomBoxAndWhiskerRenderer;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.Range;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BoxPlot {
    public static final Logger log = LoggerFactory.getLogger(BoxPlot.class);
    BoxPlotArgs args = new BoxPlotArgs();
    Util util = new Util();
    public static final Integer MAXSIZE = 10000;
    public static final Integer MAXSAMPLE = 20;

    public void boxPlot(BoxPlotArgs boxPlotArgs) throws Exception {
        log.info("BoxPlot start!");
        args = boxPlotArgs;

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

        String[] bigwigs = args.getBigwigs().split(" ");
        if (bigwigs.length > MAXSAMPLE) {
            log.error("The input bigwid files is larger than " + MAXSAMPLE + ", please re-enter the bigwigs");
            return;
        }
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

        for (String bigwig : bigwigs) {
            ArrayList<Double> averageList = new ArrayList<>();
            for (Region region : regionList) {
                String startChr = region.getChrom();
                Integer startBase = region.getStart();
                String endChr = region.getChrom();
                Integer endBase = region.getEnd();

                double sumInRegion = 0.0;
                BBFileReader reader = new BBFileReader(bigwig);
                BigWigIterator iter = reader.getBigWigIterator(startChr, startBase, endChr, endBase, true);
                Integer index = 0;
                while(iter.hasNext()) {
                    WigItem wigItem = iter.next();
                    sumInRegion += wigItem.getWigValue();
                    index++;
                }
                reader.close();
                Double average = sumInRegion / index;
                averageList.add(average);
            }
            String bigwigName = new File(bigwig).getName();
            dataset.add(averageList, "", bigwigName.substring(0, bigwigName.lastIndexOf(".")));
            log.info("Process " + bigwig + " end!");
        }

        Integer width = bigwigs.length * 150;
        width = width > 14400 ? 14400 : (width < 750 ? 750 : width);
        Integer height = 500;

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Sample");
        xAxis.setLabelFont(new Font("", Font.PLAIN, width / 60));
        xAxis.setTickLabelFont(new Font("", 0, width / 75));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Value");
        yAxis.setLabelFont(new Font("", Font.PLAIN, width / 60));
        yAxis.setLowerMargin(0.01);
        yAxis.setUpperMargin(0.01);
        yAxis.setRange(new Range(0, 1));
        yAxis.setTickUnit(new NumberTickUnit(0.1));
        yAxis.setTickLabelFont(new Font("", 0, width / 75));

        CustomBoxAndWhiskerRenderer renderer = new CustomBoxAndWhiskerRenderer();
        renderer.setFillBox(false);
        renderer.setMaxOutlierVisible(false);
        renderer.setMinOutlierVisible(false);
        renderer.setMaximumBarWidth(1 / Double.valueOf(bigwigs.length) / 2.0);
        renderer.setMeanVisible(false);
        renderer.setDefaultToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
        CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);

        String bedName = new File(args.getBedPath()).getName();
        JFreeChart jfreechart = new JFreeChart(
                bedName.substring(0, bedName.lastIndexOf(".")),
                new Font("", Font.BOLD, width / 50),
                plot,
                false
        );
        jfreechart.setBackgroundPaint(Color.WHITE);

        String outputFilename = "";
        if (args.getOutFormat().equals("png")) {
            outputFilename = args.getTag() +  ".boxPlot.png";
            util.saveAsPng(jfreechart, outputFilename, width, height);
        } else {
            outputFilename = args.getTag() + ".boxPlot.pdf";
            util.saveAsPdf(jfreechart, outputFilename, width, height);
        }

        log.info("BoxPlot end!");
    }

    private boolean checkArgs() {
        if (args.getBedPath() == null || args.getBedPath().equals("")) {
            log.error("The input bed file can not be null.");
            return false;
        }
        if (args.getBigwigs() == null || args.getBigwigs().equals("")) {
            log.error("The bigwig files can not be null.");
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
