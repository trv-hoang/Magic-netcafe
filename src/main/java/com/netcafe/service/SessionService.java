package com.netcafe.service;

import com.netcafe.config.AppConfig;
import com.netcafe.dao.AccountDAO;
import com.netcafe.dao.SessionDAO;
import com.netcafe.model.Account;
import com.netcafe.model.Session;
import com.netcafe.util.DBPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class SessionService {
    private final SessionDAO sessionDAO = new SessionDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final int ratePerHour = AppConfig.getInt("rate.vnd_per_hour", 5000);

    public Session startSession(int userId, String machineName) throws Exception {
        // Check if active session exists
        Optional<Session> active = sessionDAO.findActiveSessionByUserId(userId);
        if (active.isPresent()) {
            return active.get();
        }

        // Check balance (Allow GOLD users to start with <= 0)
        Optional<Account> accOpt = accountDAO.findByUserId(userId);
        long balance = accOpt.map(Account::getBalance).orElse(0L);

        // Get User Tier
        com.netcafe.model.User user = new com.netcafe.dao.UserDAO().findById(DBPool.getConnection(), userId)
                .orElseThrow(() -> new Exception("User not found"));

        if (balance <= 0 && user.getTier() != com.netcafe.model.User.Tier.GOLD) {
            throw new Exception("Insufficient balance to start session.");
        }

        int secondsPurchased;
        if (user.getTier() == com.netcafe.model.User.Tier.GOLD && balance <= 0) {
            // Post-paid: Give them a "virtual" large amount of time (e.g., 24 hours) or
            // handle differently
            // For simplicity, let's give them 24 hours if balance is 0 or negative
            secondsPurchased = 24 * 3600;
        } else {
            secondsPurchased = (int) ((balance * 3600) / ratePerHour);
            // If GOLD user has some balance but runs out, they should switch to post-paid?
            // Current logic: strict prepaid for non-GOLD.
            // Let's add buffer for GOLD users?
            if (user.getTier() == com.netcafe.model.User.Tier.GOLD) {
                secondsPurchased += 24 * 3600; // Add 24h buffer
            }
        }

        Session session = new Session();
        session.setUserId(userId);
        session.setStartTime(LocalDateTime.now());
        session.setTimePurchasedSeconds(secondsPurchased);
        session.setTimeConsumedSeconds(0);
        session.setStatus(Session.Status.ACTIVE);
        session.setMachineName(machineName);

        return sessionDAO.create(session);
    }

    public void updateConsumedTime(int sessionId, int consumedSeconds) throws SQLException {
        sessionDAO.updateConsumedTime(sessionId, consumedSeconds);
    }

    public void endSession(int sessionId, int userId, int finalConsumedSeconds) throws Exception {
        try (Connection conn = DBPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Calculate cost
                long cost = (long) Math.ceil((double) finalConsumedSeconds / 3600 * ratePerHour);

                // 2. Lock account and update balance
                Account account = accountDAO.getAccountForUpdate(conn, userId);
                long newBalance = account.getBalance() - cost;
                // Allow negative balance? Usually no, but let's assume we just deduct what was
                // consumed.
                // Or we can clamp to 0 if prepaid. But logic says we deduct exact cost.
                // For GOLD users, this will naturally go negative, which is fine.
                accountDAO.updateBalance(conn, userId, newBalance);

                // 3. Update session
                sessionDAO.endSession(conn, sessionId, Timestamp.valueOf(LocalDateTime.now()), finalConsumedSeconds);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public Optional<Session> getActiveSession(int userId) throws SQLException {
        return sessionDAO.findActiveSessionByUserId(userId);
    }
}
