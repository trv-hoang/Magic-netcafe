package com.netcafe.ui.admin;

import com.netcafe.model.Computer;
import com.netcafe.model.MaintenanceRequest;
import com.netcafe.service.ComputerService;
import com.netcafe.ui.ThemeConfig;
import com.netcafe.util.SwingUtils;

import javax.swing.*;
import java.awt.*;

import java.util.List;

public class ComputerMapPanel extends JPanel {
    private final ComputerService computerService = new ComputerService();
    private final JPanel mapPanel;
    private final JPanel requestPanel;
    private Timer refreshTimer;

    public ComputerMapPanel() {
        setLayout(new BorderLayout());

        // 1. Map Area (Center)
        mapPanel = new JPanel(new GridLayout(4, 5, 10, 10)); // 4 rows, 5 cols for 20 PCs
        mapPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mapPanel.setBackground(Color.WHITE);
        add(new JScrollPane(mapPanel), BorderLayout.CENTER);

        // 2. Maintenance Requests (Right)
        requestPanel = new JPanel();
        requestPanel.setLayout(new BoxLayout(requestPanel, BoxLayout.Y_AXIS));
        requestPanel.setPreferredSize(new Dimension(300, 0));
        requestPanel.setBorder(BorderFactory.createTitledBorder("Maintenance Requests"));

        JScrollPane requestScroll = new JScrollPane(requestPanel);
        add(requestScroll, BorderLayout.EAST);

        // Refresh Timer
        refreshTimer = new Timer(3000, e -> loadData());
        refreshTimer.start();

        loadData();
    }

    private void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<Computer> computers;
            List<MaintenanceRequest> requests;

            @Override
            protected Void doInBackground() throws Exception {
                computers = computerService.getAllComputers();
                requests = computerService.getPendingMaintenanceRequests();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    updateMap(computers);
                    updateRequests(requests);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void updateMap(List<Computer> computers) {
        mapPanel.removeAll();
        for (Computer c : computers) {
            JButton btn = new JButton(c.getName());
            btn.setPreferredSize(new Dimension(100, 80));
            btn.setFont(ThemeConfig.FONT_BODY_BOLD);

            switch (c.getStatus()) {
                case AVAILABLE:
                    btn.setBackground(ThemeConfig.SUCCESS);
                    btn.setForeground(Color.WHITE);
                    break;
                case OCCUPIED:
                    btn.setBackground(ThemeConfig.DANGER);
                    btn.setForeground(Color.WHITE);
                    break;
                case MAINTENANCE:
                case DIRTY:
                    btn.setBackground(ThemeConfig.ACCENT);
                    btn.setForeground(Color.WHITE);
                    break;
            }

            // Context Menu
            JPopupMenu popup = new JPopupMenu();
            JMenuItem itemAvailable = new JMenuItem("Mark Available");
            itemAvailable.addActionListener(e -> updateStatus(c.getId(), Computer.Status.AVAILABLE));

            JMenuItem itemMaintenance = new JMenuItem("Mark Maintenance");
            itemMaintenance.addActionListener(e -> updateStatus(c.getId(), Computer.Status.MAINTENANCE));

            popup.add(itemAvailable);
            popup.add(itemMaintenance);

            btn.setComponentPopupMenu(popup);

            mapPanel.add(btn);
        }
        mapPanel.revalidate();
        mapPanel.repaint();
    }

    private void updateRequests(List<MaintenanceRequest> requests) {
        requestPanel.removeAll();
        for (MaintenanceRequest r : requests) {
            JPanel p = new JPanel(new BorderLayout());
            p.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            p.setMaximumSize(new Dimension(280, 80));
            p.setBackground(Color.WHITE);

            JLabel lblTitle = new JLabel(r.getComputerName() + " - " + r.getReporterName());
            lblTitle.setFont(ThemeConfig.FONT_BODY_BOLD);

            JLabel lblIssue = new JLabel("<html>" + r.getIssue() + "</html>");
            lblIssue.setFont(ThemeConfig.FONT_SMALL);

            JButton btnResolve = new JButton("Resolve");
            btnResolve.addActionListener(e -> resolveRequest(r.getId(), r.getComputerId()));

            p.add(lblTitle, BorderLayout.NORTH);
            p.add(lblIssue, BorderLayout.CENTER);
            p.add(btnResolve, BorderLayout.EAST);

            requestPanel.add(p);
            requestPanel.add(Box.createVerticalStrut(5));
        }
        requestPanel.revalidate();
        requestPanel.repaint();
    }

    private void updateStatus(int id, Computer.Status status) {
        try {
            computerService.updateComputerStatus(id, status);
            loadData();
        } catch (Exception ex) {
            SwingUtils.showError(this, "Error updating status", ex);
        }
    }

    private void resolveRequest(int reqId, int compId) {
        try {
            computerService.resolveMaintenanceRequest(reqId, compId);
            loadData();
            SwingUtils.showInfo(this, "Request resolved!");
        } catch (Exception ex) {
            SwingUtils.showError(this, "Error resolving request", ex);
        }
    }
}
