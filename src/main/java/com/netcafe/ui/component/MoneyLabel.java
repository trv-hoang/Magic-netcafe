package com.netcafe.ui.component;

import com.netcafe.ui.ThemeConfig;

import javax.swing.*;
import java.awt.*;

/**
 * A label for displaying formatted currency amounts.
 * 
 * Usage:
 * MoneyLabel price = MoneyLabel.price(50000); // "50.000đ" in green
 * MoneyLabel debt = MoneyLabel.debt(10000); // "10.000đ" in red
 */
public class MoneyLabel extends JLabel {

    private MoneyLabel(long amount, Color color, int fontSize) {
        super(formatPrice(amount));
        setFont(new Font("SansSerif", Font.BOLD, fontSize));
        setForeground(color);
    }

    // Factory methods
    public static MoneyLabel price(long amount) {
        return new MoneyLabel(amount, ThemeConfig.SUCCESS, 14);
    }

    public static MoneyLabel price(long amount, int fontSize) {
        return new MoneyLabel(amount, ThemeConfig.SUCCESS, fontSize);
    }

    public static MoneyLabel debt(long amount) {
        return new MoneyLabel(amount, ThemeConfig.DANGER, 14);
    }

    public static MoneyLabel neutral(long amount) {
        return new MoneyLabel(amount, ThemeConfig.TEXT_PRIMARY, 14);
    }

    public static MoneyLabel large(long amount) {
        return new MoneyLabel(amount, ThemeConfig.SUCCESS, 20);
    }

    public void updateAmount(long amount) {
        setText(formatPrice(amount));
    }

    private static String formatPrice(long price) {
        return String.format("%,dđ", price).replace(",", ".");
    }
}
