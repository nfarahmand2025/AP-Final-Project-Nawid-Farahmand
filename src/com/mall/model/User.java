package com.mall.model;

import java.util.Objects;

/**
 * Abstract base class representing a generic user in the system.
 * Demonstrates Abstraction and Inheritance.
 */
public abstract class User {
    protected String id;
    protected String username;
    protected String password;
    protected UserRole role;

    /**
     * Defines the types of users allowed in the Shopping Mall system.
     */
    public enum UserRole {
        ADMIN, CUSTOMER
    }

    public User(String id, String username, String password, UserRole role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof User))
            return false;
        User user = (User) o;
        return Objects.equals(id, user.id) || Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
}