package com.mall.ui;

import com.mall.model.Product;
import com.mall.service.MallManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class AdminDashboardPanel extends JPanel {
    // Reference to the main application frame for navigation between views
    private MainFrame parent;
    // Manager that provides access to services like productService and persistence
    private MallManager manager;
    // Panel that holds product cards in a grid layout
    private JPanel gridPanel;
    // Search input used to filter products by name
    private JTextField searchField;
    // Scroll pane wrapping the gridPanel to provide scrolling for long lists
    private JScrollPane scrollPane;

    // Constructor: builds the admin dashboard UI and wires up actions
    public AdminDashboardPanel(MainFrame parent, MallManager manager) {
        this.parent = parent;
        this.manager = manager;
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_COLOR);

        // --- 1. Navigation Bar ---
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(UIConstants.SURFACE_COLOR);
        navBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_LIGHT),
                new EmptyBorder(16, UIConstants.GUTTER, 16, UIConstants.GUTTER)));

        // Left Side: Title & Search
        JPanel leftWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        leftWrapper.setOpaque(false);

        // Title label for the admin dashboard
        JLabel adminTitle = new JLabel("ADMIN PANEL");
        adminTitle.setFont(UIConstants.H2_FONT);
        adminTitle.setForeground(UIConstants.PRIMARY_COLOR);

        // Search Component
        searchField = new JTextField(15);
        styleSearchField(searchField); // Apply visual styling to the search field
        ModernButton searchBtn = new ModernButton("Search");
        searchBtn.setPreferredSize(new Dimension(90, 32));
        // When search button is clicked, refresh the product grid using the search text
        searchBtn.addActionListener(e -> refreshProductGrid());

        leftWrapper.add(adminTitle);
        leftWrapper.add(searchField);
        leftWrapper.add(searchBtn);

        // Right Side: Global Actions (Add product, view sales, logout)
        JPanel actionsWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        actionsWrapper.setOpaque(false);

        ModernButton addProdBtn = new ModernButton("+ Add Product");
        addProdBtn.setPreferredSize(new Dimension(150, 45));
        // Navigate to the add product view when clicked
        addProdBtn.addActionListener(e -> parent.showView("ADD_PRODUCT"));

        ModernButton salesHistoryBtn = new ModernButton("Sales History");
        salesHistoryBtn.setPreferredSize(new Dimension(120, 45));
        // Navigate to the sales history view
        salesHistoryBtn.addActionListener(e -> parent.showView("SALES_HISTORY"));

        ModernButton logoutBtn = new ModernButton("Logout");
        logoutBtn.setPreferredSize(new Dimension(100, 45));
        // Navigate back to the login view (performing logout sandboxed here)
        logoutBtn.addActionListener(e -> parent.showView("LOGIN"));

        actionsWrapper.add(addProdBtn);
        actionsWrapper.add(salesHistoryBtn);
        actionsWrapper.add(logoutBtn);

        navBar.add(leftWrapper, BorderLayout.WEST);
        navBar.add(actionsWrapper, BorderLayout.EAST);

        // --- 2. Main Content (Product Grid) ---
        // Grid holds ProductCard components in 3 columns and dynamic rows
        gridPanel = new JPanel(new GridLayout(0, 3, UIConstants.GRID_GAP, UIConstants.GRID_GAP));
        gridPanel.setBackground(UIConstants.BG_COLOR);
        gridPanel.setBorder(
                new EmptyBorder(UIConstants.GUTTER, UIConstants.GUTTER, UIConstants.GUTTER, UIConstants.GUTTER));

        // Scroll pane wraps the grid to allow vertical scrolling for long catalogs
        scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        // Improve scroll smoothness/step
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(navBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    // Apply consistent styling to the search input to match the app's design system
    private void styleSearchField(JTextField field) {
        field.setFont(UIConstants.INPUT_FONT);
        field.setPreferredSize(new Dimension(180, 32));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
    }

    // Rebuilds the product grid based on the current search query.
    // Queries the product service for matching results and populates ProductCard components.
    public void refreshProductGrid() {
        gridPanel.removeAll();
        // Use the product service searchByName which performs case-insensitive substring search
        List<Product> products = manager.getProductService().searchByName(searchField.getText());

        if (products.isEmpty()) {
            // Show an empty message when no products match the search
            JLabel emptyMsg = new JLabel("No products found for this search.");
            emptyMsg.setFont(UIConstants.CAPTION_FONT);
            gridPanel.add(emptyMsg);
        } else {
            // Create and add a ProductCard for each matching product
            for (Product p : products) {
                gridPanel.add(new ProductCard(p, manager, parent));
            }
        }
        // Refresh layout and repaint to reflect new content
        gridPanel.revalidate();
        gridPanel.repaint();

        // Scroll to top of the grid after refresh to show the first results
        SwingUtilities.invokeLater(() -> {
            if (scrollPane != null && scrollPane.getVerticalScrollBar() != null) {
                scrollPane.getVerticalScrollBar().setValue(0);
            }
        });
    }
}
