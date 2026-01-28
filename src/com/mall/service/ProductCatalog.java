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

    private final Map<String, Product> products = new LinkedHashMap<>();

    public void setProducts(List<Product> loadedProducts) {
        products.clear();
        for (Product p : loadedProducts) {
            products.put(p.getId(), p);
        }
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    public boolean addProduct(Product p) {
        if (p == null || p.getId() == null)
            return false;
        if (products.containsKey(p.getId()))
            return false;
        products.put(p.getId(), p);
        return true;
    }

    public Optional<Product> getProductById(String id) {
        return Optional.ofNullable(products.get(id));
    }

    public boolean updateProduct(Product updated) {
        if (updated == null || updated.getId() == null)
            return false;
        if (!products.containsKey(updated.getId()))
            return false;
        products.put(updated.getId(), updated);
        return true;
    }

    public boolean removeProduct(String id) {
        return products.remove(id) != null;
    }

    public List<Product> searchByName(String q) {
        if (q == null || q.isBlank())
            return getAllProducts();
        String s = q.toLowerCase();
        return products.values().stream()
                .filter(p -> p.getName().toLowerCase().contains(s))
                .collect(Collectors.toList());
    }

    public List<Product> filterByCategory(String category) {
        if (category == null || category.isBlank())
            return getAllProducts();
        String c = category.toLowerCase();
        return products.values().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().toLowerCase().equals(c))
                .collect(Collectors.toList());
    }

    public List<Product> sortByPrice(boolean ascending) {
        Comparator<Product> cmp = Comparator.comparing(Product::getPrice);
        if (!ascending)
            cmp = cmp.reversed();
        return products.values().stream()
                .sorted(cmp)
                .collect(Collectors.toList());
    }
}
