package com.netcafe.ui.admin;

import com.netcafe.model.User;

import javax.swing.*;
import java.awt.*;

public class UserDialog extends JDialog {
    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JTextField txtFullName = new JTextField(20);
    private final JComboBox<User.Role> cbRole = new JComboBox<>(User.Role.values());
    private boolean succeeded;

    public UserDialog(Frame parent) {
        super(parent, "Create New User", true);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.HORIZONTAL;
        cs.insets = new Insets(10, 10, 10, 10);

        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(new JLabel("Username: "), cs);

        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(txtUsername, cs);

        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(new JLabel("Password: "), cs);

        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(txtPassword, cs);

        cs.gridx = 0;
        cs.gridy = 2;
        cs.gridwidth = 1;
        panel.add(new JLabel("Full Name: "), cs);

        cs.gridx = 1;
        cs.gridy = 2;
        cs.gridwidth = 2;
        panel.add(txtFullName, cs);

        cs.gridx = 0;
        cs.gridy = 3;
        cs.gridwidth = 1;
        panel.add(new JLabel("Role: "), cs);

        cs.gridx = 1;
        cs.gridy = 3;
        cs.gridwidth = 2;
        panel.add(cbRole, cs);

        JButton btnLogin = new JButton("Create");
        btnLogin.setBackground(new Color(52, 152, 219));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.putClientProperty("JButton.buttonType", "roundRect");

        btnLogin.addActionListener(e -> {
            if (getUsername().trim().isEmpty() || getPassword().trim().isEmpty()) {
                JOptionPane.showMessageDialog(UserDialog.this,
                        "Username and Password are required",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            succeeded = true;
            dispose();
        });
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> {
            succeeded = false;
            dispose();
        });

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        bp.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        bp.add(btnLogin);
        bp.add(btnCancel);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    public String getUsername() {
        return txtUsername.getText().trim();
    }

    public String getPassword() {
        return new String(txtPassword.getPassword());
    }

    public String getFullName() {
        return txtFullName.getText().trim();
    }

    public User.Role getRole() {
        return (User.Role) cbRole.getSelectedItem();
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}
