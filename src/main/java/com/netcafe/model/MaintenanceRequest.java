package com.netcafe.model;

import java.time.LocalDateTime;

public class MaintenanceRequest {
    public enum Status {
        PENDING, IN_PROGRESS, RESOLVED
    }

    private int id;
    private int computerId;
    private int userId;
    private String issue;
    private Status status;
    private LocalDateTime createdAt;

    // Helper fields for display
    private String computerName;
    private String reporterName;

    public MaintenanceRequest() {
    }

    public MaintenanceRequest(int computerId, int userId, String issue) {
        this.computerId = computerId;
        this.userId = userId;
        this.issue = issue;
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getComputerId() {
        return computerId;
    }

    public void setComputerId(int computerId) {
        this.computerId = computerId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }
}
