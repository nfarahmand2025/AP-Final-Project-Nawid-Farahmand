package com.mall.persistence;

import java.util.List;

import com.mall.model.Product;
import com.mall.model.User;
import com.mall.model.SaleRecord;

public class SystemStateDto {
    // DTO fields: store lists representing the current saved state of the system
    private List<Product> products;
    private List<User> users;
    private List<SaleRecord> sales;

    // Constructor: initialize the DTO with lists of products, users, and sales
    public SystemStateDto(List<Product> products, List<User> users, List<SaleRecord> sales) {
        this.products = products;
        this.users = users;
        this.sales = sales;
    }

    // Getter for the list of products contained in the system state
    public List<Product> getProducts() {
        return products;
    }

    // Getter for the list of users contained in the system state
    public List<User> getUsers() {
        return users;
    }

    // Getter for the list of sales records contained in the system state
    public List<SaleRecord> getSales() {
        return sales;
    }
}