package com.mall.model;

public abstract class User {
    protected String id;
    protected String username;
    protected String password;
    protected UserRole role;

    public enum UserRole {
        ADMIN, CUSTOMER
    }

    public User(String id, String username, String password, UserRole role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getId()
    {
        return id;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public UserRole getRole()
    {
        return role;
    }
}
