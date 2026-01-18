package com.netcafe.ui.admin;

import com.netcafe.service.AIService;
import com.netcafe.service.AnalyticsService;
import com.netcafe.service.DemoService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AIAnalystPanel extends JPanel {

    private final AIService aiService = new AIService();
    private final AnalyticsService analyticsService = new AnalyticsService();
    private final DemoService demoService = new DemoService(); // Initialize DemoService

    private final JTextArea chatArea = new JTextArea();
    private final JPanel chartContainer = new JPanel(new BorderLayout());
    private final JTextField txtInput = new JTextField();

    private final Color BG_COLOR = new Color(245, 247, 250); // Light gray
    private final Color CHAT_BG_COLOR = Color.WHITE;
    private final Color TEXT_COLOR = new Color(50, 50, 50);
    private final Color PRIMARY_COLOR = new Color(0, 120, 215); // Blue

    public AIAnalystPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.35);
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);

        // --- LEFT PANEL: JARVIS CHAT ---
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), "Jarvis AI Assistant"));
        chatPanel.setBackground(BG_COLOR);

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setBackground(CHAT_BG_COLOR);
        chatArea.setForeground(TEXT_COLOR);
        chatArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(null);
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        // --- BOTTOM PANEL: BUTTONS & INPUT ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BG_COLOR);

        JPanel quickOptionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        quickOptionsPanel.setBackground(BG_COLOR);

        // 1. Keep only 3 main buttons (Removed separate Demo button)
        JButton btnRevenue = createStyledButton("Revenue & Forecast", new Color(46, 204, 113));
        JButton btnTopProduct = createStyledButton("Top Products", new Color(230, 126, 34));
        JButton btnTopUser = createStyledButton("Top User", new Color(52, 152, 219));

        // 2. Assign events
        btnRevenue.addActionListener(e -> handleQuickAction("REVENUE"));
        btnTopProduct.addActionListener(e -> handleQuickAction("TOP_PRODUCT"));
        btnTopUser.addActionListener(e -> handleQuickAction("TOP_USER"));

        // 3. Add buttons to Panel
        quickOptionsPanel.add(btnRevenue);
        quickOptionsPanel.add(btnTopProduct);
        quickOptionsPanel.add(btnTopUser);

        // --- CHAT INPUT FRAME ---
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        inputPanel.setBackground(BG_COLOR);

        txtInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JButton btnSend = new JButton("Send");
        btnSend.setBackground(PRIMARY_COLOR);
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 12));

        txtInput.addActionListener(e -> processQuery());
        btnSend.addActionListener(e -> processQuery());

        inputPanel.add(txtInput, BorderLayout.CENTER);
        inputPanel.add(btnSend, BorderLayout.EAST);

        bottomPanel.add(quickOptionsPanel, BorderLayout.NORTH);
        bottomPanel.add(inputPanel, BorderLayout.SOUTH);

        chatPanel.add(bottomPanel, BorderLayout.SOUTH);

        // --- RIGHT PANEL: CHARTS ---
        chartContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), "Analytics Charts"));
        chartContainer.setBackground(Color.WHITE);
        JLabel lblPlaceholder = new JLabel("Select an option from the left to display data", SwingConstants.CENTER);
        lblPlaceholder.setForeground(Color.GRAY);
        chartContainer.add(lblPlaceholder, BorderLayout.CENTER);

        splitPane.setLeftComponent(chatPanel);
        splitPane.setRightComponent(chartContainer);

        add(splitPane, BorderLayout.CENTER);

        appendChat("Jarvis: Hello Admin. How can I help you today?");
    }

    private void handleQuickAction(String actionType) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    if (actionType.equals("REVENUE")) {
                        // --- IMPORTANT MODIFICATION HERE ---
                        // Before fetching data, call DemoService to INJECT DEMO DATA first
                        appendChat("Jarvis: Analyzing data and running forecast algorithm...");
                        demoService.generateSmartDemoData();

                        // Then fetch data (Data is now complete and ready)
                        DefaultCategoryDataset dataset = analyticsService.getRevenuePredictionData();

                        appendChat("Jarvis: Here is the actual revenue and AI forecast chart for this week.");
                        updateChart("Revenue Chart (Next 7 Days)", "Date", "VND", dataset, "LINE");

                    } else if (actionType.equals("TOP_PRODUCT")) {
                        // Top Món ăn
                        DefaultCategoryDataset dataset = analyticsService.getTopProductsData();
                        appendChat("Jarvis: Here are the best-selling products today.");
                        updateChart("Top Selling Products", "Product", "Quantity", dataset, "BAR");

                    } else if (actionType.equals("TOP_USER")) {
                        // Top User
                        DefaultCategoryDataset dataset = analyticsService.getTopUsersData();
                        int newUserCount = analyticsService.getNewUserCountToday();

                        appendChat("Jarvis: There are " + newUserCount + " new customer accounts created today.");
                        appendChat("Jarvis: Displaying top customers by total top-up amount.");
                        updateChart("Top Customers (Top-up)", "Customer", "Total Top-up (VND)", dataset, "BAR");
                    }
                } catch (Exception e) {
                    appendChat("Jarvis: Error fetching data - " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
        };
        worker.execute();
    }

    private void processQuery() {
        String query = txtInput.getText().trim();
        if (query.isEmpty())
            return;

        appendChat("Admin: " + query);
        txtInput.setText("");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return aiService.getResponse(query);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    if (response != null)
                        appendChat("Jarvis: " + response);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void updateChart(String title, String categoryAxisLabel, String valueAxisLabel,
            DefaultCategoryDataset dataset, String type) {
        SwingUtilities.invokeLater(() -> {
            JFreeChart chart;

            if ("BAR".equals(type)) {
                chart = ChartFactory.createBarChart(
                        title, categoryAxisLabel, valueAxisLabel,
                        dataset, PlotOrientation.VERTICAL, true, true, false);

                CategoryPlot plot = chart.getCategoryPlot();
                plot.setBackgroundPaint(Color.WHITE);
                plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
                BarRenderer renderer = (BarRenderer) plot.getRenderer();
                renderer.setBarPainter(new StandardBarPainter());
                renderer.setSeriesPaint(0, new Color(52, 152, 219));
            } else {
                chart = ChartFactory.createLineChart(
                        title, categoryAxisLabel, valueAxisLabel,
                        dataset, PlotOrientation.VERTICAL, true, true, false);

                CategoryPlot plot = chart.getCategoryPlot();
                plot.setBackgroundPaint(Color.WHITE);
                plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

                // Actual line (Dark blue)
                plot.getRenderer().setSeriesPaint(0, new Color(41, 128, 185));
                plot.getRenderer().setSeriesStroke(0, new BasicStroke(2.5f));

                // Forecast line (Orange, dashed)
                plot.getRenderer().setSeriesPaint(1, new Color(230, 126, 34));
                plot.getRenderer().setSeriesStroke(1, new BasicStroke(2.5f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND, 1.0f, new float[] { 5, 3 }, 0));

                org.jfree.chart.axis.CategoryAxis domainAxis = plot.getDomainAxis();
                domainAxis.setCategoryLabelPositions(org.jfree.chart.axis.CategoryLabelPositions.UP_45);
            }

            ChartPanel chartPanel = new ChartPanel(chart);
            chartContainer.removeAll();
            chartContainer.add(chartPanel, BorderLayout.CENTER);
            chartContainer.revalidate();
            chartContainer.repaint();
        });
    }

    private void appendChat(String text) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(text + "\n\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}