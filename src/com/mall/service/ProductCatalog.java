package com.mall.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mall.model.Product;

public class ProductCatalog {

    // Internal storage: preserves insertion order and maps product ID -> Product
    private final Map<String, Product> products = new LinkedHashMap<>();

    // Replace current products with a fresh list of loaded products.
    // Clears existing map and re-populates it using each product's id as the key.
    public void setProducts(List<Product> loadedProducts) {
        products.clear();
        for (Product p : loadedProducts) {
            products.put(p.getId(), p);
        }
    }

    // Return a copy of all products as a List to avoid exposing internal map.
    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    // Add a new product only if it's non-null and has a non-null id,
    // and there isn't already a product with the same id.
    // Returns true when insertion succeeded, false otherwise.
    public boolean addProduct(Product p) {
        if (p == null || p.getId() == null)
            return false;
        if (products.containsKey(p.getId()))
            return false;
        products.put(p.getId(), p);
        return true;
    }

    // Retrieve a product by id wrapped in Optional to signal presence/absence.
    public Optional<Product> getProductById(String id) {
        return Optional.ofNullable(products.get(id));
    }

    // Update an existing product: requires non-null product and id,
    // and that a product with the given id already exists.
    // Returns true if update occurred, false otherwise.
    public boolean updateProduct(Product updated) {
        if (updated == null || updated.getId() == null)
            return false;
        if (!products.containsKey(updated.getId()))
            return false;
        products.put(updated.getId(), updated);
        return true;
    }

    // Remove a product by id. Returns true if a product was removed.
    public boolean removeProduct(String id) {
        return products.remove(id) != null;
    }

    // Search products by name (case-insensitive substring match).
    // If query is null or blank, return all products.
    public List<Product> searchByName(String q) {
        if (q == null || q.isBlank())
            return getAllProducts();
        String s = q.toLowerCase();
        return products.values().stream()
                // Filter products whose name contains the query substring (case-insensitive).
                .filter(p -> p.getName().toLowerCase().contains(s))
                .collect(Collectors.toList());
    }

    // Filter products by exact category match (case-insensitive).
    // If category is null or blank, return all products.
    public List<Product> filterByCategory(String category) {
        if (category == null || category.isBlank())
            return getAllProducts();
        String c = category.toLowerCase();
        return products.values().stream()
                // Keep products that have a non-null category equal to the requested one.
                .filter(p -> p.getCategory() != null && p.getCategory().toLowerCase().equals(c))
                .collect(Collectors.toList());
    }

    // Return products sorted by price. When ascending is true sorts low->high,
    // otherwise sorts high->low. Uses Product::getPrice for comparison.
    public List<Product> sortByPrice(boolean ascending) {
        Comparator<Product> cmp = Comparator.comparing(Product::getPrice);
        if (!ascending)
            cmp = cmp.reversed();
        return products.values().stream()
                .sorted(cmp)
                .collect(Collectors.toList());
    }
}
