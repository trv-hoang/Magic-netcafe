package com.netcafe.dao;

import com.netcafe.model.Session;
import com.netcafe.util.DBPool;

import java.sql.*;
import java.util.Optional;

public class SessionDAO {

    public Session create(Session session) throws SQLException {
        String sql = "INSERT INTO sessions (user_id, start_time, time_purchased_seconds, time_consumed_seconds, status, machine_name) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, session.getUserId());
            stmt.setTimestamp(2, Timestamp.valueOf(session.getStartTime()));
            stmt.setInt(3, session.getTimePurchasedSeconds());
            stmt.setInt(4, session.getTimeConsumedSeconds());
            stmt.setString(5, session.getStatus().name());
            stmt.setString(6, session.getMachineName());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating session failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    session.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating session failed, no ID obtained.");
                }
            }
        }
        return session;
    }

    public void updateConsumedTime(int sessionId, int consumedSeconds) throws SQLException {
        String sql = "UPDATE sessions SET time_consumed_seconds = ? WHERE id = ?";
        try (Connection conn = DBPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, consumedSeconds);
            stmt.setInt(2, sessionId);
            stmt.executeUpdate();
        }
    }

    public void endSession(Connection conn, int sessionId, Timestamp endTime, int finalConsumedSeconds) throws SQLException {
        String sql = "UPDATE sessions SET end_time = ?, time_consumed_seconds = ?, status = 'ENDED' WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, endTime);
            stmt.setInt(2, finalConsumedSeconds);
            stmt.setInt(3, sessionId);
            stmt.executeUpdate();
        }
    }
    
    public Optional<Session> findActiveSessionByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM sessions WHERE user_id = ? AND status = 'ACTIVE' ORDER BY start_time DESC LIMIT 1";
        try (Connection conn = DBPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    private Session mapRow(ResultSet rs) throws SQLException {
        Session s = new Session();
        s.setId(rs.getInt("id"));
        s.setUserId(rs.getInt("user_id"));
        s.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        Timestamp end = rs.getTimestamp("end_time");
        if (end != null) s.setEndTime(end.toLocalDateTime());
        s.setTimePurchasedSeconds(rs.getInt("time_purchased_seconds"));
        s.setTimeConsumedSeconds(rs.getInt("time_consumed_seconds"));
        s.setStatus(Session.Status.valueOf(rs.getString("status")));
        s.setMachineName(rs.getString("machine_name"));
        return s;
    }
}
