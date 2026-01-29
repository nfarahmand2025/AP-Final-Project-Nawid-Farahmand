package com.mall.persistence;

import com.mall.model.*;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class JsonDataHandler implements DataStorageInterface {

    @Override
    public void save(String filePath, SystemStateDto state) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("{\n");

            // 1. Products
            writer.write("  \"products\": [\n");
            List<Product> products = state.getProducts();
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                writer.write(String.format(
                        "    {\"id\":\"%s\", \"name\":\"%s\", \"cat\":\"%s\", \"price\":\"%s\", \"stock\":%d, \"rating\":%s, \"img\":\"%s\"}",
                        p.getId(), p.getName(), p.getCategory(), p.getPrice(), p.getStockQty(), p.getAverageRating(),
                        p.getImagePath()));
                if (i < products.size() - 1)
                    writer.write(",");
                writer.write("\n");
            }
            writer.write("  ],\n");

            // 2. Users
            writer.write("  \"users\": [\n");
            List<User> users = state.getUsers();
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                String balance = (u instanceof Customer) ? ((Customer) u).getBalance().toString() : "0";
                writer.write(String.format(
                        "    {\"role\":\"%s\", \"id\":\"%s\", \"user\":\"%s\", \"pass\":\"%s\", \"bal\":\"%s\"}",
                        u.getRole(), u.getId(), u.getUsername(), u.getPassword(), balance));
                if (i < users.size() - 1)
                    writer.write(",");
                writer.write("\n");
            }
            writer.write("  ],\n");

            // 3. Sales
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

            // 4. Carts (The new independent section)
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
                if (line.contains("\"products\"")) {
                    section = "P";
                    continue;
                }
                if (line.contains("\"users\"")) {
                    section = "U";
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

                if (line.trim().startsWith("{")) {
                    if (section.equals("P"))
                        products.add(parseProduct(line));
                    else if (section.equals("U"))
                        users.add(parseUser(line));
                    else if (section.equals("S"))
                        sales.add(parseSale(line));
                    else if (section.equals("C"))
                        cartLines.add(line);
                }
            }
        }

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

    private void rebuildCartFromLine(Customer c, String line, List<Product> catalog) {
        int start = line.indexOf("[") + 1;
        int end = line.lastIndexOf("]");
        if (start >= end)
            return;

        String itemsPart = line.substring(start, end).trim();
        if (itemsPart.isEmpty())
            return;

        String[] entries = itemsPart.split("\\},\\s*\\{");
        for (String entry : entries) {
            if (!entry.startsWith("{"))
                entry = "{" + entry;
            if (!entry.endsWith("}"))
                entry = entry + "}";

            String pid = extract(entry, "pid");
            int qty = Integer.parseInt(extract(entry, "qty"));

            catalog.stream()
                    .filter(p -> p.getId().equals(pid))
                    .findFirst()
                    .ifPresent(p -> c.getCart().addProduct(p, qty));
        }
    }

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

    private Product parseProduct(String l) {
        return new Product(extract(l, "id"), extract(l, "name"), extract(l, "cat"),
                new BigDecimal(extract(l, "price")), Integer.parseInt(extract(l, "stock")), "", extract(l, "img"));
    }

    private User parseUser(String l) {
        if (extract(l, "role").equals("ADMIN"))
            return new com.mall.model.Administrator(extract(l, "id"), extract(l, "user"), extract(l, "pass"));
        return new com.mall.model.Customer(extract(l, "id"), extract(l, "user"), extract(l, "pass"),
                new BigDecimal(extract(l, "bal")));
    }

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