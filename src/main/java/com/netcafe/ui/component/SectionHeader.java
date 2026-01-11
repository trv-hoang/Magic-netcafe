package com.netcafe.ui.component;

import com.netcafe.ui.ThemeConfig;

import javax.swing.*;
import java.awt.*;

/**
 * A section header with title and optional action button.
 * 
 * Usage:
 * SectionHeader header = new SectionHeader("Your Cart");
 * SectionHeader header = new SectionHeader("Orders", "View All", () ->
 * showAll());
 */
public class SectionHeader extends JPanel {

    public SectionHeader(String title) {
        this(title, null, null);
    }

    public SectionHeader(String title, String actionText, Runnable onAction) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitle.setForeground(ThemeConfig.TEXT_PRIMARY);
        add(lblTitle, BorderLayout.WEST);

        if (actionText != null && onAction != null) {
            JButton btnAction = new JButton(actionText);
            btnAction.setFont(new Font("SansSerif", Font.PLAIN, 12));
            btnAction.setForeground(ThemeConfig.PRIMARY);
            btnAction.setContentAreaFilled(false);
            btnAction.setBorderPainted(false);
            btnAction.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnAction.addActionListener(e -> onAction.run());
            add(btnAction, BorderLayout.EAST);
        }
    }
}
