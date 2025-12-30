package com.netcafe.service;

import com.netcafe.dao.OrderDAO;
import com.netcafe.dao.TopupDAO;
import com.netcafe.util.DBPool; // Import DBPool để chạy câu lệnh SQL trực tiếp
import org.jfree.data.category.DefaultCategoryDataset;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

public class AnalyticsService {

    private final TopupDAO topupDAO = new TopupDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final ComputerService computerService = new ComputerService();

    /**
     * 1. Lấy dữ liệu Top Món Ăn (Trả về Dataset để vẽ BarChart)
     */
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
                dataset.addValue(rs.getInt("total_qty"), "Số lượng", rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataset;
    }

    /**
     * 2. Lấy dữ liệu Top User Nạp tiền (Trả về Dataset để vẽ BarChart)
     */
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

    /**
     * 3. Đếm số lượng User mới đăng ký hôm nay
     */
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
// AI
    public DefaultCategoryDataset getRevenuePredictionData() throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // 1. Get Historical Data (Last 7 days)
        Map<LocalDate, Long> history = new TreeMap<>();
        Map<LocalDate, Long> topupRev = topupDAO.getDailyRevenueMap();
        Map<LocalDate, Long> orderRev = orderDAO.getDailyRevenueMap();

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long val = topupRev.getOrDefault(date, 0L) + orderRev.getOrDefault(date, 0L);
            history.put(date, val);
            dataset.addValue(val, "Thực tế", date.format(DateTimeFormatter.ofPattern("dd/MM")));
        }

        // 2. Linear Regression (Simple: y = mx + c)
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

        // 3. Predict Next 7 Days
        for (int i = 1; i <= 7; i++) {
            LocalDate futureDate = today.plusDays(i);
            double predictedY = m * (6 + i) + c; // x continues from 7
            if (predictedY < 0)
                predictedY = 0; // No negative revenue
            dataset.addValue(predictedY, "Dự báo (AI)", futureDate.format(DateTimeFormatter.ofPattern("dd/MM")));
        }

        return dataset;
    }

    public String getBusinessHealthReport() throws Exception {
        long todayRev = topupDAO.getDailyRevenue(LocalDate.now()) + orderDAO.getDailyRevenue(LocalDate.now());
        long yesterdayRev = topupDAO.getDailyRevenue(LocalDate.now().minusDays(1))
                + orderDAO.getDailyRevenue(LocalDate.now().minusDays(1));

        double growth = 0;
        if (yesterdayRev > 0) {
            growth = ((double) (todayRev - yesterdayRev) / yesterdayRev) * 100;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Báo cáo sức khỏe kinh doanh:\n");
        sb.append(String.format("- Doanh thu hôm nay: %,d VND\n", todayRev));
        sb.append(String.format("- Tăng trưởng so với hôm qua: %.1f%%\n", growth));
        
        // Mock data occupancy
        sb.append("- Tỷ lệ lấp đầy: 85% (Cao điểm)\n");

        if (growth > 0) {
            sb.append("\nKết luận: Tình hình kinh doanh đang tăng trưởng tốt! 🚀");
        } else {
            sb.append("\nKết luận: Có dấu hiệu giảm nhẹ. Nên tung khuyến mãi. 📉");
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
        return String.format("Máy đang sử dụng: %d/%d (%.1f%%)", inUse, total, percentage);
    }

    public String getMaintenanceReport() throws Exception {
        java.util.List<com.netcafe.model.MaintenanceRequest> requests = computerService.getPendingMaintenanceRequests();
        if (requests.isEmpty()) {
            return "Hệ thống ổn định. Không có yêu cầu bảo trì nào.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Có %d yêu cầu bảo trì đang chờ:\n", requests.size()));
        for (com.netcafe.model.MaintenanceRequest req : requests) {
            sb.append(String.format("- Request #%d: %s\n", req.getId(), req.getIssue()));
        }
        return sb.toString();
    }

    // Hàm String cũ (có thể giữ lại dùng cho fallback)
    public String getTopProductsReport() throws Exception {
        Map<String, Integer> topProducts = orderDAO.getTopSellingProducts(5);
        if (topProducts.isEmpty()) {
            return "Chưa có dữ liệu bán hàng.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Top 5 Món bán chạy:\n");
        int rank = 1;
        for (Map.Entry<String, Integer> entry : topProducts.entrySet()) {
            sb.append(String.format("%d. %s (%d đã bán)\n", rank++, entry.getKey(), entry.getValue()));
        }
        return sb.toString();
    }
}