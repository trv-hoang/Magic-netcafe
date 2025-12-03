package com.netcafe.dao;

import com.netcafe.model.Account;
import com.netcafe.util.DBPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AccountDAO {

    public void create(Account account) throws SQLException {
        try (Connection conn = DBPool.getConnection()) {
            create(conn, account);
        }
    }

    public void create(Connection conn, Account account) throws SQLException {
        String sql = "INSERT INTO accounts (user_id, balance) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, account.getUserId());
            stmt.setLong(2, account.getBalance());
            stmt.executeUpdate();
        }
    }

    public Optional<Account> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE user_id = ?";
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

    // Must be called within an existing transaction
    public Account getAccountForUpdate(Connection conn, int userId) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE user_id = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                } else {
                    throw new SQLException("Account not found for user_id: " + userId);
                }
            }
        }
    }

    // Must be called within an existing transaction
    public void updateBalance(Connection conn, int userId, long newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, newBalance);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        Account acc = new Account();
        acc.setId(rs.getInt("id"));
        acc.setUserId(rs.getInt("user_id"));
        acc.setBalance(rs.getLong("balance"));
        return acc;
    }
}
