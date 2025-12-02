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
        setSize(450, 300);
        setLocationRelativeTo(null);

        // Set Icon
        java.net.URL iconURL = getClass().getResource("/images/icon.jpg");
        if (iconURL != null) {
            Image icon = new ImageIcon(iconURL).getImage();
            setIconImage(icon);

            // macOS Dock Icon
            try {
                if (java.awt.Taskbar.isTaskbarSupported()) {
                    java.awt.Taskbar.getTaskbar().setIconImage(icon);
                }
            } catch (Exception e) {
                System.err.println("Taskbar icon error: " + e.getMessage());
            }
        }

        // Main Panel with padding
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        setContentPane(mainPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lblTitle = new JLabel("Magic netCafe", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTitle.setForeground(new Color(50, 100, 200));
        mainPanel.add(lblTitle, gbc);

        // Username
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(txtPassword, gbc);

        // Button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        btnLogin.setPreferredSize(new Dimension(120, 40));
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLogin.setBackground(new Color(50, 150, 250));
        btnLogin.setForeground(Color.WHITE);

        mainPanel.add(btnLogin, gbc);

        btnLogin.addActionListener(e -> login());
        getRootPane().setDefaultButton(btnLogin);
    }

    private void login() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

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
                    JOptionPane.showMessageDialog(LoginFrame.this, "Login failed: " + msg, "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
