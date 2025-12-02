@echo off
echo Setting up NetCafe Database...

:: Check if mysql is accessible
where mysql >nul 2>nul
if %errorlevel% neq 0 (
    echo MySQL is not found in PATH. Please install MySQL and add it to your PATH.
    pause
    exit /b 1
)

:: Try to connect as root
mysql -u root -e "SELECT 1" >nul 2>nul
if %errorlevel% equ 0 (
    set AUTH_CMD=-u root
    goto :Setup
)

:: If root fails, try current user (no -u)
mysql -e "SELECT 1" >nul 2>nul
if %errorlevel% equ 0 (
    set AUTH_CMD=
    goto :Setup
)

echo Cannot connect to MySQL as root or current user without password.
echo Please ensure MySQL is running and you have access.
pause
exit /b 1

:Setup
echo Connected to MySQL.

:: Create DB and User
(
echo CREATE DATABASE IF NOT EXISTS netcafe;
echo CREATE USER IF NOT EXISTS 'netcafe'@'localhost' IDENTIFIED BY 'secret';
echo GRANT ALL PRIVILEGES ON netcafe.* TO 'netcafe'@'localhost';
echo FLUSH PRIVILEGES;
) | mysql %AUTH_CMD%

:: Run Schema
mysql %AUTH_CMD% netcafe < sql\schema.sql

echo Database setup complete.
pause
