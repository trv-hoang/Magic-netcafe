package com.netcafe.dao;

import com.netcafe.util.DBPool;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset; // Chỉ giữ lại 1 dòng này
import java.sql.*;

public class StatisticsDAO {

    // 1. Doanh thu 12 tháng gần nhất (Line Chart) - Đã cập nhật logic chuẩn
    public DefaultCategoryDataset getMonthlyRevenue() throws SQLException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // SQL lấy 12 tháng gần nhất, hiển thị dạng mm/yyyy
        String sql = "SELECT DATE_FORMAT(created_at, '%m/%Y') as time_label, " +
                "       YEAR(created_at) as y, " +
                "       MONTH(created_at) as m, " +
                "       SUM(total) as revenue " +
                "FROM (" +
                "  SELECT created_at, total_price as total FROM orders WHERE status = 'SERVED' " +
                "  UNION ALL " +
                "  SELECT created_at, amount as total FROM topup_requests WHERE status = 'APPROVED' " +
                ") as combined_table " +
                "WHERE created_at >= DATE_SUB(NOW(), INTERVAL 12 MONTH) " +
                "GROUP BY y, m, time_label " +
                "ORDER BY y ASC, m ASC";

        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                dataset.addValue(rs.getDouble("revenue"), "Tổng Doanh Thu", rs.getString("time_label"));
            }

            if (!hasData) {
                dataset.addValue(0, "Tổng Doanh Thu", "Hiện tại");
            }
        }
        return dataset;
    }

    // 2. Top Món ăn (Bar Chart)
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

    // 3. Top User Nạp tiền (Bar Chart)
    public DefaultCategoryDataset getTopSpenders() throws SQLException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

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
                // Tránh lỗi null dataset nếu chưa có ai nạp
                // dataset.addValue(0, "Số tiền nạp", "Chưa có dữ liệu");
            }
        }
        return dataset;
    }

    // 4. Cơ cấu Doanh thu (Pie Chart)
    public DefaultPieDataset<String> getRevenueStructure() throws SQLException {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        String sql = "SELECT " +
                "  (SELECT COALESCE(SUM(total_price), 0) FROM orders WHERE status = 'SERVED') as service_revenue, " +
                "  (SELECT COALESCE(SUM(amount), 0) FROM topup_requests WHERE status = 'APPROVED') as topup_revenue";

        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                double serviceRev = rs.getDouble("service_revenue");
                double topupRev = rs.getDouble("topup_revenue");

                dataset.setValue("Dịch vụ (Đồ ăn/Uống)", serviceRev);
                dataset.setValue("Giờ chơi (Nạp tiền)", topupRev);

                if (serviceRev == 0 && topupRev == 0) {
                    dataset.setValue("Chưa có dữ liệu", 1);
                }
            }
        }
        return dataset;
    }

    // 5. Tỷ lệ sản phẩm (Pie Chart phụ - Optional)
    public DefaultPieDataset<String> getProductCategoryRatio() throws SQLException {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        String sql = "SELECT p.category, SUM(o.qty) as total_qty " +
                "FROM orders o " +
                "JOIN products p ON o.product_id = p.id " +
                "WHERE o.status = 'SERVED' " +
                "GROUP BY p.category";

        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                dataset.setValue(rs.getString("category"), rs.getInt("total_qty"));
            }
        }
        return dataset;
    }
}