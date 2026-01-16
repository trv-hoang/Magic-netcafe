package com.netcafe.service;

import com.netcafe.util.DBPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Random;

public class DemoService {

    public void generateSmartDemoData() {
        String sqlTopup = "INSERT INTO topup_requests (user_id, amount, status, created_at) VALUES (?, ?, 'APPROVED', ?)";
        String sqlOrder = "INSERT INTO orders (user_id, product_id, qty, total_price, status, created_at) VALUES (?, ?, ?, ?, 'SERVED', ?)";

        // Xóa dữ liệu cũ của 7 ngày gần nhất để tránh bị cộng dồn quá nhiều khi bấm nhiều lần
        cleanRecentData();

        Random rand = new Random();
        LocalDate today = LocalDate.now();

        try (Connection conn = DBPool.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // Chạy vòng lặp cho 7 ngày gần nhất (từ 6 ngày trước đến hôm nay)
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                
                // 1. Tạo giả lập Nạp tiền (Topup)
                // Mỗi ngày tạo 2-3 giao dịch nạp tiền ngẫu nhiên
                int topupCount = 2 + rand.nextInt(3); 
                try (PreparedStatement stmt = conn.prepareStatement(sqlTopup)) {
                    for (int j = 0; j < topupCount; j++) {
                        // Random user từ id 1 đến 10
                        stmt.setInt(1, 1 + rand.nextInt(10)); 
                        // Random tiền nạp: 20k, 50k, 100k, 200k, 500k
                        int[] amounts = {20000, 50000, 100000, 200000, 500000};
                        int amount = amounts[rand.nextInt(amounts.length)];
                        
                        stmt.setLong(2, amount);
                        // Set ngày giờ (Random giờ từ 8h sáng đến 22h tối)
                        stmt.setTimestamp(3, java.sql.Timestamp.valueOf(date.atTime(8 + rand.nextInt(14), rand.nextInt(59))));
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }

                // 2. Tạo giả lập Gọi món (Order)
                // Mỗi ngày tạo 3-5 đơn hàng
                int orderCount = 3 + rand.nextInt(4);
                try (PreparedStatement stmt = conn.prepareStatement(sqlOrder)) {
                    for (int j = 0; j < orderCount; j++) {
                        stmt.setInt(1, 1 + rand.nextInt(10)); // User ID
                        stmt.setInt(2, 1 + rand.nextInt(15)); // Product ID (giả sử có 15 món)
                        int qty = 1 + rand.nextInt(5); // Số lượng 1-5
                        stmt.setInt(3, qty);
                        stmt.setLong(4, qty * 20000L); // Giá đại diện
                        stmt.setTimestamp(5, java.sql.Timestamp.valueOf(date.atTime(9 + rand.nextInt(12), rand.nextInt(59))));
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
            }
            
            conn.commit(); // Lưu tất cả vào DB
            System.out.println("Đã phân tích dữ liệu thành công cho ngày: " + today);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanRecentData() {
        // Xóa dữ liệu của 7 ngày qua để làm sạch biểu đồ trước khi bơm dữ liệu mới
        String sql1 = "DELETE FROM topup_requests WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
        String sql2 = "DELETE FROM orders WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
        
        try (Connection conn = DBPool.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sql1)) { stmt.executeUpdate(); }
            try (PreparedStatement stmt = conn.prepareStatement(sql2)) { stmt.executeUpdate(); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}