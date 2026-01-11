package com.netcafe.ui.user;

import com.netcafe.config.AppConfig;
import com.netcafe.model.Session;
import com.netcafe.model.User;
import com.netcafe.service.BillingService;
import com.netcafe.service.SessionService;
import com.netcafe.util.SwingUtils;
import com.netcafe.util.TimeUtil;
import com.netcafe.ui.ThemeConfig;

import javax.swing.*;
import java.awt.*;

public class UserHeaderPanel extends JPanel {
    private final User user;
    private final SessionService sessionService = new SessionService();
    private final BillingService billingService = new BillingService();
    private final Runnable onLogout;

    private final JLabel lblBalance = new JLabel("Balance: Loading...");
    private final JLabel lblPoints = new JLabel("Points: --");
    private final JLabel lblTier = new JLabel("Tier: --");
    private final JLabel lblTimeRemaining = new JLabel("Time Remaining: --:--:--");
    private final JLabel lblStatus = new JLabel("Status: IDLE");

    private Timer timer;
    private Session currentSession;
    private long currentBalance;
    private int ratePerHour;

    public UserHeaderPanel(User user, Runnable onLogout) {
        this.user = user;
        this.onLogout = onLogout;
        this.ratePerHour = AppConfig.getInt("rate.vnd_per_hour", 10000);

        setLayout(new BorderLayout(20, 0));
        setBackground(ThemeConfig.BG_PANEL);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)));

        initUI();
        refreshBalance();
        checkActiveSession();

        // Balance refresh timer - runs every 5 seconds
        Timer balanceTimer = new Timer(5000, e -> refreshBalance());
        balanceTimer.start();
    }

    private void initUI() {
        // Left: Welcome & Status
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        leftPanel.setBackground(ThemeConfig.BG_PANEL);

        JLabel lblWelcome = new JLabel("Welcome, " + user.getFullName());
        lblWelcome.setFont(ThemeConfig.FONT_HEADER);
        lblWelcome.setForeground(ThemeConfig.TEXT_PRIMARY);

        lblStatus.setFont(ThemeConfig.FONT_BODY);
        lblStatus.setForeground(ThemeConfig.TEXT_SECONDARY);

        leftPanel.add(lblWelcome);
        leftPanel.add(lblStatus);
        add(leftPanel, BorderLayout.WEST);

        // Center: Balance & Time
        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 0, 2));
        centerPanel.setBackground(ThemeConfig.BG_PANEL);

        lblBalance.setFont(ThemeConfig.FONT_SUBHEADER);
        lblBalance.setForeground(ThemeConfig.SUCCESS);

        lblPoints.setFont(ThemeConfig.FONT_BODY_BOLD);
        lblPoints.setForeground(ThemeConfig.ACCENT);

        lblTier.setFont(ThemeConfig.FONT_BODY_BOLD);
        lblTier.setForeground(new Color(255, 215, 0)); // Gold color default

        lblTimeRemaining.setFont(ThemeConfig.FONT_MONO);
        lblTimeRemaining.setForeground(ThemeConfig.TEXT_PRIMARY);

        centerPanel.add(lblBalance);
        centerPanel.add(lblPoints);
        centerPanel.add(lblTier);
        centerPanel.add(lblTimeRemaining);
        add(centerPanel, BorderLayout.CENTER);

        // Right: Logout Button
        JButton btnLogout = new JButton("Logout");
        btnLogout.putClientProperty("JButton.buttonType", "roundRect");
        btnLogout.setBackground(ThemeConfig.DANGER);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(ThemeConfig.FONT_SMALL);
        btnLogout.setMargin(new Insets(4, 12, 4, 12));
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> logout());

        JButton btnReport = new JButton("Report Issue");
        btnReport.putClientProperty("JButton.buttonType", "roundRect");
        btnReport.setBackground(ThemeConfig.ACCENT);
        btnReport.setForeground(Color.WHITE);
        btnReport.setFont(ThemeConfig.FONT_SMALL);
        btnReport.setMargin(new Insets(4, 12, 4, 12));
        btnReport.setFocusPainted(false);
        btnReport.addActionListener(
                e -> new ReportIssueDialog(SwingUtilities.getWindowAncestor(this), user).setVisible(true));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(ThemeConfig.BG_PANEL);
        rightPanel.add(btnReport);
        rightPanel.add(btnLogout);

        add(rightPanel, BorderLayout.EAST);
    }

    private void logout() {
        // Center dialog on the whole app window, not just header
        java.awt.Window parentWindow = SwingUtilities.getWindowAncestor(this);
        if (SwingUtils.showConfirm(parentWindow, "Are you sure you want to logout?")) {
            stopSession();
        }
    }

    public void refreshBalance() {
        SwingWorker<Long, Void> worker = new SwingWorker<>() {
            @Override
            protected Long doInBackground() throws Exception {
                return billingService.getBalance(user.getId());
            }

            @Override
            protected void done() {
                try {
                    currentBalance = get();
                    lblBalance.setText("Balance: " + String.format("%,d VND", currentBalance));

                    // Refresh points
                    new SwingWorker<Integer, Void>() {
                        @Override
                        protected Integer doInBackground() throws Exception {
                            return billingService.getPoints(user.getId());
                        }

                        @Override
                        protected void done() {
                            try {
                                int points = get();
                                lblPoints.setText("Points: " + points);

                                // Update Tier Display
                                com.netcafe.model.User.Tier tier = user.getTier();
                                // We might need to refresh user from DB to get latest tier if it changed
                                // For now, let's assume it's static or we fetch it.
                                // Actually, points update might change tier, so we should ideally re-fetch
                                // user.
                                // But for simplicity, let's just use what we have or do a quick check.
                                if (points >= 5000)
                                    tier = com.netcafe.model.User.Tier.GOLD;
                                else if (points >= 1000)
                                    tier = com.netcafe.model.User.Tier.SILVER;
                                else
                                    tier = com.netcafe.model.User.Tier.BRONZE;

                                lblTier.setText("Tier: " + tier);
                                switch (tier) {
                                    case GOLD:
                                        lblTier.setForeground(new Color(255, 215, 0));
                                        break;
                                    case SILVER:
                                        lblTier.setForeground(new Color(192, 192, 192));
                                        break;
                                    case BRONZE:
                                        lblTier.setForeground(new Color(205, 127, 50));
                                        break;
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }.execute();
                } catch (Exception ex) {
                    System.err.println("Balance refresh error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void checkActiveSession() {
        SwingWorker<Session, Void> worker = new SwingWorker<>() {
            @Override
            protected Session doInBackground() throws Exception {
                return sessionService.getActiveSession(user.getId()).orElse(null);
            }

            @Override
            protected void done() {
                try {
                    Session session = get();
                    if (session != null) {
                        resumeSession(session);
                    }
                } catch (Exception ex) {
                    System.err.println("Session check error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void resumeSession(Session session) {
        this.currentSession = session;
        lblStatus.setText("Status: ACTIVE on " + session.getMachineName());
        startTimer();
    }

    private void startTimer() {
        if (timer != null && timer.isRunning())
            return;

        timer = new Timer(1000, e -> {
            if (currentSession == null)
                return;

            currentSession.setTimeConsumedSeconds(currentSession.getTimeConsumedSeconds() + 1);

            // Calc remaining
            long totalSecondsAvailable = (currentBalance * 3600) / ratePerHour;
            long remaining = totalSecondsAvailable - currentSession.getTimeConsumedSeconds();

            if (remaining <= 0) {
                stopSession();
                SwingUtils.showInfo(this, "Time is up!");
            } else {
                lblTimeRemaining.setText("Time Remaining: " + TimeUtil.formatDuration(remaining));
                // Red if under 15 mins (900 seconds)
                if (remaining < 900) {
                    lblTimeRemaining.setForeground(new Color(231, 76, 60));
                } else {
                    lblTimeRemaining.setForeground(ThemeConfig.TEXT_PRIMARY);
                }

                // Update displayed balance every 5 seconds based on time consumed
                if (currentSession.getTimeConsumedSeconds() % 5 == 0) {
                    long consumedAmount = (long) (currentSession.getTimeConsumedSeconds() * ratePerHour / 3600.0);
                    long displayBalance = Math.max(0, currentBalance - consumedAmount);
                    lblBalance.setText("Balance: " + String.format("%,d VND", displayBalance));
                }
            }

            // Persist every 60s
            if (currentSession.getTimeConsumedSeconds() % 60 == 0) {
                persistSession();
            }
        });
        timer.start();
    }

    private void persistSession() {
        if (currentSession == null)
            return;
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                sessionService.updateConsumedTime(currentSession.getId(), currentSession.getTimeConsumedSeconds());
                return null;
            }
        };
        worker.execute();
    }

    public void stopSession() {
        if (timer != null)
            timer.stop();
        if (currentSession == null) {
            onLogout.run();
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                sessionService.endSession(currentSession.getId(), user.getId(),
                        currentSession.getTimeConsumedSeconds());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    onLogout.run();
                } catch (Exception ex) {
                    SwingUtils.showError(UserHeaderPanel.this, "Error stopping session", ex);
                    onLogout.run();
                }
            }
        };
        worker.execute();
    }
}
