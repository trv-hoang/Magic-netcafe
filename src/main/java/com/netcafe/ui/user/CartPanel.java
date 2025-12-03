package com.netcafe.ui.user;

import com.netcafe.model.Product;
import com.netcafe.model.User;
import com.netcafe.service.BillingService;
import com.netcafe.util.SwingUtils;
import com.netcafe.ui.ThemeConfig;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CartPanel extends JPanel {
    private final User user;
    private final BillingService billingService = new BillingService();
    private final Runnable onCheckoutSuccess;

    private static class CartItem {
        Product product;
        int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }

    private final List<CartItem> cartItems = new ArrayList<>();
    private final DefaultTableModel cartModel = new DefaultTableModel(new String[] { "Name", "Price", "Qty" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JLabel lblCartTotal = new JLabel("Total: 0 VND");

    public CartPanel(User user, Runnable onCheckoutSuccess) {
        this.user = user;
        this.onCheckoutSuccess = onCheckoutSuccess;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(400, 0));
        setBackground(ThemeConfig.BG_PANEL);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        initUI();
    }

    private void initUI() {
        JLabel lblTitle = new JLabel("Your Cart");
        lblTitle.setFont(ThemeConfig.FONT_HEADER);
        lblTitle.setForeground(ThemeConfig.TEXT_PRIMARY);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(lblTitle, BorderLayout.NORTH);

        JTable table = new JTable(cartModel);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(50);
        table.getTableHeader().setFont(ThemeConfig.FONT_BODY_BOLD);
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.setSelectionBackground(new Color(240, 248, 255));
        table.setSelectionForeground(Color.BLACK);
        table.setFont(ThemeConfig.FONT_BODY);

        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setCellRenderer(new QuantityCellRenderer());

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());

                if (row >= 0 && col == 2) {
                    Rectangle cellRect = table.getCellRect(row, col, false);
                    int x = e.getX() - cellRect.x;
                    int width = cellRect.width;
                    int oneThird = width / 3;

                    if (x < oneThird) {
                        CartItem item = cartItems.get(row);
                        item.quantity--;
                        if (item.quantity <= 0) {
                            cartItems.remove(row);
                        }
                        refreshCartTable();
                    } else if (x >= 2 * oneThird) {
                        cartItems.get(row).quantity++;
                        refreshCartTable();
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 15));
        bottomPanel.setBackground(ThemeConfig.BG_PANEL);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        lblCartTotal.setFont(ThemeConfig.FONT_SUBHEADER);
        lblCartTotal.setForeground(ThemeConfig.TEXT_PRIMARY);
        lblCartTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        bottomPanel.add(lblCartTotal, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        btnPanel.setBackground(ThemeConfig.BG_PANEL);

        JButton btnClear = new JButton("Clear");
        JButton btnCheckout = new JButton("Checkout");

        btnClear.putClientProperty("JButton.buttonType", "roundRect");
        btnClear.setBackground(ThemeConfig.DANGER);
        btnClear.setForeground(Color.WHITE);
        btnClear.setFont(ThemeConfig.FONT_BODY_BOLD);

        btnCheckout.putClientProperty("JButton.buttonType", "roundRect");
        btnCheckout.setBackground(ThemeConfig.SUCCESS);
        btnCheckout.setForeground(Color.WHITE);
        btnCheckout.setFont(ThemeConfig.FONT_BODY_BOLD);

        btnPanel.add(btnClear);
        btnPanel.add(btnCheckout);
        bottomPanel.add(btnPanel, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        btnClear.addActionListener(e -> {
            cartItems.clear();
            refreshCartTable();
        });

        btnCheckout.addActionListener(e -> checkout());
    }

    public void addToCart(Product p) {
        boolean found = false;
        for (CartItem item : cartItems) {
            boolean sameId = (p.getId() != -1 && item.product.getId() == p.getId());
            boolean sameTopup = (p.getId() == -1 && item.product.getId() == -1
                    && item.product.getPrice() == p.getPrice());

            if (sameId || sameTopup) {
                item.quantity++;
                found = true;
                break;
            }
        }
        if (!found) {
            cartItems.add(new CartItem(p, 1));
        }
        refreshCartTable();
    }

    private void refreshCartTable() {
        cartModel.setRowCount(0);
        for (CartItem item : cartItems) {
            cartModel.addRow(new Object[] {
                    item.product.getName(),
                    item.product.getPrice(),
                    item.quantity
            });
        }
        updateCartTotal();
    }

    private void updateCartTotal() {
        long total = cartItems.stream().mapToLong(i -> i.product.getPrice() * i.quantity).sum();
        lblCartTotal.setText("Total: " + String.format("%,d VND", total));
    }

    private void checkout() {
        if (cartItems.isEmpty()) {
            SwingUtils.showInfo(this, "Cart is empty!");
            return;
        }

        long total = cartItems.stream().mapToLong(i -> i.product.getPrice() * i.quantity).sum();

        if (SwingUtils.showConfirm(this, "Pay " + String.format("%,d VND", total) + " for items?")) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    for (CartItem item : cartItems) {
                        Product p = item.product;
                        if (p.getCategory() == Product.Category.TOPUP) {
                            billingService.requestTopup(user.getId(), p.getPrice() * item.quantity);
                        } else {
                            billingService.placeOrder(user.getId(), p.getId(), item.quantity,
                                    p.getPrice() * item.quantity);
                        }
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        SwingUtils.showInfo(CartPanel.this,
                                "Order placed successfully! Admin will serve/approve your requests.");
                        cartItems.clear();
                        refreshCartTable();
                        onCheckoutSuccess.run();
                    } catch (Exception ex) {
                        SwingUtils.showError(CartPanel.this, "Payment failed", ex);
                    }
                }
            };
            worker.execute();
        }
    }

    // Custom Renderer for Quantity Column
    class QuantityCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JButton btnMinus = new JButton("-");
        private final JLabel lblQty = new JLabel("0");
        private final JButton btnPlus = new JButton("+");

        public QuantityCellRenderer() {
            setLayout(new GridLayout(1, 3, 2, 0));
            setOpaque(true);
            setBackground(Color.WHITE);

            btnMinus.setFocusable(false);
            btnPlus.setFocusable(false);
            lblQty.setHorizontalAlignment(SwingConstants.CENTER);
            lblQty.setFont(new Font("SansSerif", Font.BOLD, 12));

            add(btnMinus);
            add(lblQty);
            add(btnPlus);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(Color.WHITE);
            }
            lblQty.setText(value.toString());
            return this;
        }
    }
}
