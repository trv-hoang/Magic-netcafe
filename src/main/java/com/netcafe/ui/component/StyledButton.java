package com.netcafe.ui.component;

import com.netcafe.ui.ThemeConfig;

import javax.swing.*;
import java.awt.*;

/**
 * Factory class for creating consistently styled buttons.
 */
public class StyledButton {

    /**
     * Creates a primary styled button (blue background, white text).
     */
    public static JButton primary(String text) {
        JButton btn = new JButton(text);
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setBackground(ThemeConfig.PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(ThemeConfig.FONT_SMALL);
        return btn;
    }

    /**
     * Creates a success styled button (green background, white text).
     */
    public static JButton success(String text) {
        JButton btn = new JButton(text);
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setBackground(ThemeConfig.SUCCESS);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(ThemeConfig.FONT_BODY_BOLD);
        return btn;
    }

    /**
     * Creates a danger styled button (red background, white text).
     */
    public static JButton danger(String text) {
        JButton btn = new JButton(text);
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setBackground(ThemeConfig.DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(ThemeConfig.FONT_BODY_BOLD);
        return btn;
    }

    /**
     * Creates a secondary styled button (light gray background, dark text).
     */
    public static JButton secondary(String text) {
        JButton btn = new JButton(text);
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setBackground(new Color(236, 240, 241));
        btn.setForeground(new Color(44, 62, 80));
        btn.setFocusPainted(false);
        btn.setFont(ThemeConfig.FONT_BODY_BOLD);
        return btn;
    }

    /**
     * Creates an accent styled button (uses ThemeConfig.ACCENT).
     */
    public static JButton accent(String text) {
        JButton btn = new JButton(text);
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setBackground(ThemeConfig.ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(ThemeConfig.FONT_BODY_BOLD);
        return btn;
    }
}
