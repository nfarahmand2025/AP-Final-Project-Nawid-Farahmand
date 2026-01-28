package com.mall.service;

import com.mall.model.User;
import com.mall.model.Customer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AuthenticationService {
    private List<User> users;
    private User currentUser;

    public AuthenticationService(List<User> users) {
        this.users = users;
    }

    public List<User> getAllUsers() {
        return users;
    }

    public void registerCustomer(String username, String password) {
        Customer newCustomer = new Customer(
                UUID.randomUUID().toString(),
                username,
                password,
                BigDecimal.ZERO);
        users.add(newCustomer);
    }

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

    public User getCurrentUser() {
        return currentUser;
    }

    public Customer getCurrentCustomer() {
        if (currentUser instanceof com.mall.model.Customer) {
            return (com.mall.model.Customer) currentUser;
        }
        return null;
    }

    public boolean isCustomer() {
        return currentUser instanceof Customer;
    }

    public void depositBalance(BigDecimal amount) {
        if (currentUser instanceof Customer) {
            Customer c = (Customer) currentUser;
            c.setBalance(c.getBalance().add(amount));
        }
    }
}