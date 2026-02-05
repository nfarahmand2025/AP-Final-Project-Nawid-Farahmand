package com.mall.service;

import com.mall.persistence.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * The central controller for the Shopping Mall application.
 * Orchestrates the interaction between the persistence layer and business
 * services.
 */
public class MallManager {
    private final ProductService productService;
    private AuthenticationService authService;
    private final SaleService saleService;
    private final DataStorageInterface dataHandler;
    private final String DATA_PATH = "data.json";

    /**
     * Initializes the system, sets up services, and loads initial state from
     * storage.
     */
    public MallManager() {
        this.productService = new ProductService();
        this.saleService = new SaleService(this);
        this.dataHandler = new JsonDataHandler();

        // Initial setup of authService with empty list in case file doesn't exist
        this.authService = new AuthenticationService(new ArrayList<>());

        loadData();
    }

    /**
     * Loads the system state from the persistence layer.
     * Maps DTO data into the respective service modules.
     */
    public void loadData() {
        try {
            SystemStateDto state = dataHandler.load(DATA_PATH);

            // Re-populate the products
            this.productService.setProducts(state.getProducts());

            // Re-initialize the auth service with persistent user data
            this.authService = new AuthenticationService(state.getUsers());

            // Load historical sales
            this.saleService.setSales(state.getSales());

            System.out.println("System data loaded successfully.");
        } catch (IOException e) {
            System.err.println("Failed to load data. Starting with a fresh state.");
            e.printStackTrace();
        }
    }

    /**
     * Collects data from all services and persists it using the DataHandler.
     * Called whenever a significant state change occurs (e.g., add product,
     * checkout).
     */
    public void saveData() {
        try {
            SystemStateDto state = new SystemStateDto(
                    productService.getAllProducts(),
                    authService.getAllUsers(),
                    saleService.getAllSales());

            dataHandler.save(DATA_PATH, state);
        } catch (IOException e) {
            System.err.println("Critical Error: Failed to save system state.");
            e.printStackTrace();
        }
    }

    // --- Service Accessors for the GUI ---

    public ProductService getProductService() {
        return productService;
    }

    public AuthenticationService getAuthService() {
        return authService;
    }

    public SaleService getSaleService() {
        return saleService;
    }
}
