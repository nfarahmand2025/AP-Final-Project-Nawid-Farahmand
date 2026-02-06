package com.mall.persistence;

import com.mall.model.*;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles JSON-like persistence for the Shopping Mall system.
 * Implements DataStorageInterface to ensure modularity and loose coupling.
 */
public class JsonDataHandler implements DataStorageInterface {

    // --- Save with Helper Methods for Serialization ---
    @Override
    public void save(String filePath, SystemStateDto state) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("{\n");

            // 1. Users
            writer.write("  \"users\": [\n");
            List<User> users = state.getUsers();
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                // If the user is a Customer, include their balance; admins get balance 0
                String balance = (u instanceof Customer) ? ((Customer) u).getBalance().toString() : "0";
                writer.write(String.format(
                        "    {\"role\":\"%s\", \"id\":\"%s\", \"user\":\"%s\", \"pass\":\"%s\", \"bal\":\"%s\"}",
                        u.getRole(), u.getId(), u.getUsername(), u.getPassword(), balance));
                if (i < users.size() - 1)
                    writer.write(",");
                writer.write("\n");
            }
            writer.write("  ],\n");

            // 2. Products
            writer.write("  \"products\": [\n");
            List<Product> products = state.getProducts();
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                // Serialize each product including ratings by calling serializeRatings
                writer.write(String.format(
                        "    {\"id\":\"%s\", \"name\":\"%s\", \"cat\":\"%s\", \"price\":\"%s\", \"stock\":%d, \"desc\":\"%s\", \"img\":\"%s\", \"ratings\":%s}",
                        p.getId(), p.getName(), p.getCategory(), p.getPrice(), p.getStockQty(),
                        p.getDescription(), p.getImagePath(), serializeRatings(p.getRatings())));
                if (i < products.size() - 1)
                    writer.write(",");
                writer.write("\n");
            }
            writer.write("  ],\n");
            // 3. Sales
            writer.write("  \"sales\": [\n");
            List<SaleRecord> sales = state.getSales();
            for (int i = 0; i < sales.size(); i++) {
                SaleRecord s = sales.get(i);
                // Each sale record is written with transaction id, user, product, quantity, amount and date
                writer.write(String.format(
                        "    {\"tid\":\"%s\", \"user\":\"%s\", \"prod\":\"%s\", \"qty\":%d, \"amt\":\"%s\", \"date\":\"%s\"}",
                        s.getTransactionId(), s.getCustomerUsername(), s.getProductName(),
                        s.getQuantity(), s.getAmountPaid(), s.getDate()));
                if (i < sales.size() - 1)
                    writer.write(",");
                writer.write("\n");
            }
            writer.write("  ],\n");

            // 4. Carts
            writer.write("  \"carts\": [\n");
            // Collect only customers (admins don't have carts)
            List<Customer> customers = users.stream()
                    .filter(u -> u instanceof Customer)
                    .map(u -> (Customer) u)
                    .toList();

            for (int i = 0; i < customers.size(); i++) {
                Customer c = customers.get(i);
                // Serialize cart items for each customer using serializeCart
                writer.write(String.format("    {\"userId\":\"%s\", \"items\":%s}",
                        c.getId(), serializeCart(c)));
                if (i < customers.size() - 1)
                    writer.write(",");
                writer.write("\n");
            }
            writer.write("  ]\n}");
        }
    }

    // Convert ratings map (Customer -> Integer) into a JSON-like object where keys are customer IDs
    private String serializeRatings(Map<Customer, Integer> ratings) {
        if (ratings == null || ratings.isEmpty())
            return "{}";
        return "{" + ratings.entrySet().stream()
                .map(e -> "\"" + e.getKey().getId() + "\":" + e.getValue())
                .collect(Collectors.joining(",")) + "}";
    }

    // Convert a customer's cart into a JSON-like array of {pid, qty} objects
    private String serializeCart(Customer customer) {
        List<CartItem> items = customer.getCart().getItems();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            sb.append(String.format("{\"pid\":\"%s\", \"qty\":%d}",
                    item.getProduct().getId(), item.getQuantity()));
            if (i < items.size() - 1)
                sb.append(",");
        }
        return sb.append("]").toString();
    }

    // --- Loading with Parsing Logic ---

    @Override
    public SystemStateDto load(String filePath) throws IOException {
        List<Product> products = new ArrayList<>();
        List<User> users = new ArrayList<>();
        List<SaleRecord> sales = new ArrayList<>();
        List<String> cartLines = new ArrayList<>();

        File file = new File(filePath);
        if (!file.exists())
            // If file doesn't exist, return empty state placeholders
            return new SystemStateDto(products, users, sales);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String section = "";
            while ((line = reader.readLine()) != null) {
                // Detect which top-level section we are in by matching section names
                if (line.contains("\"users\"")) {
                    section = "U";
                    continue;
                }
                if (line.contains("\"products\"")) {
                    section = "P";
                    continue;
                }
                if (line.contains("\"sales\"")) {
                    section = "S";
                    continue;
                }
                if (line.contains("\"carts\"")) {
                    section = "C";
                    continue;
                }

                // Each object line starts with '{' according to the save format
                if (line.trim().startsWith("{")) {
                    if (section.equals("U"))
                        users.add(parseUser(line));
                    else if (section.equals("P"))
                        products.add(parseProduct(line, users));
                    else if (section.equals("S"))
                        sales.add(parseSale(line));
                    else if (section.equals("C"))
                        // Save cart lines to process after users/products are available
                        cartLines.add(line);
                }
            }
        }

        // Rebuild carts after all users and products are loaded
        for (String cLine : cartLines) {
            String uId = extract(cLine, "userId");
            users.stream()
                    .filter(u -> u.getId().equals(uId) && u instanceof Customer)
                    .map(u -> (Customer) u)
                    .findFirst()
                    .ifPresent(customer -> rebuildCartFromLine(customer, cLine, products));
        }

        return new SystemStateDto(products, users, sales);
    }

    // Parse a single user line into either Administrator or Customer based on role
    private User parseUser(String l) {
        if (extract(l, "role").equals("ADMIN"))
            return new Administrator(
                    extract(l, "id"),
                    extract(l, "user"),
                    extract(l, "pass"));
        return new Customer(
                extract(l, "id"),
                extract(l, "user"),
                extract(l, "pass"),
                new BigDecimal(extract(l, "bal")));
    }

    // Rebuild customer's cart from serialized items using product catalog to find product objects
    private void rebuildCartFromLine(Customer c, String line, List<Product> catalog) {
        int start = line.indexOf("[") + 1;
        int end = line.lastIndexOf("]");
        if (start >= end)
            return;

        String itemsPart = line.substring(start, end).trim();
        if (itemsPart.isEmpty())
            return;

        // Split entries on '},{' with optional whitespace — this produces each item JSON-ish chunk
        String[] entries = itemsPart.split("\\},\\s*\\{");
        for (String entry : entries) {
            // Ensure each entry is a well-formed brace-enclosed object
            if (!entry.startsWith("{"))
                entry = "{" + entry;
            if (!entry.endsWith("}"))
                entry = entry + "}";

            String pid = extract(entry, "pid");
            int qty = Integer.parseInt(extract(entry, "qty"));

            // Find the matching product in the loaded catalog and add to the customer's cart
            catalog.stream()
                    .filter(p -> p.getId().equals(pid))
                    .findFirst()
                    .ifPresent(p -> c.getCart().addProduct(p, qty));
        }
    }

    // Extract a value for a given key from a JSON-like line. Handles both quoted and unquoted numeric values.
    private String extract(String line, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = line.indexOf(pattern) + pattern.length();
        int end = line.indexOf("\"", start);
        if (line.indexOf(pattern) == -1) {
            // If not found as a quoted string value, try unquoted value (e.g., numbers) with ':' delimiter
            pattern = "\"" + key + "\":";
            start = line.indexOf(pattern) + pattern.length();
            end = line.indexOf(",", start);
            if (end == -1)
                end = line.indexOf("}", start);
        }
        return line.substring(start, end).trim();
    }

    // Parse a product line into a Product object and attempt to attach ratings
    private Product parseProduct(String l, List<User> users) {
        Product p = new Product(
                extract(l, "id"),
                extract(l, "name"),
                extract(l, "cat"),
                new BigDecimal(extract(l, "price")),
                Integer.parseInt(extract(l, "stock")),
                extract(l, "desc"),
                extract(l, "img"));
        // Load ratings if present
        parseRatingsInto(p, l, users);
        return p;
    }

    // Parse the ratings object within a product line and attach ratings using customer references
    private void parseRatingsInto(Product p, String line, List<User> users) {
        if (line.contains("\"ratings\":{")) {
            int start = line.indexOf("\"ratings\":{") + 10;
            int end = line.indexOf("}", start);
            String content = line.substring(start + 1, end).trim();
            if (content.isEmpty())
                return;

            // Each pair looks like "userId":value
            String[] pairs = content.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":");
                String userId = kv[0].replace("\"", "").trim();
                int val = Integer.parseInt(kv[1].trim());
                Customer customer = null;
                // Find the Customer object by ID from the already parsed users list
                for (User u : users) {
                    if (userId.equals(u.getId())) {
                        customer = (Customer) u;
                        break;
                    }
                }
                if (customer == null) {
                    // If a referenced customer is missing, log a warning to stderr
                    System.err.println("customer with " + userId + " ID not found in the system!");
                }
                // Even if customer is null, the method call tries to add/update rating with possibly null customer
                p.addOrUpdateRating(customer, val);
            }
        } else {
            // If ratings key is missing, log a note (could be normal for unrated items)
            System.err.println("Rating for product " + p.getId() + "does not exist!");
        }
    }

    // Parse a sale record line into a SaleRecord object
    private SaleRecord parseSale(String l) {
        return new SaleRecord(
                extract(l, "tid"),
                extract(l, "user"),
                extract(l, "prod"),
                Integer.parseInt(extract(l, "qty")),
                new BigDecimal(extract(l, "amt")),
                LocalDateTime.parse(extract(l, "date")));
    }
}