package com.netcafe.model;

import java.time.LocalDateTime;

public class Session {
    private int id;
    private int userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int timePurchasedSeconds;
    private int timeConsumedSeconds;
    private Status status;
    private String machineName;

    public enum Status {
        ACTIVE, ENDED
    }

    public Session() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public int getTimePurchasedSeconds() { return timePurchasedSeconds; }
    public void setTimePurchasedSeconds(int timePurchasedSeconds) { this.timePurchasedSeconds = timePurchasedSeconds; }
    public int getTimeConsumedSeconds() { return timeConsumedSeconds; }
    public void setTimeConsumedSeconds(int timeConsumedSeconds) { this.timeConsumedSeconds = timeConsumedSeconds; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }
}
