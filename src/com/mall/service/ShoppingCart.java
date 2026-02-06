package com.mall.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mall.model.CartItem;
import com.mall.model.Product;

/**
 * Manages items selected by the customer for purchase.
 * Provides logic for price calculation and local stock validation.
 */
public class ShoppingCart {

    private final List<CartItem> items = new ArrayList<>();

    /**
     * Adds a product to the cart. If the product already exists, increases the
     * quantity.
     * 
     * @throws IllegalArgumentException if stock is insufficient.
     */
    public void addProduct(Product product, int qty) {
        if (product == null)
            throw new IllegalArgumentException("Product is null");
        if (qty <= 0)
            throw new IllegalArgumentException("Quantity must be > 0");

        Optional<CartItem> existing = items.stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + qty;
            if (newQty > product.getStockQty())
                throw new IllegalArgumentException("Not enough stock available");
            item.setQuantity(newQty);
        } else {
            if (qty > product.getStockQty())
                throw new IllegalArgumentException("Not enough stock available");
            items.add(new CartItem(product, qty));
        }
    }

    /**
     * Updates quantity of an item. Removes item if quantity is set to 0.
     */
    public void updateProductQuantity(String productId, int qty) {
        items.stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    if (qty <= 0) {
                        items.remove(item);
                    } else {
                        if (qty > item.getProduct().getStockQty())
                            throw new IllegalArgumentException("Insufficient stock");
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

    /**
     * @return Defensive copy of items in the cart to maintain encapsulation.
     */
    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Calculates the total price of all items using BigDecimal for precision.
     */
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
