package com.netcafe.service;

import com.netcafe.dao.AccountDAO;
import com.netcafe.dao.OrderDAO;
import com.netcafe.dao.TopupDAO;
import com.netcafe.dao.TopupRequestDAO;
import com.netcafe.model.Account;
import com.netcafe.model.Order;
import com.netcafe.model.Topup;
import com.netcafe.model.TopupRequest;
import com.netcafe.util.DBPool;

import java.sql.Connection;
import java.util.List;

public class BillingService {
    private final AccountDAO accountDAO = new AccountDAO();
    private final TopupDAO topupDAO = new TopupDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final TopupRequestDAO topupRequestDAO = new TopupRequestDAO();
    private final com.netcafe.dao.UserDAO userDAO = new com.netcafe.dao.UserDAO();

    private static final String TOPUP_METHOD_ADMIN = "ADMIN_APPROVED";

    private int calculatePoints(long amount) {
        return (int) (amount / 1000);
    }

    public void requestTopup(int userId, long amount) throws Exception {
        TopupRequest request = new TopupRequest(userId, amount);
        topupRequestDAO.create(request);
    }

    public void approveTopup(int requestId) throws Exception {
        try (Connection conn = DBPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Get Request
                TopupRequest req = topupRequestDAO.findById(conn, requestId)
                        .orElseThrow(() -> new Exception("Topup request not found"));

                if (req.getStatus() != TopupRequest.Status.PENDING) {
                    throw new Exception("Request is not PENDING");
                }

                // 2. Perform Topup (Logic from original topup method)
                // Create Topup Record
                // Create Topup Record
                Topup topup = new Topup(req.getUserId(), req.getAmount(), TOPUP_METHOD_ADMIN);
                topupDAO.create(conn, topup);

                // Update Balance
                try {
                    Account acc = accountDAO.getAccountForUpdate(conn, req.getUserId());
                    accountDAO.updateBalance(conn, req.getUserId(), acc.getBalance() + req.getAmount());
                } catch (Exception e) {
                    Account newAcc = new Account(req.getUserId(), req.getAmount());
                    accountDAO.create(conn, newAcc);
                }

                // 3. Update Request Status
                topupRequestDAO.updateStatus(conn, requestId, TopupRequest.Status.APPROVED);

                // 4. Award Points (1 Point per 1,000 VND)
                // 4. Award Points (1 Point per 1,000 VND)
                int pointsEarned = calculatePoints(req.getAmount());
                if (pointsEarned > 0) {
                    com.netcafe.model.User user = userDAO.findById(conn, req.getUserId())
                            .orElseThrow(() -> new Exception("User not found"));
                    userDAO.updatePoints(conn, req.getUserId(), user.getPoints() + pointsEarned);
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<TopupRequest> getPendingTopupRequests() throws Exception {
        return topupRequestDAO.findAllPending();
    }

    public void topup(int userId, long amount, String method) throws Exception {
        try (Connection conn = DBPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Create Topup record
                Topup topup = new Topup(userId, amount, method);
                topupDAO.create(conn, topup);

                // 2. Update Balance (lock first)
                // If account doesn't exist, create it? Or assume it exists.
                // Let's assume it exists or create if not.
                // For simplicity, let's try to lock, if fail, create.
                try {
                    Account acc = accountDAO.getAccountForUpdate(conn, userId);
                    accountDAO.updateBalance(conn, userId, acc.getBalance() + amount);
                } catch (Exception e) {
                    // Account might not exist
                    Account newAcc = new Account(userId, amount);
                    accountDAO.create(conn, newAcc);
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private final com.netcafe.dao.ProductDAO productDAO = new com.netcafe.dao.ProductDAO();

    public void placeOrder(int userId, int productId, int qty, long totalPrice) throws Exception {
        try (Connection conn = DBPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Check Product Stock (Locking would be better, but simple check for now)
                com.netcafe.model.Product product = productDAO.findById(conn, productId)
                        .orElseThrow(() -> new Exception("Product not found"));

                if (product.getStock() < qty) {
                    throw new Exception("Insufficient stock. Available: " + product.getStock());
                }

                // 2. Check balance
                Account acc = accountDAO.getAccountForUpdate(conn, userId);
                if (acc.getBalance() < totalPrice) {
                    throw new Exception("Insufficient balance for order.");
                }

                // 3. Deduct Stock
                productDAO.updateStock(conn, productId, product.getStock() - qty);

                // 4. Deduct balance
                accountDAO.updateBalance(conn, userId, acc.getBalance() - totalPrice);

                // 5. Create Order
                Order order = new Order(userId, productId, qty, totalPrice);
                orderDAO.create(conn, order);

                // 4. Award Points (1 Point per 1,000 VND)
                // 4. Award Points (1 Point per 1,000 VND)
                int pointsEarned = calculatePoints(totalPrice);
                if (pointsEarned > 0) {
                    com.netcafe.model.User user = userDAO.findById(conn, userId)
                            .orElseThrow(() -> new Exception("User not found"));
                    userDAO.updatePoints(conn, userId, user.getPoints() + pointsEarned);
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public long getBalance(int userId) throws Exception {
        return accountDAO.findByUserId(userId).map(Account::getBalance).orElse(0L);
    }

    public void setBalance(int userId, long newBalance) throws Exception {
        try (Connection conn = DBPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try {
                    // Try to lock and update existing account
                    accountDAO.getAccountForUpdate(conn, userId);
                    accountDAO.updateBalance(conn, userId, newBalance);
                } catch (Exception e) {
                    // Account not found, create new one
                    // Note: create() uses its own connection/transaction
                    Account newAcc = new Account(userId, newBalance);
                    accountDAO.create(conn, newAcc);
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void redeemPoints(int userId, int pointsToRedeem) throws Exception {
        if (pointsToRedeem <= 0)
            return;

        // Ratio: 100 Points = 5,000 VND
        long amountVND = (pointsToRedeem / 100) * 5000;
        if (amountVND <= 0)
            throw new Exception("Minimum redemption is 100 points.");

        try (Connection conn = DBPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Check points (lock user row ideally, but for now just read)
                com.netcafe.model.User user = userDAO.findById(conn, userId)
                        .orElseThrow(() -> new Exception("User not found"));

                if (user.getPoints() < pointsToRedeem) {
                    throw new Exception("Insufficient points.");
                }

                // 2. Update Points
                userDAO.updatePoints(conn, userId, user.getPoints() - pointsToRedeem);

                // 3. Add Balance
                try {
                    Account acc = accountDAO.getAccountForUpdate(conn, userId);
                    accountDAO.updateBalance(conn, userId, acc.getBalance() + amountVND);
                } catch (Exception e) {
                    // Create account if not exists
                    Account newAcc = new Account(userId, amountVND);
                    accountDAO.create(conn, newAcc);
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public int getPoints(int userId) throws Exception {
        return userDAO.findAll().stream().filter(u -> u.getId() == userId).findFirst()
                .map(com.netcafe.model.User::getPoints).orElse(0);
    }
}
