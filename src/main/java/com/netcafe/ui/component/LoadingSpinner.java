package com.netcafe.ui.component;

import com.netcafe.ui.ThemeConfig;

import javax.swing.*;
import java.awt.*;

/**
 * A loading spinner component for async operations.
 * 
 * Usage:
 * LoadingSpinner spinner = new LoadingSpinner();
 * LoadingSpinner spinner = new LoadingSpinner("Loading products...");
 */
public class LoadingSpinner extends JPanel {

    private final Timer animationTimer;
    private int angle = 0;
    private final JLabel lblMessage;

    public LoadingSpinner() {
        this("Loading...");
    }

    public LoadingSpinner(String message) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setOpaque(true);

        // Spinner
        JPanel spinnerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = 40;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(new Color(230, 230, 230));
                g2.drawArc(x, y, size, size, 0, 360);

                g2.setColor(ThemeConfig.PRIMARY);
                g2.drawArc(x, y, size, size, angle, 90);

                g2.dispose();
            }
        };
        spinnerPanel.setPreferredSize(new Dimension(60, 60));
        spinnerPanel.setMaximumSize(new Dimension(60, 60));
        spinnerPanel.setBackground(Color.WHITE);
        spinnerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblMessage = new JLabel(message);
        lblMessage.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblMessage.setForeground(Color.GRAY);
        lblMessage.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(Box.createVerticalGlue());
        add(spinnerPanel);
        add(Box.createVerticalStrut(10));
        add(lblMessage);
        add(Box.createVerticalGlue());

        animationTimer = new Timer(30, e -> {
            angle -= 10;
            if (angle < 0)
                angle = 360;
            spinnerPanel.repaint();
        });
    }

    public void start() {
        animationTimer.start();
    }

    public void stop() {
        animationTimer.stop();
    }

    public void setMessage(String message) {
        lblMessage.setText(message);
    }
}
