package com.netcafe.ui.admin;

import com.netcafe.dao.StatisticsDAO;
import com.netcafe.ui.component.StyledButton;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class StatisticsPanel extends JPanel {

    private StatisticsDAO statsDAO;

    public StatisticsPanel() {
        statsDAO = new StatisticsDAO();
        initUI();
    }

    private void initUI() {
        // USE BORDER LAYOUT TO OPTIMIZE SPACE
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 247, 250));

        // --- CENTER AREA: CONTAINS 4 CHARTS (GRID 2x2) ---
        JPanel chartsContainer = new JPanel(new GridLayout(2, 2, 20, 20));
        chartsContainer.setOpaque(false); // Expose parent panel background

        // 1. Line Chart (Revenue Trend)
        chartsContainer.add(createRevenueLineChart());

        // 2. Bar Chart (Top Products)
        chartsContainer.add(createBarChart("TOP SELLING PRODUCTS", "Product", "Quantity", 2));

        // 3. Bar Chart (Top User)
        chartsContainer.add(createBarChart("TOP USERS BY TOP-UP", "User", "VND", 3));

        // 4. Pie Chart (Revenue Structure)
        chartsContainer.add(createRevenueStructurePieChart());

        add(chartsContainer, BorderLayout.CENTER);

        // --- BOTTOM AREA: CONTROL BUTTONS ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setOpaque(false);

        JButton btnRefresh = StyledButton.primary("Refresh Data");
        btnRefresh.setPreferredSize(new Dimension(180, 40));
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnRefresh.addActionListener(e -> refreshData());
        controlPanel.add(btnRefresh);

        add(controlPanel, BorderLayout.SOUTH);
    }

    // --- CHART 1: LINE CHART ---
    private ChartPanel createRevenueLineChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            dataset = statsDAO.getMonthlyRevenue();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart lineChart = ChartFactory.createLineChart(
                "REVENUE TREND", "Month", "Revenue (VND)",
                dataset, PlotOrientation.VERTICAL, true, true, false);

        CategoryPlot plot = lineChart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));

        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(255, 99, 71));
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        renderer.setSeriesShapesVisible(0, true);
        plot.setRenderer(renderer);

        // Format Y axis as currency
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setNumberFormatOverride(NumberFormat.getNumberInstance(Locale.US));

        return new ChartPanel(lineChart);
    }

    // --- CHART 2 & 3: BAR CHART ---
    private ChartPanel createBarChart(String title, String xLabel, String yLabel, int type) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            if (type == 2)
                dataset = statsDAO.getTopSellingProducts();
            else if (type == 3)
                dataset = statsDAO.getTopSpenders();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        PlotOrientation orientation = (type == 2) ? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL;

        JFreeChart chart = ChartFactory.createBarChart(
                title, xLabel, yLabel,
                dataset, orientation, false, true, false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());

        if (type == 2) {
            renderer.setSeriesPaint(0, new Color(40, 167, 69)); // Green
        } else {
            renderer.setSeriesPaint(0, new Color(23, 162, 184)); // Cyan
        }

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setNumberFormatOverride(NumberFormat.getNumberInstance(Locale.US));

        return new ChartPanel(chart);
    }

    // --- CHART 4: PIE CHART - NEW ---
    private ChartPanel createRevenueStructurePieChart() {
        // [IMPORTANT] Added <String> to match StatisticsDAO
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        try {
            dataset = statsDAO.getRevenueStructure();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart pieChart = ChartFactory.createPieChart(
                "MONTHLY REVENUE STRUCTURE", // Title
                dataset, // Data
                true, // Legend
                true,
                false);

        // Customize Pie Chart appearance
        PiePlot<String> plot = (PiePlot<String>) pieChart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false); // Remove outline

        // Colors for sections
        plot.setSectionPaint("Services (Food/Drink)", new Color(255, 193, 7)); // Yellow
        plot.setSectionPaint("Play Time (Top-up)", new Color(0, 123, 255)); // Blue
        plot.setSectionPaint("No Data", Color.LIGHT_GRAY);

        // Display Labels
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0} : {1} ({2})",
                new DecimalFormat("#,##0 VND"),
                new DecimalFormat("0%")));

        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));

        return new ChartPanel(pieChart);
    }

    public void refreshData() {
        removeAll();
        initUI();
        revalidate();
        repaint();
    }
}