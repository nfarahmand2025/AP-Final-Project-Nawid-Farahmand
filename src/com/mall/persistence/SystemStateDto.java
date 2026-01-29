package com.mall.persistence;

import java.util.List;

import com.mall.model.Product;
import com.mall.model.User;
import com.mall.model.SaleRecord;

public class SystemStateDto {
    private List<Product> products;
    private List<User> users;
    private List<SaleRecord> sales;

    public SystemStateDto(List<Product> products, List<User> users, List<SaleRecord> sales) {
        this.products = products;
        this.users = users;
        this.sales = sales;
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<SaleRecord> getSales() {
        return sales;
    }
}
