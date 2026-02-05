package com.mall.service;

import com.mall.model.SaleRecord;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages transaction history and revenue reporting.
 * Allows Administrators to monitor sales.
 */
public class SaleService {
    private List<SaleRecord> sales = new ArrayList<>();
    private final MallManager manager;

    /**
     * @param manager The central controller used to trigger data persistence.
     */
    public SaleService(MallManager manager) {
        this.manager = manager;
    }

    public void setSales(List<SaleRecord> loadedSales) {
        if (loadedSales != null) {
            this.sales = new ArrayList<>(loadedSales);
        }
    }

    /**
     * @return A copy of all sales records for administrative review.
     */
    public List<SaleRecord> getAllSales() {
        return new ArrayList<>(sales);
    }

    /**
     * Records a new transaction and persists it to storage.
     */
    public void addSale(SaleRecord record) {
        if (record != null) {
            this.sales.add(record);
            manager.saveData();
        }
    }

    /**
     * Removes a specific transaction record.
     */
    public void deleteRecord(String transactionId) {
        boolean removed = sales.removeIf(s -> s.getTransactionId().equals(transactionId));
        if (removed) {
            manager.saveData();
        }
    }

    /**
     * Calculates total revenue from all completed sales.
     */
    public BigDecimal getTotalRevenue() {
        return sales.stream()
                .map(SaleRecord::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
