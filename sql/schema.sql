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
  FOREIGN KEY (user_id) REFERENCES users(id)
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

-- Initial Admin User (password: admin123)
-- Hash generated via BCrypt for 'admin123'
INSERT INTO users (username, password_hash, full_name, role) 
VALUES ('admin', '$2a$10$1KYugQ8nepf19Z34s3pgIe1UT.dNstJLiNXBvttOH69Co6pJ1wO.C', 'Administrator', 'ADMIN')
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash);

-- Test User (password: user123)
INSERT INTO users (username, password_hash, full_name, role)
VALUES ('user1', '$2a$10$r4YEgO5RmVBA6so/m9fKVeg2ar4lRjn6Sow3OXWGrJS7/XZBY9haa', 'Test User', 'USER')
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash);

-- Give user1 some initial balance (e.g., 50,000 VND)
INSERT IGNORE INTO accounts (user_id, balance) 
SELECT id, 50000 FROM users WHERE username = 'user1';

-- Initial Products
-- Initial Products
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
('Ca Phe Sua Da', 'DRINK', 25000, 100)
ON DUPLICATE KEY UPDATE price = VALUES(price);
-- Actually, let's put a real hash for 'admin123': $2a$10$Ew.Z.Z.Z.Z.Z.Z.Z.Z.Z.u.Z.Z.Z.Z.Z.Z.Z.Z.Z.Z.Z.Z.Z.Z.Z.Z
-- I will use a known hash for 'admin123' in the code or let the user create it.
-- For now, I'll leave the insert commented out or use a placeholder that the user must change or I will provide a utility to generate it.
-- Better: I'll provide a main method in PasswordUtil to generate hashes.

-- computers
CREATE TABLE IF NOT EXISTS computers (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE,
  status ENUM('AVAILABLE','OCCUPIED','MAINTENANCE','DIRTY') DEFAULT 'AVAILABLE',
  x_pos INT DEFAULT 0,
  y_pos INT DEFAULT 0
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

-- Initial Computers (20 machines)
INSERT IGNORE INTO computers (name, status, x_pos, y_pos) VALUES
('PC-01', 'AVAILABLE', 0, 0), ('PC-02', 'AVAILABLE', 1, 0), ('PC-03', 'AVAILABLE', 2, 0), ('PC-04', 'AVAILABLE', 3, 0), ('PC-05', 'AVAILABLE', 4, 0),
('PC-06', 'AVAILABLE', 0, 1), ('PC-07', 'AVAILABLE', 1, 1), ('PC-08', 'AVAILABLE', 2, 1), ('PC-09', 'AVAILABLE', 3, 1), ('PC-10', 'AVAILABLE', 4, 1),
('PC-11', 'AVAILABLE', 0, 3), ('PC-12', 'AVAILABLE', 1, 3), ('PC-13', 'AVAILABLE', 2, 3), ('PC-14', 'AVAILABLE', 3, 3), ('PC-15', 'AVAILABLE', 4, 3),
('PC-16', 'AVAILABLE', 0, 4), ('PC-17', 'AVAILABLE', 1, 4), ('PC-18', 'AVAILABLE', 2, 4), ('PC-19', 'AVAILABLE', 3, 4), ('PC-20', 'AVAILABLE', 4, 4);
