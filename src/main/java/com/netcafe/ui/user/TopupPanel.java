package com.netcafe.ui.user;

import com.netcafe.model.Product;
import com.netcafe.model.User;
import com.netcafe.service.BillingService;
import com.netcafe.util.SwingUtils;
import com.netcafe.ui.component.TopupCard;
import com.netcafe.ui.component.StyledButton;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Panel for topping up user balance with predefined amounts or custom amount.
 */
public class TopupPanel extends JPanel {
    private final User user;
    private final BillingService billingService = new BillingService();
    private final Consumer<Product> onAddToCart;
    private final Runnable onRedeemSuccess;

    public TopupPanel(User user, Consumer<Product> onAddToCart, Runnable onRedeemSuccess) {
        this.user = user;
        this.onAddToCart = onAddToCart;
        this.onRedeemSuccess = onRedeemSuccess;

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        initUI();
    }

    private void initUI() {
        // 1. Info Panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setBackground(Color.WHITE);
        JLabel lblInfo = new JLabel("Select an amount to add to your cart");
        lblInfo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblInfo.setForeground(Color.GRAY);
        infoPanel.add(lblInfo);
        add(infoPanel, BorderLayout.NORTH);

        // 2. Quick Select Cards (GridLayout 2x3)
        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        gridPanel.setBackground(Color.WHITE);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        long[] amounts = { 10000, 20000, 50000, 100000, 200000, 500000 };

        for (long amount : amounts) {
            TopupCard card = new TopupCard(amount, "Add to Cart", () -> requestTopup(amount));
            gridPanel.add(card);
        }

        add(gridPanel, BorderLayout.CENTER);

        // 3. Custom Amount & Redeem
        JPanel southContainer = new JPanel();
        southContainer.setLayout(new BoxLayout(southContainer, BoxLayout.Y_AXIS));
        southContainer.setBackground(Color.WHITE);

        // Custom Amount Panel
        JPanel customPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        customPanel.setBackground(new Color(248, 249, 250));
        customPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 25, 15, 25)));
        customPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel lblCustom = new JLabel("Custom:");
        lblCustom.setFont(new Font("SansSerif", Font.BOLD, 16));

        JTextField txtCustom = new JTextField(18);
        txtCustom.setFont(new Font("SansSerif", Font.PLAIN, 16));
        txtCustom.putClientProperty("JTextField.placeholderText", "Enter amount...");

        JButton btnRequest = StyledButton.primary("Add to Cart");
        btnRequest.setPreferredSize(new Dimension(130, 30));
        btnRequest.addActionListener(e -> {
            try {
                String text = txtCustom.getText().trim().replace(",", "").replace(".", "");
                long amount = Long.parseLong(text);
                if (amount <= 0) {
                    SwingUtils.showError(this, "Amount must be positive.");
                    return;
                }
                requestTopup(amount);
                txtCustom.setText("");
            } catch (NumberFormatException ex) {
                SwingUtils.showError(this, "Invalid number format.");
            }
        });

        customPanel.add(lblCustom);
        customPanel.add(txtCustom);
        customPanel.add(btnRequest);
        southContainer.add(customPanel);

        southContainer.add(Box.createVerticalStrut(10));

        // Redeem Points Panel
        JPanel redeemPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        redeemPanel.setBackground(Color.WHITE);
        redeemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton btnRedeem = StyledButton.accent("Redeem Points (100pts = 5k)");
        btnRedeem.setPreferredSize(new Dimension(250, 30));
        btnRedeem.addActionListener(e -> redeemPoints());
        redeemPanel.add(btnRedeem);
        southContainer.add(redeemPanel);

        add(southContainer, BorderLayout.SOUTH);
    }

    private void requestTopup(long amount) {
        Product p = new Product();
        p.setId(-1);
        p.setName("Topup " + formatPrice(amount));
        p.setCategory(Product.Category.TOPUP);
        p.setPrice(amount);
        p.setStock(1);
        onAddToCart.accept(p);
    }

    private void redeemPoints() {
        String input = JOptionPane.showInputDialog(this, "Enter points to redeem (Min 100):");
        if (input != null && !input.isEmpty()) {
            try {
                int points = Integer.parseInt(input);
                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        billingService.redeemPoints(user.getId(), points);
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            get();
                            SwingUtils.showInfo(TopupPanel.this, "Redeemed successfully!");
                            onRedeemSuccess.run();
                        } catch (Exception ex) {
                            SwingUtils.showError(TopupPanel.this, "Error", ex);
                        }
                    }
                };
                worker.execute();
            } catch (NumberFormatException ex) {
                SwingUtils.showError(this, "Invalid number.");
            }
        }
    }

    private static String formatPrice(long price) {
        return String.format("%,dđ", price).replace(",", ".");
    }
}
