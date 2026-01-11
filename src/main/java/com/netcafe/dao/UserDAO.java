package com.netcafe.dao;

import com.netcafe.model.User;
import com.netcafe.util.DBPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public User create(User user) throws SQLException {
        try (Connection conn = DBPool.getConnection()) {
            return create(conn, user);
        }
    }

    public User create(Connection conn, User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, full_name, role, tier) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getRole().name());
            stmt.setString(5, user.getTier().name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
        return user;
    }

    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DBPool.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, role = ? WHERE id = ?";
        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getRole().name());
            stmt.setInt(3, user.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(int userId) throws SQLException {
        try (Connection conn = DBPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Delete related records first (order matters due to foreign keys)
                // 1. orders
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM orders WHERE user_id = ?")) {
                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }
                // 3. sessions
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM sessions WHERE user_id = ?")) {
                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }
                // 4. topups
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM topups WHERE user_id = ?")) {
                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }
                // 5. topup_requests
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM topup_requests WHERE user_id = ?")) {
                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }
                // 6. messages (user can be sender or receiver)
                try (PreparedStatement stmt = conn
                        .prepareStatement("DELETE FROM messages WHERE sender_id = ? OR receiver_id = ?")) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, userId);
                    stmt.executeUpdate();
                }
                // 7. maintenance_requests
                try (PreparedStatement stmt = conn
                        .prepareStatement("DELETE FROM maintenance_requests WHERE user_id = ?")) {
                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }
                // 8. accounts
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM accounts WHERE user_id = ?")) {
                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }
                // Finally delete the user
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public Optional<User> findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
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

    public void updatePoints(Connection conn, int userId, int newPoints) throws SQLException {
        String sql = "UPDATE users SET points = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newPoints);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    public void updatePoints(int userId, int newPoints) throws SQLException {
        try (Connection conn = DBPool.getConnection()) {
            updatePoints(conn, userId, newPoints);
        }
    }

    public void updateTier(Connection conn, int userId, User.Tier newTier) throws SQLException {
        String sql = "UPDATE users SET tier = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newTier.name());
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(User.Role.valueOf(rs.getString("role")));
        user.setTier(User.Tier.valueOf(rs.getString("tier")));
        user.setPoints(rs.getInt("points"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return user;
    }
}
