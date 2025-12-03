package com.netcafe.ui.user;

import com.netcafe.util.SwingUtils;
import com.netcafe.ui.ThemeConfig;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {

    public GamePanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
        setBackground(Color.WHITE);

        String[] games = { "League of Legends", "Dota 2", "PUBG", "Valorant", "FIFA Online 4", "CS2", "Minecraft",
                "Roblox" };
        for (String game : games) {
            JButton btnGame = new JButton(game);
            btnGame.setPreferredSize(new Dimension(180, 100));
            btnGame.setFont(new Font("SansSerif", Font.BOLD, 14));

            // Button Styling
            btnGame.putClientProperty("JButton.buttonType", "roundRect");
            btnGame.setBackground(new Color(240, 240, 240));
            btnGame.setForeground(ThemeConfig.TEXT_PRIMARY);
            btnGame.setFocusPainted(false);

            btnGame.addActionListener(e -> SwingUtils.showInfo(this, "Launching " + game + "..."));
            add(btnGame);
        }
    }
}
