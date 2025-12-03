package com.netcafe.ui.user;

import com.netcafe.model.Computer;
import com.netcafe.model.User;
import com.netcafe.service.ComputerService;
import com.netcafe.ui.ThemeConfig;
import com.netcafe.util.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ReportIssueDialog extends JDialog {
    private final ComputerService computerService = new ComputerService();
    private final User user;
    private JComboBox<String> cbComputers;
    private JTextArea txtIssue;
    private List<Computer> computers;

    public ReportIssueDialog(Window owner, User user) {
        super(owner, "Report Issue", ModalityType.APPLICATION_MODAL);
        this.user = user;

        setSize(400, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;

        form.add(new JLabel("Select Computer:"), gbc);

        cbComputers = new JComboBox<>();
        loadComputers();
        gbc.gridy = 1;
        form.add(cbComputers, gbc);

        gbc.gridy = 2;
        form.add(new JLabel("Describe Issue:"), gbc);

        txtIssue = new JTextArea(5, 20);
        txtIssue.setLineWrap(true);
        gbc.gridy = 3;
        form.add(new JScrollPane(txtIssue), gbc);

        add(form, BorderLayout.CENTER);

        JButton btnSubmit = new JButton("Submit Report");
        btnSubmit.setBackground(ThemeConfig.PRIMARY);
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.addActionListener(e -> submit());

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnSubmit);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadComputers() {
        try {
            computers = computerService.getAllComputers();
            for (Computer c : computers) {
                cbComputers.addItem(c.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void submit() {
        String issue = txtIssue.getText().trim();
        if (issue.isEmpty()) {
            SwingUtils.showError(this, "Please describe the issue.");
            return;
        }

        int idx = cbComputers.getSelectedIndex();
        if (idx == -1)
            return;

        Computer c = computers.get(idx);

        try {
            computerService.reportIssue(c.getId(), user.getId(), issue);
            SwingUtils.showInfo(this, "Issue reported successfully!");
            dispose();
        } catch (Exception ex) {
            SwingUtils.showError(this, "Error reporting issue", ex);
        }
    }
}
