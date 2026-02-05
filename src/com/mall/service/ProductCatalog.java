package com.mall.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mall.model.Product;

/**
 * ProductCatalog is a simple in-memory product repository.
 *
 * <p>It stores products in insertion order (LinkedHashMap), provides basic CRUD
 * operations, simple search/filter and price-based sorting. This class is not
 * thread-safe and is intended for single-threaded or externally-synchronized
 * usage (e.g. during application startup or in tests).</p>
 */
public class ProductCatalog {

    /**
     * The backing map holding products keyed by their id. LinkedHashMap keeps
     * insertion order when returning all products.
     */
    private final Map<String, Product> products = new LinkedHashMap<>();

    /**
     * Replace the existing catalog contents with the provided list. Existing
     * entries are cleared and replaced in the same order as the list.
     *
     * @param loadedProducts list of products to load into the catalog
     */
    public void setProducts(List<Product> loadedProducts) {
        products.clear();
        for (Product p : loadedProducts) {
            products.put(p.getId(), p);
        }
    }

    /**
     * Return a copy of all products in insertion order. A new list is returned
     * to avoid exposing internal collection state.
     *
     * @return list of all products
     */
    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    /**
     * Add a new product to the catalog. The method validates the product and
     * its id and prevents overwriting an existing entry.
     *
     * @param p product to add
     * @return true if the product was added, false otherwise
     */
    public boolean addProduct(Product p) {
        if (p == null || p.getId() == null)
            return false;
        if (products.containsKey(p.getId()))
            return false;
        products.put(p.getId(), p);
        return true;
    }

    /**
     * Retrieve a product by its id.
     *
     * @param id product id
     * @return Optional containing the product if found, or empty otherwise
     */
    public Optional<Product> getProductById(String id) {
        return Optional.ofNullable(products.get(id));
    }

    /**
     * Update an existing product in the catalog. The method requires that the
     * product with the given id already exists; otherwise it returns false.
     *
     * @param updated product with updated values
     * @return true if the product was updated, false otherwise
     */
    public boolean updateProduct(Product updated) {
        if (updated == null || updated.getId() == null)
            return false;
        if (!products.containsKey(updated.getId()))
            return false;
        products.put(updated.getId(), updated);
        return true;
    }

    /**
     * Remove a product from the catalog by id.
     *
     * @param id product id to remove
     * @return true if a product was removed, false if no product with the id
     * existed
     */
    public boolean removeProduct(String id) {
        return products.remove(id) != null;
    }

    /**
     * Search products by name. If the query is null or blank, all products are
     * returned. The search is case-insensitive and matches substrings.
     *
     * @param q search query
     * @return list of products matching the name query
     */
    public List<Product> searchByName(String q) {
        if (q == null || q.isBlank())
            return getAllProducts();
        String s = q.toLowerCase();
        return products.values().stream()
                .filter(p -> p.getName().toLowerCase().contains(s))
                .collect(Collectors.toList());
    }

    /**
     * Filter products by category. Comparison is case-insensitive. If the
     * category argument is null or blank, all products are returned.
     *
     * @param category category name
     * @return list of products in the given category
     */
    public List<Product> filterByCategory(String category) {
        if (category == null || category.isBlank())
            return getAllProducts();
        String c = category.toLowerCase();
        return products.values().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().toLowerCase().equals(c))
                .collect(Collectors.toList());
    }

    /**
     * Return products sorted by price. The original catalog ordering is not
     * modified because this method returns a new, sorted list.
     *
     * @param ascending true for ascending order, false for descending
     * @return list of products sorted by price
     */
    public List<Product> sortByPrice(boolean ascending) {
        Comparator<Product> cmp = Comparator.comparing(Product::getPrice);
        if (!ascending)
            cmp = cmp.reversed();
        return products.values().stream()
                .sorted(cmp)
                .collect(Collectors.toList());
    }
}
