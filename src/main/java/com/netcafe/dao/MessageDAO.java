package com.netcafe.dao;

import com.netcafe.model.Message;
import com.netcafe.util.DBPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public void create(Message msg) throws SQLException {
        String sql = "INSERT INTO messages (sender_id, receiver_id, content) VALUES (?, ?, ?)";
        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, msg.getSenderId());
            stmt.setInt(2, msg.getReceiverId());
            stmt.setString(3, msg.getContent());
            stmt.executeUpdate();
        }
    }

    public List<Message> findAll() throws SQLException {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT * FROM messages ORDER BY created_at DESC";
        try (Connection conn = DBPool.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Message> findConversation(int userId1, int userId2) throws SQLException {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE (sender_id = ? AND receiver_id = ?) " +
                "OR (sender_id = ? AND receiver_id = ?) ORDER BY created_at ASC";
        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);
            stmt.setInt(3, userId2);
            stmt.setInt(4, userId1);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Message mapRow(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getInt("id"));
        m.setSenderId(rs.getInt("sender_id"));
        m.setReceiverId(rs.getInt("receiver_id"));
        m.setContent(rs.getString("content"));
        m.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return m;
    }
}
