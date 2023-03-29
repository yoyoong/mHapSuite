package com;

import com.args.HeatMapViewArgs;
import com.bean.Region;
import com.common.Util;
import com.common.bigwigTool.BBFileReader;
import com.common.bigwigTool.BigWigIterator;
import com.common.bigwigTool.WigItem;
import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.DefaultXYZDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HeatMapView {
    public static final Logger log = LoggerFactory.getLogger(HeatMapView.class);
    HeatMapViewArgs args = new HeatMapViewArgs();
    Util util = new Util();
    public static final Integer MAXSIZE = 10000;

    public void heatMapView(HeatMapViewArgs heatMapViewArgs) throws Exception {
        log.info("HeatMapView start!");
        args = heatMapViewArgs;

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        // get bedfile list
        String[] bedPaths = args.getBedPaths().split(" ");
        Integer windowNum = (args.getUpLength() + args.getDownLength()) / args.getWindow() + 1;
        BBFileReader reader = new BBFileReader(args.getBigwig());
        List<Plot> heatPlotList = new ArrayList<>();
        List<Integer> heatHeightList = new ArrayList<>();
        DefaultCategoryDataset lineDataset = new DefaultCategoryDataset();
        for (String bedPath : bedPaths) {
            String bedFileName = new File(bedPath).getName();
            String bedFileLabel = bedFileName.substring(0, bedFileName.lastIndexOf("."));
            List<Region> regionList = util.getBedRegionList(bedPath);
            if (regionList.size() < 1) {
                log.info("The bed file:" + bedPath + " is null, please check.");
                continue;
            }

            Double[][] valueList = new Double[regionList.size()][windowNum];
            for (int i = 0; i < windowNum; i++) {
                Double allSumOfWindow = 0.0;
                Integer allNumOfWindow = 0;
                for (int j = 0; j < regionList.size(); j++) {
                    Region region = regionList.get(j);
                    Integer midSiteOfRegion = (region.getStart() + region.getEnd()) / 2;
                    Integer startSiteOfWindow = midSiteOfRegion - args.getUpLength() + args.getWindow() * i;
                    if (startSiteOfWindow < 0) {
                        startSiteOfWindow = 0;
                    }
                    Integer endSiteOfWindow = startSiteOfWindow + args.getWindow();

                    BigWigIterator iter = reader.getBigWigIterator(region.getChrom(), startSiteOfWindow, region.getChrom(), endSiteOfWindow, true);
                    Double sumOfWindowOfRegion = 0.0;
                    Integer numOfWindowOfRegion = 0;
                    while(iter.hasNext()) {
                        WigItem wigItem = iter.next();
                        sumOfWindowOfRegion += wigItem.getWigValue();
                        numOfWindowOfRegion ++;
                    }
                    Double averageOfWindowOfRegion = numOfWindowOfRegion > 0 ? sumOfWindowOfRegion / numOfWindowOfRegion : 0;
                    valueList[j][i] = averageOfWindowOfRegion;
                    allSumOfWindow += sumOfWindowOfRegion;
                    allNumOfWindow += numOfWindowOfRegion;
                }
                Double average = allNumOfWindow > 0 ? allSumOfWindow / allNumOfWindow : 0;
                Integer xAxisPos = args.getUpLength() * (-1) + args.getWindow() * i;
                lineDataset.addValue(average, bedFileLabel, xAxisPos);
            }

            XYPlot heatPlot = generateHeatPlot(valueList, bedFileLabel);
            heatPlotList.add(heatPlot);
            Integer height = regionList.size() / 20 < 500 ? 500 : regionList.size() / 20;
            heatHeightList.add(height);
            log.info("Read " + bedPath + " end!");
        }
        reader.close();

        // merge the line plot and heat plot
        List<Plot> plotList = new ArrayList<>();
        List<Integer> heightList = new ArrayList<>();
        Integer width = windowNum * 50 < 500 ? 500 : windowNum * 50;
        CategoryPlot linePlot = generateLinePlot(lineDataset);
        plotList.add(linePlot);
        plotList.addAll(heatPlotList);
        heightList.add(width / 2);
        heightList.addAll(heatHeightList);

        // 输出到文件
        String outputPath = args.getTag() + ".heatMapView." + args.getOutFormat();
        if (args.getOutFormat().equals("pdf")) {
            saveAsPdf(plotList, outputPath, width, heightList);
        } else if (args.getOutFormat().equals("png")) {
            saveAsPng(plotList, outputPath, width, heightList);
        }

        log.info("HeatMapView end!");
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
        if (args.getTag() == null) {
            log.error("The tag can not be null.");
            return false;
        }

        return true;
    }

    private CategoryPlot generateLinePlot(CategoryDataset dataset) {
        JFreeChart jFreeChart = ChartFactory.createLineChart(
                "",//图名字
                "",//横坐标
                "",//纵坐标
                dataset,//数据集
                PlotOrientation.VERTICAL,
                true, // 显示图例
                true, // 采用标准生成器
                false);// 是否生成超链接

        CategoryPlot categoryPlot = (CategoryPlot) jFreeChart.getPlot();
        categoryPlot.setBackgroundPaint(Color.WHITE);
        categoryPlot.setRangeGridlinesVisible(false);
        categoryPlot.setOutlinePaint(Color.BLACK);

        // xy轴
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLowerMargin(0);
        xAxis.setUpperMargin(0);
        categoryPlot.setDomainAxis(xAxis);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickUnit(new NumberTickUnit(0.2));
        yAxis.setRange(new Range(0, 1));
        categoryPlot.setRangeAxis(yAxis);

        return categoryPlot;
    }


    private XYPlot generateHeatPlot(Double[][] dataMatrix, String yAxisLable) {
        // 创建数据集
        DefaultXYZDataset dataset = new DefaultXYZDataset();
        double x[] = new double[dataMatrix.length * dataMatrix[0].length];
        double y[] = new double[dataMatrix.length * dataMatrix[0].length];
        double z[] = new double[dataMatrix.length * dataMatrix[0].length];
        for (int i = 0; i < dataMatrix.length; i++) {
            for (int j = 0; j < dataMatrix[0].length; j++) {
                x[dataMatrix[0].length * i + j] = j;
                y[dataMatrix[0].length * i + j] = i;
                if (dataMatrix[i][j].isNaN()) {
                    dataMatrix[i][j] = 0.0;
                }
                z[dataMatrix[0].length * i + j] = dataMatrix[i][j];
            }
        }
        double pos[][] = {x, y, z};
        dataset.addSeries( "Series" , pos);

        // xy轴
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLowerMargin(0);
        xAxis.setUpperMargin(0);
        xAxis.setVisible(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickUnit(new NumberTickUnit(dataMatrix.length * 2));
        yAxis.setRange(new Range(1, dataMatrix.length));
        yAxis.setAxisLineVisible(false);
        yAxis.setVisible(true);
        yAxis.setLabel(yAxisLable);
        yAxis.setLabelFont(new Font("", Font.PLAIN, 15));

        // 颜色定义
        LookupPaintScale paintScale = new LookupPaintScale(0, 1, Color.black);
        for (Double j = 0.0; j < 255.0; j++) {
            paintScale.add((255 - j) / 255, new Color((int) (255.0 - j / 3 * 2), (int) (255.0 - j / 3 * 2), j.intValue()));
        }

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

    // 保存为文件
    public void saveAsPdf(List<Plot> plotList, String outputPath, Integer width, List<Integer> heightList) throws FileNotFoundException, DocumentException {
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
            jFreeChart.setBackgroundPaint(Color.WHITE);
            Double plotWidth = width.doubleValue();

            if (i == 0) {
                String bigwigName = new File(args.getBigwig()).getName();
                String title = bigwigName.substring(0, bigwigName.lastIndexOf("."));
                jFreeChart = new JFreeChart(title, new Font("", Font.PLAIN, width / 50), plotList.get(i), false);
                plotWidth = plotWidth - width * 0.023;
            } else if (i == plotList.size() - 1) {
                // 颜色定义
                LookupPaintScale paintScale = new LookupPaintScale(0, 1, Color.black);
                for (Double j = 0.0; j < 255.0; j++) {
                    paintScale.add((255 - j) / 255, new Color((int) (255.0 - j / 3 * 2), (int) (255.0 - j / 3 * 2), j.intValue()));
                }
                // 颜色示意图
                PaintScaleLegend paintScaleLegend = new PaintScaleLegend(paintScale, new NumberAxis());
                paintScaleLegend.setStripWidth(width / 100);
                paintScaleLegend.setPosition(RectangleEdge.RIGHT);
                paintScaleLegend.setAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
                paintScaleLegend.setMargin(heightList.get(i) / 3, 0, heightList.get(i) / 3, 0);
                paintScaleLegend.setVisible(true);

                jFreeChart.addSubtitle(paintScaleLegend);
            } else {
                plotWidth = plotWidth - width * 0.023;
            }
            jFreeChart.setBackgroundPaint(Color.WHITE);
            Rectangle2D rectangle2D0 = new Rectangle2D.Double(0, nextHeight, plotWidth, heightList.get(i));
            jFreeChart.draw(graphics2D, rectangle2D0);
            pdfContentByte.addTemplate(pdfTemplate, 0, 0);
            nextHeight += heightList.get(i);
        }

        graphics2D.dispose();
        // 关闭文档，才能输出
        document.close();
        pdfWriter.close();
    }

    public void saveAsPng(List<Plot> plotList, String outputPath, Integer width, List<Integer> heightList) throws IOException {
        File outFile = new File(outputPath);
        Integer sumHeight = 0;
        for (int i = 0; i < heightList.size(); i++) {
            sumHeight += heightList.get(i);
        }
        BufferedImage bufferedImage = new BufferedImage(width, sumHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, sumHeight);

        Integer nextHeight = 0;
        for (int i = 0; i < plotList.size(); i++) {
            Graphics2D graphics2D = bufferedImage.createGraphics();
            JFreeChart jFreeChart = new JFreeChart("", null, plotList.get(i), false);
            Double plotWidth = width.doubleValue();

            if (i == 0) {
                String bigwigName = new File(args.getBigwig()).getName();
                String title = bigwigName.substring(0, bigwigName.lastIndexOf("."));
                jFreeChart = new JFreeChart(title, new Font("", Font.PLAIN, width / 50), plotList.get(i), false);
                plotWidth = plotWidth - width * 0.023;
            } else if (i == plotList.size() - 1) {
                // 颜色定义
                LookupPaintScale paintScale = new LookupPaintScale(0, 1, Color.black);
                for (Double j = 0.0; j < 255.0; j++) {
                    paintScale.add((255 - j) / 255, new Color((int) (255.0 - j / 3 * 2), (int) (255.0 - j / 3 * 2), j.intValue()));
                }
                // 颜色示意图
                NumberAxis numberAxis = new NumberAxis();
                numberAxis.setTickUnit(new NumberTickUnit(0.1));
                PaintScaleLegend paintScaleLegend = new PaintScaleLegend(paintScale, numberAxis);
                paintScaleLegend.setStripWidth(width / 100);
                paintScaleLegend.setPosition(RectangleEdge.RIGHT);
                paintScaleLegend.setAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
                paintScaleLegend.setMargin(heightList.get(i) / 3, 0, heightList.get(i) / 3, 0);
                paintScaleLegend.setVisible(true);

                jFreeChart.addSubtitle(paintScaleLegend);
            } else {
                plotWidth = plotWidth - width  * 0.023;
            }

            jFreeChart.setBackgroundPaint(Color.WHITE);
            Rectangle2D rectangle2D0 = new Rectangle2D.Double(0, nextHeight, plotWidth, heightList.get(i));
            jFreeChart.draw(graphics2D, rectangle2D0);
            nextHeight += heightList.get(i);
            graphics2D.dispose();
        }

        RenderedImage rendImage = bufferedImage;
        ImageIO.write(rendImage, "png", outFile);
    }
}
