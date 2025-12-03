package com.netcafe.ui.main;

import com.netcafe.model.User;

import com.netcafe.ui.user.UserPanel;

import javax.swing.*;
import java.awt.Image;

public class MainFrame extends JFrame {

    public MainFrame(User user) {

        setTitle("Magic netCafe - " + user.getUsername());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Set Icon
        java.net.URL iconURL = getClass().getResource("/images/icon.jpg");
        if (iconURL != null) {
            Image icon = new ImageIcon(iconURL).getImage();
            setIconImage(icon);

            // macOS Dock Icon
            try {
                if (java.awt.Taskbar.isTaskbarSupported()) {
                    java.awt.Taskbar.getTaskbar().setIconImage(icon);
                }
            } catch (Exception e) {
                System.err.println("Taskbar icon error: " + e.getMessage());
            }
        }

        if (user.getRole() == User.Role.ADMIN) {
            JPanel adminPanel = new JPanel(new java.awt.BorderLayout());

            // Header
            JPanel header = new JPanel(new java.awt.BorderLayout());
            header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            header.setBackground(com.netcafe.ui.ThemeConfig.BG_PANEL);

            JLabel title = new JLabel("Admin Dashboard");
            title.setFont(com.netcafe.ui.ThemeConfig.FONT_HEADER);

            JButton btnLogout = new JButton("Logout");
            btnLogout.putClientProperty("JButton.buttonType", "roundRect");
            btnLogout.setBackground(com.netcafe.ui.ThemeConfig.DANGER);
            btnLogout.setForeground(java.awt.Color.WHITE);
            btnLogout.setFont(com.netcafe.ui.ThemeConfig.FONT_SMALL);
            btnLogout.addActionListener(e -> {
                dispose();
                new com.netcafe.ui.login.LoginFrame().setVisible(true);
            });

            header.add(title, java.awt.BorderLayout.WEST);
            header.add(btnLogout, java.awt.BorderLayout.EAST);

            adminPanel.add(header, java.awt.BorderLayout.NORTH);

            JTabbedPane adminTabs = new JTabbedPane();
            adminTabs.addTab("Dashboard", new com.netcafe.ui.admin.DashboardPanel());
            adminTabs.addTab("Order Management", new com.netcafe.ui.admin.OrderManagementPanel());
            adminTabs.addTab("Topup Requests", new com.netcafe.ui.admin.TopupRequestPanel());
            adminTabs.addTab("User Management", new com.netcafe.ui.admin.UserManagementPanel());
            adminTabs.addTab("Computer Map", new com.netcafe.ui.admin.ComputerMapPanel());

            adminPanel.add(adminTabs, java.awt.BorderLayout.CENTER);
            add(adminPanel);
        } else {
            add(new UserPanel(user));
        }
    }
}
