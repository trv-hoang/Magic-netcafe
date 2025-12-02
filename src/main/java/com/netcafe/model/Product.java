package com.netcafe.model;

public class Product {
    private int id;
    private String name;
    private Category category;
    private long price;
    private int stock;

    public enum Category {
        FOOD, DRINK, GAME_ITEM, TOPUP
    }

    public Product() {
    }

    public Product(String name, Category category, long price, int stock) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Override
    public String toString() {
        return name + " (" + price + ")";
    }
}
