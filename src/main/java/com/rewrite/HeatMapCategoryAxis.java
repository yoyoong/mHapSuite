package com.rewrite;

import org.jfree.chart.axis.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.text.TextBlock;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.category.CategoryDataset;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class HeatMapCategoryAxis extends CategoryAxis {

    /**
     * 重写获取横坐标的方法，根据步数踩点展示，防止横坐标密密麻麻
     */
    @Override
    public List<Tick> refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
        List<Tick> ticks = new ArrayList<>();
        if (dataArea.getHeight() <= 0.0 || dataArea.getWidth() < 0.0) {
            return ticks;
        }
        CategoryPlot plot = (CategoryPlot) getPlot();
        CategoryDataset dataset = plot.getDataset();
        int centerIndex = 0;
        for (int i = 0; i < dataset.getColumnCount(); i++) {
            if (String.valueOf(dataset.getColumnKey(i)).equals("center")) {
                centerIndex = i;
                break;
            }
        }

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

                if (categoryIndex == 0 || categoryIndex == centerIndex || categoryIndex == categories.size() - 1) {
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
