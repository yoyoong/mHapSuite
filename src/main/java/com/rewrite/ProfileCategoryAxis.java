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
        List<?> categories = plot.getCategoriesForAxis(this);
        double max = 0.0;
        if (categories != null) {
            CategoryLabelPosition position = super.getCategoryLabelPositions().getLabelPosition(edge);
            int categoryIndex = 0;
            for (Object o : categories) {
                Comparable<?> category = (Comparable<?>) o;
                g2.setFont(getTickLabelFont(category));
                TextBlock label = new TextBlock();
                label.addLine(category.toString(), getTickLabelFont(category), getTickLabelPaint(category));
                if (edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM) {
                    max = Math.max(max, calculateTextBlockHeight(label, position, g2));
                } else if (edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT) {
                    max = Math.max(max, calculateTextBlockWidth(label, position, g2));
                }

                if (categoryIndex == 0 || categoryIndex == startIndex || categoryIndex == endIndex || categoryIndex == categories.size() - 1) {
                    Tick tick = new CategoryTick(category, label, position.getLabelAnchor(), position.getRotationAnchor(), position.getAngle());
                    ticks.add(tick);
                } else {
                    Tick tick = new CategoryTick(category, new TextBlock(), position.getLabelAnchor(), position.getRotationAnchor(), position.getAngle());
                    ticks.add(tick);
                }
                categoryIndex = categoryIndex + 1;
            }
        }
        state.setMax(max);
        return ticks;
    }

}
