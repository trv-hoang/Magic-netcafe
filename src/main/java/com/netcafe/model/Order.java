package com.netcafe.model;

import java.time.LocalDateTime;

public class Order {
    private int id;
    private int userId;
    private int productId;
    private int qty;
    private long totalPrice;
    private LocalDateTime createdAt;
    private String status;

    public Order() {
    }

    public Order(int userId, int productId, int qty, long totalPrice) {
        this.userId = userId;
        this.productId = productId;
        this.qty = qty;
        this.totalPrice = totalPrice;
        this.status = "PENDING";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(long totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
