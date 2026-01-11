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
 * Panel displaying products (Foods or Drinks) in a scrollable grid.
 */
public class ShopPanel extends JPanel {
    private final ProductService productService = new ProductService();
    private final Consumer<Product> onAddToCart;
    private final Product.Category category;
    private final JPanel gridPanel;
    private final JScrollPane scrollPane;

    public ShopPanel(Product.Category category, Consumer<Product> onAddToCart) {
        this.category = category;
        this.onAddToCart = onAddToCart;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Use WrapLayout for proper wrapping and scrolling
        gridPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 15));
        gridPanel.setBackground(Color.WHITE);

        scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(Color.WHITE);

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

    /**
     * WrapLayout - a FlowLayout subclass that wraps components properly.
     */
    private static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1);
            return minimum;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();
                if (targetWidth == 0) {
                    targetWidth = Integer.MAX_VALUE;
                }

                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
                int maxWidth = targetWidth - horizontalInsetsAndGap;

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int rowHeight = 0;

                int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

                        if (rowWidth + d.width > maxWidth) {
                            dim.width = Math.max(dim.width, rowWidth);
                            if (dim.height > 0) {
                                dim.height += vgap;
                            }
                            dim.height += rowHeight;
                            rowWidth = 0;
                            rowHeight = 0;
                        }

                        if (rowWidth != 0) {
                            rowWidth += hgap;
                        }
                        rowWidth += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }

                dim.width = Math.max(dim.width, rowWidth);
                if (dim.height > 0) {
                    dim.height += vgap;
                }
                dim.height += rowHeight;

                dim.width += horizontalInsetsAndGap;
                dim.height += insets.top + insets.bottom + vgap * 2;

                Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
                if (scrollPane != null && target.isValid()) {
                    dim.width = ((JScrollPane) scrollPane).getViewport().getWidth();
                }

                return dim;
            }
        }
    }
}
