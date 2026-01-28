package com.mall.model;

import java.math.BigDecimal;

import com.mall.service.ShoppingCart;

public class Customer extends User {
    private final ShoppingCart cart;
    private BigDecimal balance;

    public Customer(String id, String username, String password, BigDecimal balance) {
        super(id, username, password, UserRole.CUSTOMER);
        this.balance = balance;
        this.cart = new ShoppingCart();
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public ShoppingCart getCart() {
        return cart;
    }
}
+