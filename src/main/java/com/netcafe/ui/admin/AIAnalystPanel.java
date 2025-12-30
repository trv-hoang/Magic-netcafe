package com.netcafe.ui.admin;

import com.netcafe.service.AIService;
import com.netcafe.service.AnalyticsService;
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

    private final JTextArea chatArea = new JTextArea();
    private final JPanel chartContainer = new JPanel(new BorderLayout());
    private final JTextField txtInput = new JTextField();

    // Màu sắc giao diện hiện đại
    private final Color BG_COLOR = new Color(245, 247, 250); // Xám nhạt
    private final Color CHAT_BG_COLOR = Color.WHITE;
    private final Color TEXT_COLOR = new Color(50, 50, 50);
    private final Color PRIMARY_COLOR = new Color(0, 120, 215); // Xanh dương

    public AIAnalystPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        // Split Pane: Chat (Left) vs Chart (Right)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.35); // 35% Chat, 65% Chart
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);

        // --- 1. Chat Panel Setup ---
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), "Trợ lý ảo Jarvis"));
        chatPanel.setBackground(BG_COLOR);

        // Cấu hình vùng hiển thị Chat (Style mới)
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setBackground(CHAT_BG_COLOR);
        chatArea.setForeground(TEXT_COLOR);
        chatArea.setMargin(new Insets(10, 10, 10, 10)); // Padding text
        
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(null);
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel chứa Input và các nút Quick Option
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BG_COLOR);

        // A. Quick Options (3 nút chức năng bạn yêu cầu)
        JPanel quickOptionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        quickOptionsPanel.setBackground(BG_COLOR);
        
        JButton btnRevenue = createStyledButton("💰 Doanh thu", new Color(46, 204, 113));
        JButton btnTopProduct = createStyledButton("🍔 Top Món", new Color(230, 126, 34));
        JButton btnTopUser = createStyledButton("🏆 Top User", new Color(52, 152, 219));

        // Sự kiện cho các nút
        btnRevenue.addActionListener(e -> handleQuickAction("REVENUE"));
        btnTopProduct.addActionListener(e -> handleQuickAction("TOP_PRODUCT"));
        btnTopUser.addActionListener(e -> handleQuickAction("TOP_USER"));

        quickOptionsPanel.add(btnRevenue);
        quickOptionsPanel.add(btnTopProduct);
        quickOptionsPanel.add(btnTopUser);

        // B. Input Area
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        inputPanel.setBackground(BG_COLOR);

        txtInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        JButton btnSend = new JButton("Gửi");
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

        // --- 2. Chart Panel Setup ---
        chartContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), "Biểu đồ phân tích"));
        chartContainer.setBackground(Color.WHITE);
        JLabel lblPlaceholder = new JLabel("Chọn một mục bên trái để hiển thị dữ liệu", SwingConstants.CENTER);
        lblPlaceholder.setForeground(Color.GRAY);
        chartContainer.add(lblPlaceholder, BorderLayout.CENTER);

        splitPane.setLeftComponent(chatPanel);
        splitPane.setRightComponent(chartContainer);

        add(splitPane, BorderLayout.CENTER);

        appendChat("Jarvis: Xin chào Admin. Tôi có thể giúp gì cho bạn hôm nay?");
    }

    // --- Xử lý các nút bấm nhanh (Quick Actions) ---
    private void handleQuickAction(String actionType) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    if (actionType.equals("REVENUE")) {
                        // 1. Doanh thu (Line Chart)
                        DefaultCategoryDataset dataset = analyticsService.getRevenuePredictionData();
                        appendChat("Jarvis: Đang hiển thị biểu đồ doanh thu và dự báo...");
                        updateChart("Biểu đồ Doanh thu (7 ngày tới)", "Ngày", "VND", dataset, "LINE");
                    
                    } else if (actionType.equals("TOP_PRODUCT")) {
                        // 2. Top Món ăn (Bar Chart)
                        DefaultCategoryDataset dataset = analyticsService.getTopProductsData(); // Bạn cần thêm hàm này ở Service
                        appendChat("Jarvis: Đây là các món ăn bán chạy nhất trong ngày.");
                        updateChart("Top Món Ăn Bán Chạy", "Món", "Số lượng", dataset, "BAR");
                    
                    } else if (actionType.equals("TOP_USER")) {
                        // 3. Top User & Số lượng User mới
                        DefaultCategoryDataset dataset = analyticsService.getTopUsersData(); // Bạn cần thêm hàm này ở Service
                        int newUserCount = analyticsService.getNewUserCountToday(); // Bạn cần thêm hàm này ở Service
                        
                        appendChat("Jarvis: Có " + newUserCount + " tài khoản khách hàng mới được tạo hôm nay.");
                        appendChat("Jarvis: Đang hiển thị Top khách hàng nạp tiền nhiều nhất.");
                        updateChart("Top Khách Hàng (Nạp tiền)", "Khách hàng", "Tổng nạp (VND)", dataset, "BAR");
                    }
                } catch (Exception e) {
                    appendChat("Jarvis: Lỗi khi lấy dữ liệu - " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
        };
        worker.execute();
    }

    // --- Xử lý chat text thông thường ---
    private void processQuery() {
        String query = txtInput.getText().trim();
        if (query.isEmpty()) return;

        appendChat("Admin: " + query);
        txtInput.setText("");

        // Logic cũ của bạn (có thể giữ lại hoặc map các từ khóa vào handleQuickAction)
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                // ... (Logic regex cũ của bạn giữ nguyên ở đây nếu muốn) ...
                // Demo fallback:
                return aiService.getResponse(query);
            }
            @Override
            protected void done() {
                try {
                     String response = get();
                     if(response != null) appendChat("Jarvis: " + response);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    // --- Hàm cập nhật biểu đồ đa năng (Line hoặc Bar) ---
    private void updateChart(String title, String categoryAxisLabel, String valueAxisLabel, 
                             DefaultCategoryDataset dataset, String type) {
        SwingUtilities.invokeLater(() -> {
            JFreeChart chart;

            if ("BAR".equals(type)) {
                // Tạo Bar Chart cho Top User / Top Món
                chart = ChartFactory.createBarChart(
                        title, categoryAxisLabel, valueAxisLabel,
                        dataset, PlotOrientation.VERTICAL, true, true, false);
                
                // Tùy chỉnh Bar Chart cho đẹp (Flat Design)
                CategoryPlot plot = chart.getCategoryPlot();
                plot.setBackgroundPaint(Color.WHITE);
                plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
                BarRenderer renderer = (BarRenderer) plot.getRenderer();
                renderer.setBarPainter(new StandardBarPainter()); // Bỏ hiệu ứng bóng 3D
                renderer.setSeriesPaint(0, new Color(52, 152, 219)); // Màu cột xanh đẹp

            } else {
                // Mặc định là Line Chart cho Doanh thu
                chart = ChartFactory.createLineChart(
                        title, categoryAxisLabel, valueAxisLabel,
                        dataset, PlotOrientation.VERTICAL, true, true, false);
                
                CategoryPlot plot = chart.getCategoryPlot();
                plot.setBackgroundPaint(Color.WHITE);
                plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
               plot.getRenderer().setSeriesPaint(0, new Color(41, 128, 185)); // Xanh dương đậm (Thực tế)
    plot.getRenderer().setSeriesPaint(1, new Color(230, 126, 34)); // Cam (Dự báo)
                
    // (Tùy chọn) Làm nét đường, thêm marker
    plot.getRenderer().setSeriesStroke(0, new BasicStroke(2.5f));
    plot.getRenderer().setSeriesStroke(1, new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{5, 3}, 0));
    
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

    // Helper tạo nút đẹp
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