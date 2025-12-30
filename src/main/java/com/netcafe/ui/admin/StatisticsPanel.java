package com.netcafe.ui.admin;

import com.netcafe.dao.StatisticsDAO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

public class StatisticsPanel extends JPanel {

    private StatisticsDAO statsDAO;

    public StatisticsPanel() {
        statsDAO = new StatisticsDAO();
        initUI();
    }

    private void initUI() {
        // Layout 2x2: 4 ô
        setLayout(new GridLayout(2, 2, 20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 247, 250)); // Màu nền xám nhẹ hiện đại

        // 1. Biểu đồ Đường (Doanh thu)
        add(createRevenueLineChart());

        // 2. Biểu đồ Cột Ngang (Top Món)
        add(createBarChart("TOP MÓN ĂN BÁN CHẠY", "Món", "Số lượng", 2));

        // 3. Biểu đồ Cột Đứng (Top User)
        add(createBarChart("TOP USER NẠP TIỀN", "User", "VND", 3));

        // 4. Panel Chức năng
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBackground(new Color(245, 247, 250));
        
        JButton btnRefresh = new JButton("Cập nhật dữ liệu");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRefresh.setBackground(new Color(0, 123, 255)); // Màu xanh nút Bootstrap
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnRefresh.addActionListener(e -> refreshData());
        controlPanel.add(btnRefresh);
        add(controlPanel);
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
                "XU HƯỚNG DOANH THU", // Title
                "Tháng",             // X-Axis Label
                "Doanh thu (VND)",   // Y-Axis Label
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        // Style cho biểu đồ đường
        CategoryPlot plot = lineChart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(220, 220, 220)); // Lưới màu xám nhạt
        
        // Chỉnh đường kẻ (Line) và điểm (Dot)
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(255, 99, 71)); // Màu cà chua (Tomato)
        renderer.setSeriesStroke(0, new BasicStroke(3.0f)); // Đường dày 3px
        renderer.setSeriesShapesVisible(0, true); // Hiện chấm tròn tại các điểm
        plot.setRenderer(renderer);

        return new ChartPanel(lineChart);
    }

    // --- CHART 2 & 3: BAR CHART (CỘT 2D PHẲNG) ---
    private ChartPanel createBarChart(String title, String xLabel, String yLabel, int type) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            if (type == 2) dataset = statsDAO.getTopSellingProducts();
            else if (type == 3) dataset = statsDAO.getTopSpenders();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        PlotOrientation orientation = (type == 2) ? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL;

        // Dùng createBarChart thay vì createBarChart3D để tránh lỗi
        JFreeChart chart = ChartFactory.createBarChart(
                title, xLabel, yLabel,
                dataset, orientation, false, true, false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));

        // Tùy chỉnh màu sắc cột cho đẹp (Flat Design)
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter()); // Tắt hiệu ứng bóng (Gradient) để phẳng đẹp
        
        if (type == 2) {
            renderer.setSeriesPaint(0, new Color(40, 167, 69)); // Màu xanh lá (Top Món)
        } else {
            renderer.setSeriesPaint(0, new Color(23, 162, 184)); // Màu xanh Cyan (Top User)
        }

        // Định dạng số trên trục Y (ví dụ: 1,000,000)
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setNumberFormatOverride(NumberFormat.getNumberInstance(Locale.US));

        return new ChartPanel(chart);
    }

    public void refreshData() {
        removeAll();
        initUI();
        revalidate();
        repaint();
    }
}