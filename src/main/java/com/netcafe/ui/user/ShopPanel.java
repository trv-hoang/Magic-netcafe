package com.netcafe.ui.user;

import com.netcafe.model.Product;
import com.netcafe.service.ProductService;
import com.netcafe.util.SwingUtils;
import com.netcafe.ui.ThemeConfig;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ShopPanel extends JPanel {
    private final ProductService productService = new ProductService();
    private final Consumer<Product> onAddToCart;
    private final Product.Category category;

    public ShopPanel(Product.Category category, Consumer<Product> onAddToCart) {
        this.category = category;
        this.onAddToCart = onAddToCart;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel gridPanel = new JPanel(new GridLayout(0, 3, 10, 10)); // 3 columns
        gridPanel.setBackground(Color.WHITE);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        loadProducts(gridPanel);
    }

    private void loadProducts(JPanel gridPanel) {
        SwingWorker<List<Product>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Product> doInBackground() throws Exception {
                return productService.getAllProducts().stream()
                        .filter(p -> p.getCategory() == category)
                        .collect(Collectors.toList());
            }

            @Override
            protected void done() {
                try {
                    List<Product> products = get();
                    gridPanel.removeAll();
                    for (Product p : products) {
                        gridPanel.add(createProductCard(p));
                    }
                    gridPanel.revalidate();
                    gridPanel.repaint();
                } catch (Exception ex) {
                    SwingUtils.showError(ShopPanel.this, "Error loading products", ex);
                }
            }
        };
        worker.execute();
    }

    private JPanel createProductCard(Product p) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(ThemeConfig.BG_PANEL);
        // Subtle border with padding
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        card.setPreferredSize(new Dimension(180, 260));

        // Preview Picture
        JLabel lblImage = new JLabel();
        lblImage.setPreferredSize(new Dimension(180, 120));
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblImage.setBackground(new Color(241, 245, 249)); // Light gray bg
        lblImage.setOpaque(true);

        // Try to load image
        String imagePath = "/images/" + p.getName() + ".jpg";
        java.net.URL imgURL = getClass().getResource(imagePath);

        ImageIcon icon = null;
        if (imgURL != null) {
            icon = new ImageIcon(imgURL);
        } else {
            // Fallback: Try loading from source directory (for dev mode)
            java.io.File devFile = new java.io.File("src/main/resources/images/" + p.getName() + ".jpg");
            if (devFile.exists()) {
                icon = new ImageIcon(devFile.getAbsolutePath());
            }
        }

        if (icon != null) {
            // Scale to fit while preserving aspect ratio
            Image img = icon.getImage();
            int originalWidth = icon.getIconWidth();
            int originalHeight = icon.getIconHeight();

            if (originalWidth > 0 && originalHeight > 0) {
                double ratio = Math.min(150.0 / originalWidth, 110.0 / originalHeight); // Max 150x110
                int newWidth = (int) (originalWidth * ratio);
                int newHeight = (int) (originalHeight * ratio);

                Image scaledImg = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                lblImage.setIcon(new ImageIcon(scaledImg));
            } else {
                setPlaceholderImage(lblImage, p);
            }
        } else {
            setPlaceholderImage(lblImage, p);
        }

        card.add(lblImage, BorderLayout.CENTER);

        // Details
        JPanel details = new JPanel(new GridLayout(3, 1, 0, 8));
        details.setBackground(ThemeConfig.BG_PANEL);
        details.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JLabel lblName = new JLabel(p.getName(), SwingConstants.CENTER);
        lblName.setFont(ThemeConfig.FONT_BODY_BOLD);
        lblName.setForeground(ThemeConfig.TEXT_PRIMARY);

        JLabel lblPrice = new JLabel(String.format("%,d VND", p.getPrice()), SwingConstants.CENTER);
        lblPrice.setForeground(ThemeConfig.SUCCESS);
        lblPrice.setFont(ThemeConfig.FONT_BODY_BOLD);

        JButton btnAdd = new JButton("Add to Cart");
        btnAdd.putClientProperty("JButton.buttonType", "roundRect");
        btnAdd.setBackground(ThemeConfig.PRIMARY);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setFont(ThemeConfig.FONT_SMALL);
        btnAdd.setPreferredSize(new Dimension(0, 32));

        details.add(lblName);
        details.add(lblPrice);
        details.add(btnAdd);
        card.add(details, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> onAddToCart.accept(p));

        return card;
    }

    private void setPlaceholderImage(JLabel lbl, Product p) {
        lbl.setText(String.valueOf(p.getName().charAt(0)));
        lbl.setFont(new Font("SansSerif", Font.BOLD, 48));
        lbl.setForeground(new Color(200, 200, 200));
    }
}
