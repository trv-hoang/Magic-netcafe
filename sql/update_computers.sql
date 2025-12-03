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
('PC-11', 'AVAILABLE', 0, 2), ('PC-12', 'AVAILABLE', 1, 2), ('PC-13', 'AVAILABLE', 2, 2), ('PC-14', 'AVAILABLE', 3, 2), ('PC-15', 'AVAILABLE', 4, 2),
('PC-16', 'AVAILABLE', 0, 3), ('PC-17', 'AVAILABLE', 1, 3), ('PC-18', 'AVAILABLE', 2, 3), ('PC-19', 'AVAILABLE', 3, 3), ('PC-20', 'AVAILABLE', 4, 3);
