package com.netcafe.ui.admin;

import javax.swing.*;
import java.awt.*;

public class AdminPanel extends JPanel {

    public AdminPanel() {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // Logout Button in Tab Header
        JPanel trailingPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        trailingPanel.setOpaque(false);
        trailingPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 3)); // Spacing

        JButton btnLogout = new JButton("Logout");
        btnLogout.putClientProperty("JButton.buttonType", "roundRect");
        btnLogout.setBackground(com.netcafe.ui.ThemeConfig.DANGER); // Red
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(com.netcafe.ui.ThemeConfig.FONT_SMALL);
        btnLogout.setMargin(new Insets(4, 12, 4, 12));
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> logout());

        trailingPanel.add(btnLogout);

        // Add to trailing edge of tabs
        tabbedPane.putClientProperty("JTabbedPane.trailingComponent", trailingPanel);

        tabbedPane.addTab("Dashboard", new DashboardPanel());
        tabbedPane.addTab("Topup Requests", new TopupRequestPanel());
        tabbedPane.addTab("Order Management", new OrderManagementPanel());
        tabbedPane.addTab("Staff Management", new StaffManagementPanel());
        tabbedPane.addTab("User Management", new UserManagementPanel());
        tabbedPane.addTab("Messages", new MessagePanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null)
                window.dispose();
            new com.netcafe.ui.login.LoginFrame().setVisible(true);
        }
    }
}
