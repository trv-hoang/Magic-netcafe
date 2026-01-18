package com.netcafe.dao;

import com.netcafe.util.DBPool;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset; // Keep only this import
import java.sql.*;

public class StatisticsDAO {

    // 1. Revenue of last 12 months (Line Chart) - Updated with standard logic
    public DefaultCategoryDataset getMonthlyRevenue() throws SQLException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // SQL gets last 12 months, displayed as mm/yyyy
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
                dataset.addValue(rs.getDouble("revenue"), "Total Revenue", rs.getString("time_label"));
            }

            if (!hasData) {
                dataset.addValue(0, "Total Revenue", "Current");
            }
        }
        return dataset;
    }

    // 2. Top Products (Bar Chart)
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
                dataset.addValue(rs.getInt("total_qty"), "Quantity", rs.getString("name"));
            }
        }
        return dataset;
    }

    // 3. Top Users by Top-up (Bar Chart)
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
                dataset.addValue(rs.getDouble("total_topup"), "Top-up Amount", rs.getString("username"));
            }
            if (!hasData) {
                // Avoid null dataset error if no one has topped up
                // dataset.addValue(0, "Top-up Amount", "No Data");
            }
        }
        return dataset;
    }

    // 4. Revenue Structure (Pie Chart)
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

                dataset.setValue("Services (Food/Drink)", serviceRev);
                dataset.setValue("Gaming Time (Top-up)", topupRev);

                if (serviceRev == 0 && topupRev == 0) {
                    dataset.setValue("No Data", 1);
                }
            }
        }
        return dataset;
    }

    // 5. Product Category Ratio (Secondary Pie Chart - Optional)
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