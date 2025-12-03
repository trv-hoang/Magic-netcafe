package com.netcafe.dao;

import com.netcafe.model.Computer;
import com.netcafe.util.DBPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComputerDAO {

    public List<Computer> findAll() throws SQLException {
        List<Computer> list = new ArrayList<>();
        String sql = "SELECT * FROM computers ORDER BY id ASC";
        try (Connection conn = DBPool.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Optional<Computer> findById(int id) throws SQLException {
        String sql = "SELECT * FROM computers WHERE id = ?";
        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void updateStatus(int id, Computer.Status status) throws SQLException {
        String sql = "UPDATE computers SET status = ? WHERE id = ?";
        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    private Computer mapRow(ResultSet rs) throws SQLException {
        Computer c = new Computer();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setStatus(Computer.Status.valueOf(rs.getString("status")));
        c.setXPos(rs.getInt("x_pos"));
        c.setYPos(rs.getInt("y_pos"));
        return c;
    }
}
