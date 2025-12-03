package com.netcafe.util;

import javax.swing.*;
import java.awt.*;

public class SwingUtils {

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showError(Component parent, String message, Throwable ex) {
        ex.printStackTrace(); // Log to console for debugging
        String fullMessage = message;
        if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
            fullMessage += ": " + ex.getMessage();
        }
        JOptionPane.showMessageDialog(parent, fullMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
