package com.mall.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Product {
    private String id;
    private String name;
    private String category;
    private BigDecimal price;
    private int stockQty;
    private String description;
    private String imagePath;
    private double averageRating;
    private int ratingCount;

    public Product(String id, String name, String category, BigDecimal price,
            int stockQty, String description, String imagePath) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQty = stockQty;
        this.description = description;
        this.imagePath = imagePath;
        this.averageRating = 0.0;
        this.ratingCount = 0;
    }

    // Getters & Setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStockQty() {
        return stockQty;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setStockQty(int stockQty) {
        this.stockQty = stockQty;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("Product[id=%s, name=%s, category=%s, price=%s, stock=%d]",
                id, name, category, price.toPlainString(), stockQty);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Product))
            return false;
        Product p = (Product) o;
        return Objects.equals(id, p.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void addRating(int score) {
        double totalScore = (this.averageRating * this.ratingCount) + score;
        this.ratingCount++;
        this.averageRating = totalScore / this.ratingCount;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public String getImagePath() {
        return imagePath;
    }
}
