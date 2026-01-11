package com.netcafe.ui.admin;

import com.netcafe.ui.ThemeConfig;

import javax.swing.*;
import java.awt.*;

public class AdminDashboardPanel extends JPanel {

    public AdminDashboardPanel(JFrame parentFrame) {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(33, 33, 33)); // Keep dark header for contrast
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setFont(ThemeConfig.FONT_HEADER);
        titleLabel.setForeground(Color.WHITE);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(ThemeConfig.DANGER);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            int confirm = javax.swing.JOptionPane.showConfirmDialog(
                    parentFrame,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    javax.swing.JOptionPane.YES_NO_OPTION);
            if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                parentFrame.dispose();
                new com.netcafe.ui.login.LoginFrame().setVisible(true);
            }
        });

        // AI Chat Button
        JButton btnAI = new JButton("🤖 AI");
        btnAI.setBackground(ThemeConfig.PRIMARY);
        btnAI.setForeground(Color.WHITE);
        btnAI.setFocusPainted(false);
        btnAI.putClientProperty("JButton.buttonType", "roundRect");
        btnAI.addActionListener(e -> {
            AIAnalystDialog dialog = new AIAnalystDialog(parentFrame);
            dialog.setVisible(true);
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(new Color(33, 33, 33));
        rightPanel.add(btnAI);
        rightPanel.add(btnLogout);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Tabs (Main content)
        JTabbedPane adminTabs = new JTabbedPane();
        adminTabs.addTab("Order Management", new OrderManagementPanel());
        adminTabs.addTab("Topup Requests", new TopupRequestPanel());
        adminTabs.addTab("User Management", new UserManagementPanel());
        adminTabs.addTab("Messages", new MessagePanel());
        adminTabs.addTab("Computer Map", new ComputerMapPanel());
        adminTabs.addTab("Statistics", new StatisticsPanel());

        add(adminTabs, BorderLayout.CENTER);
    }

}
