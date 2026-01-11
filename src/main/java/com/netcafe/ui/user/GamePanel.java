package com.netcafe.ui.user;

import com.netcafe.util.SwingUtils;
import com.netcafe.ui.component.ProductCard;

import javax.swing.*;
import java.awt.*;

/**
 * Panel displaying available games in a scrollable grid.
 */
public class GamePanel extends JPanel {

    public GamePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Use WrapLayout for proper wrapping and scrolling
        JPanel gridPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 15));
        gridPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);

        String[] games = {
                "League of Legends", "Dota 2", "PUBG", "Valorant",
                "FIFA Online 4", "CS2", "Minecraft", "Roblox"
        };

        for (String game : games) {
            ProductCard card = new ProductCard(
                    game,
                    "Play Now",
                    () -> SwingUtils.showInfo(this, "Launching " + game + "..."));
            gridPanel.add(card);
        }
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
