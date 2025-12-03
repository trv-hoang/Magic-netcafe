package com.netcafe.ui.admin;

import com.netcafe.model.TopupRequest;
import com.netcafe.service.BillingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TopupRequestPanel extends JPanel {
    private final BillingService billingService = new BillingService();
    private final DefaultTableModel topupModel = new DefaultTableModel(
            new String[] { "ID", "User ID", "Amount", "Status" }, 0);

    public TopupRequestPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTable table = new JTable(topupModel);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnApprove = new JButton("Approve Selected");

        btnApprove.setBackground(com.netcafe.ui.ThemeConfig.SUCCESS);
        btnApprove.setForeground(Color.WHITE);
        btnApprove.putClientProperty("JButton.buttonType", "roundRect");
        btnApprove.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);

        btnRefresh.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);

        btnPanel.add(btnRefresh);
        btnPanel.add(btnApprove);
        add(btnPanel, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadTopupRequests());
        btnApprove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1)
                approveTopup((int) topupModel.getValueAt(row, 0));
        });

        loadTopupRequests();
    }

    private void loadTopupRequests() {
        SwingWorker<List<TopupRequest>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TopupRequest> doInBackground() throws Exception {
                return billingService.getPendingTopupRequests();
            }

            @Override
            protected void done() {
                try {
                    List<TopupRequest> list = get();
                    topupModel.setRowCount(0);
                    for (TopupRequest r : list)
                        topupModel.addRow(new Object[] { r.getId(), r.getUserId(), r.getAmount(), r.getStatus() });
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(TopupRequestPanel.this, "Error loading topups: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void approveTopup(int id) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                billingService.approveTopup(id);
                return null;
            }

            @Override
            protected void done() {
                loadTopupRequests();
                JOptionPane.showMessageDialog(TopupRequestPanel.this, "Approved!");
            }
        };
        worker.execute();
    }
}
