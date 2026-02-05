package com.mall.model;

import java.math.BigDecimal;
import com.mall.service.ShoppingCart;

/**
 * Represents a customer who can browse products and manage a shopping cart.
 * Includes balance management for bonus point functionality.
 */
public class Customer extends User {
    private final ShoppingCart cart;
    private BigDecimal balance;

    /**
     * Initializes a new Customer with a unique ID and starting balance.
     */
    public Customer(String id, String username, String password, BigDecimal balance) {
        super(id, username, password, UserRole.CUSTOMER);
        this.balance = balance;
        this.cart = new ShoppingCart();
    }

    /**
     * @return The current funds available for the customer to make purchases.
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Updates the customer's balance (e.g., after a successful checkout).
     */
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    /**
     * Provides access to the customer's active shopping cart.
     */
    public ShoppingCart getCart() {
        return cart;
    }
}