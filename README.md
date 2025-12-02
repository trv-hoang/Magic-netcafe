# Magic netCafe Management Application

A standalone Swing desktop application for managing a NetCafe, featuring user sessions, product ordering, and admin management.

## Features

### Core

- **User & Admin Roles**: Secure login with BCrypt password hashing.
- **Session Management**:
  - Automatic session start on login.
  - Live countdown timer based on balance.
  - Automatic session end on logout or when time runs out.
- **Persistence**: MySQL database with connection pooling (HikariCP).

### User Features

- **Dashboard**: View balance, time remaining, and session status.
- **Product Ordering**:
  - Browse Foods and Drinks with image previews.
  - **Shopping Cart**: Add items, view total, and checkout.
  - Orders are sent to Admin for status tracking.
- **Top-up System**:
  - Request balance top-ups (e.g., 10k, 20k, 50k).
  - Requests are sent to Admin for approval.
- **Game Launcher**: Quick access to popular games (Simulation).

### Admin Features

- **Dashboard**:
  - **Revenue Chart**: Visual bar chart of monthly revenue (Topups + Orders).
- **Order Management (Split View)**:
  - **Product Management (Left)**: Add, Edit, and Delete products (Foods/Drinks).
    - Manage Name, Category, Price, Stock, and **Images**.
  - **Serve Orders (Right)**: View pending orders and mark them as served.
- **Top-up Management**: View and approve/reject user top-up requests.
- **User Management**: (Basic) Create users via database.

## Prerequisites

- Java 17+
- Maven
- MySQL 8+

## Setup

1. **Database Setup (Automatic)**:
   Run the provided script to create the database, user, and schema automatically:

   **Linux/macOS**:

   ```bash
   ./setup_db.sh
   ```

   **Windows**:

   ```batch
   setup_db.bat
   ```

   _This script creates a MySQL user `netcafe` with password `secret` and imports the schema._

2. **Database Setup (Manual - Optional)**:
   If the script fails, you can set it up manually:

   ```sql
   CREATE DATABASE netcafe;
   CREATE USER 'netcafe'@'localhost' IDENTIFIED BY 'secret';
   GRANT ALL PRIVILEGES ON netcafe.* TO 'netcafe'@'localhost';
   USE netcafe;
   SOURCE sql/schema.sql;
   ```

3. **Configuration**:
   The `src/main/resources/application.properties` is already configured to use the `netcafe` user created above.

   ```properties
   db.url=jdbc:mysql://localhost:3306/netcafe?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   db.user=netcafe
   db.password=secret
   ```

## Build & Run

1. **Build**:

   ```bash
   mvn clean package
   ```

2. **Run**:

   **Standard Run**:

   ```bash
   mvn exec:java
   ```

   **Run with macOS Dock Name ("Magic netCafe")**:
   To see the correct application name in the macOS Dock, use:

   ```bash
   mvn exec:exec
   ```

## Usage

### Login Credentials

- **Admin**:
  - Username: `admin`
  - Password: `admin123`
- **User**:
  - Username: `user1`
  - Password: `user123`

### Workflows

1. **Product Management (Admin)**:

   - Go to "Order Management".
   - **Add Product**: Click "Add", fill details, select an image (JPG), and save.
   - **Edit/Delete**: Select a product from the table and use the buttons.

2. **User Top-up**:

   - User logs in -> Go to "Topup" tab -> Click an amount -> Confirm.
   - Admin logs in -> Go to "Topup Requests" tab -> Select request -> Click "Approve".
   - User balance is updated immediately.

3. **Ordering Food/Drink**:
   - User logs in -> Browse "Foods" or "Drinks" -> Click "Add" on items.
   - Items appear in "Your Cart" on the right.
   - Click "Checkout" to place order.
   - Admin logs in -> Go to "Order Management" (Right side) -> View pending orders -> Click "Serve Order" when ready.
