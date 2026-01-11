package com.netcafe.ui.component;

import com.netcafe.ui.ThemeConfig;

import javax.swing.*;
import java.awt.*;

/**
 * A card component for displaying topup amounts.
 * Shows the amount and an "Add to Cart" button.
 */
public class TopupCard extends JPanel {

    private static final int CARD_WIDTH = 160;
    private static final int CARD_HEIGHT = 100;

    public TopupCard(long amount, String buttonText, Runnable onAction) {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));

        // Amount label
        JLabel lblAmount = new JLabel(formatPrice(amount), SwingConstants.CENTER);
        lblAmount.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblAmount.setForeground(ThemeConfig.TEXT_PRIMARY);
        add(lblAmount, BorderLayout.CENTER);

        // Button
        JButton btn = StyledButton.primary(buttonText);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            if (onAction != null)
                onAction.run();
        });
        add(btn, BorderLayout.SOUTH);
    }

    private static String formatPrice(long price) {
        return String.format("%,dđ", price).replace(",", ".");
    }
}
