package com.netcafe;

import com.netcafe.ui.login.LoginFrame;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        System.setProperty("apple.awt.application.name", "Magic netCafe");

        SwingUtilities.invokeLater(() -> {
            try {
                // Use Mac Light Theme for a cleaner look if available, otherwise Light
                try {
                    // Try to set FlatMacLightLaf
                    Class<?> clazz = Class.forName("com.formdev.flatlaf.themes.FlatMacLightLaf");
                    java.lang.reflect.Method setupMethod = clazz.getMethod("setup");
                    setupMethod.invoke(null);
                } catch (Exception ex) {
                    com.formdev.flatlaf.FlatLightLaf.setup();
                }

                // Global UI Defaults
                UIManager.put("Button.font", com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);
                UIManager.put("Button.arc", com.netcafe.ui.ThemeConfig.CORNER_RADIUS);
                UIManager.put("Component.focusWidth", 1);
                UIManager.put("Panel.background", com.netcafe.ui.ThemeConfig.BG_MAIN);
                UIManager.put("TextField.arc", com.netcafe.ui.ThemeConfig.CORNER_RADIUS);
                UIManager.put("PasswordField.arc", com.netcafe.ui.ThemeConfig.CORNER_RADIUS);

                // OptionPane Defaults
                UIManager.put("OptionPane.messageFont", com.netcafe.ui.ThemeConfig.FONT_BODY);
                UIManager.put("OptionPane.buttonFont", com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);
                UIManager.put("OptionPane.background", com.netcafe.ui.ThemeConfig.BG_MAIN);
                UIManager.put("OptionPane.messageForeground", com.netcafe.ui.ThemeConfig.TEXT_PRIMARY);
                UIManager.put("Panel.background", com.netcafe.ui.ThemeConfig.BG_MAIN); // Ensure panels inside dialogs
                                                                                       // match
            } catch (Exception e) {
                System.err.println("Failed to set LookAndFeel: " + e.getMessage());
            }
            new LoginFrame().setVisible(true);
        });
    }
}
