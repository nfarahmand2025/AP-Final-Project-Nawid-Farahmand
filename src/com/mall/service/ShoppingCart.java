package com.mall.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mall.model.CartItem;
import com.mall.model.Product;

public class ShoppingCart {

    private final List<CartItem> items = new ArrayList<>();

    public void addProduct(Product product, int qty) {
        if (product == null)
            throw new IllegalArgumentException("Product is null");
        if (qty <= 0)
            throw new IllegalArgumentException("Quantity must be > 0");
        if (qty > product.getStockQty())
            throw new IllegalArgumentException("Not enough stock");

        Optional<CartItem> existing = items.stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + qty;
            if (newQty > product.getStockQty())
                throw new IllegalArgumentException("Not enough stock for combined quantity");
            item.setQuantity(newQty);
        } else {
            items.add(new CartItem(product, qty));
        }
    }

    public void updateProductQuantity(String productId, int qty) {
        items.stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    if (qty <= 0)
                        items.remove(item);
                    else {
                        if (qty > item.getProduct().getStockQty())
                            throw new IllegalArgumentException("Not enough stock to set this quantity");
                        item.setQuantity(qty);
                    }
                });
    }

    public void removeProduct(String productId) {
        items.removeIf(i -> i.getProduct().getId().equals(productId));
    }

    public void remove(CartItem item) {
        items.remove(item);
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            total = total.add(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return total;
    }

    public boolean validateStock() {
        for (CartItem item : items) {
            if (item.getQuantity() > item.getProduct().getStockQty())
                return false;
        }
        return true;
    }

    public boolean checkout() {
        if (!validateStock())
            return false;

        for (CartItem item : items) {
            Product p = item.getProduct();
            p.setStockQty(p.getStockQty() - item.getQuantity());
        }

        items.clear();
        return true;
    }
}
