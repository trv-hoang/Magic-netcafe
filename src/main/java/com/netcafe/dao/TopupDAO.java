package com.netcafe.dao;

import com.netcafe.model.Topup;
import com.netcafe.util.DBPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TopupDAO {

    public void create(Connection conn, Topup topup) throws SQLException {
        String sql = "INSERT INTO topups (user_id, amount, method) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, topup.getUserId());
            stmt.setLong(2, topup.getAmount());
            stmt.setString(3, topup.getMethod());
            stmt.executeUpdate();
        }
    }

    public List<Topup> findAll() throws SQLException {
        List<Topup> list = new ArrayList<>();
        String sql = "SELECT * FROM topups";
        try (Connection conn = DBPool.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public long getDailyRevenue(java.time.LocalDate date) throws SQLException {
        String sql = "SELECT SUM(amount) FROM topups WHERE DATE(created_at) = ?";
        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }

    public long getMonthlyRevenue(java.time.YearMonth month) throws SQLException {
        String sql = "SELECT SUM(amount) FROM topups WHERE YEAR(created_at) = ? AND MONTH(created_at) = ?";
        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, month.getYear());
            stmt.setInt(2, month.getMonthValue());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }

    public java.util.Map<String, Long> getTopSpenders(int limit) throws SQLException {
        java.util.Map<String, Long> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT u.username, SUM(t.amount) as total " +
                "FROM topups t JOIN users u ON t.user_id = u.id " +
                "GROUP BY u.username " +
                "ORDER BY total DESC LIMIT ?";
        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("username"), rs.getLong("total"));
                }
            }
        }
        return map;
    }

    private Topup mapRow(ResultSet rs) throws SQLException {
        Topup t = new Topup();
        t.setId(rs.getInt("id"));
        t.setUserId(rs.getInt("user_id"));
        t.setAmount(rs.getLong("amount"));
        t.setMethod(rs.getString("method"));
        t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return t;
    }
}
