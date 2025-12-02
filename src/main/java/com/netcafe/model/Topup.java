package com.netcafe.model;

import java.time.LocalDateTime;

public class Topup {
    private int id;
    private int userId;
    private long amount;
    private String method;
    private LocalDateTime createdAt;

    public Topup() {}

    public Topup(int userId, long amount, String method) {
        this.userId = userId;
        this.amount = amount;
        this.method = method;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
