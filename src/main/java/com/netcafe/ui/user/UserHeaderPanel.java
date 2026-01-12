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
    private final JLabel lblStatus = new JLabel("Status: ACTIVE");

    private Timer mainTimer;
    private Session currentSession;
    private int ratePerHour;
    private int secondsSinceLastDeduct = 0;
    private long displayBalance = 0; // Local tracking for real-time display

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
        startSession(); // Auto-start session when user logs in
    }

    private void initUI() {
        // Left: Welcome & Status
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        leftPanel.setBackground(ThemeConfig.BG_PANEL);

        JLabel lblWelcome = new JLabel("Welcome, " + user.getFullName());
        lblWelcome.setFont(ThemeConfig.FONT_HEADER);
        lblWelcome.setForeground(ThemeConfig.TEXT_PRIMARY);

        lblStatus.setFont(ThemeConfig.FONT_BODY);
        lblStatus.setForeground(new Color(46, 204, 113)); // Green for active

        leftPanel.add(lblWelcome);
        leftPanel.add(lblStatus);
        add(leftPanel, BorderLayout.WEST);

        // Center: Balance & Time
        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 0, 2));
        centerPanel.setBackground(ThemeConfig.BG_PANEL);

        lblBalance.setFont(ThemeConfig.FONT_HEADER);
        lblBalance.setForeground(ThemeConfig.TEXT_PRIMARY);

        lblTimeRemaining.setFont(ThemeConfig.FONT_BODY_BOLD);
        lblTimeRemaining.setForeground(ThemeConfig.TEXT_PRIMARY);

        lblPoints.setFont(ThemeConfig.FONT_SMALL);
        lblPoints.setForeground(ThemeConfig.TEXT_SECONDARY);

        lblTier.setFont(ThemeConfig.FONT_SMALL);
        lblTier.setForeground(new Color(205, 127, 50)); // Bronze default

        centerPanel.add(lblBalance);
        centerPanel.add(lblTimeRemaining);
        centerPanel.add(lblPoints);
        centerPanel.add(lblTier);
        add(centerPanel, BorderLayout.CENTER);

        // Right: Buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(ThemeConfig.BG_PANEL);

        // AI Button
        JButton btnAI = new JButton("🤖 AI");
        btnAI.putClientProperty("JButton.buttonType", "roundRect");
        btnAI.setBackground(ThemeConfig.PRIMARY);
        btnAI.setForeground(Color.WHITE);
        btnAI.setFont(ThemeConfig.FONT_SMALL);
        btnAI.setMargin(new Insets(4, 12, 4, 12));
        btnAI.setFocusPainted(false);
        btnAI.addActionListener(e -> openAIChat());

        // Logout button
        JButton btnLogout = new JButton("Logout");
        btnLogout.putClientProperty("JButton.buttonType", "roundRect");
        btnLogout.setBackground(ThemeConfig.DANGER);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(ThemeConfig.FONT_SMALL);
        btnLogout.setMargin(new Insets(4, 12, 4, 12));
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> logout());

        rightPanel.add(btnAI);
        rightPanel.add(btnLogout);
        add(rightPanel, BorderLayout.EAST);
    }

    private void openAIChat() {
        JDialog chatDialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "AI Assistant", false);
        chatDialog.setContentPane(new ChatPanel(user));
        chatDialog.setSize(400, 500);
        chatDialog.setLocationRelativeTo(this);
        chatDialog.setVisible(true);
    }

    private void logout() {
        java.awt.Window parentWindow = SwingUtilities.getWindowAncestor(this);
        if (SwingUtils.showConfirm(parentWindow, "Are you sure you want to logout?")) {
            stopSession();
        }
    }

    private void startSession() {
        // Check for existing session or create new one
        new SwingWorker<Session, Void>() {
            @Override
            protected Session doInBackground() throws Exception {
                Session existing = sessionService.getActiveSession(user.getId()).orElse(null);
                if (existing != null) {
                    return existing;
                }
                // Create new session
                String machineName = "PC-" + String.format("%02d", (int) (Math.random() * 20 + 1));
                return sessionService.startSession(user.getId(), machineName);
            }

            @Override
            protected void done() {
                try {
                    currentSession = get();
                    lblStatus.setText("Status: ACTIVE on " + currentSession.getMachineName());
                    refreshBalanceAndStart();
                } catch (Exception ex) {
                    SwingUtils.showError(UserHeaderPanel.this, "Error starting session", ex);
                }
            }
        }.execute();
    }

    private void refreshBalanceAndStart() {
        // Get initial balance and start timer
        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() throws Exception {
                return billingService.getBalance(user.getId());
            }

            @Override
            protected void done() {
                try {
                    long balance = get();
                    updateDisplay(balance);
                    refreshPointsAndTier();
                    startMainTimer();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void startMainTimer() {
        if (mainTimer != null && mainTimer.isRunning())
            return;

        mainTimer = new Timer(1000, e -> {
            if (currentSession == null)
                return;

            secondsSinceLastDeduct++;

            // Deduct 1 second worth from local display balance
            long perSecondCost = ratePerHour / 3600;
            displayBalance = Math.max(0, displayBalance - perSecondCost);

            // Update display - time remaining is calculated from balance
            long timeRemainingSeconds = (displayBalance * 3600) / ratePerHour;
            lblBalance.setText("Balance: " + String.format("%,d VND", displayBalance));
            lblTimeRemaining.setText("Time Remaining: " + TimeUtil.formatDuration(timeRemainingSeconds));

            if (timeRemainingSeconds < 900) {
                lblTimeRemaining.setForeground(new Color(231, 76, 60));
            } else {
                lblTimeRemaining.setForeground(ThemeConfig.TEXT_PRIMARY);
            }

            // Sync with DB every 5 seconds
            if (secondsSinceLastDeduct >= 5) {
                secondsSinceLastDeduct = 0;
                deductAndRefresh();
            }

            // Check if time/balance is up
            if (displayBalance <= 0) {
                stopSession();
                SwingUtils.showInfo(UserHeaderPanel.this, "Time is up! Please top up.");
            }
        });
        mainTimer.start();
    }

    private void deductAndRefresh() {
        // Deduct 5 seconds worth of usage from DB
        final long amountToDeduct = (long) (5 * ratePerHour / 3600.0);

        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() throws Exception {
                // Deduct balance in DB
                if (amountToDeduct > 0) {
                    billingService.deductBalance(user.getId(), amountToDeduct);
                }
                // Update session time
                if (currentSession != null) {
                    sessionService.updateConsumedTime(currentSession.getId(), currentSession.getTimeConsumedSeconds());
                }
                // Return new balance from DB
                return billingService.getBalance(user.getId());
            }

            @Override
            protected void done() {
                try {
                    long dbBalance = get();
                    // Only sync if DB balance is HIGHER than local (admin top-up)
                    // Otherwise, continue smooth local countdown
                    if (dbBalance > displayBalance) {
                        displayBalance = dbBalance;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void updateDisplay(long balance) {
        displayBalance = balance; // Sync local tracking
        lblBalance.setText("Balance: " + String.format("%,d VND", balance));

        // Calculate time remaining
        long totalSecondsAvailable = (balance * 3600) / ratePerHour;
        lblTimeRemaining.setText("Time Remaining: " + TimeUtil.formatDuration(totalSecondsAvailable));

        // Color based on time
        if (totalSecondsAvailable < 900) { // Less than 15 mins
            lblTimeRemaining.setForeground(new Color(231, 76, 60));
        } else {
            lblTimeRemaining.setForeground(ThemeConfig.TEXT_PRIMARY);
        }
    }

    private void refreshPointsAndTier() {
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

                    // Calculate tier
                    User.Tier tier;
                    if (points >= 5000)
                        tier = User.Tier.GOLD;
                    else if (points >= 1000)
                        tier = User.Tier.SILVER;
                    else
                        tier = User.Tier.BRONZE;

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
    }

    // Public method for external callers (CartPanel, TopupPanel) to refresh balance
    public void refreshBalance() {
        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() throws Exception {
                return billingService.getBalance(user.getId());
            }

            @Override
            protected void done() {
                try {
                    long balance = get();
                    updateDisplay(balance);
                    refreshPointsAndTier();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    public void stopSession() {
        if (mainTimer != null)
            mainTimer.stop();

        if (currentSession == null) {
            onLogout.run();
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                sessionService.endSession(currentSession.getId(), user.getId(),
                        currentSession.getTimeConsumedSeconds());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    onLogout.run();
                } catch (Exception ex) {
                    SwingUtils.showError(UserHeaderPanel.this, "Error stopping session", ex);
                    onLogout.run();
                }
            }
        }.execute();
    }
}
