-- ==========================================
-- PHẦN 1: LÀM SẠCH DATABASE (RESET)
-- ==========================================
-- Xóa bảng theo thứ tự ngược lại của khóa ngoại để tránh lỗi
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

-- ==========================================
-- PHẦN 2: CẤU TRÚC BẢNG (GIỮ NGUYÊN CÁI CŨ)
-- ==========================================

-- users
CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(100),
  role ENUM('ADMIN','USER') DEFAULT 'USER',
  tier ENUM('BRONZE','SILVER','GOLD') DEFAULT 'BRONZE',
  points INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- accounts
CREATE TABLE IF NOT EXISTS accounts (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL UNIQUE,
  balance BIGINT NOT NULL DEFAULT 0,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- topups
CREATE TABLE IF NOT EXISTS topups (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  amount BIGINT NOT NULL,
  method VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- computers
CREATE TABLE IF NOT EXISTS computers (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE,
  status ENUM('AVAILABLE','OCCUPIED','MAINTENANCE','DIRTY') DEFAULT 'AVAILABLE',
  x_pos INT DEFAULT 0,
  y_pos INT DEFAULT 0
);

-- sessions
CREATE TABLE IF NOT EXISTS sessions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  start_time TIMESTAMP NOT NULL,
  end_time TIMESTAMP NULL,
  time_purchased_seconds INT NOT NULL,
  time_consumed_seconds INT DEFAULT 0,
  status ENUM('ACTIVE','ENDED') DEFAULT 'ACTIVE',
  machine_name VARCHAR(50),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (machine_name) REFERENCES computers(name)
);

-- products
CREATE TABLE IF NOT EXISTS products (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) UNIQUE,
  category ENUM('FOOD','DRINK','GAME_ITEM') DEFAULT 'FOOD',
  price BIGINT NOT NULL,
  stock INT DEFAULT 0
);

-- topup_requests
CREATE TABLE IF NOT EXISTS topup_requests (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  amount BIGINT NOT NULL,
  status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- orders
CREATE TABLE IF NOT EXISTS orders (
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

-- messages
CREATE TABLE IF NOT EXISTS messages (
  id INT AUTO_INCREMENT PRIMARY KEY,
  sender_id INT NOT NULL,
  receiver_id INT NOT NULL,
  content TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (sender_id) REFERENCES users(id),
  FOREIGN KEY (receiver_id) REFERENCES users(id)
);

-- maintenance_requests
CREATE TABLE IF NOT EXISTS maintenance_requests (
  id INT AUTO_INCREMENT PRIMARY KEY,
  computer_id INT NOT NULL,
  user_id INT NOT NULL,
  issue TEXT NOT NULL,
  status ENUM('PENDING','IN_PROGRESS','RESOLVED') DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (computer_id) REFERENCES computers(id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ==========================================
-- PHẦN 3: DỮ LIỆU MẪU (BỔ SUNG THÊM)
-- ==========================================

-- 1. Initial Admin User & User1
INSERT INTO users (username, password_hash, full_name, role) 
VALUES ('admin', '$2a$10$1KYugQ8nepf19Z34s3pgIe1UT.dNstJLiNXBvttOH69Co6pJ1wO.C', 'Administrator', 'ADMIN');

INSERT INTO users (username, password_hash, full_name, role)
VALUES ('user1', '$2a$10$r4YEgO5RmVBA6so/m9fKVeg2ar4lRjn6Sow3OXWGrJS7/XZBY9haa', 'Test User', 'USER');

INSERT INTO accounts (user_id, balance) SELECT id, 50000 FROM users WHERE username = 'user1';

-- 2. Thêm 10 User giả lập để test biểu đồ (Mật khẩu mặc định: 123)
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

-- Tạo ví 0đ cho các user mới này
INSERT IGNORE INTO accounts (user_id, balance)
SELECT id, 0 FROM users WHERE username NOT IN ('admin', 'user1');

-- 3. Initial Products
INSERT INTO products (name, category, price, stock) VALUES 
('Banh Mi', 'FOOD', 20000, 100),
('Com Rang', 'FOOD', 35000, 100),
('Bo Kho', 'FOOD', 40000, 100),
('Bun Rieu', 'FOOD', 30000, 100),
('My Trung', 'FOOD', 25000, 100),
('Pho Bo', 'FOOD', 45000, 50),
('Bun Cha', 'FOOD', 40000, 50),
('Com Tam', 'FOOD', 35000, 50),
('Goi Cuon', 'FOOD', 15000, 100),
('Nem Ran', 'FOOD', 20000, 100),
('CocaCola', 'DRINK', 10000, 200),
('Pepsi', 'DRINK', 10000, 200),
('Sprite', 'DRINK', 10000, 200),
('Bo Huc', 'DRINK', 15000, 200),
('Sting', 'DRINK', 12000, 200),
('Tra Da', 'DRINK', 5000, 500),
('Ca Phe Sua Da', 'DRINK', 25000, 100);

-- 4. Initial Computers
INSERT INTO computers (name, status, x_pos, y_pos) VALUES
('PC-01', 'AVAILABLE', 0, 0), ('PC-02', 'AVAILABLE', 1, 0), ('PC-03', 'AVAILABLE', 2, 0), ('PC-04', 'AVAILABLE', 3, 0), ('PC-05', 'AVAILABLE', 4, 0),
('PC-06', 'AVAILABLE', 0, 1), ('PC-07', 'AVAILABLE', 1, 1), ('PC-08', 'AVAILABLE', 2, 1), ('PC-09', 'AVAILABLE', 3, 1), ('PC-10', 'AVAILABLE', 4, 1),
('PC-11', 'AVAILABLE', 0, 3), ('PC-12', 'AVAILABLE', 1, 3), ('PC-13', 'AVAILABLE', 2, 3), ('PC-14', 'AVAILABLE', 3, 3), ('PC-15', 'AVAILABLE', 4, 3),
('PC-16', 'AVAILABLE', 0, 4), ('PC-17', 'AVAILABLE', 1, 4), ('PC-18', 'AVAILABLE', 2, 4), ('PC-19', 'AVAILABLE', 3, 4), ('PC-20', 'AVAILABLE', 4, 4);

-- 5. Giả lập Doanh thu NẠP TIỀN (Topup Requests - Status APPROVED)
-- Tháng 1
INSERT INTO topup_requests (user_id, amount, status, created_at) VALUES 
((SELECT id FROM users WHERE username='yasuo_gank_tem'), 20000, 'APPROVED', '2024-01-05 10:00:00'),
((SELECT id FROM users WHERE username='best_leesin'), 10000, 'APPROVED', '2024-01-12 15:30:00'),
((SELECT id FROM users WHERE username='khach_vang_lai'), 5000, 'APPROVED', '2024-01-20 09:00:00');

-- Tháng 2 (Tết)
INSERT INTO topup_requests (user_id, amount, status, created_at) VALUES 
((SELECT id FROM users WHERE username='boy_nha_giau'), 500000, 'APPROVED', '2024-02-10 14:00:00'), 
((SELECT id FROM users WHERE username='tuan_tien_ti'), 200000, 'APPROVED', '2024-02-11 16:00:00'),
((SELECT id FROM users WHERE username='hacker_lor'), 50000, 'APPROVED', '2024-02-15 19:00:00');

-- Tháng 3
INSERT INTO topup_requests (user_id, amount, status, created_at) VALUES 
((SELECT id FROM users WHERE username='boy_nha_giau'), 200000, 'APPROVED', '2024-03-05 08:00:00'),
((SELECT id FROM users WHERE username='meo_simmy_fake'), 20000, 'APPROVED', '2024-03-10 12:00:00'),
((SELECT id FROM users WHERE username='thanh_trung_sk'), 100000, 'APPROVED', '2024-03-25 20:00:00');

-- Tháng 4
INSERT INTO topup_requests (user_id, amount, status, created_at) VALUES 
((SELECT id FROM users WHERE username='tuan_tien_ti'), 500000, 'APPROVED', '2024-04-01 10:00:00'),
((SELECT id FROM users WHERE username='boy_nha_giau'), 1000000, 'APPROVED', '2024-04-15 11:00:00'),
((SELECT id FROM users WHERE username='vua_tro_choi'), 50000, 'APPROVED', '2024-04-20 18:00:00');

-- Tháng 5 (Hiện tại)
INSERT INTO topup_requests (user_id, amount, status, created_at) VALUES 
((SELECT id FROM users WHERE username='yasuo_gank_tem'), 20000, 'APPROVED', '2024-05-01 09:00:00'),
((SELECT id FROM users WHERE username='best_leesin'), 20000, 'APPROVED', '2024-05-02 10:00:00'),
((SELECT id FROM users WHERE username='hacker_lor'), 100000, 'APPROVED', '2024-05-03 14:00:00'),
((SELECT id FROM users WHERE username='co_giao_thao'), 50000, 'APPROVED', '2024-05-05 19:30:00'),
((SELECT id FROM users WHERE username='boy_nha_giau'), 200000, 'APPROVED', NOW()); 

-- 6. Giả lập Doanh thu BÁN HÀNG (Orders - Status SERVED)
-- Tháng 1 & 2
INSERT INTO orders (user_id, product_id, qty, total_price, status, created_at) VALUES 
((SELECT id FROM users WHERE username='yasuo_gank_tem'), (SELECT id FROM products WHERE name='Sting'), 2, 24000, 'SERVED', '2024-01-05 10:30:00'),
((SELECT id FROM users WHERE username='boy_nha_giau'), (SELECT id FROM products WHERE name='My Trung'), 1, 25000, 'SERVED', '2024-02-10 15:00:00');

-- Tháng 3
INSERT INTO orders (user_id, product_id, qty, total_price, status, created_at) VALUES 
((SELECT id FROM users WHERE username='thanh_trung_sk'), (SELECT id FROM products WHERE name='Com Rang'), 2, 70000, 'SERVED', '2024-03-25 21:00:00'),
((SELECT id FROM users WHERE username='thanh_trung_sk'), (SELECT id FROM products WHERE name='Sting'), 5, 60000, 'SERVED', '2024-03-25 21:00:00');

-- Tháng 4
INSERT INTO orders (user_id, product_id, qty, total_price, status, created_at) VALUES 
((SELECT id FROM users WHERE username='boy_nha_giau'), (SELECT id FROM products WHERE name='Pho Bo'), 2, 90000, 'SERVED', '2024-04-15 12:00:00'),
((SELECT id FROM users WHERE username='tuan_tien_ti'), (SELECT id FROM products WHERE name='Sting'), 10, 120000, 'SERVED', '2024-04-01 11:00:00'),
((SELECT id FROM users WHERE username='vua_tro_choi'), (SELECT id FROM products WHERE name='Banh Mi'), 3, 60000, 'SERVED', '2024-04-20 18:30:00');

-- Tháng 5
INSERT INTO orders (user_id, product_id, qty, total_price, status, created_at) VALUES 
((SELECT id FROM users WHERE username='yasuo_gank_tem'), (SELECT id FROM products WHERE name='Sting'), 5, 60000, 'SERVED', NOW()),
((SELECT id FROM users WHERE username='hacker_lor'), (SELECT id FROM products WHERE name='My Trung'), 3, 75000, 'SERVED', NOW()),
((SELECT id FROM users WHERE username='meo_simmy_fake'), (SELECT id FROM products WHERE name='Pepsi'), 4, 40000, 'SERVED', NOW()),
((SELECT id FROM users WHERE username='boy_nha_giau'), (SELECT id FROM products WHERE name='Sting'), 20, 240000, 'SERVED', NOW()),
((SELECT id FROM users WHERE username='khach_vang_lai'), (SELECT id FROM products WHERE name='Tra Da'), 10, 50000, 'SERVED', NOW());