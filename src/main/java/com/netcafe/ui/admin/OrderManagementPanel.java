package com.netcafe.ui.admin;

import com.netcafe.dao.OrderDAO;
import com.netcafe.model.Order;
import com.netcafe.model.Product;
import com.netcafe.service.ProductService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderManagementPanel extends JPanel {
    private final ProductService productService = new ProductService();
    private final OrderDAO orderDAO = new OrderDAO();

    private final DefaultTableModel productModel = new DefaultTableModel(
            new String[] { "ID", "Name", "Category", "Price", "Stock" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel orderModel = new DefaultTableModel(
            new String[] { "ID", "User", "Product", "Qty", "Total", "Status", "Date" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public OrderManagementPanel() {
        setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createProductManagementPanel(),
                createServeOrderPanel());
        splitPane.setResizeWeight(0.6); // 60% for Product Management
        splitPane.setDividerSize(5);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createProductManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Product Management"));

        JTable table = new JTable(productModel);
        table.setRowHeight(25);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
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
        // ➤➤➤ CĂN GIỮA CÁC CỘT MONG MUỐN
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // ID
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); // User
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Product
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Qty
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Total
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); // Status
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer); // Date
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

        // Auto-refresh orders every 5 seconds
        javax.swing.Timer refreshTimer = new javax.swing.Timer(5000, e -> loadOrders());
        refreshTimer.start();

        return panel;
    }

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
                    JOptionPane.showMessageDialog(OrderManagementPanel.this,
                            "Error loading products: " + ex.getMessage());
                }
            }
        };
        worker.execute();
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
                        loadProducts();
                        com.netcafe.util.SwingUtils.showInfo(OrderManagementPanel.this, "Saved!");
                    } catch (Exception ex) {
                        com.netcafe.util.SwingUtils.showError(OrderManagementPanel.this, "Error loading products", ex);
                    }
                }
            };
            worker.execute();
        }
    }

    private void deleteProduct(int id) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                productService.deleteProduct(id);
                return null;
            }

            @Override
            protected void done() {
                loadProducts();
                com.netcafe.util.SwingUtils.showInfo(OrderManagementPanel.this, "Deleted!");
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
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    for (Order o : list) {
                        String productName = productService
                                .getProductById(o.getProductId())
                                .map(Product::getName)
                                .orElse("Unknown");
                        String formattedDate = "";
                        if (o.getCreatedAt() != null) {
                            formattedDate = o.getCreatedAt().format(formatter);
                        }
                        orderModel.addRow(new Object[] {
                                o.getId(),
                                o.getUserId(),
                                productName,
                                o.getQty(),
                                o.getTotalPrice(),
                                o.getStatus(),
                                formattedDate
                        });
                    }

                } catch (Exception ex) {
                    com.netcafe.util.SwingUtils.showError(OrderManagementPanel.this, "Error loading orders", ex);
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
                com.netcafe.util.SwingUtils.showInfo(OrderManagementPanel.this, "Served!");
            }
        };
        worker.execute();
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
                    com.netcafe.util.SwingUtils.showInfo(OrderManagementPanel.this, "Order deleted!");
                } catch (Exception ex) {
                    com.netcafe.util.SwingUtils.showError(OrderManagementPanel.this, "Error deleting order", ex);
                }
            }
        };
        worker.execute();
    }
}
