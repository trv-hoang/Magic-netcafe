package com.netcafe.ui.component;

import com.netcafe.ui.ThemeConfig;

import javax.swing.*;
import java.awt.*;

/**
 * A circular avatar component with initials fallback.
 * 
 * Usage:
 * UserAvatar avatar = new UserAvatar("John Doe", 40);
 * UserAvatar avatar = new UserAvatar("VH", 32); // Just initials
 */
public class UserAvatar extends JPanel {

    private final String initials;
    private final int size;
    private final Color bgColor;

    public UserAvatar(String name, int size) {
        this.size = size;
        this.initials = getInitials(name);
        this.bgColor = generateColor(name);

        setPreferredSize(new Dimension(size, size));
        setMaximumSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw circle background
        g2.setColor(bgColor);
        g2.fillOval(0, 0, size, size);

        // Draw initials
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, size / 3));
        FontMetrics fm = g2.getFontMetrics();
        int x = (size - fm.stringWidth(initials)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(initials, x, y);

        g2.dispose();
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty())
            return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private Color generateColor(String name) {
        // Generate a consistent color based on name
        int hash = name.hashCode();
        Color[] colors = {
                new Color(52, 152, 219), // Blue
                new Color(155, 89, 182), // Purple
                new Color(46, 204, 113), // Green
                new Color(230, 126, 34), // Orange
                new Color(231, 76, 60), // Red
                new Color(26, 188, 156), // Teal
        };
        return colors[Math.abs(hash) % colors.length];
    }
}
