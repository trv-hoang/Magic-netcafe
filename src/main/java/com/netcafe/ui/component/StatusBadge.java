package com.netcafe.ui.component;

import javax.swing.*;
import java.awt.*;

/**
 * A colored badge/pill component for displaying status.
 * 
 * Usage:
 * StatusBadge badge = StatusBadge.success("ACTIVE");
 * StatusBadge tier = StatusBadge.warning("BRONZE");
 */
public class StatusBadge extends JLabel {

    private StatusBadge(String text, Color bgColor, Color textColor) {
        super(text);
        setOpaque(true);
        setBackground(bgColor);
        setForeground(textColor);
        setFont(new Font("SansSerif", Font.BOLD, 11));
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.dispose();
        super.paintComponent(g);
    }

    // Factory methods
    public static StatusBadge success(String text) {
        return new StatusBadge(text, new Color(39, 174, 96), Color.WHITE);
    }

    public static StatusBadge warning(String text) {
        return new StatusBadge(text, new Color(243, 156, 18), Color.WHITE);
    }

    public static StatusBadge danger(String text) {
        return new StatusBadge(text, new Color(231, 76, 60), Color.WHITE);
    }

    public static StatusBadge info(String text) {
        return new StatusBadge(text, new Color(52, 152, 219), Color.WHITE);
    }

    public static StatusBadge neutral(String text) {
        return new StatusBadge(text, new Color(149, 165, 166), Color.WHITE);
    }
}
