@echo off
echo Setting up NetCafe Database...

:: Check if mysql is accessible
where mysql >nul 2>nul
if %errorlevel% neq 0 (
    echo MySQL is not found in PATH. Please install MySQL and add it to your PATH.
    pause
    exit /b 1
)

:: Try to connect as root without password
mysql -u root -e "SELECT 1" >nul 2>nul
if %errorlevel% equ 0 (
    set AUTH_CMD=-u root
    goto :Setup
)

:: Try to connect as root WITH password
echo Root access without password failed. Trying with password...
mysql -u root -p -e "SELECT 1" >nul 2>nul
if %errorlevel% equ 0 (
    set AUTH_CMD=-u root -p
    goto :Setup
)

echo.
echo Automatic connection failed.
echo Please enter your MySQL credentials manually.
set /p DB_USER=Username (default: root): 
if "%DB_USER%"=="" set DB_USER=root
set /p DB_PASS=Password: 

:: Verify manual credentials
mysql -u %DB_USER% --password="%DB_PASS%" -e "SELECT 1" >nul 2>nul
if %errorlevel% equ 0 (
    set AUTH_CMD=-u %DB_USER% --password="%DB_PASS%"
    goto :Setup
)

echo.
echo Error: Could not connect to MySQL with the provided credentials.
echo Please check your username and password.
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
