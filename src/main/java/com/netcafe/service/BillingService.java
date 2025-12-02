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
                Topup topup = new Topup(req.getUserId(), req.getAmount(), "ADMIN_APPROVED");
                topupDAO.create(conn, topup);

                // Update Balance
                try {
                    Account acc = accountDAO.getAccountForUpdate(conn, req.getUserId());
                    accountDAO.updateBalance(conn, req.getUserId(), acc.getBalance() + req.getAmount());
                } catch (Exception e) {
                    Account newAcc = new Account(req.getUserId(), req.getAmount());
                    accountDAO.create(newAcc);
                }

                // 3. Update Request Status
                topupRequestDAO.updateStatus(conn, requestId, TopupRequest.Status.APPROVED);

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
                    accountDAO.create(newAcc);
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void placeOrder(int userId, int productId, int qty, long totalPrice) throws Exception {
        try (Connection conn = DBPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Check balance
                Account acc = accountDAO.getAccountForUpdate(conn, userId);
                if (acc.getBalance() < totalPrice) {
                    throw new Exception("Insufficient balance for order.");
                }

                // 2. Deduct balance
                accountDAO.updateBalance(conn, userId, acc.getBalance() - totalPrice);

                // 3. Create Order
                Order order = new Order(userId, productId, qty, totalPrice);
                orderDAO.create(conn, order);

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
                    accountDAO.create(newAcc);
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
