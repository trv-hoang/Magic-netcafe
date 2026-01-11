package com.netcafe.ui.component;

import com.netcafe.ui.ThemeConfig;

import javax.swing.*;
import java.awt.*;

/**
 * A reusable empty state panel with title, subtitle, and optional action.
 * 
 * Usage:
 * EmptyState empty = new EmptyState("Your cart is empty", "Add items from the
 * menu");
 * EmptyState empty = new EmptyState("No results", "Try a different search",
 * "Clear", () -> clear());
 */
public class EmptyState extends JPanel {

    public EmptyState(String title, String subtitle) {
        this(title, subtitle, null, null);
    }

    public EmptyState(String title, String subtitle, String actionText, Runnable onAction) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(60, 20, 60, 20));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitle.setForeground(Color.GRAY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(150, 150, 150));
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(lblTitle);
        add(Box.createVerticalStrut(5));
        add(lblSubtitle);

        if (actionText != null && onAction != null) {
            add(Box.createVerticalStrut(15));
            JButton btnAction = StyledButton.primary(actionText);
            btnAction.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnAction.addActionListener(e -> onAction.run());
            add(btnAction);
        }
    }
}
