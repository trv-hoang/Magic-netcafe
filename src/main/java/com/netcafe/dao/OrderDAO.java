package com.netcafe.dao;

import com.netcafe.model.Order;
import com.netcafe.util.DBPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public void create(Connection conn, Order order) throws SQLException {
        String sql = "INSERT INTO orders (user_id, product_id, qty, total_price, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, order.getUserId());
            stmt.setInt(2, order.getProductId());
            stmt.setInt(3, order.getQty());
            stmt.setLong(4, order.getTotalPrice());
            stmt.setString(5, "PENDING"); // Default
            stmt.executeUpdate();
        }
    }

    public List<Order> findAllPending() throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE status = 'PENDING' ORDER BY created_at ASC";
        try (Connection conn = DBPool.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Order> findAll() throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders";
        try (Connection conn = DBPool.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void updateStatus(int orderId, String status) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        }
    }

    public void delete(int orderId) throws SQLException {
        String sql = "DELETE FROM orders WHERE id = ?";
        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.executeUpdate();
        }
    }

    public long getDailyRevenue(java.time.LocalDate date) throws SQLException {
        String sql = "SELECT SUM(total_price) FROM orders WHERE status = 'SERVED' AND DATE(created_at) = ?";
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
        String sql = "SELECT SUM(total_price) FROM orders WHERE status = 'SERVED' AND YEAR(created_at) = ? AND MONTH(created_at) = ?";
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

    public java.util.Map<java.time.YearMonth, Long> getMonthlyRevenueMap() throws SQLException {
        java.util.Map<java.time.YearMonth, Long> map = new java.util.HashMap<>();
        String sql = "SELECT YEAR(created_at) as y, MONTH(created_at) as m, SUM(total_price) as total " +
                "FROM orders WHERE status = 'SERVED' " +
                "GROUP BY YEAR(created_at), MONTH(created_at)";
        try (Connection conn = DBPool.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                java.time.YearMonth ym = java.time.YearMonth.of(rs.getInt("y"), rs.getInt("m"));
                map.put(ym, rs.getLong("total"));
            }
        }
        return map;
    }

    public java.util.Map<java.time.LocalDate, Long> getDailyRevenueMap() throws SQLException {
        java.util.Map<java.time.LocalDate, Long> map = new java.util.HashMap<>();
        String sql = "SELECT DATE(created_at) as d, SUM(total_price) as total " +
                "FROM orders WHERE status = 'SERVED' " +
                "GROUP BY DATE(created_at)";
        try (Connection conn = DBPool.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getDate("d").toLocalDate(), rs.getLong("total"));
            }
        }
        return map;
    }

    public java.util.Map<String, Integer> getTopSellingProducts(int limit) throws SQLException {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT p.name, SUM(o.qty) as total_qty " +
                "FROM orders o JOIN products p ON o.product_id = p.id " +
                "WHERE o.status = 'SERVED' " +
                "GROUP BY p.name " +
                "ORDER BY total_qty DESC LIMIT ?";
        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("name"), rs.getInt("total_qty"));
                }
            }
        }
        return map;
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setUserId(rs.getInt("user_id"));
        o.setProductId(rs.getInt("product_id"));
        o.setQty(rs.getInt("qty"));
        o.setTotalPrice(rs.getLong("total_price"));
        o.setStatus(rs.getString("status"));
        o.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return o;
    }
}
