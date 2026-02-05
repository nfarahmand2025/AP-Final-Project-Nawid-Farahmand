package com.mall.persistence;

import com.mall.model.*;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple JSON-like persistence handler for the shopping mall system.
 * Uses manual string serialization/parsing (no JSON library).
 */
public class JsonDataHandler implements DataStorageInterface {

    // --- Save with Helper Methods for Serialization ---

    /**
     * Persist the given SystemStateDto to a file in a JSON-like format.
     * Top-level arrays: "users", "products", "sales", "carts".
     *
     * @param filePath file to write to
     * @param state    system state to persist
     * @throws IOException on I/O error
     */
    @Override
    public void save(String filePath, SystemStateDto state) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("{\n");

            // --- 1. Users: serialize all users (Administrator or Customer) ---
            writer.write("  \"users\": [\n");
            List<User> users = state.getUsers();
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                // Customer has balance, Administrators are saved with balance "0"
                String balance = (u instanceof Customer) ? ((Customer) u).getBalance().toString() : "0";
                writer.write(String.format(
                        "    {\"role\":\"%s\", \"id\":\"%s\", \"user\":\"%s\", \"pass\":\"%s\", \"bal\":\"%s\"}",
                        u.getRole(), u.getId(), u.getUsername(), u.getPassword(), balance));
                if (i < users.size() - 1)
                    writer.write(",");
                writer.write("\n");
            }
            writer.write("  ],\n");

            // --- 2. Products: serialize product catalog and ratings ---
            writer.write("  \"products\": [\n");
            List<Product> products = state.getProducts();
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                writer.write(String.format(
                        "    {\"id\":\"%s\", \"name\":\"%s\", \"cat\":\"%s\", \"price\":\"%s\", \"stock\":%d, \"desc\":\"%s\", \"img\":\"%s\", \"ratings\":%s}",
                        p.getId(), p.getName(), p.getCategory(), p.getPrice(), p.getStockQty(),
                        p.getDescription(), p.getImagePath(), serializeRatings(p.getRatings())));
                if (i < products.size() - 1)
                    writer.write(",");
                writer.write("\n");
            }
            writer.write("  ],\n");

            // --- 3. Sales: serialize sales history (one record per entry) ---
            writer.write("  \"sales\": [\n");
            List<SaleRecord> sales = state.getSales();
            for (int i = 0; i < sales.size(); i++) {
                SaleRecord s = sales.get(i);
                writer.write(String.format(
                        "    {\"tid\":\"%s\", \"user\":\"%s\", \"prod\":\"%s\", \"qty\":%d, \"amt\":\"%s\", \"date\":\"%s\"}",
                        s.getTransactionId(), s.getCustomerUsername(), s.getProductName(),
                        s.getQuantity(), s.getAmountPaid(), s.getDate()));
                if (i < sales.size() - 1)
                    writer.write(",");
                writer.write("\n");
            }
            writer.write("  ],\n");

            // --- 4. Carts: serialize each customer's cart as product id + qty pairs ---
            writer.write("  \"carts\": [\n");
            List<Customer> customers = users.stream()
                    .filter(u -> u instanceof Customer)
                    .map(u -> (Customer) u)
                    .toList();

            for (int i = 0; i < customers.size(); i++) {
                Customer c = customers.get(i);
                writer.write(String.format("    {\"userId\":\"%s\", \"items\":%s}",
                        c.getId(), serializeCart(c)));
                if (i < customers.size() - 1)
                    writer.write(",");
                writer.write("\n");
            }
            writer.write("  ]\n}");
        }
    }

    /**
     * Convert ratings map (Customer -> Integer) into a JSON-like object string.
     *
     * @param ratings ratings map
     * @return serialized ratings or "{}" if empty
     */
    private String serializeRatings(Map<Customer, Integer> ratings) {
        if (ratings == null || ratings.isEmpty())
            return "{}";
        return "{" + ratings.entrySet().stream()
                .map(e -> "\"" + e.getKey().getId() + "\":" + e.getValue())
                .collect(Collectors.joining(",")) + "}";
    }

    /**
     * Serialize a customer's cart as an array of {"pid":"...", "qty":N} items.
     *
     * @param customer the customer
     * @return serialized cart array
     */
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

    /**
     * Load persisted state from file and rebuild products, users and sales.
     * Expects the same compact JSON-like format produced by save(...).
     *
     * @param filePath path to read from
     * @return reconstructed SystemStateDto
     * @throws IOException on I/O error
     */
    @Override
    public SystemStateDto load(String filePath) throws IOException {
        List<Product> products = new ArrayList<>();
        List<User> users = new ArrayList<>();
        List<SaleRecord> sales = new ArrayList<>();
        List<String> cartLines = new ArrayList<>();

        File file = new File(filePath);
        if (!file.exists())
            return new SystemStateDto(products, users, sales);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String section = "";
            while ((line = reader.readLine()) != null) {
                // Detect section headers and switch parsing mode
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

                // Each object in the arrays starts with '{' (after indentation)
                if (line.trim().startsWith("{")) {
                    if (section.equals("U"))
                        users.add(parseUser(line));                     // parse user object
                    else if (section.equals("P"))
                        products.add(parseProduct(line, users));        // parse product + ratings
                    else if (section.equals("S"))
                        sales.add(parseSale(line));                     // parse sale record
                    else if (section.equals("C"))
                        cartLines.add(line);                            // collect cart lines for later linking
                }
            }
        }

        // Rebuild carts after users & products are loaded, matching by userId & productId
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

    /**
     * Parse a compact user object line into Administrator or Customer.
     *
     * @param l serialized user line
     * @return User instance
     */
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

    /**
     * Rebuild a customer's cart from its serialized line, resolving product ids.
     *
     * @param c       customer to populate
     * @param line    serialized cart line
     * @param catalog available products to resolve by id
     */
    private void rebuildCartFromLine(Customer c, String line, List<Product> catalog) {
        int start = line.indexOf("[") + 1;
        int end = line.lastIndexOf("]");
        if (start >= end)
            return;

        String itemsPart = line.substring(start, end).trim();
        if (itemsPart.isEmpty())
            return;

        // Split entries like {"pid":"p1","qty":2}, {"pid":"p2","qty":1}
        String[] entries = itemsPart.split("\\},\\s*\\{");
        for (String entry : entries) {
            if (!entry.startsWith("{"))
                entry = "{" + entry;
            if (!entry.endsWith("}"))
                entry = entry + "}";

            String pid = extract(entry, "pid");
            int qty = Integer.parseInt(extract(entry, "qty"));

            // Resolve product by id and add to cart (if found)
            catalog.stream()
                    .filter(p -> p.getId().equals(pid))
                    .findFirst()
                    .ifPresent(p -> c.getCart().addProduct(p, qty));
        }
    }

    /**
     * Extract the value for a key from a compact JSON-like single-line object.
     * Supports quoted ("key":"value") and unquoted ("key":value) values.
     *
     * @param line serialized object line
     * @param key  key to extract
     * @return extracted value string
     */
    private String extract(String line, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = line.indexOf(pattern) + pattern.length();
        int end = line.indexOf("\"", start);
        if (line.indexOf(pattern) == -1) {
            pattern = "\"" + key + "\":";
            start = line.indexOf(pattern) + pattern.length();
            end = line.indexOf(",", start);
            if (end == -1)
                end = line.indexOf("}", start);
        }
        return line.substring(start, end).trim();
    }

    /**
     * Build a Product from a product line and populate its ratings.
     *
     * @param l     product line
     * @param users loaded users (for resolving ratings' customer ids)
     * @return Product instance
     */
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

    /**
     * Parse embedded ratings ("ratings":{"userId":score,...}) and add them to product.
     * Logs a warning if a rating references an unknown user.
     *
     * @param p     product to populate
     * @param line  serialized product line
     * @param users loaded users
     */
    private void parseRatingsInto(Product p, String line, List<User> users) {
        if (line.contains("\"ratings\":{")) {
            int start = line.indexOf("\"ratings\":{") + 10;
            int end = line.indexOf("}", start);
            String content = line.substring(start + 1, end).trim();
            if (content.isEmpty())
                return;

            String[] pairs = content.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":");
                String userId = kv[0].replace("\"", "").trim();
                int val = Integer.parseInt(kv[1].trim());
                Customer customer = null;
                for (User u : users) {
                    if (userId.equals(u.getId())) {
                        customer = (Customer) u;
                        break;
                    }
                }
                if (customer == null) {
                    System.err.println("customer with " + userId + " ID not found in the system!");
                }
                p.addOrUpdateRating(customer, val);
            }
        } else {
            // Ratings missing for this product — non-fatal, logged for debugging
            System.err.println("Rating for product " + p.getId() + "does not exist!");
        }
    }

    /**
     * Parse a sale record line into a SaleRecord.
     *
     * @param l serialized sale line
     * @return SaleRecord instance
     */
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
