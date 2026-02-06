package com.mall.ui;

import com.mall.model.Product;
import com.mall.service.MallManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/*
 * AdminDashboardPanel
 * -------------------
 * This panel is the main dashboard for the Admin user.
 * It provides:
 *  - Search functionality to find products by name
 *  - A product grid display using ProductCard
 *  - Buttons for Add Product, Sales History, and Logout
 *  - Auto scroll reset after refreshing the product grid
 */
public class AdminDashboardPanel extends JPanel {

    // Reference to the main application frame for navigation
    private MainFrame parent;

    // MallManager is used to access product services and manage data
    private MallManager manager;

    // Panel that holds the product cards in a grid layout
    private JPanel gridPanel;

    // Search input field
    private JTextField searchField;

    // Scroll pane to allow scrolling through product cards
    private JScrollPane scrollPane;

    /*
     * Constructor: Initializes the Admin Dashboard UI components
     */
    public AdminDashboardPanel(MainFrame parent, MallManager manager) {
        this.parent = parent;
        this.manager = manager;

        // Set layout and background theme
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_COLOR);

        // -----------------------------
        // 1. Navigation Bar Section
        // -----------------------------
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(UIConstants.SURFACE_COLOR);
        navBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_LIGHT),
                new EmptyBorder(16, UIConstants.GUTTER, 16, UIConstants.GUTTER)));

        // Left Side: Title and Search
        JPanel leftWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        leftWrapper.setOpaque(false);

        JLabel adminTitle = new JLabel("ADMIN PANEL");
        adminTitle.setFont(UIConstants.H2_FONT);
        adminTitle.setForeground(UIConstants.PRIMARY_COLOR);

        // Search input field
        searchField = new JTextField(15);
        styleSearchField(searchField);

        // Search button triggers refresh of the product grid
        ModernButton searchBtn = new ModernButton("Search");
        searchBtn.setPreferredSize(new Dimension(90, 32));
        searchBtn.addActionListener(e -> refreshProductGrid());

        leftWrapper.add(adminTitle);
        leftWrapper.add(searchField);
        leftWrapper.add(searchBtn);

        // Right Side: Admin action buttons
        JPanel actionsWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        actionsWrapper.setOpaque(false);

        // Add product button navigates to Add Product page
        ModernButton addProdBtn = new ModernButton("+ Add Product");
        addProdBtn.setPreferredSize(new Dimension(150, 45));
        addProdBtn.addActionListener(e -> parent.showView("ADD_PRODUCT"));

        // Sales history button navigates to Sales History page
        ModernButton salesHistoryBtn = new ModernButton("Sales History");
        salesHistoryBtn.setPreferredSize(new Dimension(120, 45));
        salesHistoryBtn.addActionListener(e -> parent.showView("SALES_HISTORY"));

        // Logout button navigates back to login screen
        ModernButton logoutBtn = new ModernButton("Logout");
        logoutBtn.setPreferredSize(new Dimension(100, 45));
        logoutBtn.addActionListener(e -> parent.showView("LOGIN"));

        actionsWrapper.add(addProdBtn);
        actionsWrapper.add(salesHistoryBtn);
        actionsWrapper.add(logoutBtn);

        navBar.add(leftWrapper, BorderLayout.WEST);
        navBar.add(actionsWrapper, BorderLayout.EAST);

        // -----------------------------
        // 2. Product Grid Section
        // -----------------------------
        gridPanel = new JPanel(new GridLayout(0, 3, UIConstants.GRID_GAP, UIConstants.GRID_GAP));
        gridPanel.setBackground(UIConstants.BG_COLOR);

        // Adds padding around product grid
        gridPanel.setBorder(new EmptyBorder(
                UIConstants.GUTTER, UIConstants.GUTTER,
                UIConstants.GUTTER, UIConstants.GUTTER));

        // ScrollPane allows scrolling if products exceed screen size
        scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Add components to main panel
        add(navBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    /*
     * Styles the search field (font, border, padding)
     */
    private void styleSearchField(JTextField field) {
        field.setFont(UIConstants.INPUT_FONT);
        field.setPreferredSize(new Dimension(180, 32));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
    }

    /*
     * Refreshes the product grid based on search results.
     * - Clears old product cards
     * - Loads new products from ProductService
     * - Adds ProductCard UI components dynamically
     * - Resets scrollbar to the top after refresh
     */
    public void refreshProductGrid() {

        // Remove all old components from grid panel
        gridPanel.removeAll();

        // Get product list based on search text
        List<Product> products = manager.getProductService().searchByName(searchField.getText());

        // If no products found, display a message
        if (products.isEmpty()) {
            JLabel emptyMsg = new JLabel("No products found for this search.");
            emptyMsg.setFont(UIConstants.CAPTION_FONT);
            gridPanel.add(emptyMsg);
        } else {
            // Add each product as a ProductCard component
            for (Product p : products) {
                gridPanel.add(new ProductCard(p, manager, parent));
            }
        }

        // Refresh UI display
        gridPanel.revalidate();
        gridPanel.repaint();

        // Reset scroll bar to the top after updating grid
        SwingUtilities.invokeLater(() -> {
            if (scrollPane != null && scrollPane.getVerticalScrollBar() != null) {
                scrollPane.getVerticalScrollBar().setValue(0);
            }
        });
    }
}
