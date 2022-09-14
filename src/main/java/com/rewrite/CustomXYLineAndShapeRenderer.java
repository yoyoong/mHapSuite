package com.rewrite;

import org.jfree.chart.LegendItem;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.text.TextUtils;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.awt.geom.Point2D;

public class CustomXYLineAndShapeRenderer extends XYLineAndShapeRenderer {

    // 自定义图例
    @Override
    public LegendItem getLegendItem(int datasetIndex, int series) {
        LegendItem legendItem = new LegendItem("");
        Shape shape = this.getDefaultLegendShape();
        if (series == 0) {
            String label = " Methylated,+ strand  ";
            Paint paint = Color.BLACK;
            Stroke lineStroke = this.lookupSeriesStroke(series);
            Paint linePaint = Color.BLACK;
            legendItem = new LegendItem(label, label, "", "", shape, paint, lineStroke, linePaint);
            return legendItem;
        } else if (series == 1) {
            String label = " Methylated,- strand  ";
            Paint paint = Color.BLUE;
            Stroke lineStroke = this.lookupSeriesStroke(series);
            Paint linePaint = Color.BLUE;
            legendItem = new LegendItem(label, label, "", "", shape, paint, lineStroke, linePaint);
            return legendItem;
        } else if (series == 2) {
            String label = " Unmethylated,+ strand  ";
            Paint paint = Color.WHITE;
            Stroke lineStroke = this.lookupSeriesStroke(series);
            Paint linePaint = Color.BLACK;
            legendItem = new LegendItem(label, label, "", "", shape, paint, lineStroke, linePaint);
            return legendItem;
        } else if (series == 3) {
            String label = " Unmethylated,- strand  ";
            Paint paint = Color.WHITE;
            Stroke lineStroke = this.lookupSeriesStroke(series);
            Paint linePaint = Color.BLUE;
            legendItem = new LegendItem(label, label, "", "", shape, paint, lineStroke, linePaint);
            return legendItem;
        } else {
            return null;
        }
    }

    // 自定义显示在series上的数据值
    @Override
    protected void drawItemLabel(Graphics2D g2, PlotOrientation orientation, XYDataset dataset, int series, int item, double x, double y, boolean negative) {
        XYItemLabelGenerator generator = this.getItemLabelGenerator(series, item);
        if (generator != null) {
            if (series == dataset.getSeriesCount() - 1) { // 最下面的刻度糖葫芦全部显示标签
                Font labelFont = this.getItemLabelFont(series, item);
                Paint paint = this.getItemLabelPaint(series, item);
                g2.setFont(labelFont);
                g2.setPaint(paint);
                String label = generator.generateLabel(dataset, series, item);
                ItemLabelPosition position;
                if (!negative) {
                    position = this.getPositiveItemLabelPosition(series, item);
                } else {
                    position = this.getNegativeItemLabelPosition(series, item);
                }

                Point2D anchorPoint = this.calculateLabelAnchorPoint(position.getItemLabelAnchor(), x, y, orientation);
                TextUtils.drawRotatedString(label, g2, (float)anchorPoint.getX(), (float)anchorPoint.getY(),
                        position.getTextAnchor(), position.getAngle(), position.getRotationAnchor());
            } else { // 普通糖葫芦仅显示最后一个，且数量为甲基化位点个数
                if (series % 3 == 0) { // 对含全部位点的行进行处理
                    String seriesString = String.valueOf(dataset.getSeriesKey(series));
                    String cpg = seriesString.substring(seriesString.indexOf("_") + 1, seriesString.indexOf("*")); // 找出mhap中的cpg
                    if (item == cpg.length() - 1) { // 最后一个糖葫芦
                        String label = seriesString.substring(seriesString.indexOf("*") + 1); // 找出mhap中的cnt，作为显示的标签
                        ItemLabelPosition position;
                        if (!negative) {
                            position = this.getPositiveItemLabelPosition(series, item);
                        } else {
                            position = this.getNegativeItemLabelPosition(series, item);
                        }
                        Point2D anchorPoint = this.calculateLabelAnchorPoint(position.getItemLabelAnchor(), x + 30, y, orientation);
                        TextUtils.drawRotatedString(label, g2, (float)anchorPoint.getX(), (float)anchorPoint.getY(),
                                position.getTextAnchor(), position.getAngle(), position.getRotationAnchor());
                    }
                }
            }
        }
    }
}
