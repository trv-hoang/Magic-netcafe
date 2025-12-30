package com.netcafe.dao;

import com.netcafe.util.DBPool;
import org.jfree.data.category.DefaultCategoryDataset;
import java.sql.*;

public class StatisticsDAO {

    // 1. Doanh thu tổng hợp (Line Chart sẽ dùng cái này)
    public DefaultCategoryDataset getMonthlyRevenue() throws SQLException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // Lấy doanh thu từ cả Order và Topup
        String sql = "SELECT m_month, SUM(total) as revenue FROM (" +
                     "  SELECT MONTH(created_at) as m_month, total_price as total FROM orders WHERE status = 'SERVED' " +
                     "  UNION ALL " +
                     "  SELECT MONTH(created_at) as m_month, amount as total FROM topup_requests WHERE status = 'APPROVED' " +
                     ") as combined_table " +
                     "GROUP BY m_month ORDER BY m_month ASC";

        try (Connection conn = DBPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                // Dataset cho Line Chart
                dataset.addValue(rs.getDouble("revenue"), "Tổng Doanh Thu", "Tháng " + rs.getInt("m_month"));
            }
        }
        return dataset;
    }

    // 2. Top Món ăn (Giữ nguyên)
    public DefaultCategoryDataset getTopSellingProducts() throws SQLException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String sql = "SELECT p.name, SUM(o.qty) as total_qty FROM orders o " +
                     "JOIN products p ON o.product_id = p.id " +
                     "WHERE p.category IN ('FOOD', 'DRINK') AND o.status = 'SERVED' " +
                     "GROUP BY p.id, p.name ORDER BY total_qty DESC LIMIT 5";

        try (Connection conn = DBPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                dataset.addValue(rs.getInt("total_qty"), "Số lượng", rs.getString("name"));
            }
        }
        return dataset;
    }

    // 3. Top User Nạp tiền (SỬA LẠI CHO CHUẨN BẢNG topup_requests)
    public DefaultCategoryDataset getTopSpenders() throws SQLException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Query bảng topup_requests, status APPROVED
        String sql = "SELECT u.username, SUM(t.amount) as total_topup " +
                     "FROM topup_requests t " +
                     "JOIN users u ON t.user_id = u.id " +
                     "WHERE t.status = 'APPROVED' " +
                     "GROUP BY u.id, u.username " +
                     "ORDER BY total_topup DESC " +
                     "LIMIT 5";

        try (Connection conn = DBPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                dataset.addValue(rs.getDouble("total_topup"), "Số tiền nạp", rs.getString("username"));
            }
            if (!hasData) {
                System.out.println("Warning: Không tìm thấy dữ liệu topup nào đã APPROVED!");
            }
        }
        return dataset;
    }
}