package com;

import com.args.BoxViewArgs;
import com.bean.Region;
import com.common.Util;
import com.common.bigwigTool.BBFileReader;
import com.common.bigwigTool.BigWigIterator;
import com.common.bigwigTool.WigItem;
import com.rewrite.CustomBoxAndWhiskerRenderer;
import org.apache.commons.compress.utils.Lists;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class BoxView {
    public static final Logger log = LoggerFactory.getLogger(BoxView.class);
    BoxViewArgs args = new BoxViewArgs();
    Util util = new Util();
    public static final Integer MAXSIZE = 10000;
    public static final Integer MAXSAMPLE = 20;

    public void boxView(BoxViewArgs boxViewArgs) throws Exception {
        log.info("BoxView start!");
        args = boxViewArgs;

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


//        int threadNum = bigwigs.length;// 线程数
//        CountDownLatch countDownLatch = new CountDownLatch(threadNum);// 计数器
//        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);// 创建一个线程池
//        List<Callable<Boolean>> tasks = Lists.newArrayList();// 定义一个任务集合
//        Callable<Boolean> task;// 定义一个任务
//
//        for (String bigwig : bigwigs) {
//            task = new Callable<Boolean>() {
//                @Override
//                public Boolean call() throws Exception {
//                    log.info("Process " + bigwig + " begin!");
//                    ArrayList<Double> averageList = new ArrayList<>();
//                    for (Region region : regionList) {
//                        String startChr = region.getChrom();
//                        Integer startBase = region.getStart();
//                        String endChr = region.getChrom();
//                        Integer endBase = region.getEnd();
//
//                        double sumInRegion = 0.0;
//                        BBFileReader reader = new BBFileReader(bigwig);
//                        BigWigIterator iter = reader.getBigWigIterator(startChr, startBase, endChr, endBase, true);
//                        Integer index = 0;
//                        while(iter.hasNext()) {
//                            WigItem wigItem = iter.next();
//                            sumInRegion += wigItem.getWigValue();
//                            index++;
//                        }
//                        Double average = sumInRegion / index;
//                        averageList.add(average);
//                    }
//                    dataset.add(averageList, "", bigwig);
//                    log.info("Process " + bigwig + " end!");
//                    return true;
//                }
//            };
//            countDownLatch.countDown(); // 减少计数器的计数
//            tasks.add(task); // 任务处理完加入集合
//        }
//
//        try {
//            executorService.invokeAll(tasks); // 执行给定的任务
//            countDownLatch.await(); // 等待计数器归零
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        executorService.shutdown(); // 关闭线程池

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Sample");
        Integer labelSize = bigwigs.length * 4 < 10 ? 10 : bigwigs.length * 2;
        xAxis.setLabelFont(new Font("", Font.PLAIN, labelSize));
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Value");
        yAxis.setRange(new Range(0, 1));
        yAxis.setTickUnit(new NumberTickUnit(0.1));
        yAxis.setLabelFont(new Font("", Font.PLAIN, labelSize));

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
                new Font("", Font.BOLD, labelSize),
                plot,
                false
        );
        jfreechart.setBackgroundPaint(Color.WHITE);

        Integer width = bigwigs.length * 150;
        width = width > 14400 ? 14400 : (width < 500 ? 500 : width);
        Integer height = 500;

        String outputFilename = "";
        if (args.getOutFormat().equals("png")) {
            outputFilename = args.getTag() +  ".boxView.png";
            util.saveAsPng(jfreechart, outputFilename, width, height);
        } else {
            outputFilename = args.getTag() + ".boxView.pdf";
            util.saveAsPdf(jfreechart, outputFilename, width, height);
        }

        log.info("BoxView end!");
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
        return true;
    }

}
