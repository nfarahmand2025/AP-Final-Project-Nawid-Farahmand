package com.mall.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a product in the shopping mall.
 * Responsible for holding product data and managing user ratings.
 */
public class Product {
    private String id;
    private String name;
    private String category;
    private BigDecimal price;
    private int stockQty;
    private String description;
    private String imagePath;
    private Map<Customer, Integer> ratings;

    public Product(String id, String name, String category, BigDecimal price,
                   int stockQty, String description, String imagePath) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQty = stockQty;
        this.description = description;
        this.imagePath = imagePath;
        this.ratings = new HashMap<>();
    }

    /**
     * Adds a new rating or updates an existing one for a specific customer.
     * This ensures each customer has exactly one rating per product.
     *
     * @param customer indicates the customer.
     * @param rating   Integer value of the rating.
     */
    public void addOrUpdateRating(Customer customer, int rating) {
        // If the customer already exists in the map, .put() will overwrite the old
        // value.
        ratings.put(customer, rating);
    }

    /**
     * Calculates the average rating.
     *
     * @return The arithmetic mean of all ratings, or 0.0 if no ratings exist.
     */
    public double getAverageRating() {
        if (ratings.isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        for (int r : ratings.values()) {
            sum += r;
        }
        return sum / ratings.size();
    }

    // getters:
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

    public String getImagePath() {
        return imagePath;
    }

    public Map<Customer, Integer> getRatings(){
        return ratings;
    }

    // setters:
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
}
