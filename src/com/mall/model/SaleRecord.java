package com.mall.model;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class SaleRecord {
    private String transactionId;
    private String customerUsername;
    private String productName;
    private int quantity;
    private BigDecimal amountPaid;
    private LocalDateTime date;

    public SaleRecord(String transactionId, String customerUsername, String productName, int quantity,
            BigDecimal amountPaid, LocalDateTime date) {
        this.transactionId = transactionId;
        this.customerUsername = customerUsername;
        this.productName = productName;
        this.quantity = quantity;
        this.amountPaid = amountPaid;
        this.date = date;
    }

    // Getters
    public String getTransactionId() {
        return transactionId;
    }

    public String getCustomerUsername() {
        return customerUsername;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public LocalDateTime getDate() {
        return date;
    }
}