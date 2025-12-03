package com.netcafe.ui.user;

import com.netcafe.model.Product;
import com.netcafe.model.User;
import com.netcafe.service.BillingService;
import com.netcafe.util.SwingUtils;
import com.netcafe.ui.ThemeConfig;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

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
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        setBackground(Color.WHITE);

        initUI();
    }

    private void initUI() {
        // 1. Info Panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setBackground(Color.WHITE);
        JLabel lblInfo = new JLabel("Select an amount or enter a custom value to request top-up.");
        lblInfo.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lblInfo.setForeground(Color.GRAY);
        infoPanel.add(lblInfo);
        add(infoPanel, BorderLayout.NORTH);

        // 2. Quick Select Buttons (Grid)
        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        gridPanel.setBackground(Color.WHITE);
        long[] amounts = { 10000, 20000, 50000, 100000, 200000, 500000 };

        for (long amount : amounts) {
            JButton btn = new JButton(String.format("%,d VND", amount));
            btn.setFont(new Font("SansSerif", Font.BOLD, 16));
            btn.setFocusPainted(false);
            btn.putClientProperty("JButton.buttonType", "roundRect");
            btn.setBackground(new Color(236, 240, 241));
            btn.setForeground(new Color(44, 62, 80));
            btn.addActionListener(e -> requestTopup(amount));
            gridPanel.add(btn);
        }
        add(gridPanel, BorderLayout.CENTER);

        // 3. Custom Amount & Redeem
        JPanel southContainer = new JPanel(new GridLayout(2, 1, 0, 20));
        southContainer.setBackground(Color.WHITE);

        // Custom Amount
        JPanel customPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        customPanel.setBackground(Color.WHITE);
        customPanel.setBorder(BorderFactory.createTitledBorder("Custom Amount"));

        JTextField txtCustom = new JTextField(12);
        txtCustom.putClientProperty("JTextField.placeholderText", "Enter amount...");

        JButton btnRequest = new JButton("Request Top-up");
        btnRequest.putClientProperty("JButton.buttonType", "roundRect");
        btnRequest.setBackground(ThemeConfig.PRIMARY);
        btnRequest.setForeground(Color.WHITE);
        btnRequest.setFont(new Font("SansSerif", Font.BOLD, 14));

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

        customPanel.add(new JLabel("Amount (VND):"));
        customPanel.add(txtCustom);
        customPanel.add(btnRequest);
        southContainer.add(customPanel);

        // Redeem Points
        JPanel redeemPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        redeemPanel.setBackground(Color.WHITE);
        JButton btnRedeem = new JButton("Redeem Points (100pts = 5k)");
        btnRedeem.putClientProperty("JButton.buttonType", "roundRect");
        btnRedeem.setBackground(ThemeConfig.ACCENT);
        btnRedeem.setForeground(Color.WHITE);
        btnRedeem.setFont(new Font("SansSerif", Font.BOLD, 14));

        btnRedeem.addActionListener(e -> redeemPoints());
        redeemPanel.add(btnRedeem);
        southContainer.add(redeemPanel);

        add(southContainer, BorderLayout.SOUTH);
    }

    private void requestTopup(long amount) {
        Product p = new Product();
        p.setId(-1); // Dummy ID
        p.setName("Topup " + amount);
        p.setCategory(Product.Category.TOPUP);
        p.setPrice(amount);
        p.setStock(1);
        onAddToCart.accept(p);
        SwingUtils.showInfo(this, "Topup added to cart. Please checkout to confirm.");
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
}
