package com.mall.model;

/**
 * Represents a specific product and its quantity within a user's shopping cart.
 * This class allows the cart to track how many of each item are selected.
 */
public class CartItem {
    private final Product product;
    private int quantity;

    /**
     * @param product  The product being added to the cart.
     * @param quantity The initial number of units selected.
     */
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    /**
     * Updates the quantity of the item in the cart.
     * Useful for dynamic UI updates when the user increases/decreases count.
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return String.format("CartItem[product=%s, qty=%d]", product.getId(), quantity);
    }
}