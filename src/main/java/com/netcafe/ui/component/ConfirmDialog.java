package com.netcafe.ui.component;

import com.netcafe.ui.ThemeConfig;

import javax.swing.*;
import java.awt.*;

/**
 * Styled dialog utilities to replace JOptionPane.
 * 
 * Usage:
 * ConfirmDialog.show(parent, "Delete this item?", () -> delete());
 * ConfirmDialog.showInfo(parent, "Operation successful!");
 * ConfirmDialog.showError(parent, "Something went wrong");
 */
public class ConfirmDialog {

    public static void show(Component parent, String message, Runnable onConfirm) {
        show(parent, "Confirm", message, onConfirm);
    }

    public static void show(Component parent, String title, String message, Runnable onConfirm) {
        JDialog dialog = createDialog(parent, title);
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel lblMessage = new JLabel("<html><body style='width: 250px'>" + message + "</body></html>");
        lblMessage.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblMessage.setForeground(ThemeConfig.TEXT_PRIMARY);
        lblMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(lblMessage);
        content.add(Box.createVerticalStrut(20));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton btnCancel = StyledButton.secondary("Cancel");
        JButton btnConfirm = StyledButton.primary("Confirm");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnConfirm.addActionListener(e -> {
            dialog.dispose();
            if (onConfirm != null)
                onConfirm.run();
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnConfirm);
        content.add(btnPanel);

        dialog.add(content);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    public static void showInfo(Component parent, String message) {
        showMessage(parent, "Info", message, ThemeConfig.PRIMARY);
    }

    public static void showError(Component parent, String message) {
        showMessage(parent, "Error", message, ThemeConfig.DANGER);
    }

    public static void showSuccess(Component parent, String message) {
        showMessage(parent, "Success", message, ThemeConfig.SUCCESS);
    }

    private static void showMessage(Component parent, String title, String message, Color accentColor) {
        JDialog dialog = createDialog(parent, title);
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel lblMessage = new JLabel("<html><body style='width: 250px'>" + message + "</body></html>");
        lblMessage.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblMessage.setForeground(ThemeConfig.TEXT_PRIMARY);
        lblMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(lblMessage);
        content.add(Box.createVerticalStrut(20));

        JButton btnOk = new JButton("OK");
        btnOk.setBackground(accentColor);
        btnOk.setForeground(Color.WHITE);
        btnOk.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnOk.setFocusPainted(false);
        btnOk.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        btnOk.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOk.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnOk.addActionListener(e -> dialog.dispose());
        content.add(btnOk);

        dialog.add(content);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static JDialog createDialog(Component parent, String title) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog;
        if (window instanceof Frame) {
            dialog = new JDialog((Frame) window, title, true);
        } else if (window instanceof Dialog) {
            dialog = new JDialog((Dialog) window, title, true);
        } else {
            dialog = new JDialog((Frame) null, title, true);
        }
        dialog.setUndecorated(false);
        dialog.setResizable(false);
        return dialog;
    }
}
