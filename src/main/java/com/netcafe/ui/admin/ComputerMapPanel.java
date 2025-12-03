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

        // Map Visualization (Center)
        mapPanel = new JPanel(null); // Absolute positioning
        mapPanel.setBackground(new Color(60, 63, 65)); // Dark floor color
        mapPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        mapPanel.setPreferredSize(new Dimension(800, 600)); // Fixed size for scrolling

        JScrollPane scrollPane = new JScrollPane(mapPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Add visual labels
        JLabel lblEntrance = new JLabel("ENTRANCE", SwingConstants.CENTER);
        lblEntrance.setFont(ThemeConfig.FONT_HEADER);
        lblEntrance.setForeground(Color.LIGHT_GRAY);
        lblEntrance.setBounds(300, 550, 200, 30);
        mapPanel.add(lblEntrance);

        JLabel lblWalkway = new JLabel("WALKWAY", SwingConstants.CENTER);
        lblWalkway.setFont(ThemeConfig.FONT_BODY_BOLD);
        lblWalkway.setForeground(Color.GRAY);
        lblWalkway.setBounds(300, 280, 200, 30); // Between row 2 (y=1) and row 3 (y=3) -> y=2 approx
        mapPanel.add(lblWalkway);

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
            btn.setToolTipText("Status: " + c.getStatus());
            btn.setFont(ThemeConfig.FONT_BODY_BOLD);

            // Calculate position (50px padding, 120px width spacing, 100px height spacing)
            int x = 50 + c.getXPos() * 120;
            int y = 50 + c.getYPos() * 100;
            btn.setBounds(x, y, 100, 80);

            updateButtonAppearance(btn, c.getStatus());

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

    private void updateButtonAppearance(JButton btn, Computer.Status status) {
        switch (status) {
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
