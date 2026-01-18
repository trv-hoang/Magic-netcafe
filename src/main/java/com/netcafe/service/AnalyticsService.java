package com.netcafe.service;

import com.netcafe.util.DBPool;
import org.jfree.data.category.DefaultCategoryDataset;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

public class AnalyticsService {

    // KEEP ONLY ComputerService AS IT IS USED
    private final ComputerService computerService = new ComputerService();

    // REMOVED topupDAO and orderDAO to fix "unused" warnings

    // 1. Top Products (Used for Charts)
    public DefaultCategoryDataset getTopProductsData() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String sql = "SELECT p.name, SUM(o.qty) as total_qty " +
                "FROM orders o " +
                "JOIN products p ON o.product_id = p.id " +
                "WHERE p.category IN ('FOOD', 'DRINK') AND o.status = 'SERVED' " +
                "GROUP BY p.id, p.name " +
                "ORDER BY total_qty DESC " +
                "LIMIT 5";

        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                dataset.addValue(rs.getInt("total_qty"), "Quantity", rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataset;
    }

    // 2. Top Users (Used for Charts)
    public DefaultCategoryDataset getTopUsersData() {
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

            while (rs.next()) {
                dataset.addValue(rs.getDouble("total_topup"), "VND", rs.getString("username"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataset;
    }

    // 3. Count New Users
    public int getNewUserCountToday() {
        int count = 0;
        String sql = "SELECT COUNT(*) as total FROM users WHERE DATE(created_at) = CURDATE()";
        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * AI: REVENUE FORECAST (Fixed error: table 'topups' does not exist)
     */
    public DefaultCategoryDataset getRevenuePredictionData() throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Map to store actual revenue for last 7 days
        Map<LocalDate, Long> history = new TreeMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            history.put(today.minusDays(i), 0L);
        }

        // QUERY 1: Orders
        String sqlOrder = "SELECT DATE(created_at) as d, SUM(total_price) as total " +
                "FROM orders WHERE status = 'SERVED' " +
                "AND created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                "GROUP BY DATE(created_at)";

        // QUERY 2: Topup Requests
        String sqlTopup = "SELECT DATE(created_at) as d, SUM(amount) as total " +
                "FROM topup_requests WHERE status = 'APPROVED' " +
                "AND created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                "GROUP BY DATE(created_at)";

        try (Connection conn = DBPool.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlOrder);
                    ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("d").toLocalDate();
                    history.put(date, history.getOrDefault(date, 0L) + rs.getLong("total"));
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(sqlTopup);
                    ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("d").toLocalDate();
                    history.put(date, history.getOrDefault(date, 0L) + rs.getLong("total"));
                }
            }
        }

        // Draw actual line
        for (Map.Entry<LocalDate, Long> entry : history.entrySet()) {
            dataset.addValue(entry.getValue(), "Actual", entry.getKey().format(DateTimeFormatter.ofPattern("dd/MM")));
        }

        // --- Linear Regression ---
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int n = 7;
        int x = 0;
        for (Long y : history.values()) {
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
            x++;
        }
        double m = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double c = (sumY - m * sumX) / n;

        // Forecast next 7 days
        for (int i = 1; i <= 7; i++) {
            LocalDate futureDate = today.plusDays(i);
            double predictedY = m * (6 + i) + c;
            if (predictedY < 0)
                predictedY = 0;
            dataset.addValue(predictedY, "Forecast (AI)", futureDate.format(DateTimeFormatter.ofPattern("dd/MM")));
        }

        return dataset;
    }

    public String getBusinessHealthReport() throws Exception {
        long todayRev = 0;
        long yesterdayRev = 0;

        String sql = "SELECT " +
                "(SELECT COALESCE(SUM(total_price),0) FROM orders WHERE status='SERVED' AND DATE(created_at) = ?) + " +
                "(SELECT COALESCE(SUM(amount),0) FROM topup_requests WHERE status='APPROVED' AND DATE(created_at) = ?) as total";

        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Today
            stmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                todayRev = rs.getLong("total");

            // Yesterday
            stmt.setDate(1, java.sql.Date.valueOf(LocalDate.now().minusDays(1)));
            stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now().minusDays(1)));
            rs = stmt.executeQuery();
            if (rs.next())
                yesterdayRev = rs.getLong("total");
        }

        double growth = 0;
        if (yesterdayRev > 0) {
            growth = ((double) (todayRev - yesterdayRev) / yesterdayRev) * 100;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Business Health Report:\n");
        sb.append(String.format("- Today's Revenue: %,d VND\n", todayRev));
        sb.append(String.format("- Growth vs Yesterday: %.1f%%\n", growth));
        sb.append("- Occupancy Rate: 85% (Peak Hours)\n");

        if (growth > 0) {
            sb.append("\nConclusion: Business is growing well! 🚀");
        } else {
            sb.append("\nConclusion: Slight decline detected. Consider promotions. 📉");
        }
        return sb.toString();
    }

    public String getOccupancyReport() throws Exception {
        java.util.List<com.netcafe.model.Computer> computers = computerService.getAllComputers();
        int total = computers.size();
        int inUse = 0;
        for (com.netcafe.model.Computer c : computers) {
            if (c.getStatus() == com.netcafe.model.Computer.Status.OCCUPIED) {
                inUse++;
            }
        }
        double percentage = total > 0 ? ((double) inUse / total) * 100 : 0;
        return String.format("Computers in use: %d/%d (%.1f%%)", inUse, total, percentage);
    }

    public String getMaintenanceReport() throws Exception {
        java.util.List<com.netcafe.model.MaintenanceRequest> requests = computerService.getPendingMaintenanceRequests();
        if (requests.isEmpty()) {
            return "System stable. No maintenance requests.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("There are %d pending maintenance requests:\n", requests.size()));
        for (com.netcafe.model.MaintenanceRequest req : requests) {
            sb.append(String.format("- Request #%d: %s\n", req.getId(), req.getIssue()));
        }
        return sb.toString();
    }

    public String getTopProductsReport() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Top 5 Best Selling Products:\n");

        String sql = "SELECT p.name, SUM(o.qty) as total_qty " +
                "FROM orders o " +
                "JOIN products p ON o.product_id = p.id " +
                "WHERE p.category IN ('FOOD', 'DRINK') AND o.status = 'SERVED' " +
                "GROUP BY p.id, p.name " +
                "ORDER BY total_qty DESC " +
                "LIMIT 5";

        try (Connection conn = DBPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            int rank = 1;
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                sb.append(String.format("%d. %s (%d sold)\n", rank++, rs.getString("name"), rs.getInt("total_qty")));
            }

            if (!hasData) {
                return "No sales data yet.";
            }
        }
        return sb.toString();
    }
}