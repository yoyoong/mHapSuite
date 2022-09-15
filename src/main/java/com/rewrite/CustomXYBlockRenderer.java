package com.rewrite;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.util.Args;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class CustomXYBlockRenderer extends XYBlockRenderer {
    private double blockWidth = 1.0D;
    private double blockHeight = 1.0D;
    private RectangleAnchor blockAnchor;
    private double xOffset;
    private double yOffset;
    private PaintScale paintScale;
    private Integer blockNum;

    public PaintScale getPaintScale() {
        return this.paintScale;
    }

    public void setPaintScale(PaintScale scale) {
        Args.nullNotPermitted(scale, "scale");
        this.paintScale = scale;
        this.fireChangeEvent();
    }

    public Integer getBlockNum() {
        return blockNum;
    }

    public void setBlockNum(Integer blockNum) {
        this.blockNum = blockNum;
    }

    // 自定义图例
    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, CrosshairState crosshairState, int pass) {
        double x = dataset.getXValue(series, item);
        double y = dataset.getYValue(series, item);
        double z = 0.0D;
        if (dataset instanceof XYZDataset) {
            z = ((XYZDataset)dataset).getZValue(series, item);
        }

        Double blockWidth = dataArea.getWidth() / this.blockNum;
        Double halfBlockWidth = dataArea.getWidth() / this.blockNum / 2;
                
        int xa = (int) (dataArea.getX() * 1.05 + halfBlockWidth + blockWidth * x + halfBlockWidth * y);
        int xb = (int) (dataArea.getX() * 1.05 + blockWidth * x + halfBlockWidth * y);
        int xc = (int) (dataArea.getX() * 1.05 + halfBlockWidth + blockWidth * x + halfBlockWidth * y);
        int xd = (int) (dataArea.getX() * 1.05 + blockWidth + blockWidth * x + halfBlockWidth * y);
        int ya = (int) (dataArea.getY() + halfBlockWidth / Math.sqrt(3) * y);
        int yb = (int) (dataArea.getY() + halfBlockWidth / Math.sqrt(3) + (halfBlockWidth / Math.sqrt(3)) * y);
        int yc = (int) (dataArea.getY() + blockWidth / Math.sqrt(3) + (halfBlockWidth / Math.sqrt(3)) * y);
        int yd = (int) (dataArea.getY() + halfBlockWidth / Math.sqrt(3) + (halfBlockWidth / Math.sqrt(3)) * y);
        int [] xPos = {xa, xb, xc, xd};
        int [] yPos = {ya, yb, yc, yd};
        g2.drawPolygon(xPos, yPos, 4);
        g2.setPaint(this.paintScale.getPaint(z));
        g2.fillPolygon(xPos, yPos, 4);

        g2.setStroke(new BasicStroke(Float.valueOf(String.valueOf(blockWidth / 20))));
        g2.setPaint(Color.WHITE);
        g2.drawPolygon(xPos, yPos, 4);

    }
}
