package com.netcafe.ui.user;

import com.netcafe.util.SwingUtils;
import com.netcafe.ui.component.ProductCard;

import javax.swing.*;
import java.awt.*;

/**
 * Panel displaying available games in a grid of cards.
 */
public class GamePanel extends JPanel {

    public GamePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Use FlowLayout with left alignment for proper wrapping
        JPanel gridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        gridPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Make the FlowLayout wrap properly
        scrollPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int width = scrollPane.getViewport().getWidth();
                gridPanel.setPreferredSize(new Dimension(width, gridPanel.getPreferredSize().height));
                gridPanel.revalidate();
            }
        });

        add(scrollPane, BorderLayout.CENTER);

        String[] games = {
                "League of Legends", "Dota 2", "PUBG", "Valorant",
                "FIFA Online 4", "CS2", "Minecraft", "Roblox"
        };

        for (String game : games) {
            ProductCard card = new ProductCard(
                    game,
                    "Play Now",
                    () -> SwingUtils.showInfo(this, "Launching " + game + "..."));
            gridPanel.add(card);
        }
    }
}
