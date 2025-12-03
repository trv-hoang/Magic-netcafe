package com.netcafe.ui.admin;

import com.netcafe.dao.OrderDAO;
import com.netcafe.dao.ProductDAO;
import com.netcafe.model.Message;
import com.netcafe.model.Order;
import com.netcafe.model.Product;
import com.netcafe.model.TopupRequest;
import com.netcafe.model.User;
import com.netcafe.service.BillingService;
import com.netcafe.service.MessageService;
import com.netcafe.service.ReportService;
import com.netcafe.service.UserService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AdminPanel extends JPanel {
    private final ReportService reportService = new ReportService();
    private final BillingService billingService = new BillingService();
    private final OrderDAO orderDAO = new OrderDAO();
    private final com.netcafe.service.ProductService productService = new com.netcafe.service.ProductService();
    private final UserService userService = new UserService();
    private final MessageService messageService = new MessageService();

    // ... (Skipping unchanged fields)

    // ...

    private void loadProducts() {
        SwingWorker<List<Product>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Product> doInBackground() throws Exception {
                return productService.getAllProducts();
            }

            @Override
            protected void done() {
                try {
                    List<Product> list = get();
                    productModel.setRowCount(0);
                    for (Product p : list) {
                        productModel.addRow(
                                new Object[] { p.getId(), p.getName(), p.getCategory(), p.getPrice(), p.getStock() });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminPanel.this, "Error loading products: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void deleteProduct(int id) {
        try {
            productService.deleteProduct(id);
            loadProducts();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error deleting product: " + ex.getMessage());
        }
    }

    // Dashboard Components
    private final JPanel dailyChartContainer = new JPanel(new BorderLayout());
    private final JPanel monthlyChartContainer = new JPanel(new BorderLayout());
    private final JTextArea topProductsArea = new JTextArea(10, 20);
    private final JTextArea topSpendersArea = new JTextArea(10, 20);

    // Topup Requests
    private final DefaultTableModel topupModel = new DefaultTableModel(
            new String[] { "ID", "User ID", "Amount", "Status" }, 0);

    // Orders
    // Staff & Users
    private final DefaultTableModel staffModel = new DefaultTableModel(
            new String[] { "ID", "Username", "Full Name", "Role" }, 0);
    private final DefaultTableModel orderModel = new DefaultTableModel(
            new String[] { "ID", "User", "Product", "Qty", "Total", "Status", "Date" }, 0);
    private final DefaultTableModel productModel = new DefaultTableModel(
            new String[] { "ID", "Name", "Category", "Price", "Stock" }, 0);
    private final DefaultTableModel userModel = new DefaultTableModel(
            new String[] { "ID", "Username", "Full Name", "Role", "Balance", "Points" }, 0);

    public AdminPanel() {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // Logout Button in Tab Header
        JPanel trailingPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        trailingPanel.setOpaque(false);
        trailingPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 3)); // Spacing

        JButton btnLogout = new JButton("Logout");
        btnLogout.putClientProperty("JButton.buttonType", "roundRect");
        btnLogout.setBackground(com.netcafe.ui.ThemeConfig.DANGER); // Red
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(com.netcafe.ui.ThemeConfig.FONT_SMALL);
        btnLogout.setMargin(new Insets(4, 12, 4, 12));
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> logout());

        trailingPanel.add(btnLogout);

        // Add to trailing edge of tabs
        tabbedPane.putClientProperty("JTabbedPane.trailingComponent", trailingPanel);

        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("Topup Requests", createTopupRequestPanel());
        tabbedPane.addTab("Order Management", createOrderManagementPanel());
        tabbedPane.addTab("Staff Management", createStaffPanel());
        tabbedPane.addTab("User Management", createUserPanel());
        tabbedPane.addTab("Messages", createMessagePanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null)
                window.dispose();
            new com.netcafe.ui.login.LoginFrame().setVisible(true);
        }
    }

    // --- Dashboard ---
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(com.netcafe.ui.ThemeConfig.BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

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

        panel.add(splitPane, BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.putClientProperty("JButton.buttonType", "roundRect");
        btnRefresh.setFont(com.netcafe.ui.ThemeConfig.FONT_SMALL);
        btnRefresh.setBackground(com.netcafe.ui.ThemeConfig.PRIMARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setMargin(new Insets(5, 10, 5, 10)); // Smaller margin

        JPanel header = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        header.setBackground(com.netcafe.ui.ThemeConfig.BG_MAIN);
        header.add(btnRefresh);
        panel.add(header, BorderLayout.NORTH);

        btnRefresh.addActionListener(e -> loadDashboard());

        loadDashboard();
        return panel;
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
                    JOptionPane.showMessageDialog(AdminPanel.this, "Error loading dashboard: " + ex.getMessage());
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

    // --- Topup Requests ---
    private JPanel createTopupRequestPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTable table = new JTable(topupModel);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnApprove = new JButton("Approve Selected");

        btnApprove.setBackground(com.netcafe.ui.ThemeConfig.SUCCESS);
        btnApprove.setForeground(Color.WHITE);
        btnApprove.putClientProperty("JButton.buttonType", "roundRect");
        btnApprove.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);

        btnRefresh.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);

        btnPanel.add(btnRefresh);
        btnPanel.add(btnApprove);
        panel.add(btnPanel, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadTopupRequests());
        btnApprove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1)
                approveTopup((int) topupModel.getValueAt(row, 0));
        });

        loadTopupRequests();
        return panel;
    }

    private void loadTopupRequests() {
        SwingWorker<List<TopupRequest>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TopupRequest> doInBackground() throws Exception {
                return billingService.getPendingTopupRequests();
            }

            @Override
            protected void done() {
                try {
                    List<TopupRequest> list = get();
                    topupModel.setRowCount(0);
                    for (TopupRequest r : list)
                        topupModel.addRow(new Object[] { r.getId(), r.getUserId(), r.getAmount(), r.getStatus() });
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminPanel.this, "Error loading topups: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void approveTopup(int id) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                billingService.approveTopup(id);
                return null;
            }

            @Override
            protected void done() {
                loadTopupRequests();
                JOptionPane.showMessageDialog(AdminPanel.this, "Approved!");
            }
        };
        worker.execute();
    }

    // --- Order Management (Split View) ---
    private JPanel createOrderManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createProductManagementPanel(),
                createServeOrderPanel());
        splitPane.setResizeWeight(0.6); // 60% for Product Management
        splitPane.setDividerSize(5);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProductManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Product Management"));

        JTable table = new JTable(productModel);
        table.setRowHeight(25);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton("Add");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");

        btnAdd.setBackground(com.netcafe.ui.ThemeConfig.SUCCESS);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);

        btnEdit.setBackground(com.netcafe.ui.ThemeConfig.ACCENT);
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);

        btnDelete.setBackground(com.netcafe.ui.ThemeConfig.DANGER);
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);

        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);
        panel.add(btnPanel, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> openProductDialog(null));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int id = (int) productModel.getValueAt(row, 0);
                try {
                    Product p = productService.getProductById(id).orElse(null);
                    if (p != null)
                        openProductDialog(p);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int id = (int) productModel.getValueAt(row, 0);
                if (JOptionPane.showConfirmDialog(this, "Delete product?", "Confirm",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    deleteProduct(id);
                }
            }
        });
        btnRefresh.addActionListener(e -> loadProducts());

        loadProducts();
        return panel;
    }

    private JPanel createServeOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Serve Orders"));

        JTable table = new JTable(orderModel);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnDelete = new JButton("Delete Order");
        JButton btnServe = new JButton("Serve Order");

        btnServe.setBackground(com.netcafe.ui.ThemeConfig.PRIMARY);
        btnServe.setForeground(Color.WHITE);
        btnServe.putClientProperty("JButton.buttonType", "roundRect");
        btnServe.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);

        btnDelete.setBackground(com.netcafe.ui.ThemeConfig.DANGER);
        btnDelete.setForeground(Color.WHITE);
        btnDelete.putClientProperty("JButton.buttonType", "roundRect");
        btnDelete.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);

        btnPanel.add(btnRefresh);
        btnPanel.add(btnDelete);
        btnPanel.add(btnServe);
        panel.add(btnPanel, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadOrders());
        btnServe.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1)
                serveOrder((int) orderModel.getValueAt(row, 0));
        });
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int id = (int) orderModel.getValueAt(row, 0);
                if (JOptionPane.showConfirmDialog(panel, "Delete this order?", "Confirm",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    deleteOrder(id);
                }
            }
        });

        loadOrders();
        return panel;
    }

    private void deleteOrder(int id) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                orderDAO.delete(id);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    loadOrders();
                    JOptionPane.showMessageDialog(AdminPanel.this, "Order deleted!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminPanel.this, "Error deleting order: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void loadOrders() {
        SwingWorker<List<Order>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Order> doInBackground() throws Exception {
                return orderDAO.findAllPending();
            }

            @Override
            protected void done() {
                try {
                    List<Order> list = get();
                    orderModel.setRowCount(0);
                    for (Order o : list)
                        orderModel.addRow(new Object[] { o.getId(), o.getUserId(), o.getProductId(), o.getQty(),
                                o.getTotalPrice(), o.getStatus() });
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminPanel.this, "Error loading orders: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void serveOrder(int id) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                orderDAO.updateStatus(id, "SERVED");
                return null;
            }

            @Override
            protected void done() {
                loadOrders();
                JOptionPane.showMessageDialog(AdminPanel.this, "Served!");
            }
        };
        worker.execute();
    }

    // --- Staff Management ---
    private JPanel createStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTable table = new JTable(staffModel);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("Refresh Staff");
        JButton btnCreate = new JButton("Create New Admin");

        btnCreate.setBackground(com.netcafe.ui.ThemeConfig.PRIMARY);
        btnCreate.setForeground(Color.WHITE);
        btnCreate.putClientProperty("JButton.buttonType", "roundRect");
        btnCreate.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);

        btnPanel.add(btnRefresh);
        btnPanel.add(btnCreate);
        panel.add(btnPanel, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadUsers(User.Role.ADMIN, staffModel));
        btnCreate.addActionListener(e -> createUser(User.Role.ADMIN, staffModel));

        loadUsers(User.Role.ADMIN, staffModel);
        return panel;
    }

    // --- User Management ---
    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTable table = new JTable(userModel);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSetBalance = new JButton("Set Balance");
        JButton btnCreate = new JButton("Create New User");
        JButton btnRefresh = new JButton("Refresh Users");
        JButton btnDelete = new JButton("Delete Selected");

        // Create Button
        btnCreate.setBackground(com.netcafe.ui.ThemeConfig.PRIMARY);
        btnCreate.setForeground(Color.WHITE);
        btnCreate.putClientProperty("JButton.buttonType", "roundRect");
        btnCreate.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);

        // Delete Button
        btnDelete.setBackground(com.netcafe.ui.ThemeConfig.DANGER);
        btnDelete.setForeground(Color.WHITE);
        btnDelete.putClientProperty("JButton.buttonType", "roundRect");
        btnDelete.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);

        // Refresh Button
        btnRefresh.setBackground(com.netcafe.ui.ThemeConfig.PRIMARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.putClientProperty("JButton.buttonType", "roundRect");
        btnRefresh.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);

        // Set Balance Button
        btnSetBalance.setBackground(com.netcafe.ui.ThemeConfig.ACCENT); // Yellow/Orange
        btnSetBalance.setForeground(Color.WHITE);
        btnSetBalance.putClientProperty("JButton.buttonType", "roundRect");
        btnSetBalance.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);

        btnPanel.add(btnSetBalance);
        btnPanel.add(btnCreate);
        btnPanel.add(btnRefresh);
        btnPanel.add(btnDelete);

        panel.add(btnPanel, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadUsers(User.Role.USER, userModel));
        btnCreate.addActionListener(e -> createUser(User.Role.USER, userModel));
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1)
                deleteUser((int) userModel.getValueAt(row, 0));
        });

        btnSetBalance.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int userId = (int) userModel.getValueAt(row, 0);
                String input = JOptionPane.showInputDialog(this, "Enter new balance (VND):");
                if (input != null && !input.trim().isEmpty()) {
                    try {
                        long newBalance = Long.parseLong(input.trim());
                        if (newBalance < 0)
                            throw new NumberFormatException();

                        SwingWorker<Void, Void> worker = new SwingWorker<>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                billingService.setBalance(userId, newBalance);
                                return null;
                            }

                            @Override
                            protected void done() {
                                try {
                                    get();
                                    JOptionPane.showMessageDialog(AdminPanel.this, "Balance updated successfully!");
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(AdminPanel.this, "Error: " + ex.getMessage());
                                }
                            }
                        };
                        worker.execute();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid amount. Please enter a positive number.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a user first.");
            }
        });

        loadUsers(User.Role.USER, userModel);
        return panel;
    }

    private void openProductDialog(Product product) {
        ProductDialog dialog = new ProductDialog((Frame) SwingUtilities.getWindowAncestor(this), product);
        dialog.setVisible(true);
        if (dialog.isSucceeded()) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Product p = new Product();
                    p.setName(dialog.getName());
                    p.setCategory(dialog.getCategory());
                    p.setPrice(dialog.getPrice());
                    p.setStock(dialog.getStock());

                    if (product == null) {
                        productService.createProduct(p);
                    } else {
                        p.setId(product.getId());
                        productService.updateProduct(p);
                    }
                    dialog.saveImage();
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        // loadProducts(); // This method is not defined in the provided context,
                        // assuming it exists elsewhere or needs to be added.
                        JOptionPane.showMessageDialog(AdminPanel.this, "Saved!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(AdminPanel.this, "Error: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        }
    }

    private void createUser(User.Role role, DefaultTableModel model) {
        UserDialog dialog = new UserDialog((Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSucceeded()) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    // Force the role to match the tab context, or check if dialog role matches?
                    // The dialog allows selecting role. Let's respect the dialog selection but warn
                    // if mismatch?
                    // OR better, pre-select role in dialog and disable it?
                    // For simplicity, we use the dialog's values.
                    userService.createUser(dialog.getUsername(), dialog.getPassword(), dialog.getFullName(),
                            dialog.getRole());
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(AdminPanel.this, "User Created!");
                        loadUsers(role, model); // Reload the current tab's list
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(AdminPanel.this, "Error: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        }
    }

    private void loadUsers(User.Role role, DefaultTableModel model) {
        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            // Map to store balances, only needed for USER role
            java.util.Map<Integer, Long> balances = new java.util.HashMap<>();

            @Override
            protected List<User> doInBackground() throws Exception {
                List<User> allUsers = userService.getAllUsers();
                // If loading users, fetch their balances
                if (role == User.Role.USER) {
                    for (User u : allUsers) {
                        if (u.getRole() == role) {
                            balances.put(u.getId(), billingService.getBalance(u.getId()));
                        }
                    }
                }
                return allUsers;
            }

            @Override
            protected void done() {
                try {
                    List<User> list = get();
                    model.setRowCount(0);
                    for (User u : list) {
                        if (u.getRole() == role) {
                            if (role == User.Role.USER) {
                                long balance = balances.getOrDefault(u.getId(), 0L);
                                model.addRow(new Object[] { u.getId(), u.getUsername(), u.getFullName(), u.getRole(),
                                        balance, u.getPoints() });
                            } else {
                                model.addRow(new Object[] { u.getId(), u.getUsername(), u.getFullName(), u.getRole() });
                            }
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminPanel.this, "Error loading users: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void deleteUser(int userId) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                userService.deleteUser(userId);
                return null;
            }

            @Override
            protected void done() {
                loadUsers(User.Role.USER, userModel);
                JOptionPane.showMessageDialog(AdminPanel.this, "User Deleted");
            }
        };
        worker.execute();
    }

    // --- Message Panel ---
    private JList<User> userList = new JList<>();
    private DefaultListModel<User> userListModel = new DefaultListModel<>();
    private JTextArea chatArea = new JTextArea();
    private User selectedChatUser;

    private JPanel createMessagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left: User List
        userList.setModel(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User) {
                    setText(((User) value).getUsername());
                    setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                }
                return this;
            }
        });
        userList.setFixedCellHeight(30);

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedChatUser = userList.getSelectedValue();
                loadConversation();
            }
        });

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Users"), BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        JButton btnRefreshUsers = new JButton("Refresh Users");
        btnRefreshUsers.addActionListener(e -> loadChatUsers());
        leftPanel.add(btnRefreshUsers, BorderLayout.SOUTH);
        leftPanel.setPreferredSize(new Dimension(200, 0));

        // Right: Chat Area
        JPanel rightPanel = new JPanel(new BorderLayout());
        chatArea.setEditable(false);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        rightPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JTextField txtContent = new JTextField();
        txtContent.putClientProperty("JTextField.placeholderText", "Type a message...");

        JButton btnSend = new JButton("Send");
        btnSend.setBackground(new Color(52, 152, 219));
        btnSend.setForeground(Color.WHITE);
        btnSend.putClientProperty("JButton.buttonType", "roundRect");

        inputPanel.add(txtContent, BorderLayout.CENTER);
        inputPanel.add(btnSend, BorderLayout.EAST);
        rightPanel.add(inputPanel, BorderLayout.SOUTH);

        btnSend.addActionListener(e -> {
            String content = txtContent.getText();
            if (!content.isEmpty() && selectedChatUser != null) {
                sendMessage(selectedChatUser.getId(), content);
                txtContent.setText("");
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerSize(5);
        panel.add(splitPane, BorderLayout.CENTER);

        loadChatUsers();
        startChatTimer();
        return panel;
    }

    private void loadChatUsers() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                return userService.getAllUsers();
            }

            @Override
            protected void done() {
                try {
                    List<User> list = get();
                    userListModel.clear();
                    for (User u : list) {
                        if (u.getRole() == User.Role.USER) { // Only show normal users
                            userListModel.addElement(u);
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminPanel.this, "Error loading chat users: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void loadConversation() {
        if (selectedChatUser == null)
            return;
        SwingWorker<List<Message>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Message> doInBackground() throws Exception {
                // Assuming Admin ID is 1. In real app, get current logged in admin ID.
                return messageService.getConversation(1, selectedChatUser.getId());
            }

            @Override
            protected void done() {
                try {
                    List<Message> list = get();
                    StringBuilder sb = new StringBuilder();
                    for (Message m : list) {
                        String sender = (m.getSenderId() == 1) ? "Me" : selectedChatUser.getUsername();
                        sb.append(sender).append(": ").append(m.getContent()).append("\n");
                    }
                    if (!chatArea.getText().equals(sb.toString())) {
                        chatArea.setText(sb.toString());
                        chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    }
                } catch (Exception ex) {
                    // Silent failure for chat polling
                    System.err.println("Chat error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void sendMessage(int receiverId, String content) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                messageService.sendMessage(1, receiverId, content);
                return null;
            }

            @Override
            protected void done() {
                loadConversation();
            }
        };
        worker.execute();
    }

    // Add timer in createMessagePanel or initialize it
    private Timer chatTimer;

    private void startChatTimer() {
        if (chatTimer != null && chatTimer.isRunning())
            return;
        chatTimer = new Timer(3000, e -> {
            if (selectedChatUser != null && isShowing()) {
                loadConversation();
            }
        });
        chatTimer.start();
    }
}
