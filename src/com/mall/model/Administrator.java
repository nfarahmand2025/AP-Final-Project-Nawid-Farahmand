package com.mall.model;

/**
 * Represents a user with elevated privileges to manage the product catalog.
 * Admins can add, edit, or remove products from the system.
 */
public class Administrator extends User {

    /**
     * Constructs an Administrator. Role is automatically set to ADMIN.
     */
    public Administrator(String id, String username, String password) {
        super(id, username, password, UserRole.ADMIN);
    }
}