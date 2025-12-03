package com.netcafe.model;

public class Computer {
    public enum Status {
        AVAILABLE, OCCUPIED, MAINTENANCE, DIRTY
    }

    private int id;
    private String name;
    private Status status;
    private int xPos;
    private int yPos;

    public Computer() {
    }

    public Computer(int id, String name, Status status, int xPos, int yPos) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getXPos() {
        return xPos;
    }

    public void setXPos(int xPos) {
        this.xPos = xPos;
    }

    public int getYPos() {
        return yPos;
    }

    public void setYPos(int yPos) {
        this.yPos = yPos;
    }
}
