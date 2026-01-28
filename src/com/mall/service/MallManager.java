package com.mall.service;

import com.mall.persistence.*;
import java.io.IOException;

public class MallManager {
    private ProductCatalog catalog;
    private AuthenticationService authService;
    private SaleService saleService;
    private JsonDataHandler dataHandler;
    private final String DATA_PATH = "data.json";

    public MallManager() {
        this.catalog = new ProductCatalog();
        this.saleService = new SaleService(this);
        this.dataHandler = new JsonDataHandler();
        loadData();
    }

    public void loadData() {
        try {
            SystemStateDto state = dataHandler.load(DATA_PATH);
            this.catalog.setProducts(state.getProducts());
            this.authService = new AuthenticationService(state.getUsers());
            this.saleService.setSales(state.getSales());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveData() {
        try {
            SystemStateDto state = new SystemStateDto(
                    catalog.getAllProducts(),
                    authService.getAllUsers(),
                    saleService.getAllSales());
            dataHandler.save(DATA_PATH, state);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ProductCatalog getCatalog() {
        return catalog;
    }

    public AuthenticationService getAuthService() {
        return authService;
    }

    public SaleService getSaleService() {
        return saleService;
    }
}