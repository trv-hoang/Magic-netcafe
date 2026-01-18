package com.netcafe.ui.user;

import com.netcafe.model.Product;
import com.netcafe.model.User;
import com.netcafe.service.BillingService;
import com.netcafe.util.SwingUtils;
import com.netcafe.ui.ThemeConfig;
import com.netcafe.ui.component.StyledButton;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern cart panel with card-based items, empty state, and sticky total.
 */
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
    private final JPanel itemsPanel;
    private final JPanel emptyStatePanel;
    private final JLabel lblTotal;

    public CartPanel(User user, Runnable onCheckoutSuccess) {
        this.user = user;
        this.onCheckoutSuccess = onCheckoutSuccess;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(350, 0));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(230, 230, 230)));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        JLabel lblTitle = new JLabel("Your Cart");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitle.setForeground(ThemeConfig.TEXT_PRIMARY);
        header.add(lblTitle, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Items container (scrollable)
        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(new Color(248, 249, 250));

        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // Empty state panel
        emptyStatePanel = createEmptyState();
        itemsPanel.add(emptyStatePanel);

        // Bottom panel (sticky total + buttons)
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        // Total row
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setBackground(Color.WHITE);
        totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lblTotalLabel = new JLabel("Total");
        lblTotalLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblTotalLabel.setForeground(Color.GRAY);

        lblTotal = new JLabel("0đ");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTotal.setForeground(ThemeConfig.SUCCESS);

        totalRow.add(lblTotalLabel, BorderLayout.WEST);
        totalRow.add(lblTotal, BorderLayout.EAST);
        bottomPanel.add(totalRow);
        bottomPanel.add(Box.createVerticalStrut(15));

        // Buttons row
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setBackground(Color.WHITE);
        btnRow.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton btnClear = StyledButton.danger("Clear");
        JButton btnCheckout = StyledButton.success("Checkout");
        btnClear.setPreferredSize(new Dimension(0, 40));
        btnCheckout.setPreferredSize(new Dimension(0, 40));
        btnCheckout.setFont(new Font("SansSerif", Font.BOLD, 14));

        btnClear.addActionListener(e -> {
            cartItems.clear();
            refreshCart();
        });

        btnCheckout.addActionListener(e -> checkout());

        btnRow.add(btnClear);
        btnRow.add(btnCheckout);
        bottomPanel.add(btnRow);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createEmptyState() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(60, 20, 60, 20));

        JLabel msgLabel = new JLabel("Your cart is empty");
        msgLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        msgLabel.setForeground(Color.GRAY);
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Add items from the menu");
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subLabel.setForeground(new Color(150, 150, 150));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(msgLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subLabel);

        return panel;
    }

    private JPanel createCartItemCard(CartItem item, int index) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        // Left: Name + Unit price
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);

        JLabel lblName = new JLabel(item.product.getName());
        lblName.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblName.setForeground(ThemeConfig.TEXT_PRIMARY);

        JLabel lblUnitPrice = new JLabel(formatPrice(item.product.getPrice()) + " each");
        lblUnitPrice.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblUnitPrice.setForeground(Color.GRAY);

        leftPanel.add(lblName);
        leftPanel.add(Box.createVerticalStrut(3));
        leftPanel.add(lblUnitPrice);
        card.add(leftPanel, BorderLayout.CENTER);

        // Right: Quantity selector + Subtotal
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);

        // Quantity selector
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        qtyPanel.setBackground(Color.WHITE);

        JButton btnMinus = createQtyButton("-");
        JLabel lblQty = new JLabel(String.valueOf(item.quantity));
        lblQty.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblQty.setPreferredSize(new Dimension(30, 25));
        lblQty.setHorizontalAlignment(SwingConstants.CENTER);
        JButton btnPlus = createQtyButton("+");

        btnMinus.addActionListener(e -> {
            item.quantity--;
            if (item.quantity <= 0) {
                cartItems.remove(index);
            }
            refreshCart();
        });

        btnPlus.addActionListener(e -> {
            item.quantity++;
            refreshCart();
        });

        qtyPanel.add(btnMinus);
        qtyPanel.add(lblQty);
        qtyPanel.add(btnPlus);

        // Subtotal
        JLabel lblSubtotal = new JLabel(formatPrice(item.product.getPrice() * item.quantity));
        lblSubtotal.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblSubtotal.setForeground(ThemeConfig.SUCCESS);
        lblSubtotal.setAlignmentX(Component.RIGHT_ALIGNMENT);

        rightPanel.add(qtyPanel);
        rightPanel.add(Box.createVerticalStrut(3));
        rightPanel.add(lblSubtotal);

        card.add(rightPanel, BorderLayout.EAST);

        return card;
    }

    private JButton createQtyButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(28, 25));
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(240, 240, 240));
        btn.setForeground(ThemeConfig.TEXT_PRIMARY);
        btn.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
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
        refreshCart();
    }

    private void refreshCart() {
        itemsPanel.removeAll();

        if (cartItems.isEmpty()) {
            itemsPanel.add(emptyStatePanel);
        } else {
            for (int i = 0; i < cartItems.size(); i++) {
                itemsPanel.add(createCartItemCard(cartItems.get(i), i));
            }
            // Add some padding at bottom
            itemsPanel.add(Box.createVerticalGlue());
        }

        updateTotal();
        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private void updateTotal() {
        long total = cartItems.stream().mapToLong(i -> i.product.getPrice() * i.quantity).sum();
        lblTotal.setText(formatPrice(total));
    }

    private void checkout() {
        if (cartItems.isEmpty()) {
            SwingUtils.showInfo(this, "Cart is empty!");
            return;
        }

        long total = cartItems.stream().mapToLong(i -> i.product.getPrice() * i.quantity).sum();

        if (SwingUtils.showConfirm(this, "Pay " + formatPrice(total) + " for items?")) {
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
                                "Order placed! Admin will serve your requests.");
                        cartItems.clear();
                        refreshCart();
                        onCheckoutSuccess.run();
                    } catch (Exception ex) {
                        // Extract user-friendly message from exception
                        String errorMsg = ex.getMessage();
                        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
                            errorMsg = ex.getCause().getMessage();
                        }
                        SwingUtils.showError(CartPanel.this, errorMsg != null ? errorMsg : "Payment failed");
                    }
                }
            };
            worker.execute();
        }
    }

    private static String formatPrice(long price) {
        return String.format("%,dđ", price).replace(",", ".");
    }
}
