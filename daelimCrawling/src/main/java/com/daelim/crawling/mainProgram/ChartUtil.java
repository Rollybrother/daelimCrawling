package com.daelim.crawling.mainProgram;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.category.DefaultCategoryDataset;

import com.daelim.crawling.mainProgram.competitor.CompetitorDto;

public class ChartUtil {

    public static byte[] createChartImage(ArrayList<percentDto> percentArray) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (percentDto dto : percentArray) {
            dataset.addValue(dto.getPercent(), "Percent", dto.getShoppingMall());
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "violation rate", // 차트 제목
                "shoppingMall", // x축 레이블
                "rate", // y축 레이블
                dataset,
                PlotOrientation.VERTICAL,
                false, // 범례 표시 여부
                true, // 툴팁 표시 여부
                false // URL 생성 여부
        );

        // 불필요한 항목 제거
        LegendTitle legend = barChart.getLegend();
        if (legend != null) {
            legend.setPosition(RectangleEdge.BOTTOM);
            legend.setVisible(false); // 범례 숨김
        }

        BufferedImage chartImage = barChart.createBufferedImage(800, 600);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtils.writeBufferedImageAsPNG(baos, chartImage);
        return baos.toByteArray();
    }
    
    public static byte[] createCompetitorChartImage(ArrayList<CompetitorDto> competitorArray) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (CompetitorDto dto : competitorArray) {
            String rowKey = dto.getProductName() + ", " + dto.getShoppingMall();
            dataset.addValue(dto.getAveragePrice(), "Average Price", rowKey);
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Competitor Prices",
                "Product, Shopping Mall",
                "Average Price",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // 커스터마이즈 차트 색상 등
        CategoryPlot plot = barChart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(70, 130, 180)); // 짙은 하늘색

        BufferedImage chartImage = barChart.createBufferedImage(800, 600);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtils.writeBufferedImageAsPNG(baos, chartImage);

        return baos.toByteArray();
    }
}
