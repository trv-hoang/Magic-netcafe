package com.netcafe.ui.admin;

import com.netcafe.service.AIService;
import com.netcafe.service.AnalyticsService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;

public class AIAnalystPanel extends JPanel {

    private final AIService aiService = new AIService();
    private final AnalyticsService analyticsService = new AnalyticsService();

    private final JTextArea chatArea = new JTextArea();
    private final JPanel chartContainer = new JPanel(new BorderLayout());
    private final JTextField txtInput = new JTextField();

    public AIAnalystPanel() {
        setLayout(new BorderLayout());

        // Split Pane: Chat (Left) vs Chart (Right)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.4); // 40% Chat, 60% Chart

        // 1. Chat Panel
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createTitledBorder("Jarvis Chat"));

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        chatArea.setBackground(new Color(30, 30, 30));
        chatArea.setForeground(Color.GREEN);
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        txtInput.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtInput.addActionListener(e -> processQuery());
        JButton btnSend = new JButton("Ask");
        btnSend.addActionListener(e -> processQuery());

        inputPanel.add(txtInput, BorderLayout.CENTER);
        inputPanel.add(btnSend, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        // 2. Chart Panel
        chartContainer.setBorder(BorderFactory.createTitledBorder("Visual Analysis"));
        chartContainer.setBackground(Color.WHITE);
        chartContainer.add(new JLabel("Ask Jarvis to visualize data...", SwingConstants.CENTER), BorderLayout.CENTER);

        splitPane.setLeftComponent(chatPanel);
        splitPane.setRightComponent(chartContainer);

        add(splitPane, BorderLayout.CENTER);

        appendChat("Jarvis: Online. Ready to analyze business data.");
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
                // Check for specific commands
                if (query.matches("(?i).*(predict|forecast|future).*")) {
                    // Trigger Chart
                    DefaultCategoryDataset dataset = analyticsService.getRevenuePredictionData();
                    updateChart(dataset);
                    return "I have generated a revenue forecast for the next 7 days based on linear regression analysis.";
                } else if (query.matches("(?i).*(health|business|status|report).*")) {
                    return analyticsService.getBusinessHealthReport();
                } else if (query.matches("(?i).*(occupancy|active|users|full).*")) {
                    return analyticsService.getOccupancyReport();
                } else if (query.matches("(?i).*(maintenance|broken|repair|fix).*")) {
                    return analyticsService.getMaintenanceReport();
                } else if (query.matches("(?i).*(top|best|selling|popular|product|food).*")) {
                    return analyticsService.getTopProductsReport();
                } else {
                    // Fallback to general AI
                    String response = aiService.getResponse(query);
                    return response != null ? response
                            : "I am trained for business analytics. Try asking 'Predict revenue', 'Occupancy', or 'Best sellers'.";
                }
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    appendChat("Jarvis: " + response);
                } catch (Exception ex) {
                    appendChat("Jarvis: Error processing query. " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void updateChart(DefaultCategoryDataset dataset) {
        SwingUtilities.invokeLater(() -> {
            JFreeChart lineChart = ChartFactory.createLineChart(
                    "Revenue Forecast",
                    "Date", "Revenue (VND)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true, true, false);

            ChartPanel chartPanel = new ChartPanel(lineChart);
            chartContainer.removeAll();
            chartContainer.add(chartPanel, BorderLayout.CENTER);
            chartContainer.revalidate();
            chartContainer.repaint();
        });
    }

    private void appendChat(String text) {
        chatArea.append(text + "\n\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}
