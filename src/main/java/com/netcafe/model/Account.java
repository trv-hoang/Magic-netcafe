package com.netcafe.model;

public class Account {
    private int id;
    private int userId;
    private long balance;

    public Account() {}

    public Account(int userId, long balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public long getBalance() { return balance; }
    public void setBalance(long balance) { this.balance = balance; }
}
