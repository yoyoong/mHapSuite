package com.rewrite;

import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.renderer.Outlier;
import org.jfree.chart.renderer.OutlierList;
import org.jfree.chart.renderer.OutlierListCollection;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.SerialUtils;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CustomBoxAndWhiskerRenderer extends BoxAndWhiskerRenderer {
    private static final long serialVersionUID = 632027470694481177L;
    private transient Paint artifactPaint;
    private boolean fillBox;
    private double itemMargin;
    private double maximumBarWidth;
    private boolean medianVisible;
    private boolean meanVisible;
    private boolean maxOutlierVisible;
    private boolean minOutlierVisible;
    private boolean useOutlinePaintForWhiskers;
    private double whiskerWidth;

    public CustomBoxAndWhiskerRenderer() {
        this.artifactPaint = Color.BLACK;
        this.fillBox = true;
        this.itemMargin = 0.2D;
        this.maximumBarWidth = 1.0D;
        this.medianVisible = true;
        this.meanVisible = true;
        this.minOutlierVisible = true;
        this.maxOutlierVisible = true;
        this.useOutlinePaintForWhiskers = false;
        this.whiskerWidth = 1.0D;
        this.setDefaultLegendShape(new Rectangle2D.Double(-4.0D, -4.0D, 8.0D, 8.0D));
    }

    public Paint getArtifactPaint() {
        return this.artifactPaint;
    }

    public void setArtifactPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.artifactPaint = paint;
        this.fireChangeEvent();
    }

    public boolean getFillBox() {
        return this.fillBox;
    }

    public void setFillBox(boolean flag) {
        this.fillBox = flag;
        this.fireChangeEvent();
    }

    public double getItemMargin() {
        return this.itemMargin;
    }

    public void setItemMargin(double margin) {
        this.itemMargin = margin;
        this.fireChangeEvent();
    }

    public double getMaximumBarWidth() {
        return this.maximumBarWidth;
    }

    public void setMaximumBarWidth(double percent) {
        this.maximumBarWidth = percent;
        this.fireChangeEvent();
    }

    public boolean isMeanVisible() {
        return this.meanVisible;
    }

    public void setMeanVisible(boolean visible) {
        if (this.meanVisible != visible) {
            this.meanVisible = visible;
            this.fireChangeEvent();
        }
    }

    public boolean isMedianVisible() {
        return this.medianVisible;
    }

    public void setMedianVisible(boolean visible) {
        if (this.medianVisible != visible) {
            this.medianVisible = visible;
            this.fireChangeEvent();
        }
    }

    public boolean isMinOutlierVisible() {
        return this.minOutlierVisible;
    }

    public void setMinOutlierVisible(boolean visible) {
        if (this.minOutlierVisible != visible) {
            this.minOutlierVisible = visible;
            this.fireChangeEvent();
        }
    }

    public boolean isMaxOutlierVisible() {
        return this.maxOutlierVisible;
    }

    public void setMaxOutlierVisible(boolean visible) {
        if (this.maxOutlierVisible != visible) {
            this.maxOutlierVisible = visible;
            this.fireChangeEvent();
        }
    }

    public boolean getUseOutlinePaintForWhiskers() {
        return this.useOutlinePaintForWhiskers;
    }

    public void setUseOutlinePaintForWhiskers(boolean flag) {
        if (this.useOutlinePaintForWhiskers != flag) {
            this.useOutlinePaintForWhiskers = flag;
            this.fireChangeEvent();
        }
    }

    public double getWhiskerWidth() {
        return this.whiskerWidth;
    }

    public void setWhiskerWidth(double width) {
        if (!(width < 0.0D) && !(width > 1.0D)) {
            if (width != this.whiskerWidth) {
                this.whiskerWidth = width;
                this.fireChangeEvent();
            }
        } else {
            throw new IllegalArgumentException("Value for whisker width out of range");
        }
    }

    public LegendItem getLegendItem(int datasetIndex, int series) {
        CategoryPlot cp = this.getPlot();
        if (cp == null) {
            return null;
        } else if (this.isSeriesVisible(series) && this.isSeriesVisibleInLegend(series)) {
            CategoryDataset dataset = cp.getDataset(datasetIndex);
            String label = this.getLegendItemLabelGenerator().generateLabel(dataset, series);
            String toolTipText = null;
            if (this.getLegendItemToolTipGenerator() != null) {
                toolTipText = this.getLegendItemToolTipGenerator().generateLabel(dataset, series);
            }

            String urlText = null;
            if (this.getLegendItemURLGenerator() != null) {
                urlText = this.getLegendItemURLGenerator().generateLabel(dataset, series);
            }

            Shape shape = this.lookupLegendShape(series);
            Paint paint = this.lookupSeriesPaint(series);
            Paint outlinePaint = this.lookupSeriesOutlinePaint(series);
            Stroke outlineStroke = this.lookupSeriesOutlineStroke(series);
            LegendItem result = new LegendItem(label, label, toolTipText, urlText, shape, paint, outlineStroke, outlinePaint);
            result.setLabelFont(this.lookupLegendTextFont(series));
            Paint labelPaint = this.lookupLegendTextPaint(series);
            if (labelPaint != null) {
                result.setLabelPaint(labelPaint);
            }

            result.setDataset(dataset);
            result.setDatasetIndex(datasetIndex);
            result.setSeriesKey(dataset.getRowKey(series));
            result.setSeriesIndex(series);
            return result;
        } else {
            return null;
        }
    }

    public Range findRangeBounds(CategoryDataset dataset) {
        return super.findRangeBounds(dataset, true);
    }

    public CategoryItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, CategoryPlot plot, int rendererIndex, PlotRenderingInfo info) {
        CategoryItemRendererState state = super.initialise(g2, dataArea, plot, rendererIndex, info);
        CategoryAxis domainAxis = this.getDomainAxis(plot, rendererIndex);
        CategoryDataset dataset = plot.getDataset(rendererIndex);
        if (dataset != null) {
            int columns = dataset.getColumnCount();
            int rows = dataset.getRowCount();
            double space = 0.0D;
            PlotOrientation orientation = plot.getOrientation();
            if (orientation == PlotOrientation.HORIZONTAL) {
                space = dataArea.getHeight();
            } else if (orientation == PlotOrientation.VERTICAL) {
                space = dataArea.getWidth();
            }

            double maxWidth = space * this.getMaximumBarWidth();
            double categoryMargin = 0.0D;
            double currentItemMargin = 0.0D;
            if (columns > 1) {
                categoryMargin = domainAxis.getCategoryMargin();
            }

            if (rows > 1) {
                currentItemMargin = this.getItemMargin();
            }

            double used = space * (1.0D - domainAxis.getLowerMargin() - domainAxis.getUpperMargin() - categoryMargin - currentItemMargin);
            if (rows * columns > 0) {
                state.setBarWidth(Math.min(used / (double) (dataset.getColumnCount() * dataset.getRowCount()), maxWidth));
            } else {
                state.setBarWidth(Math.min(used, maxWidth));
            }
        }

        return state;
    }

    public void drawItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset, int row, int column, int pass) {
        if (this.getItemVisible(row, column)) {
            if (!(dataset instanceof BoxAndWhiskerCategoryDataset)) {
                throw new IllegalArgumentException("BoxAndWhiskerRenderer.drawItem() : the data should be of type BoxAndWhiskerCategoryDataset only.");
            } else {
                PlotOrientation orientation = plot.getOrientation();
                if (orientation == PlotOrientation.HORIZONTAL) {
                    this.drawHorizontalItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, column);
                } else if (orientation == PlotOrientation.VERTICAL) {
                    this.drawVerticalItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, column);
                }

            }
        }
    }

    public void drawHorizontalItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset, int row, int column) {
        BoxAndWhiskerCategoryDataset bawDataset = (BoxAndWhiskerCategoryDataset) dataset;
        double categoryEnd = domainAxis.getCategoryEnd(column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge());
        double categoryStart = domainAxis.getCategoryStart(column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge());
        double categoryWidth = Math.abs(categoryEnd - categoryStart);
        int seriesCount = this.getRowCount();
        int categoryCount = this.getColumnCount();
        double yy;
        double seriesGap;
        if (seriesCount > 1) {
            seriesGap = dataArea.getHeight() * this.getItemMargin() / (double) (categoryCount * (seriesCount - 1));
            double usedWidth = state.getBarWidth() * (double) seriesCount + seriesGap * (double) (seriesCount - 1);
            double offset = (categoryWidth - usedWidth) / 2.0D;
            yy = categoryStart + offset + (double) row * (state.getBarWidth() + seriesGap);
        } else {
            seriesGap = (categoryWidth - state.getBarWidth()) / 2.0D;
            yy = categoryStart + seriesGap;
        }

        g2.setPaint(this.getItemPaint(row, column));
        Stroke s = this.getItemStroke(row, column);
        g2.setStroke(s);
        RectangleEdge location = plot.getRangeAxisEdge();
        Number xQ1 = bawDataset.getQ1Value(row, column);
        Number xQ3 = bawDataset.getQ3Value(row, column);
        Number xMax = bawDataset.getMaxRegularValue(row, column);
        Number xMin = bawDataset.getMinRegularValue(row, column);
        Shape box = null;
        double aRadius;
        if (xQ1 != null && xQ3 != null && xMax != null && xMin != null) {
            aRadius = rangeAxis.valueToJava2D(xQ1.doubleValue(), dataArea, location);
            double xxQ3 = rangeAxis.valueToJava2D(xQ3.doubleValue(), dataArea, location);
            double xxMax = rangeAxis.valueToJava2D(xMax.doubleValue(), dataArea, location);
            double xxMin = rangeAxis.valueToJava2D(xMin.doubleValue(), dataArea, location);
            double yymid = yy + state.getBarWidth() / 2.0D;
            double halfW = state.getBarWidth() / 2.0D * this.whiskerWidth;
            box = new Rectangle2D.Double(Math.min(aRadius, xxQ3), yy, Math.abs(aRadius - xxQ3), state.getBarWidth());
            if (this.fillBox) {
                g2.fill(box);
            }

            Paint outlinePaint = this.getItemOutlinePaint(row, column);
            if (this.useOutlinePaintForWhiskers) {
                g2.setPaint(outlinePaint);
            }

            g2.draw(new java.awt.geom.Line2D.Double(xxMax, yymid, xxQ3, yymid));
            g2.draw(new java.awt.geom.Line2D.Double(xxMax, yymid - halfW, xxMax, yymid + halfW));
            g2.draw(new java.awt.geom.Line2D.Double(xxMin, yymid, aRadius, yymid));
            g2.draw(new java.awt.geom.Line2D.Double(xxMin, yymid - halfW, xxMin, yymid + halfW));
            g2.setStroke(this.getItemOutlineStroke(row, column));
            g2.setPaint(outlinePaint);
            g2.draw(box);
        }

        g2.setPaint(this.artifactPaint);
        double xxMedian;
        Number xMedian;
        if (this.meanVisible) {
            xMedian = bawDataset.getMeanValue(row, column);
            if (xMedian != null) {
                xxMedian = rangeAxis.valueToJava2D(xMedian.doubleValue(), dataArea, location);
                aRadius = state.getBarWidth() / 4.0D;
                if (xxMedian > dataArea.getMinX() - aRadius && xxMedian < dataArea.getMaxX() + aRadius) {
                    java.awt.geom.Ellipse2D.Double avgEllipse = new java.awt.geom.Ellipse2D.Double(xxMedian - aRadius, yy + aRadius, aRadius * 2.0D, aRadius * 2.0D);
                    g2.fill(avgEllipse);
                    g2.draw(avgEllipse);
                }
            }
        }

        if (this.medianVisible) {
            xMedian = bawDataset.getMedianValue(row, column);
            if (xMedian != null) {
                xxMedian = rangeAxis.valueToJava2D(xMedian.doubleValue(), dataArea, location);
                g2.draw(new java.awt.geom.Line2D.Double(xxMedian, yy, xxMedian, yy + state.getBarWidth()));
            }
        }

        if (state.getInfo() != null && box != null) {
            EntityCollection entities = state.getEntityCollection();
            if (entities != null) {
                this.addItemEntity(entities, dataset, row, column, box);
            }
        }

    }

    @Override
    public void drawVerticalItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset, int row, int column) {
        BoxAndWhiskerCategoryDataset bawDataset = (BoxAndWhiskerCategoryDataset) dataset;
        double categoryEnd = domainAxis.getCategoryEnd(column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge());
        double categoryStart = domainAxis.getCategoryStart(column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge());
        double categoryWidth = categoryEnd - categoryStart;
        int seriesCount = this.getRowCount();
        int categoryCount = this.getColumnCount();
        double xx;
        double yyAverage;
        double yyOutlier;
        if (seriesCount > 1) {
            yyAverage = dataArea.getWidth() * this.getItemMargin() / (double) (categoryCount * (seriesCount - 1));
            yyOutlier = state.getBarWidth() * (double) seriesCount + yyAverage * (double) (seriesCount - 1);
            double offset = (categoryWidth - yyOutlier) / 2.0D;
            xx = categoryStart + offset + (double) row * (state.getBarWidth() + yyAverage);
        } else {
            yyAverage = (categoryWidth - state.getBarWidth()) / 2.0D;
            xx = categoryStart + yyAverage;
        }

        Paint itemPaint = this.getItemPaint(row, column);
        g2.setPaint(itemPaint);
        Stroke s = this.getItemStroke(row, column);
        g2.setStroke(s);
        double aRadius = 0.0D;
        RectangleEdge location = plot.getRangeAxisEdge();
        Number yQ1 = bawDataset.getQ1Value(row, column);
        Number yQ3 = bawDataset.getQ3Value(row, column);
        Number yMax = bawDataset.getMaxRegularValue(row, column);
        Number yMin = bawDataset.getMinRegularValue(row, column);
        Shape box = null;
        double maxAxisValue;
        double minAxisValue;
        double oRadius;
        double outlier;
        if (yQ1 != null && yQ3 != null && yMax != null && yMin != null) {
            maxAxisValue = rangeAxis.valueToJava2D(yQ1.doubleValue(), dataArea, location);
            minAxisValue = rangeAxis.valueToJava2D(yQ3.doubleValue(), dataArea, location);
            oRadius = rangeAxis.valueToJava2D(yMax.doubleValue(), dataArea, location);
            double yyMin = rangeAxis.valueToJava2D(yMin.doubleValue(), dataArea, location);
            double xxmid = xx + state.getBarWidth() / 2.0D;
            outlier = state.getBarWidth() / 2.0D * this.whiskerWidth;
            box = new Rectangle2D.Double(xx, Math.min(maxAxisValue, minAxisValue), state.getBarWidth(), Math.abs(maxAxisValue - minAxisValue));
            if (this.fillBox) {
                g2.fill(box);
            }

            Paint outlinePaint = this.getItemOutlinePaint(row, column);
            if (this.useOutlinePaintForWhiskers) {
                g2.setPaint(outlinePaint);
            }

            g2.draw(new java.awt.geom.Line2D.Double(xxmid, oRadius, xxmid, minAxisValue));
            g2.draw(new java.awt.geom.Line2D.Double(xxmid - outlier, oRadius, xxmid + outlier, oRadius));
            g2.draw(new java.awt.geom.Line2D.Double(xxmid, yyMin, xxmid, maxAxisValue));
            g2.draw(new java.awt.geom.Line2D.Double(xxmid - outlier, yyMin, xxmid + outlier, yyMin));
            g2.setStroke(this.getItemOutlineStroke(row, column));
            g2.setPaint(outlinePaint);
            g2.draw(box);
        }

        g2.setPaint(this.artifactPaint);
        Number yMedian;
        if (this.meanVisible) {
            yMedian = bawDataset.getMeanValue(row, column);
            if (yMedian != null) {
                yyAverage = rangeAxis.valueToJava2D(yMedian.doubleValue(), dataArea, location);
                aRadius = state.getBarWidth() / 4.0D;
                if (yyAverage > dataArea.getMinY() - aRadius && yyAverage < dataArea.getMaxY() + aRadius) {
                    java.awt.geom.Ellipse2D.Double avgEllipse = new java.awt.geom.Ellipse2D.Double(xx + aRadius, yyAverage - aRadius, aRadius * 2.0D, aRadius * 2.0D);
                    g2.fill(avgEllipse);
                    g2.draw(avgEllipse);
                }
            }
        }

        if (this.medianVisible) {
            yMedian = bawDataset.getMedianValue(row, column);
            if (yMedian != null) {
                double yyMedian = rangeAxis.valueToJava2D(yMedian.doubleValue(), dataArea, location);
                g2.draw(new java.awt.geom.Line2D.Double(xx, yyMedian, xx + state.getBarWidth(), yyMedian));
            }
        }

        maxAxisValue = rangeAxis.valueToJava2D(rangeAxis.getUpperBound(), dataArea, location) + aRadius;
        minAxisValue = rangeAxis.valueToJava2D(rangeAxis.getLowerBound(), dataArea, location) - aRadius;
        g2.setPaint(itemPaint);
        oRadius = state.getBarWidth() / 3.0D;
        java.util.List outliers = new ArrayList();
        OutlierListCollection outlierListCollection = new OutlierListCollection();
        List yOutliers = bawDataset.getOutliers(row, column);
        if (yOutliers != null) {
            for (int i = 0; i < yOutliers.size(); ++i) {
                outlier = ((Number) yOutliers.get(i)).doubleValue();
                Number minOutlier = bawDataset.getMinOutlier(row, column);
                Number maxOutlier = bawDataset.getMaxOutlier(row, column);
                Number minRegular = bawDataset.getMinRegularValue(row, column);
                Number maxRegular = bawDataset.getMaxRegularValue(row, column);
                if (outlier > maxOutlier.doubleValue()) {
                    outlierListCollection.setHighFarOut(true);
                } else if (outlier < minOutlier.doubleValue()) {
                    outlierListCollection.setLowFarOut(true);
                } else if (outlier > maxRegular.doubleValue()) {
                    yyOutlier = rangeAxis.valueToJava2D(outlier, dataArea, location);
                    outliers.add(new Outlier(xx + state.getBarWidth() / 2.0D, yyOutlier, oRadius));
                } else if (outlier < minRegular.doubleValue()) {
                    yyOutlier = rangeAxis.valueToJava2D(outlier, dataArea, location);
                    outliers.add(new Outlier(xx + state.getBarWidth() / 2.0D, yyOutlier, oRadius));
                }

                Collections.sort(outliers);
            }

            Iterator iterator = outliers.iterator();

            while (iterator.hasNext()) {
                Outlier outlier1 = (Outlier) iterator.next();
                outlierListCollection.add(outlier1);
            }

            iterator = outlierListCollection.iterator();

//            while (iterator.hasNext()) {
//                OutlierList list = (OutlierList) iterator.next();
//                Outlier outlier2 = list.getAveragedOutlier();
//                Point2D point = outlier2.getPoint();
//                if (list.isMultiple()) {
//                    this.drawMultipleEllipse(point, state.getBarWidth(), oRadius, g2);
//                } else {
//                    this.drawEllipse(point, oRadius, g2);
//                }
//            }

            if (this.isMaxOutlierVisible() && outlierListCollection.isHighFarOut()) {
                this.drawHighFarOut(aRadius / 2.0D, g2, xx + state.getBarWidth() / 2.0D, maxAxisValue);
            }

            if (this.isMinOutlierVisible() && outlierListCollection.isLowFarOut()) {
                this.drawLowFarOut(aRadius / 2.0D, g2, xx + state.getBarWidth() / 2.0D, minAxisValue);
            }
        }

        if (state.getInfo() != null && box != null) {
            EntityCollection entities = state.getEntityCollection();
            if (entities != null) {
                this.addItemEntity(entities, dataset, row, column, box);
            }
        }

    }

    private void drawEllipse(Point2D point, double oRadius, Graphics2D g2) {
        Ellipse2D dot = new java.awt.geom.Ellipse2D.Double(point.getX() + oRadius / 2.0D, point.getY(), oRadius, oRadius);
        g2.draw(dot);
    }

    private void drawMultipleEllipse(Point2D point, double boxWidth, double oRadius, Graphics2D g2) {
        Ellipse2D dot1 = new java.awt.geom.Ellipse2D.Double(point.getX() - boxWidth / 2.0D + oRadius, point.getY(), oRadius, oRadius);
        Ellipse2D dot2 = new java.awt.geom.Ellipse2D.Double(point.getX() + boxWidth / 2.0D, point.getY(), oRadius, oRadius);
        g2.draw(dot1);
        g2.draw(dot2);
    }

    private void drawHighFarOut(double aRadius, Graphics2D g2, double xx, double m) {
        double side = aRadius * 2.0D;
        g2.draw(new java.awt.geom.Line2D.Double(xx - side, m + side, xx + side, m + side));
        g2.draw(new java.awt.geom.Line2D.Double(xx - side, m + side, xx, m));
        g2.draw(new java.awt.geom.Line2D.Double(xx + side, m + side, xx, m));
    }

    private void drawLowFarOut(double aRadius, Graphics2D g2, double xx, double m) {
        double side = aRadius * 2.0D;
        g2.draw(new java.awt.geom.Line2D.Double(xx - side, m - side, xx + side, m - side));
        g2.draw(new java.awt.geom.Line2D.Double(xx - side, m - side, xx, m));
        g2.draw(new java.awt.geom.Line2D.Double(xx + side, m - side, xx, m));
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writePaint(this.artifactPaint, stream);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.artifactPaint = SerialUtils.readPaint(stream);
    }
}
