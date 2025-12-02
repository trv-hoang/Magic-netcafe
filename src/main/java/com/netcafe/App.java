package com.netcafe;

import com.netcafe.ui.login.LoginFrame;

import javax.swing.*;
import java.awt.Font;
import java.awt.Insets;

public class App {
    public static void main(String[] args) {
        System.setProperty("apple.awt.application.name", "Magic netCafe");
        SwingUtilities.invokeLater(() -> {
            try {
                com.formdev.flatlaf.FlatLightLaf.setup();
                // Global UI Defaults
                UIManager.put("Button.font", new Font("SansSerif", Font.BOLD, 14));
                UIManager.put("Button.margin", new Insets(6, 12, 6, 12)); // Reduced padding
                UIManager.put("Button.arc", 12); // Slightly smaller radius
                UIManager.put("Component.focusWidth", 1);
            } catch (Exception e) {
                System.err.println("Failed to set LookAndFeel: " + e.getMessage());
            }
            new LoginFrame().setVisible(true);
        });
    }
}
