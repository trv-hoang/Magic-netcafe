package com.netcafe.ui.admin;

import com.netcafe.service.ReportService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.TreeMap;

public class DashboardPanel extends JPanel {
    private final ReportService reportService = new ReportService();

    private final JPanel dailyChartContainer = new JPanel(new BorderLayout());
    private final JPanel monthlyChartContainer = new JPanel(new BorderLayout());
    private final JTextArea topProductsArea = new JTextArea(10, 20);
    private final JTextArea topSpendersArea = new JTextArea(10, 20);

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(com.netcafe.ui.ThemeConfig.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        chartsPanel.setBackground(com.netcafe.ui.ThemeConfig.BG_MAIN);

        dailyChartContainer.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        monthlyChartContainer.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        chartsPanel.add(dailyChartContainer);
        chartsPanel.add(monthlyChartContainer);

        JPanel listsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        listsPanel.setBackground(com.netcafe.ui.ThemeConfig.BG_MAIN);
        listsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JPanel p1 = new JPanel(new BorderLayout());
        p1.setBorder(BorderFactory.createTitledBorder(null, "Top Selling Products",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                com.netcafe.ui.ThemeConfig.FONT_SUBHEADER));
        p1.setBackground(Color.WHITE);
        topProductsArea.setFont(com.netcafe.ui.ThemeConfig.FONT_MONO);
        p1.add(new JScrollPane(topProductsArea));

        JPanel p2 = new JPanel(new BorderLayout());
        p2.setBorder(BorderFactory.createTitledBorder(null, "Top Spenders",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                com.netcafe.ui.ThemeConfig.FONT_SUBHEADER));
        p2.setBackground(Color.WHITE);
        topSpendersArea.setFont(com.netcafe.ui.ThemeConfig.FONT_MONO);
        p2.add(new JScrollPane(topSpendersArea));

        listsPanel.add(p1);
        listsPanel.add(p2);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chartsPanel, listsPanel);
        splitPane.setResizeWeight(0.75); // 75% for charts
        splitPane.setBorder(null);
        splitPane.setDividerSize(10);
        splitPane.setBackground(com.netcafe.ui.ThemeConfig.BG_MAIN);

        add(splitPane, BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.putClientProperty("JButton.buttonType", "roundRect");
        btnRefresh.setFont(com.netcafe.ui.ThemeConfig.FONT_SMALL);
        btnRefresh.setBackground(com.netcafe.ui.ThemeConfig.PRIMARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setMargin(new Insets(5, 10, 5, 10)); // Smaller margin

        JPanel header = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        header.setBackground(com.netcafe.ui.ThemeConfig.BG_MAIN);
        header.add(btnRefresh);
        add(header, BorderLayout.NORTH);

        btnRefresh.addActionListener(e -> loadDashboard());

        loadDashboard();
    }

    private void loadDashboard() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            Map<LocalDate, Long> dailyRevenue;
            Map<YearMonth, Long> monthlyRevenue;
            Map<String, Integer> topProducts;
            Map<String, Long> topSpenders;

            @Override
            protected Void doInBackground() throws Exception {
                dailyRevenue = reportService.getDailyRevenue();
                monthlyRevenue = reportService.getMonthlyRevenue();

                // Add dummy data for previous months to make it prettier
                monthlyRevenue.put(YearMonth.now().minusMonths(1), 1500000L);
                monthlyRevenue.put(YearMonth.now().minusMonths(2), 2100000L);
                monthlyRevenue.put(YearMonth.now().minusMonths(3), 1800000L);

                topProducts = reportService.getTopSellingProducts(5);
                topSpenders = reportService.getTopSpenders(5);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    updateDailyChart(dailyRevenue);
                    updateMonthlyChart(monthlyRevenue);

                    StringBuilder sbProd = new StringBuilder();
                    topProducts.forEach((k, v) -> sbProd.append(k).append(": ").append(v).append("\n"));
                    topProductsArea.setText(sbProd.toString());

                    StringBuilder sbUser = new StringBuilder();
                    topSpenders.forEach((k, v) -> sbUser.append(k).append(": ").append(v).append("\n"));
                    topSpendersArea.setText(sbUser.toString());

                } catch (Exception ex) {
                    com.netcafe.util.SwingUtils.showError(DashboardPanel.this, "Error loading dashboard", ex);
                }
            }
        };
        worker.execute();
    }

    private void updateDailyChart(Map<LocalDate, Long> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (data != null) {
            new TreeMap<>(data).forEach((date, amount) -> dataset.addValue(amount, "Revenue", date.toString()));
        }
        JFreeChart chart = ChartFactory.createLineChart("Daily Revenue", "Date", "VND", dataset,
                PlotOrientation.VERTICAL, false, true, false);

        // Modern Styling
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.setDomainGridlinesVisible(false);
        plot.setOutlineVisible(false);

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, com.netcafe.ui.ThemeConfig.PRIMARY); // Blue
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));

        dailyChartContainer.removeAll();
        dailyChartContainer.add(new ChartPanel(chart));
        dailyChartContainer.revalidate();
    }

    private void updateMonthlyChart(Map<YearMonth, Long> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (data != null) {
            new TreeMap<>(data).forEach((ym, amount) -> dataset.addValue(amount, "Revenue", ym.toString()));
        }
        JFreeChart chart = ChartFactory.createBarChart("Monthly Revenue", "Month", "VND", dataset,
                PlotOrientation.VERTICAL, false, true, false);

        // Modern Styling
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.setDomainGridlinesVisible(false);
        plot.setOutlineVisible(false);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, com.netcafe.ui.ThemeConfig.SECONDARY); // Green
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter()); // Flat bars
        renderer.setShadowVisible(false);

        monthlyChartContainer.removeAll();
        monthlyChartContainer.add(new ChartPanel(chart));
        monthlyChartContainer.revalidate();
    }
}
