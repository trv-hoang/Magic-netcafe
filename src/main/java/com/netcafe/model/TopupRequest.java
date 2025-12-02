package com.netcafe.model;

import java.sql.Timestamp;

public class TopupRequest {
    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    private int id;
    private int userId;
    private long amount;
    private Status status;
    private Timestamp createdAt;

    public TopupRequest() {
    }

    public TopupRequest(int userId, long amount) {
        this.userId = userId;
        this.amount = amount;
        this.status = Status.PENDING;
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

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
