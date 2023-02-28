package com;

import com.args.ScatterViewArgs;
import com.bean.MHapInfo;
import com.common.bigwigTool.BBFileReader;
import com.common.bigwigTool.BigWigIterator;
import com.common.Util;
import com.common.bigwigTool.WigItem;
import com.rewrite.CustomXYLineAndShapeRenderer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;

public class ScatterView {
    public static final Logger log = LoggerFactory.getLogger(ScatterView.class);
    ScatterViewArgs args = new ScatterViewArgs();
    Util util = new Util();
    public static final Integer MAXSIZE = 100000;

    public void scatterView(ScatterViewArgs scatterViewArgs) throws Exception {
        log.info("ScatterView start!");
        args = scatterViewArgs;

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        // parse bigwig file get wig item list
        Integer cnt = 0;
        ArrayList<WigItem> wigItemList1 = new ArrayList<>();
        BBFileReader reader1 =new BBFileReader(args.getBigwig1());
        BigWigIterator iter1 = reader1.getBigWigIterator(args.getStartChr(), args.getStartBase(), args.getEndChr(), args.getEndBase(), true);
        while(iter1.hasNext()){
            wigItemList1.add(iter1.next());
            cnt++;
            if (cnt > MAXSIZE) {
                break;
            }
        }
        if (cnt > MAXSIZE) {
            log.error("The range of startBase and endBase is bigger than max size, please input again.");
            return;
        }
        ArrayList<WigItem> wigItemList2 = new ArrayList<>();
        BBFileReader reader2 =new BBFileReader(args.getBigwig2());
        BigWigIterator iter2 = reader2.getBigWigIterator(args.getStartChr(), args.getStartBase(), args.getEndChr(), args.getEndBase(), true);
        while(iter2.hasNext()){
            wigItemList2.add(iter2.next());
        }

        // get x axis and y axis value from wig item list
        double[] xValue = new double[10000];
        double[] yValue = new double[10000];
        Integer index = 0;
        for(Integer i = 0, j = 0; i < wigItemList1.size() || j < wigItemList2.size(); i++, j++) {
            if (wigItemList1.get(i).getStartBase() > wigItemList2.get(j).getStartBase()) {
                j++;
            } else if (wigItemList1.get(i).getStartBase() < wigItemList2.get(j).getStartBase()) {
                i++;
            }
            xValue[index] = wigItemList1.get(i).getWigValue();
            yValue[index] = wigItemList2.get(j).getWigValue();
            index++;
        }

        DefaultXYDataset dataset = new DefaultXYDataset();
        double[][] datas = new double[2][xValue.length];
        for (int i = 0; i < xValue.length; i++) {
            datas[0][i] = xValue[i];
            datas[1][i] = yValue[i];
        }
        dataset.addSeries("", datas);

        // 绘制XY图
        String title = args.getStartChr() + ":" + args.getStartBase() + "-" + args.getEndChr() + ":" + args.getEndBase();
        String xAxisLabel = new File(args.getBigwig1()).getName();
        String yAxisLabel = new File(args.getBigwig2()).getName();
        JFreeChart jfreechart = ChartFactory.createScatterPlot(title, // 标题
                xAxisLabel, // X轴标签
                yAxisLabel, // Y轴的标签
                dataset, // dataset
                PlotOrientation.VERTICAL,
                true, // legend
                false, // tooltips
                false); // URLs
        jfreechart.removeLegend(); // 去掉图例

        XYPlot xyPlot = jfreechart.getXYPlot( );
        xyPlot.setBackgroundPaint(Color.WHITE); // 背景色
        xyPlot.setDomainGridlinesVisible(false); // 不显示X轴网格线
        xyPlot.setRangeGridlinesVisible(false); // 不显示Y轴网格线
        xyPlot.setOutlineVisible(false); // 不显示数据区的边界线条

        XYItemRenderer renderer = xyPlot.getRenderer();
        //renderer.setSeriesShape(0, ShapeUtils.createDiamond(1));
        renderer.setSeriesShape(0, new Ellipse2D.Double(0, 0, 5, 5));
        renderer.setSeriesPaint(0, Color.red);

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);
        xAxis.setRange(new Range(0, 1));
        xAxis.setVisible(true);
        xAxis.setTickUnit(new NumberTickUnit(0.1));
        xyPlot.setDomainAxis(xAxis); // x axis

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisLabel);
        yAxis.setRange(new Range(0, 1));
        yAxis.setVisible(true);
        yAxis.setTickUnit(new NumberTickUnit(0.1));
        xyPlot.setRangeAxis(yAxis); // y axis

        Integer width = index / 5;
        width = width > 14400 ? 14400 : (width < 1000 ? 1000 : width);
        Integer height = width;

        String outputFilename = "";
        if (args.getOutFormat().equals("png")) {
            outputFilename = args.getTag() + "_" + args.getStartChr() + "_" + args.getStartBase() + "_"
                    + args.getEndChr() + "_" + args.getEndBase() + ".sactterView.png";
            util.saveAsPng(jfreechart, outputFilename, width, height);
        } else {
            outputFilename = args.getTag() + "_" + args.getStartChr() + "_" + args.getStartBase() + "_"
                    + args.getEndChr() + "_" + args.getEndBase() + ".sactterView.pdf";
            util.saveAsPdf(jfreechart, outputFilename, width, height);
        }

        log.info("ScatterView end!");
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
        if (args.getStartChr().trim().equals(args.getEndChr().trim())) {
            if (args.getStartBase() >= args.getEndBase()) {
                log.error("The end base should bigger than start base.");
                return false;
            }
        }

        return true;
    }

}
