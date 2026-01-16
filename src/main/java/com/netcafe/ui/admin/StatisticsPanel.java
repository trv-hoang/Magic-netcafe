package com.netcafe.ui.admin;

import com.netcafe.dao.StatisticsDAO;
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
        // SỬ DỤNG BORDER LAYOUT ĐỂ TỐI ƯU KHÔNG GIAN
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 247, 250));

        // --- KHU VỰC TRUNG TÂM: CHỨA 4 BIỂU ĐỒ (GRID 2x2) ---
        JPanel chartsContainer = new JPanel(new GridLayout(2, 2, 20, 20));
        chartsContainer.setOpaque(false); // Để lộ màu nền của panel cha

        // 1. Line Chart (Xu hướng Doanh thu)
        chartsContainer.add(createRevenueLineChart());

        // 2. Bar Chart (Top Món ăn)
        chartsContainer.add(createBarChart("TOP MÓN ĂN BÁN CHẠY", "Món", "Số lượng", 2));

        // 3. Bar Chart (Top User)
        chartsContainer.add(createBarChart("TOP USER NẠP TIỀN", "User", "VND", 3));

        // 4. Pie Chart (Cơ cấu Doanh thu)
        chartsContainer.add(createRevenueStructurePieChart());

        add(chartsContainer, BorderLayout.CENTER);

        // --- KHU VỰC DƯỚI CÙNG: NÚT CHỨC NĂNG ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setOpaque(false);

        JButton btnRefresh = new JButton("Cập nhật dữ liệu");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRefresh.setBackground(new Color(0, 123, 255));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setPreferredSize(new Dimension(180, 40));
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnRefresh.addActionListener(e -> refreshData());
        controlPanel.add(btnRefresh);

        add(controlPanel, BorderLayout.SOUTH);
    }

    // --- CHART 1: LINE CHART (ĐƯỜNG) ---
    private ChartPanel createRevenueLineChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            dataset = statsDAO.getMonthlyRevenue();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart lineChart = ChartFactory.createLineChart(
                "XU HƯỚNG DOANH THU", "Tháng", "Doanh thu (VND)",
                dataset, PlotOrientation.VERTICAL, true, true, false
        );

        CategoryPlot plot = lineChart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));

        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(255, 99, 71));
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        renderer.setSeriesShapesVisible(0, true);
        plot.setRenderer(renderer);

        // Format trục Y tiền tệ
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setNumberFormatOverride(NumberFormat.getNumberInstance(Locale.US));

        return new ChartPanel(lineChart);
    }

    // --- CHART 2 & 3: BAR CHART (CỘT) ---
    private ChartPanel createBarChart(String title, String xLabel, String yLabel, int type) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            if (type == 2) dataset = statsDAO.getTopSellingProducts();
            else if (type == 3) dataset = statsDAO.getTopSpenders();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        PlotOrientation orientation = (type == 2) ? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL;

        JFreeChart chart = ChartFactory.createBarChart(
                title, xLabel, yLabel,
                dataset, orientation, false, true, false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());

        if (type == 2) {
            renderer.setSeriesPaint(0, new Color(40, 167, 69)); // Xanh lá
        } else {
            renderer.setSeriesPaint(0, new Color(23, 162, 184)); // Xanh Cyan
        }

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setNumberFormatOverride(NumberFormat.getNumberInstance(Locale.US));

        return new ChartPanel(chart);
    }

    // --- CHART 4: PIE CHART (TRÒN) - MỚI ---
    private ChartPanel createRevenueStructurePieChart() {
        // [QUAN TRỌNG] Đã thêm <String> để khớp với StatisticsDAO
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        try {
            dataset = statsDAO.getRevenueStructure();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart pieChart = ChartFactory.createPieChart(
                "CƠ CẤU DOANH THU TRONG THÁNG", // Tiêu đề
                dataset,            // Dữ liệu
                true,               // Legend (Chú thích)
                true,
                false
        );

        // Tùy chỉnh giao diện Pie Chart
        PiePlot<String> plot = (PiePlot<String>) pieChart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false); // Bỏ viền bao quanh

        // Màu sắc cho các phần (Section)
        plot.setSectionPaint("Dịch vụ (Đồ ăn/Uống)", new Color(255, 193, 7)); // Màu Vàng
        plot.setSectionPaint("Giờ chơi (Nạp tiền)", new Color(0, 123, 255));  // Màu Xanh Dương
        plot.setSectionPaint("Chưa có dữ liệu", Color.LIGHT_GRAY);

        // Hiển thị Label
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0} : {1} ({2})",
                new DecimalFormat("#,##0 VND"),
                new DecimalFormat("0%")
        ));

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