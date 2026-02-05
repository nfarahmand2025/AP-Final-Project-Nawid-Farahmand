package com.mall.service;

import com.mall.model.User;
import com.mall.model.Customer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for user authentication and session management.
 */
public class AuthenticationService {
    private final List<User> users;
    private User currentUser;

    /**
     * @param users The global list of users loaded from the persistence layer.
     */
    public AuthenticationService(List<User> users) {
        this.users = users;
    }

    /**
     * Registers a new customer with a unique ID and zero balance.
     * Includes a check to prevent duplicate usernames.
     *
     * @return true if registration successful, false if username taken.
     */
    public boolean registerCustomer(String username, String password) {
        boolean exists = users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
        if (exists)
            return false;

        Customer newCustomer = new Customer(
                UUID.randomUUID().toString(),
                username,
                password,
                BigDecimal.ZERO);
        users.add(newCustomer);
        return true;
    }

    /**
     * Validates credentials and sets the active session.
     *
     * @return true if credentials match a known user.
     */
    public boolean login(String username, String password) {
        Optional<User> found = users.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst();

        if (found.isPresent()) {
            this.currentUser = found.get();
            return true;
        }
        return false;
    }

/**
 * Clears the current session. Essential for multi-user support.
 */
