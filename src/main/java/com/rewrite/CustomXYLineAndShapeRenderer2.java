package com.rewrite;

import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.LineUtils;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class CustomXYLineAndShapeRenderer2 extends XYLineAndShapeRenderer {
    private double width;
    List<Integer> labelList = new ArrayList<>();

    public double getWidth() {
        return width;
    }
    public void setWidth(double width) {
        this.width = width;
    }
    public List<Integer> getLabelList() {
        return labelList;
    }
    public void setLabelList(List<Integer> labelList) {
        this.labelList = labelList;
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
                String label = String.valueOf(labelList.get(item));
                ItemLabelPosition position;
                if (!negative) {
                    position = this.getPositiveItemLabelPosition(series, item);
                } else {
                    position = this.getNegativeItemLabelPosition(series, item);
                }
                Point2D anchorPoint = this.calculateLabelAnchorPoint(position.getItemLabelAnchor(), x, y, orientation);
                TextUtils.drawRotatedString(label, g2, (float)anchorPoint.getX(), (float)anchorPoint.getY(),
                        position.getTextAnchor(), position.getAngle(), position.getRotationAnchor());
            }
        }
    }
}
