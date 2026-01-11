package com.netcafe.ui.component;

import com.netcafe.ui.ThemeConfig;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

/**
 * A styled tabbed pane with modern look.
 * 
 * Usage:
 * TabPanel tabs = new TabPanel();
 * tabs.addTab("Games", gamePanel);
 * tabs.addTab("Foods", foodPanel);
 */
public class TabPanel extends JTabbedPane {

    public TabPanel() {
        setUI(new ModernTabbedPaneUI());
        setFont(new Font("SansSerif", Font.BOLD, 13));
        setBackground(Color.WHITE);
        setForeground(ThemeConfig.TEXT_PRIMARY);
        setBorder(null);
    }

    private static class ModernTabbedPaneUI extends BasicTabbedPaneUI {

        private static final Color TAB_SELECTED_BG = Color.WHITE;
        private static final Color TAB_UNSELECTED_BG = new Color(241, 245, 249);
        private static final Color TAB_HOVER_BG = new Color(226, 232, 240);
        private static final Color TAB_BORDER = new Color(203, 213, 225);
        private static final Color TAB_SELECTED_TEXT = ThemeConfig.PRIMARY;
        private static final Color TAB_UNSELECTED_TEXT = new Color(100, 116, 139);

        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(10, 20, 10, 20);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            contentBorderInsets = new Insets(0, 0, 0, 0);
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement,
                int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isSelected) {
                g2.setColor(TAB_SELECTED_BG);
            } else {
                g2.setColor(TAB_UNSELECTED_BG);
            }
            g2.fillRoundRect(x + 2, y + 2, w - 4, h - 2, 8, 8);
            g2.dispose();
        }

        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement,
                int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            if (isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeConfig.PRIMARY);
                g2.setStroke(new BasicStroke(2));
                g2.drawLine(x + 4, y + h - 1, x + w - 4, y + h - 1);
                g2.dispose();
            }
        }

        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            // Draw just a top line
            g.setColor(TAB_BORDER);
            g.drawLine(0, calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight),
                    tabPane.getWidth(), calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight));
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int tabPlacement,
                Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
            // No focus indicator
        }

        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font,
                FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(font);

            if (isSelected) {
                g2.setColor(TAB_SELECTED_TEXT);
            } else {
                g2.setColor(TAB_UNSELECTED_TEXT);
            }

            g2.drawString(title, textRect.x, textRect.y + metrics.getAscent());
            g2.dispose();
        }
    }
}
