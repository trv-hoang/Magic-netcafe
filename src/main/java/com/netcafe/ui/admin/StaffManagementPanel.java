package com.netcafe.ui.admin;

import com.netcafe.model.User;
import com.netcafe.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StaffManagementPanel extends JPanel {
    private final UserService userService = new UserService();
    private final DefaultTableModel staffModel = new DefaultTableModel(
            new String[] { "ID", "Username", "Full Name", "Role" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public StaffManagementPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTable table = new JTable(staffModel);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("Refresh Staff");
        JButton btnCreate = new JButton("Create New Admin");

        btnCreate.setBackground(com.netcafe.ui.ThemeConfig.PRIMARY);
        btnCreate.setForeground(Color.WHITE);
        btnCreate.putClientProperty("JButton.buttonType", "roundRect");
        btnCreate.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);

        btnPanel.add(btnRefresh);
        btnPanel.add(btnCreate);
        add(btnPanel, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadUsers(User.Role.ADMIN, staffModel));
        btnCreate.addActionListener(e -> createUser(User.Role.ADMIN, staffModel));

        loadUsers(User.Role.ADMIN, staffModel);
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
                        com.netcafe.util.SwingUtils.showInfo(StaffManagementPanel.this, "User Created!");
                        loadUsers(role, model);
                    } catch (Exception ex) {
                        com.netcafe.util.SwingUtils.showError(StaffManagementPanel.this, "Error", ex);
                    }
                }
            };
            worker.execute();
        }
    }

    private void loadUsers(User.Role role, DefaultTableModel model) {
        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                return userService.getAllUsers();
            }

            @Override
            protected void done() {
                try {
                    List<User> list = get();
                    model.setRowCount(0);
                    for (User u : list) {
                        if (u.getRole() == role) {
                            model.addRow(new Object[] { u.getId(), u.getUsername(), u.getFullName(), u.getRole() });
                        }
                    }
                } catch (Exception ex) {
                    com.netcafe.util.SwingUtils.showError(StaffManagementPanel.this, "Error loading users", ex);
                }
            }
        };
        worker.execute();
    }
}
