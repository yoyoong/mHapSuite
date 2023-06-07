package com.rewrite;

import org.jfree.chart.axis.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.text.TextBlock;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.category.CategoryDataset;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProfileCategoryAxis extends CategoryAxis {
    private CategoryLabelPositions categoryLabelPositions = CategoryLabelPositions.STANDARD;
    private float maximumCategoryLabelWidthRatio = 0.0F;
    /**
     * 重写获取横坐标的方法，根据步数踩点展示，防止横坐标密密麻麻
     */
    @Override
    public List<Tick> refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
        CategoryPlot plot = (CategoryPlot) getPlot();
        CategoryDataset dataset = plot.getDataset();
        int startIndex = 0;
        int endIndex = 0;
        for (int i = 0; i < dataset.getColumnCount(); i++) {
            if (String.valueOf(dataset.getColumnKey(i)).equals("start")) {
                startIndex = i;
            } else if (String.valueOf(dataset.getColumnKey(i)).equals("end")) {
                endIndex = i;
            }
        }

        List ticks = new ArrayList();
        if (!(dataArea.getHeight() <= 0.0D) && !(dataArea.getWidth() < 0.0D)) {
            List categories = plot.getCategoriesForAxis(this);
            double max = 0.0D;
            if (categories != null) {
                CategoryLabelPosition position = this.categoryLabelPositions.getLabelPosition(edge);
                float r = this.maximumCategoryLabelWidthRatio;
                if ((double)r <= 0.0D) {
                    r = position.getWidthRatio();
                }

                float l;
                if (position.getWidthType() == CategoryLabelWidthType.CATEGORY) {
                    l = (float)this.calculateCategorySize(categories.size(), dataArea, edge);
                } else if (RectangleEdge.isLeftOrRight(edge)) {
                    l = (float)dataArea.getWidth();
                } else {
                    l = (float)dataArea.getHeight();
                }

                int categoryIndex = 0;

                for(Iterator iterator = categories.iterator(); iterator.hasNext(); ++categoryIndex) {
                    Comparable category = (Comparable)iterator.next();
                    g2.setFont(this.getTickLabelFont(category));
                    TextBlock label = this.createLabel(category, l * r, edge, g2);
                    if (edge != RectangleEdge.TOP && edge != RectangleEdge.BOTTOM) {
                        if (edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT) {
                            max = Math.max(max, this.calculateTextBlockWidth(label, position, g2));
                        }
                    } else {
                        max = Math.max(max, this.calculateTextBlockHeight(label, position, g2));
                    }

                    if (categoryIndex == 0 || categoryIndex == startIndex || categoryIndex == endIndex || categoryIndex == categories.size() - 1) {
                        Tick tick = new CategoryTick(category, label, position.getLabelAnchor(), position.getRotationAnchor(), position.getAngle());
                        ticks.add(tick);
                    } else {
                        Tick tick = new CategoryTick(category, new TextBlock(), position.getLabelAnchor(), position.getRotationAnchor(), position.getAngle());
                        ticks.add(tick);
                    }
                }
            }

            state.setMax(max);
            return ticks;
        } else {
            return ticks;
        }
    }

}
