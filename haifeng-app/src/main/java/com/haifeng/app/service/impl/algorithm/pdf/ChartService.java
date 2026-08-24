package com.haifeng.app.service.impl.algorithm.pdf;

import com.haifeng.app.vo.algorithm.pdf.MacroAnalysisVO;
import com.haifeng.app.vo.algorithm.pdf.ReduceResult;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

/**
 * 图表生成服务（JFreeChart）
 * <p>生成优先级水平柱状图，用于PDF报告的综合考虑部分
 */
@Slf4j
@Service
public class ChartService {

    /**
     * 生成优先级水平柱状图
     *
     * @param rankingList 排名列表
     * @return Base64编码的PNG图片
     */
    public String generateRankingChart(List<MacroAnalysisVO.RankingItem> rankingList) {
        if (rankingList == null || rankingList.isEmpty()) {
            log.warn("Ranking list is empty, skip chart generation");
            return null;
        }

        try {
            CategoryDataset dataset = createDataset(rankingList);
            JFreeChart chart = createChart(dataset);

            // 生成图片
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(baos, chart, 800, Math.max(400, rankingList.size() * 50 + 100));

            // 转Base64
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            log.error("Failed to generate ranking chart", e);
            return null;
        }
    }

    /**
     * 生成体制内适配度水平柱状图（第十部分）
     *
     * @param scores 打分列表（专业名+分数0-100）
     * @return Base64编码的PNG图片
     */
    public String generateScoreChart(List<ReduceResult.ScoreItem> scores) {
        if (scores == null || scores.isEmpty()) {
            log.warn("Score list is empty, skip chart generation");
            return null;
        }

        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            // 从高到低排列（JFreeChart从下到上显示，所以倒序添加）
            List<ReduceResult.ScoreItem> sorted = new java.util.ArrayList<>(scores);
            sorted.sort((a, b) -> Integer.compare(b.getScore() != null ? b.getScore() : 0,
                    a.getScore() != null ? a.getScore() : 0));
            for (int i = sorted.size() - 1; i >= 0; i--) {
                ReduceResult.ScoreItem item = sorted.get(i);
                String label = item.getMajorName() != null ? item.getMajorName() : "未知专业";
                if (item.getGroupName() != null && !item.getGroupName().isBlank()) {
                    label = item.getGroupName() + " - " + label;
                }
                dataset.addValue(item.getScore() != null ? item.getScore() : 0, "体制内适配度", label);
            }
            JFreeChart chart = createChart(dataset, "体制内适配度");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(baos, chart, 800, Math.max(400, sorted.size() * 50 + 100));
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            log.error("Failed to generate score chart", e);
            return null;
        }
    }

    /**
     * 创建数据集
     */
    private CategoryDataset createDataset(List<MacroAnalysisVO.RankingItem> rankingList) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // 从高到低排列（JFreeChart从下到上显示，所以倒序添加）
        for (int i = rankingList.size() - 1; i >= 0; i--) {
            MacroAnalysisVO.RankingItem item = rankingList.get(i);
            String label = item.getRank() + ". " + item.getUniversityName() + " - " + item.getMajorName();
            dataset.addValue(item.getScore(), "综合得分", label);
        }

        return dataset;
    }

    /**
     * 创建图表
     */
    private JFreeChart createChart(CategoryDataset dataset) {
        return createChart(dataset, "综合得分");
    }

    /**
     * 创建图表（自定义X轴标签）
     */
    private JFreeChart createChart(CategoryDataset dataset, String rangeAxisLabel) {
        JFreeChart chart = ChartFactory.createBarChart(
                null,           // 标题
                rangeAxisLabel, // X轴标签
                null,           // Y轴标签
                dataset,
                PlotOrientation.HORIZONTAL,
                false,          // 不显示图例
                true,
                false
        );

        // 设置背景色
        chart.setBackgroundPaint(Color.WHITE);

        // 获取绘图区域
        CategoryPlot plot = chart.getCategoryPlot();

        // 设置绘图区域背景
        plot.setBackgroundPaint(new Color(250, 250, 250));
        plot.setRangeGridlinePaint(new Color(230, 230, 230));

        // 设置柱状图渲染器
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(79, 129, 189));  // 蓝色
        renderer.setDrawBarOutline(false);

        // 显示数值标签
        CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(
                "{2}",  // 格式：显示数值
                new java.text.DecimalFormat("0.0")
        );
        renderer.setSeriesItemLabelGenerator(0, generator);
        renderer.setSeriesItemLabelsVisible(0, true);
        renderer.setSeriesItemLabelFont(0, new Font("SansSerif", Font.PLAIN, 11));

        // 设置Y轴（类别轴）
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        domainAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));

        // 设置X轴（数值轴）
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(0, 100);  // 分数范围0-100
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 11));

        return chart;
    }
}
