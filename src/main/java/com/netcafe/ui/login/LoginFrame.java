package com.netcafe.ui.login;

import com.netcafe.model.User;
import com.netcafe.service.AuthService;
import com.netcafe.ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final AuthService authService = new AuthService();
    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JButton btnLogin = new JButton("Login");

    public LoginFrame() {
        setTitle("Magic netCafe Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600); // Larger window
        setLocationRelativeTo(null);

        // Set Icon
        java.net.URL iconURL = getClass().getResource("/images/icon.jpg");
        if (iconURL != null) {
            Image icon = new ImageIcon(iconURL).getImage();
            setIconImage(icon);
            try {
                if (java.awt.Taskbar.isTaskbarSupported()) {
                    java.awt.Taskbar.getTaskbar().setIconImage(icon);
                }
            } catch (Exception e) {
                System.err.println("Taskbar icon error: " + e.getMessage());
            }
        }

        // Main Layout: Split View (Left: Image/Brand, Right: Login Form)
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        setContentPane(mainPanel);

        // Left Panel (Brand)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(com.netcafe.ui.ThemeConfig.PRIMARY);

        JLabel lblBrand = new JLabel("Magic netCafe", SwingConstants.CENTER);
        lblBrand.setFont(com.netcafe.ui.ThemeConfig.FONT_BRAND);
        lblBrand.setForeground(Color.WHITE);
        leftPanel.add(lblBrand, BorderLayout.CENTER);

        JLabel lblSlogan = new JLabel("Experience the Magic of Gaming", SwingConstants.CENTER);
        lblSlogan.setFont(com.netcafe.ui.ThemeConfig.FONT_SLOGAN);
        lblSlogan.setForeground(com.netcafe.ui.ThemeConfig.TEXT_SLOGAN);
        lblSlogan.setBorder(BorderFactory.createEmptyBorder(0, 0, 100, 0));
        leftPanel.add(lblSlogan, BorderLayout.SOUTH);

        mainPanel.add(leftPanel);

        // Right Panel (Form)
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // Welcome Text
        JLabel lblWelcome = new JLabel("Welcome Back!");
        lblWelcome.setFont(com.netcafe.ui.ThemeConfig.FONT_HEADER);
        lblWelcome.setForeground(com.netcafe.ui.ThemeConfig.TEXT_PRIMARY);
        gbc.gridy = 0;
        formPanel.add(lblWelcome, gbc);

        JLabel lblSubtitle = new JLabel("Please enter your details to sign in.");
        lblSubtitle.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY);
        lblSubtitle.setForeground(com.netcafe.ui.ThemeConfig.TEXT_SECONDARY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 30, 0);
        formPanel.add(lblSubtitle, gbc);

        // Username
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 0, 5, 0);
        formPanel.add(new JLabel("Username"), gbc);

        txtUsername.setPreferredSize(new Dimension(300, 40));
        txtUsername.putClientProperty("JTextField.placeholderText", "Enter your username");
        gbc.gridy = 3;
        formPanel.add(txtUsername, gbc);

        // Password
        gbc.gridy = 4;
        gbc.insets = new Insets(15, 0, 5, 0);
        formPanel.add(new JLabel("Password"), gbc);

        txtPassword.setPreferredSize(new Dimension(300, 40));
        txtPassword.putClientProperty("JTextField.placeholderText", "Enter your password");
        gbc.gridy = 5;
        gbc.insets = new Insets(5, 0, 30, 0);
        formPanel.add(txtPassword, gbc);

        // Button
        btnLogin.setPreferredSize(new Dimension(300, 45));
        btnLogin.setBackground(com.netcafe.ui.ThemeConfig.PRIMARY);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);
        btnLogin.putClientProperty("JButton.buttonType", "roundRect");
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        formPanel.add(btnLogin, gbc);

        rightPanel.add(formPanel);
        mainPanel.add(rightPanel);

        btnLogin.addActionListener(e -> login());
        getRootPane().setDefaultButton(btnLogin);
    }

    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            com.netcafe.util.SwingUtils.showError(this, "Please enter both username and password.");
            return;
        }

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                User user = authService.login(username, password);
                if (user.getRole() == User.Role.USER) {
                    // Check balance
                    com.netcafe.dao.AccountDAO accountDAO = new com.netcafe.dao.AccountDAO();
                    com.netcafe.model.Account account = accountDAO.findByUserId(user.getId()).orElse(null);
                    if (account == null || account.getBalance() <= 0) {
                        throw new Exception("Xin vui long nap them tien tai quay thu ngan");
                    }
                }
                return user;
            }

            @Override
            protected void done() {
                try {
                    User user = get();

                    // Auto-start session for USER role
                    if (user.getRole() == User.Role.USER) {
                        try {
                            String machineName = java.net.InetAddress.getLocalHost().getHostName();
                            new com.netcafe.service.SessionService().startSession(user.getId(), machineName);
                        } catch (Exception e) {
                            // Ignore if session already active or other minor issues,
                            // MainFrame will handle resuming.
                            System.err.println("Session auto-start warning: " + e.getMessage());
                        }
                    }

                    new MainFrame(user).setVisible(true);
                    dispose();
                } catch (Exception ex) {
                    // Extract message from ExecutionException
                    String msg = ex.getMessage();
                    if (ex instanceof java.util.concurrent.ExecutionException) {
                        msg = ex.getCause().getMessage();
                    }
                    com.netcafe.util.SwingUtils.showError(LoginFrame.this, "Login failed: " + msg);
                }
            }
        };
        worker.execute();
    }
}
