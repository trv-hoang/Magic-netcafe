package com.netcafe.ui.user;

import com.netcafe.model.Product;
import com.netcafe.service.ProductService;
import com.netcafe.util.SwingUtils;
import com.netcafe.ui.component.ProductCard;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Panel displaying products (Foods or Drinks) in a grid of cards.
 * Uses WrapLayout for proper resizing behavior.
 */
public class ShopPanel extends JPanel {
    private final ProductService productService = new ProductService();
    private final Consumer<Product> onAddToCart;
    private final Product.Category category;
    private final JPanel gridPanel;

    public ShopPanel(Product.Category category, Consumer<Product> onAddToCart) {
        this.category = category;
        this.onAddToCart = onAddToCart;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Use FlowLayout with left alignment for proper wrapping
        gridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        gridPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Make the FlowLayout wrap properly
        scrollPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int width = scrollPane.getViewport().getWidth();
                gridPanel.setPreferredSize(new Dimension(width, gridPanel.getPreferredSize().height));
                gridPanel.revalidate();
            }
        });

        add(scrollPane, BorderLayout.CENTER);
        loadProducts();
    }

    private void loadProducts() {
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
                        ProductCard card = new ProductCard(
                                p.getName(),
                                p.getPrice(),
                                "Add to Cart",
                                () -> onAddToCart.accept(p));
                        gridPanel.add(card);
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
}
