package com.mall.persistence;

import java.util.List;

import com.mall.model.Product;
import com.mall.model.User;
import com.mall.model.SaleRecord;

/**
 * DTO class that stores the current system state for persistence.
 * Contains products, users, and sales records.
 */
public class SystemStateDto {

    // List of all products in the system
    private List<Product> products;

    // List of all users (customers and admins)
    private List<User> users;

    // List of sales transaction records
    private List<SaleRecord> sales;

    /**
     * Creates a SystemStateDto containing system data collections.
     */
    public SystemStateDto(List<Product> products, List<User> users, List<SaleRecord> sales) {
        this.products = products;
        this.users = users;
        this.sales = sales;
    }

    // Returns list of products
    public List<Product> getProducts() {
        return products;
    }

    // Returns list of users
    public List<User> getUsers() {
        return users;
    }

    // Returns list of sales records
    public List<SaleRecord> getSales() {
        return sales;
    }
}
