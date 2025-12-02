package com.netcafe.dao;

import com.netcafe.model.TopupRequest;
import com.netcafe.util.DBPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TopupRequestDAO {

    public void create(TopupRequest request) throws SQLException {
        String sql = "INSERT INTO topup_requests (user_id, amount, status) VALUES (?, ?, ?)";
        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, request.getUserId());
            stmt.setLong(2, request.getAmount());
            stmt.setString(3, request.getStatus().name());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    request.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<TopupRequest> findAllPending() throws SQLException {
        List<TopupRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM topup_requests WHERE status = 'PENDING' ORDER BY created_at ASC";
        try (Connection conn = DBPool.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void updateStatus(Connection conn, int id, TopupRequest.Status status) throws SQLException {
        String sql = "UPDATE topup_requests SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    public Optional<TopupRequest> findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM topup_requests WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    private TopupRequest mapRow(ResultSet rs) throws SQLException {
        TopupRequest r = new TopupRequest();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setAmount(rs.getLong("amount"));
        r.setStatus(TopupRequest.Status.valueOf(rs.getString("status")));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        return r;
    }
}
