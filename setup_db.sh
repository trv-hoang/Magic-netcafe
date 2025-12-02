#!/bin/bash
echo "Setting up NetCafe Database..."

# Try to detect a working user
if mysql -u root -e "SELECT 1" &> /dev/null; then
    DB_USER="root"
elif mysql -e "SELECT 1" &> /dev/null; then
    DB_USER=$(whoami)
else
    echo "Cannot connect to MySQL as root or $(whoami) without password."
    echo "Please ensure MySQL is running and you have access."
    exit 1
fi

echo "Connected to MySQL as $DB_USER"

# Create DB and User
mysql -u "$DB_USER" <<EOF
CREATE DATABASE IF NOT EXISTS netcafe;
CREATE USER IF NOT EXISTS 'netcafe'@'localhost' IDENTIFIED BY 'secret';
GRANT ALL PRIVILEGES ON netcafe.* TO 'netcafe'@'localhost';
FLUSH PRIVILEGES;
EOF

# Run Schema
mysql -u "$DB_USER" netcafe < sql/schema.sql

echo "Database setup complete."
