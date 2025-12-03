package com.netcafe.ui.user;

import com.netcafe.model.Product;
import com.netcafe.model.User;

import javax.swing.*;
import java.awt.*;

public class UserPanel extends JPanel {

    private final UserHeaderPanel headerPanel;
    private final CartPanel cartPanel;

    public UserPanel(User user) {

        setLayout(new BorderLayout());

        // 1. Header
        headerPanel = new UserHeaderPanel(user, this::closeApp);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Main Content (Tabs Center, Cart Right)
        JPanel mainContent = new JPanel(new BorderLayout());

        // Right: Cart
        cartPanel = new CartPanel(user, headerPanel::refreshBalance);
        mainContent.add(cartPanel, BorderLayout.EAST);

        // Left: Tabs (Games, Foods, Drinks, Topup)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Games", new GamePanel());
        tabbedPane.addTab("Foods", new ShopPanel(Product.Category.FOOD, cartPanel::addToCart));
        tabbedPane.addTab("Drinks", new ShopPanel(Product.Category.DRINK, cartPanel::addToCart));
        tabbedPane.addTab("Topup", new TopupPanel(user, cartPanel::addToCart, headerPanel::refreshBalance));
        tabbedPane.addTab("Chat", new ChatPanel(user));

        mainContent.add(tabbedPane, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);
    }

    private void closeApp() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
        new com.netcafe.ui.login.LoginFrame().setVisible(true);
    }
}
