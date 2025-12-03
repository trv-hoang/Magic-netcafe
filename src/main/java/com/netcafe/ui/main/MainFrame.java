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
            JTabbedPane adminTabs = new JTabbedPane();
            adminTabs.addTab("Dashboard", new com.netcafe.ui.admin.DashboardPanel());
            adminTabs.addTab("Order Management", new com.netcafe.ui.admin.OrderManagementPanel());
            adminTabs.addTab("Topup Requests", new com.netcafe.ui.admin.TopupRequestPanel());
            adminTabs.addTab("User Management", new com.netcafe.ui.admin.UserManagementPanel());
            adminTabs.addTab("Computer Map", new com.netcafe.ui.admin.ComputerMapPanel());

            add(adminTabs);
        } else {
            add(new UserPanel(user));
        }
    }
}
