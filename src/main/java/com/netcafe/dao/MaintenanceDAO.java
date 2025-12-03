package com.netcafe.dao;

import com.netcafe.model.MaintenanceRequest;
import com.netcafe.util.DBPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceDAO {

    public void create(MaintenanceRequest request) throws SQLException {
        String sql = "INSERT INTO maintenance_requests (computer_id, user_id, issue, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, request.getComputerId());
            stmt.setInt(2, request.getUserId());
            stmt.setString(3, request.getIssue());
            stmt.setString(4, request.getStatus().name());
            stmt.executeUpdate();
        }
    }

    public List<MaintenanceRequest> findAllPending() throws SQLException {
        List<MaintenanceRequest> list = new ArrayList<>();
        String sql = "SELECT m.*, c.name as computer_name, u.username as reporter_name " +
                "FROM maintenance_requests m " +
                "JOIN computers c ON m.computer_id = c.id " +
                "JOIN users u ON m.user_id = u.id " +
                "WHERE m.status = 'PENDING' " +
                "ORDER BY m.created_at DESC";
        try (Connection conn = DBPool.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void updateStatus(int id, MaintenanceRequest.Status status) throws SQLException {
        String sql = "UPDATE maintenance_requests SET status = ? WHERE id = ?";
        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    private MaintenanceRequest mapRow(ResultSet rs) throws SQLException {
        MaintenanceRequest r = new MaintenanceRequest();
        r.setId(rs.getInt("id"));
        r.setComputerId(rs.getInt("computer_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setIssue(rs.getString("issue"));
        r.setStatus(MaintenanceRequest.Status.valueOf(rs.getString("status")));
        r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        // Helper fields
        r.setComputerName(rs.getString("computer_name"));
        r.setReporterName(rs.getString("reporter_name"));

        return r;
    }
}
