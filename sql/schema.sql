-- ================================================================
-- PHẦN 1: DỌN DẸP DATABASE (DROP TABLES)
-- Xóa theo thứ tự ngược của khóa ngoại để tránh lỗi Constraint
-- ================================================================
SET FOREIGN_KEY_CHECKS = 0; -- Tắt kiểm tra khóa ngoại tạm thời để xóa cho mượt

DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS maintenance_requests;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS topup_requests;
DROP TABLE IF EXISTS sessions;
DROP TABLE IF EXISTS computers;
DROP TABLE IF EXISTS topups;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1; -- Bật lại kiểm tra khóa ngoại

-- ================================================================
-- PHẦN 2: TẠO CẤU TRÚC BẢNG (SCHEMA)
-- ================================================================

-- 1. Bảng Users (Người dùng & Admin)
CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(100),
  role ENUM('ADMIN','USER') DEFAULT 'USER',
  tier ENUM('BRONZE','SILVER','GOLD') DEFAULT 'BRONZE',
  points INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bảng Accounts (Ví tiền)
CREATE TABLE accounts (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL UNIQUE,
  balance BIGINT NOT NULL DEFAULT 0,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Bảng Products (Menu đồ ăn/uống)
CREATE TABLE products (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) UNIQUE,
  category ENUM('FOOD','DRINK','GAME_ITEM') DEFAULT 'FOOD',
  price BIGINT NOT NULL,
  stock INT DEFAULT 0
);

-- 4. Bảng Computers (Danh sách máy trạm)
CREATE TABLE computers (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE,
  status ENUM('AVAILABLE','OCCUPIED','MAINTENANCE','DIRTY') DEFAULT 'AVAILABLE',
  x_pos INT DEFAULT 0,
  y_pos INT DEFAULT 0
);

-- 5. Bảng Topup Requests (Yêu cầu nạp tiền chờ duyệt)
CREATE TABLE topup_requests (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  amount BIGINT NOT NULL,
  status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 6. Bảng Orders (Đơn gọi món)
CREATE TABLE orders (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  product_id INT NOT NULL,
  qty INT NOT NULL,
  total_price BIGINT NOT NULL,
  status ENUM('PENDING','SERVED','CANCELLED') DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (product_id) REFERENCES products(id)
);

-- 7. Bảng Sessions (Phiên chơi)
CREATE TABLE sessions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  end_time TIMESTAMP NULL,
  time_purchased_seconds INT NOT NULL,
  time_consumed_seconds INT DEFAULT 0,
  status ENUM('ACTIVE','ENDED') DEFAULT 'ACTIVE',
  machine_name VARCHAR(50),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (machine_name) REFERENCES computers(name)
);

-- 8. Bảng Messages (Tin nhắn nội bộ)
CREATE TABLE messages (
  id INT AUTO_INCREMENT PRIMARY KEY,
  sender_id INT NOT NULL,
  receiver_id INT NOT NULL,
  content TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (sender_id) REFERENCES users(id),
  FOREIGN KEY (receiver_id) REFERENCES users(id)
);

-- 9. Bảng Maintenance Requests (Báo hỏng máy)
CREATE TABLE maintenance_requests (
  id INT AUTO_INCREMENT PRIMARY KEY,
  computer_id INT NOT NULL,
  user_id INT NOT NULL,
  issue TEXT NOT NULL,
  status ENUM('PENDING','IN_PROGRESS','RESOLVED') DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (computer_id) REFERENCES computers(id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ================================================================
-- PHẦN 3: NẠP DỮ LIỆU CỐ ĐỊNH (BASE DATA)
-- ================================================================

-- 1. Tạo Admin và User mặc định
INSERT INTO users (username, password_hash, full_name, role) 
VALUES ('admin', '$2a$10$1KYugQ8nepf19Z34s3pgIe1UT.dNstJLiNXBvttOH69Co6pJ1wO.C', 'Administrator', 'ADMIN');

INSERT INTO users (username, password_hash, full_name, role)
VALUES ('user1', '$2a$10$r4YEgO5RmVBA6so/m9fKVeg2ar4lRjn6Sow3OXWGrJS7/XZBY9haa', 'Test User', 'USER');

-- 2. Tạo 10 User giả lập (Khách hàng thân thiết)
INSERT INTO users (username, password_hash, full_name, role, points, tier) VALUES 
('hacker_lor', '$2a$10$r4YEgO5RmVBA6so/m9fKVeg2ar4lRjn6Sow3OXWGrJS7/XZBY9haa', 'Hacker Mu Trang', 'USER', 500, 'GOLD'),
('yasuo_gank_tem', '$2a$10$r4YEgO5RmVBA6so/m9fKVeg2ar4lRjn6Sow3OXWGrJS7/XZBY9haa', 'Yasuo 15p GG', 'USER', 100, 'SILVER'),
('thanh_trung_sk', '$2a$10$r4YEgO5RmVBA6so/m9fKVeg2ar4lRjn6Sow3OXWGrJS7/XZBY9haa', 'Tran Thanh Trung', 'USER', 1000, 'GOLD'),
('meo_simmy_fake', '$2a$10$r4YEgO5RmVBA6so/m9fKVeg2ar4lRjn6Sow3OXWGrJS7/XZBY9haa', 'Simmy Cute', 'USER', 50, 'BRONZE'),
('tuan_tien_ti', '$2a$10$r4YEgO5RmVBA6so/m9fKVeg2ar4lRjn6Sow3OXWGrJS7/XZBY9haa', 'Pham Tuan Tu', 'USER', 2000, 'GOLD'),
('best_leesin', '$2a$10$r4YEgO5RmVBA6so/m9fKVeg2ar4lRjn6Sow3OXWGrJS7/XZBY9haa', 'Lee Sin Rung', 'USER', 20, 'BRONZE'),
('vua_tro_choi', '$2a$10$r4YEgO5RmVBA6so/m9fKVeg2ar4lRjn6Sow3OXWGrJS7/XZBY9haa', 'Yugi Oh', 'USER', 300, 'SILVER'),
('co_giao_thao', '$2a$10$r4YEgO5RmVBA6so/m9fKVeg2ar4lRjn6Sow3OXWGrJS7/XZBY9haa', 'Thao Teacher', 'USER', 150, 'SILVER'),
('boy_nha_giau', '$2a$10$r4YEgO5RmVBA6so/m9fKVeg2ar4lRjn6Sow3OXWGrJS7/XZBY9haa', 'Rich Kid 2k', 'USER', 5000, 'GOLD'),
('khach_vang_lai', '$2a$10$r4YEgO5RmVBA6so/m9fKVeg2ar4lRjn6Sow3OXWGrJS7/XZBY9haa', 'Vang Lai', 'USER', 0, 'BRONZE');

-- Tạo ví tiền cho tất cả User (Mặc định 0đ hoặc 50k)
INSERT IGNORE INTO accounts (user_id, balance) SELECT id, 0 FROM users;
UPDATE accounts SET balance = 50000 WHERE user_id = (SELECT id FROM users WHERE username='user1');

-- 3. Tạo Menu Đồ ăn / Nước uống (Full Menu)
INSERT INTO products (name, category, price, stock) VALUES 
('Sting Dau', 'DRINK', 12000, 200),
('Sting Vang', 'DRINK', 12000, 200),
('Coca Cola', 'DRINK', 10000, 200),
('Pepsi', 'DRINK', 10000, 200),
('7Up', 'DRINK', 10000, 200),
('Redbull', 'DRINK', 15000, 100),
('Number 1', 'DRINK', 12000, 100),
('Tra Xanh 0 Do', 'DRINK', 10000, 150),
('C2', 'DRINK', 8000, 150),
('Nuoc Suoi', 'DRINK', 5000, 500),
('Tra Da', 'DRINK', 2000, 1000),
('Ca Phe Den', 'DRINK', 15000, 50),
('Ca Phe Sua', 'DRINK', 20000, 50),
('Bac Xiu', 'DRINK', 25000, 50),
('My Tom Trung', 'FOOD', 15000, 100),
('My Tom Xuc Xich', 'FOOD', 20000, 100),
('My Tom Full Topping', 'FOOD', 30000, 100),
('Banh Mi Pate', 'FOOD', 15000, 50),
('Banh Mi Trung', 'FOOD', 15000, 50),
('Banh Mi Thap Cam', 'FOOD', 25000, 50),
('Com Rang Dua Bo', 'FOOD', 35000, 50),
('Com Rang Thap Cam', 'FOOD', 30000, 50),
('Com Ga Xoi Mo', 'FOOD', 40000, 50),
('Kho Ga La Chanh', 'FOOD', 20000, 100),
('Bim Bim Oishi', 'FOOD', 5000, 200);

-- 4. Tạo Máy tính (20 PC - Full Phòng)
INSERT INTO computers (name, status, x_pos, y_pos) VALUES
('MAY-01', 'AVAILABLE', 0, 0), ('MAY-02', 'AVAILABLE', 1, 0), ('MAY-03', 'AVAILABLE', 2, 0), ('MAY-04', 'AVAILABLE', 3, 0), ('MAY-05', 'AVAILABLE', 4, 0),
('MAY-06', 'OCCUPIED',  0, 1), ('MAY-07', 'AVAILABLE', 1, 1), ('MAY-08', 'AVAILABLE', 2, 1), ('MAY-09', 'AVAILABLE', 3, 1), ('MAY-10', 'AVAILABLE', 4, 1),
('MAY-11', 'AVAILABLE', 0, 2), ('MAY-12', 'MAINTENANCE', 1, 2), ('MAY-13', 'AVAILABLE', 2, 2), ('MAY-14', 'AVAILABLE', 3, 2), ('MAY-15', 'AVAILABLE', 4, 2),
('MAY-16', 'AVAILABLE', 0, 3), ('MAY-17', 'AVAILABLE', 1, 3), ('MAY-18', 'AVAILABLE', 2, 3), ('MAY-19', 'AVAILABLE', 3, 3), ('MAY-20', 'DIRTY', 4, 3);

-- ================================================================
-- PHẦN 4: GIẢ LẬP DỮ LIỆU THỐNG KÊ (12 THÁNG NĂM 2025)
-- Quan trọng: Dùng ngày cứng '2025-xx-xx' thay vì NOW() để fix lỗi chart
-- ================================================================

-- === A. NẠP TIỀN (DOANH THU GIỜ CHƠI) ===

-- Tháng 1/2025: Mới mở hàng, ít khách
INSERT INTO topup_requests (user_id, amount, status, created_at) VALUES 
((SELECT id FROM users WHERE username='yasuo_gank_tem'), 20000, 'APPROVED', '2025-01-05 09:00:00'),
((SELECT id FROM users WHERE username='khach_vang_lai'), 10000, 'APPROVED', '2025-01-10 10:00:00');

-- Tháng 2/2025: Tết Âm Lịch, nạp nhiều lì xì
INSERT INTO topup_requests (user_id, amount, status, created_at) VALUES 
((SELECT id FROM users WHERE username='boy_nha_giau'), 500000, 'APPROVED', '2025-02-10 14:00:00'), 
((SELECT id FROM users WHERE username='tuan_tien_ti'), 200000, 'APPROVED', '2025-02-12 16:00:00'),
((SELECT id FROM users WHERE username='vua_tro_choi'), 100000, 'APPROVED', '2025-02-15 09:00:00');

-- Tháng 3/2025: Ổn định
INSERT INTO topup_requests (user_id, amount, status, created_at) VALUES 
((SELECT id FROM users WHERE username='thanh_trung_sk'), 50000, 'APPROVED', '2025-03-05 18:00:00'),
((SELECT id FROM users WHERE username='meo_simmy_fake'), 20000, 'APPROVED', '2025-03-20 12:00:00');

-- Tháng 4/2025: Nghỉ lễ 30/4
INSERT INTO topup_requests (user_id, amount, status, created_at) VALUES 
((SELECT id FROM users WHERE username='hacker_lor'), 100000, 'APPROVED', '2025-04-29 20:00:00'),
((SELECT id FROM users WHERE username='boy_nha_giau'), 300000, 'APPROVED', '2025-04-30 08:00:00');

-- Tháng 5/2025: Đầu hè
INSERT INTO topup_requests (user_id, amount, status, created_at) VALUES 
((SELECT id FROM users WHERE username='best_leesin'), 50000, 'APPROVED', '2025-05-15 10:30:00'),
((SELECT id FROM users WHERE username='co_giao_thao'), 100000, 'APPROVED', '2025-05-25 19:00:00');

-- Tháng 6/2025: Cao điểm Hè (Doanh thu tăng vọt)
INSERT INTO topup_requests (user_id, amount, status, created_at) VALUES 
((SELECT id FROM users WHERE username='boy_nha_giau'), 1000000, 'APPROVED', '2025-06-01 08:00:00'),
((SELECT id FROM users WHERE username='tuan_tien_ti'), 500000, 'APPROVED', '2025-06-10 14:00:00'),
((SELECT id FROM users WHERE username='vua_tro_choi'), 200000, 'APPROVED', '2025-06-20 15:00:00');

-- Tháng 7/2025: Vẫn cao
INSERT INTO topup_requests (user_id, amount, status, created_at) VALUES 
((SELECT id FROM users WHERE username='thanh_trung_sk'), 200000, 'APPROVED', '2025-07-05 10:00:00'),
((SELECT id FROM users WHERE username='yasuo_gank_tem'), 100000, 'APPROVED', '2025-07-25 16:00:00');

-- Tháng 8/2025: Cuối hè
INSERT INTO topup_requests (user_id, amount, status, created_at) VALUES 
((SELECT id FROM users WHERE username='meo_simmy_fake'), 50000, 'APPROVED', '2025-08-15 09:00:00');

-- Tháng 9/2025: Vào năm học mới (Giảm mạnh)
INSERT INTO topup_requests (user_id, amount, status, created_at) VALUES 
((SELECT id FROM users WHERE username='khach_vang_lai'), 10000, 'APPROVED', '2025-09-05 18:00:00'),
((SELECT id FROM users WHERE username='best_leesin'), 20000, 'APPROVED', '2025-09-20 19:30:00');

-- Tháng 10/2025: Bình thường
INSERT INTO topup_requests (user_id, amount, status, created_at) VALUES 
((SELECT id FROM users WHERE username='hacker_lor'), 50000, 'APPROVED', '2025-10-10 20:00:00');

-- Tháng 11/2025: Ôn thi
INSERT INTO topup_requests (user_id, amount, status, created_at) VALUES 
((SELECT id FROM users WHERE username='tuan_tien_ti'), 100000, 'APPROVED', '2025-11-20 17:00:00');

-- Tháng 12/2025: Noel & Tết Dương (Tăng trở lại)
INSERT INTO topup_requests (user_id, amount, status, created_at) VALUES 
((SELECT id FROM users WHERE username='boy_nha_giau'), 2000000, 'APPROVED', '2025-12-24 18:00:00'),
((SELECT id FROM users WHERE username='vua_tro_choi'), 500000, 'APPROVED', '2025-12-31 22:00:00');


-- === B. GỌI MÓN (DOANH THU DỊCH VỤ) ===

-- Tháng 1
INSERT INTO orders (user_id, product_id, qty, total_price, status, created_at) VALUES 
((SELECT id FROM users WHERE username='yasuo_gank_tem'), (SELECT id FROM products WHERE name='Sting Dau'), 2, 24000, 'SERVED', '2025-01-05 09:30:00');

-- Tháng 2
INSERT INTO orders (user_id, product_id, qty, total_price, status, created_at) VALUES 
((SELECT id FROM users WHERE username='boy_nha_giau'), (SELECT id FROM products WHERE name='Com Ga Xoi Mo'), 5, 200000, 'SERVED', '2025-02-10 12:00:00'),
((SELECT id FROM users WHERE username='tuan_tien_ti'), (SELECT id FROM products WHERE name='Sting Vang'), 10, 120000, 'SERVED', '2025-02-12 14:00:00');

-- Tháng 3
INSERT INTO orders (user_id, product_id, qty, total_price, status, created_at) VALUES 
((SELECT id FROM users WHERE username='thanh_trung_sk'), (SELECT id FROM products WHERE name='My Tom Trung'), 2, 30000, 'SERVED', '2025-03-05 19:00:00');

-- Tháng 4
INSERT INTO orders (user_id, product_id, qty, total_price, status, created_at) VALUES 
((SELECT id FROM users WHERE username='boy_nha_giau'), (SELECT id FROM products WHERE name='Com Rang Dua Bo'), 2, 70000, 'SERVED', '2025-04-30 12:30:00');

-- Tháng 5
INSERT INTO orders (user_id, product_id, qty, total_price, status, created_at) VALUES 
((SELECT id FROM users WHERE username='co_giao_thao'), (SELECT id FROM products WHERE name='Ca Phe Sua'), 2, 40000, 'SERVED', '2025-05-25 20:00:00');

-- Tháng 6 (Hè nóng nực - Bán nước siêu chạy)
INSERT INTO orders (user_id, product_id, qty, total_price, status, created_at) VALUES 
((SELECT id FROM users WHERE username='boy_nha_giau'), (SELECT id FROM products WHERE name='Sting Dau'), 20, 240000, 'SERVED', '2025-06-01 10:00:00'),
((SELECT id FROM users WHERE username='tuan_tien_ti'), (SELECT id FROM products WHERE name='Pepsi'), 15, 150000, 'SERVED', '2025-06-10 15:00:00'),
((SELECT id FROM users WHERE username='vua_tro_choi'), (SELECT id FROM products WHERE name='Banh Mi Thap Cam'), 5, 125000, 'SERVED', '2025-06-20 12:00:00');

-- Tháng 7
INSERT INTO orders (user_id, product_id, qty, total_price, status, created_at) VALUES 
((SELECT id FROM users WHERE username='thanh_trung_sk'), (SELECT id FROM products WHERE name='My Tom Full Topping'), 3, 90000, 'SERVED', '2025-07-05 11:00:00'),
((SELECT id FROM users WHERE username='yasuo_gank_tem'), (SELECT id FROM products WHERE name='Tra Da'), 10, 20000, 'SERVED', '2025-07-25 15:00:00');

-- Tháng 8
INSERT INTO orders (user_id, product_id, qty, total_price, status, created_at) VALUES 
((SELECT id FROM users WHERE username='meo_simmy_fake'), (SELECT id FROM products WHERE name='Bim Bim Oishi'), 5, 25000, 'SERVED', '2025-08-15 09:30:00');

-- Tháng 9
INSERT INTO orders (user_id, product_id, qty, total_price, status, created_at) VALUES 
((SELECT id FROM users WHERE username='best_leesin'), (SELECT id FROM products WHERE name='Sting Vang'), 2, 24000, 'SERVED', '2025-09-20 20:00:00');

-- Tháng 10
INSERT INTO orders (user_id, product_id, qty, total_price, status, created_at) VALUES 
((SELECT id FROM users WHERE username='hacker_lor'), (SELECT id FROM products WHERE name='Ca Phe Den'), 1, 15000, 'SERVED', '2025-10-10 20:30:00');

-- Tháng 11
INSERT INTO orders (user_id, product_id, qty, total_price, status, created_at) VALUES 
((SELECT id FROM users WHERE username='tuan_tien_ti'), (SELECT id FROM products WHERE name='Banh Mi Pate'), 2, 30000, 'SERVED', '2025-11-20 18:00:00');

-- Tháng 12 (Lạnh - Bán mỳ tôm chạy)
INSERT INTO orders (user_id, product_id, qty, total_price, status, created_at) VALUES 
((SELECT id FROM users WHERE username='boy_nha_giau'), (SELECT id FROM products WHERE name='My Tom Full Topping'), 10, 300000, 'SERVED', '2025-12-24 20:00:00'),
((SELECT id FROM users WHERE username='vua_tro_choi'), (SELECT id FROM products WHERE name='Redbull'), 5, 75000, 'SERVED', '2025-12-31 23:00:00');