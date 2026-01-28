package com.mall.service;

import com.mall.model.SaleRecord;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SaleService {
    private List<SaleRecord> sales = new ArrayList<>();
    private MallManager manager;

    public SaleService(MallManager manager) {
        this.manager = manager;
    }

    public void setSales(List<SaleRecord> loadedSales) {
        this.sales = new ArrayList<>(loadedSales);
    }

    public List<SaleRecord> getAllSales() {
        return new ArrayList<>(sales);
    }

    public void addSale(SaleRecord record) {
        if (record != null) {
            this.sales.add(record);
            manager.saveData();
        }
    }

    public void deleteRecord(String transactionId) {
        sales.removeIf(s -> s.getTransactionId().equals(transactionId));
        manager.saveData();
    }

    public void clearHistory() {
        sales.clear();
        manager.saveData();
    }

    public BigDecimal getTotalRevenue() {
        return sales.stream()
                .map(SaleRecord::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}