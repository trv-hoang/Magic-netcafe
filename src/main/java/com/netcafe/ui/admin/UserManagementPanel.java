package com.netcafe.ui.admin;

import com.netcafe.model.User;
import com.netcafe.service.BillingService;
import com.netcafe.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserManagementPanel extends JPanel {
    private final UserService userService = new UserService();
    private final BillingService billingService = new BillingService();
    private final DefaultTableModel userModel = new DefaultTableModel(
            new String[] { "ID", "Username", "Full Name", "Role", "Tier", "Balance", "Points" }, 0);

    public UserManagementPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTable table = new JTable(userModel);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSetBalance = new JButton("Set Balance");
        JButton btnCreate = new JButton("Create New User");
        JButton btnRefresh = new JButton("Refresh Users");
        JButton btnDelete = new JButton("Delete Selected");

        // Create Button
        btnCreate.setBackground(com.netcafe.ui.ThemeConfig.PRIMARY);
        btnCreate.setForeground(Color.WHITE);
        btnCreate.putClientProperty("JButton.buttonType", "roundRect");
        btnCreate.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);

        // Delete Button
        btnDelete.setBackground(com.netcafe.ui.ThemeConfig.DANGER);
        btnDelete.setForeground(Color.WHITE);
        btnDelete.putClientProperty("JButton.buttonType", "roundRect");
        btnDelete.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);

        // Refresh Button
        btnRefresh.setBackground(com.netcafe.ui.ThemeConfig.PRIMARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.putClientProperty("JButton.buttonType", "roundRect");
        btnRefresh.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);

        // Set Balance Button
        btnSetBalance.setBackground(com.netcafe.ui.ThemeConfig.ACCENT); // Yellow/Orange
        btnSetBalance.setForeground(Color.WHITE);
        btnSetBalance.putClientProperty("JButton.buttonType", "roundRect");
        btnSetBalance.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);

        btnPanel.add(btnSetBalance);
        btnPanel.add(btnCreate);
        btnPanel.add(btnRefresh);
        btnPanel.add(btnDelete);

        add(btnPanel, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadUsers(User.Role.USER, userModel));
        btnCreate.addActionListener(e -> createUser(User.Role.USER, userModel));
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String username = (String) userModel.getValueAt(row, 1);
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to delete user '" + username + "'?\nThis action cannot be undone.",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteUser((int) userModel.getValueAt(row, 0));
                }
            } else {
                com.netcafe.util.SwingUtils.showError(this, "Please select a user first.");
            }
        });

        btnSetBalance.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int userId = (int) userModel.getValueAt(row, 0);
                String input = JOptionPane.showInputDialog(this, "Enter new balance (VND):");
                if (input != null && !input.trim().isEmpty()) {
                    try {
                        long newBalance = Long.parseLong(input.trim());
                        if (newBalance < 0)
                            throw new NumberFormatException();

                        SwingWorker<Void, Void> worker = new SwingWorker<>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                billingService.setBalance(userId, newBalance);
                                return null;
                            }

                            @Override
                            protected void done() {
                                try {
                                    get();
                                    com.netcafe.util.SwingUtils.showInfo(UserManagementPanel.this,
                                            "Balance updated successfully!");
                                    loadUsers(User.Role.USER, userModel); // Refresh to show new balance
                                } catch (Exception ex) {
                                    com.netcafe.util.SwingUtils.showError(UserManagementPanel.this, "Error", ex);
                                }
                            }
                        };
                        worker.execute();
                    } catch (NumberFormatException ex) {
                        com.netcafe.util.SwingUtils.showError(this, "Invalid amount. Please enter a positive number.");
                    }
                }
            } else {
                com.netcafe.util.SwingUtils.showError(this, "Please select a user first.");
            }
        });

        loadUsers(User.Role.USER, userModel);
    }

    private void createUser(User.Role role, DefaultTableModel model) {
        UserDialog dialog = new UserDialog((Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSucceeded()) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    userService.createUser(dialog.getUsername(), dialog.getPassword(), dialog.getFullName(),
                            dialog.getRole());
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        com.netcafe.util.SwingUtils.showInfo(UserManagementPanel.this, "User Created!");
                        loadUsers(role, model);
                    } catch (Exception ex) {
                        com.netcafe.util.SwingUtils.showError(UserManagementPanel.this, "Error", ex);
                    }
                }
            };
            worker.execute();
        }
    }

    private void loadUsers(User.Role role, DefaultTableModel model) {
        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            java.util.Map<Integer, Long> balances = new java.util.HashMap<>();

            @Override
            protected List<User> doInBackground() throws Exception {
                List<User> allUsers = userService.getAllUsers();
                if (role == User.Role.USER) {
                    for (User u : allUsers) {
                        if (u.getRole() == role) {
                            balances.put(u.getId(), billingService.getBalance(u.getId()));
                        }
                    }
                }
                return allUsers;
            }

            @Override
            protected void done() {
                try {
                    List<User> list = get();
                    model.setRowCount(0);
                    for (User u : list) {
                        if (u.getRole() == role) {
                            long balance = balances.getOrDefault(u.getId(), 0L);
                            model.addRow(new Object[] { u.getId(), u.getUsername(), u.getFullName(), u.getRole(),
                                    u.getTier(), balance, u.getPoints() });
                        }
                    }
                } catch (Exception ex) {
                    com.netcafe.util.SwingUtils.showError(UserManagementPanel.this, "Error loading users", ex);
                }
            }
        };
        worker.execute();
    }

    private void deleteUser(int userId) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                userService.deleteUser(userId);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // This will throw if doInBackground failed
                    loadUsers(User.Role.USER, userModel);
                    com.netcafe.util.SwingUtils.showInfo(UserManagementPanel.this, "User Deleted");
                } catch (Exception ex) {
                    com.netcafe.util.SwingUtils.showError(UserManagementPanel.this,
                            "Failed to delete user: " + ex.getCause().getMessage());
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }
}
