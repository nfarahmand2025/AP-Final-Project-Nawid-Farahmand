package com.mall.ui;

import com.mall.model.Product;
import com.mall.service.MallManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ProductCatalogPanel extends JPanel {
    // Reference to main application frame for navigation and callbacks
    private MainFrame parent;
    // Manager for product and user services
    private MallManager manager;
    // Panel containing product cards in grid layout
    private JPanel gridPanel;
    // Search input field for product names
    private JTextField searchField;
    // Dropdown filter for product categories
    private JComboBox<String> categoryFilter;
    // Dropdown filter for sorting options
    private JComboBox<String> sortFilter;
    // Label showing user's current balance
    private JLabel balanceLabel;
    // Scroll pane to hold the grid of products
    private JScrollPane scrollPane;

    public ProductCatalogPanel(MainFrame parent, MallManager manager) {
        this.parent = parent;
        this.manager = manager;

        // Set main layout and background color
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_COLOR);

        // --- 1. Top Navigation & Filter Bar ---
        // Container panel for top navigation (brand + actions) and filters
        JPanel navContainer = new JPanel(new GridLayout(2, 1));
        navContainer.setBackground(UIConstants.SURFACE_COLOR);
        navContainer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_LIGHT));

        // Row 1: Brand logo and user action buttons
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setBorder(new EmptyBorder(16, UIConstants.GUTTER, 16, UIConstants.GUTTER));

        // Brand label/logo
        JLabel logo = new JLabel("AMIRKABIR MALL");
        logo.setFont(UIConstants.H2_FONT);
        logo.setForeground(UIConstants.PRIMARY_COLOR);
        topRow.add(logo, BorderLayout.WEST);

        // Container for user actions: balance, charge, cart, logout
        JPanel actionsWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionsWrapper.setOpaque(false);

        balanceLabel = new JLabel("Balance: $0.00");
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        balanceLabel.setForeground(UIConstants.TEXT_PRIMARY);

        // Buttons for charging account, accessing cart, and logout
        ModernButton chargeBtn = new ModernButton("Charge");
        chargeBtn.setPreferredSize(new Dimension(120, 45));
        chargeBtn.addActionListener(e -> parent.showView("PAYMENT"));

        ModernButton cartBtn = new ModernButton("Cart");
        cartBtn.setPreferredSize(new Dimension(110, 45));
        cartBtn.addActionListener(e -> parent.showView("CART"));

        ModernButton logoutBtn = new ModernButton("Logout");
        logoutBtn.setPreferredSize(new Dimension(100, 45));
        logoutBtn.addActionListener(e -> parent.showView("LOGIN"));

        actionsWrapper.add(balanceLabel);
        actionsWrapper.add(chargeBtn);
        actionsWrapper.add(cartBtn);
        actionsWrapper.add(logoutBtn);
        topRow.add(actionsWrapper, BorderLayout.EAST);

        // Row 2: Filter bar with search, category, and sort options
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterRow.setOpaque(false);
        filterRow.setBorder(new EmptyBorder(0, UIConstants.GUTTER, 8, UIConstants.GUTTER));

        // Search field and button
        searchField = new JTextField(15);
        styleSearchField(searchField);
        ModernButton searchBtn = new ModernButton("Search");
        searchBtn.setPreferredSize(new Dimension(100, 32));
        searchBtn.addActionListener(e -> applyFilters());

        // Category dropdown filter
        categoryFilter = createStyledCombo(new String[] { "All Categories" });
        // Sort dropdown filter
        sortFilter = createStyledCombo(new String[] { "Newest", "Price: Low to High", "Price: High to Low" });

        // Add filter components to filter row
        JLabel findLbl = new JLabel("Find:");
        findLbl.setFont(UIConstants.LABEL_FONT);
        filterRow.add(findLbl);
        filterRow.add(searchField);
        filterRow.add(searchBtn);

        JLabel catLbl = new JLabel("   Category:");
        catLbl.setFont(UIConstants.LABEL_FONT);
        filterRow.add(catLbl);
        filterRow.add(categoryFilter);

        JLabel sortLbl = new JLabel("   Sort:");
        sortLbl.setFont(UIConstants.LABEL_FONT);
        filterRow.add(sortLbl);
        filterRow.add(sortFilter);

        navContainer.add(topRow);
        navContainer.add(filterRow);

        // --- 2. Product Grid ---
        // Grid panel to display product cards in 3 columns
        gridPanel = new JPanel(new GridLayout(0, 3, UIConstants.GRID_GAP, UIConstants.GRID_GAP));
        gridPanel.setBackground(UIConstants.BG_COLOR);
        gridPanel.setBorder(new EmptyBorder(UIConstants.GUTTER, UIConstants.GUTTER, UIConstants.GUTTER, UIConstants.GUTTER));

        // Scroll pane to make product grid scrollable
        scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Add navigation bar and product grid to main panel
        add(navContainer, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    // Apply standard styling to search input field
    private void styleSearchField(JTextField field) {
        field.setFont(UIConstants.INPUT_FONT);
        field.setPreferredSize(new Dimension(200, 32));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
    }

    // Create a styled combo box with attached filter action
    private JComboBox<String> createStyledCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(UIConstants.CAPTION_FONT);
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(150, 32));
        combo.addActionListener(e -> applyFilters());
        return combo;
    }

    // Update category dropdown based on current products
    private void updateCategoryList() {
        String currentSelection = (String) categoryFilter.getSelectedItem();
        if (categoryFilter.getActionListeners().length > 0) {
            categoryFilter.removeActionListener(categoryFilter.getActionListeners()[0]);
        }

        // Use TreeSet to maintain unique sorted categories
        Set<String> categories = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        categories.add("All Categories");

        // Populate categories from all products
        List<Product> allProducts = manager.getProductService().searchByName("");
        for (Product p : allProducts) {
            if (p.getCategory() != null && !p.getCategory().isEmpty()) {
                categories.add(p.getCategory());
            }
        }

        // Set updated model and restore selection if possible
        categoryFilter.setModel(new DefaultComboBoxModel<>(categories.toArray(new String[0])));
        if (categories.contains(currentSelection)) {
            categoryFilter.setSelectedItem(currentSelection);
        } else {
            categoryFilter.setSelectedIndex(0);
        }

        categoryFilter.addActionListener(e -> applyFilters());
    }

    // Refresh balance label and product grid
    public void refresh() {
        var user = manager.getAuthService().getCurrentUser();
        if (user instanceof com.mall.model.Customer) {
            BigDecimal balance = ((com.mall.model.Customer) user).getBalance();
            balanceLabel.setText("Balance: $" + String.format("%.2f", balance));
        }
        updateCategoryList();
        applyFilters();
    }

    // Apply search, category, and sort filters to product grid
    private void applyFilters() {
        gridPanel.removeAll();

        List<Product> results = manager.getProductService().searchByName(searchField.getText());
        String cat = (String) categoryFilter.getSelectedItem();
        String sortOption = (String) sortFilter.getSelectedItem();

        // Filter by selected category
        if (!cat.equals("All Categories")) {
            results = results.stream()
                    .filter(p -> p.getCategory().equalsIgnoreCase(cat))
                    .collect(Collectors.toList());
        }

        // Sort products based on selected option
        if (sortOption != null) {
            if (sortOption.equals("Price: Low to High")) {
                results.sort(Comparator.comparing(Product::getPrice));
            } else if (sortOption.equals("Price: High to Low")) {
                results.sort(Comparator.comparing(Product::getPrice).reversed());
            }
        }

        // Display message if no products match filters
        if (results.isEmpty()) {
            JLabel emptyLabel = new JLabel("No products found in this category");
            emptyLabel.setFont(UIConstants.CAPTION_FONT);
            emptyLabel.setForeground(UIConstants.TEXT_SECONDARY);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            gridPanel.setLayout(new BorderLayout());
            gridPanel.add(emptyLabel, BorderLayout.CENTER);
        } else {
            // Populate grid with product cards
            gridPanel.setLayout(new GridLayout(0, 3, UIConstants.GRID_GAP, UIConstants.GRID_GAP));
            for (Product p : results) {
                gridPanel.add(new ProductCard(p, manager, parent));
            }
        }

        // Refresh UI
        gridPanel.revalidate();
        gridPanel.repaint();

        // Reset scroll position to top
        SwingUtilities.invokeLater(() -> {
            if (scrollPane != null && scrollPane.getVerticalScrollBar() != null) {
                scrollPane.getVerticalScrollBar().setValue(0);
            }
        });
    }
}
