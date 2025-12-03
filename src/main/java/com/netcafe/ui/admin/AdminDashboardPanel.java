package com.netcafe.ui.admin;

import com.netcafe.ui.ThemeConfig;

import javax.swing.*;
import java.awt.*;

public class AdminDashboardPanel extends JPanel {

    public AdminDashboardPanel(Runnable logoutAction) {
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        header.setBackground(ThemeConfig.BG_PANEL);

        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(ThemeConfig.FONT_HEADER);

        JButton btnLogout = new JButton("Logout");
        btnLogout.putClientProperty("JButton.buttonType", "roundRect");
        btnLogout.setBackground(ThemeConfig.DANGER);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(ThemeConfig.FONT_SMALL);
        btnLogout.addActionListener(e -> logoutAction.run());

        header.add(title, BorderLayout.WEST);
        header.add(btnLogout, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Tabs
        JTabbedPane adminTabs = new JTabbedPane();
        adminTabs.addTab("Dashboard", new DashboardPanel());
        adminTabs.addTab("Order Management", new OrderManagementPanel());
        adminTabs.addTab("Topup Requests", new TopupRequestPanel());
        adminTabs.addTab("User Management", new UserManagementPanel());
        adminTabs.addTab("Messages", new MessagePanel());
        adminTabs.addTab("Computer Map", new ComputerMapPanel());

        add(adminTabs, BorderLayout.CENTER);
    }
}
