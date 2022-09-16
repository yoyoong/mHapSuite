package com.rewrite;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.util.Args;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class CustomXYBlockRenderer2 extends XYBlockRenderer {
    private double blockWidth = 1.0D;
    private double blockHeight = 1.0D;
    private RectangleAnchor blockAnchor;
    private double xOffset;
    private double yOffset;
    private PaintScale paintScale;
    private Integer xBlockNum;
    private Integer yBlockNum;

    public PaintScale getPaintScale() {
        return this.paintScale;
    }

    public void setPaintScale(PaintScale scale) {
        Args.nullNotPermitted(scale, "scale");
        this.paintScale = scale;
        this.fireChangeEvent();
    }

    public Integer getxBlockNum() {
        return xBlockNum;
    }

    public void setxBlockNum(Integer xBlockNum) {
        this.xBlockNum = xBlockNum;
    }

    public Integer getyBlockNum() {
        return yBlockNum;
    }

    public void setyBlockNum(Integer yBlockNum) {
        this.yBlockNum = yBlockNum;
    }

    // 自定义图例
    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, CrosshairState crosshairState, int pass) {
        double x = dataset.getXValue(series, item);
        double y = dataset.getYValue(series, item);

        if (x != 0 && y != 0) {
            int xa = (int) (dataArea.getX() + dataArea.getWidth() / this.xBlockNum * x);
            int xb = (int) (dataArea.getX() + dataArea.getWidth() / this.xBlockNum * (x + 1));
            int xc = (int) (dataArea.getX() + dataArea.getWidth() / this.xBlockNum * (x + 1));
            int xd = (int) (dataArea.getX() + dataArea.getWidth() / this.xBlockNum * x);
            int ya = (int) (dataArea.getY() + dataArea.getHeight() / this.yBlockNum * (this.yBlockNum - y));
            int yb = (int) (dataArea.getY() + dataArea.getHeight() / this.yBlockNum * (this.yBlockNum - y));
            int yc = (int) (dataArea.getY() + dataArea.getHeight() / this.yBlockNum * (this.yBlockNum - y + 1));
            int yd = (int) (dataArea.getY() + dataArea.getHeight() / this.yBlockNum * (this.yBlockNum - y + 1));

            int [] xPos = {xa, xb, xc, xd};
            int [] yPos = {ya, yb, yc, yd};
            g2.drawPolygon(xPos, yPos, 4);
            g2.setPaint(new Color(0, 136, 0));
            g2.fillPolygon(xPos, yPos, 4);
        }
    }
}
