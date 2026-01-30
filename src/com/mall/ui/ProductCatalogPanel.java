package com.mall.ui;

import com.mall.model.Product;
import com.mall.service.MallManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class ProductCatalogPanel extends JPanel {
    private MainFrame parent;
    private MallManager manager;
    private JPanel gridPanel;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JComboBox<String> sortFilter;
    private JLabel balanceLabel;

    public ProductCatalogPanel(MainFrame parent, MallManager manager) {
        this.parent = parent;
        this.manager = manager;

        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_COLOR);

        // --- 1. Top Navigation & Filter Bar ---
        JPanel navContainer = new JPanel(new GridLayout(2, 1));
        navContainer.setBackground(UIConstants.SURFACE_COLOR);
        // Subtle bottom border using BORDER_LIGHT
        navContainer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_LIGHT));

        // Row 1: Brand and User Actions
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        // Padding: 8px Grid (16px top/bottom, 32px Gutter left/right)
        topRow.setBorder(new EmptyBorder(16, UIConstants.GUTTER, 16, UIConstants.GUTTER));

        JLabel logo = new JLabel("AMIRKABIR MALL");
        // Brand logo font standardized to H2 size
        logo.setFont(UIConstants.H2_FONT);
        logo.setForeground(UIConstants.PRIMARY_COLOR);
        topRow.add(logo, BorderLayout.WEST);

        JPanel actionsWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionsWrapper.setOpaque(false);

        balanceLabel = new JLabel("Balance: $0.00");
        // Price/Balance font set to 15pt Bold fall-back (close to spec)
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        balanceLabel.setForeground(UIConstants.TEXT_PRIMARY);

        // Buttons must use ModernButton and height logic
        ModernButton chargeBtn = new ModernButton("💰 Charge");
        chargeBtn.setPreferredSize(new Dimension(120, 45)); // Standardized 45px height
        chargeBtn.addActionListener(e -> parent.showView("PAYMENT"));

        ModernButton cartBtn = new ModernButton("🛒 Cart");
        cartBtn.setPreferredSize(new Dimension(110, 45)); // Standardized 45px height
        cartBtn.addActionListener(e -> parent.showView("CART"));

        ModernButton logoutBtn = new ModernButton("Logout");
        logoutBtn.setPreferredSize(new Dimension(100, 45)); // Standardized 45px height
        logoutBtn.addActionListener(e -> parent.showView("LOGIN"));

        actionsWrapper.add(balanceLabel);
        actionsWrapper.add(chargeBtn);
        actionsWrapper.add(cartBtn);
        actionsWrapper.add(logoutBtn);
        topRow.add(actionsWrapper, BorderLayout.EAST);

        // Row 2: Filters
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterRow.setOpaque(false);
        filterRow.setBorder(new EmptyBorder(0, UIConstants.GUTTER, 8, UIConstants.GUTTER));

        searchField = new JTextField(15);
        styleSearchField(searchField);

        ModernButton searchBtn = new ModernButton("Search");
        searchBtn.setPreferredSize(new Dimension(100, 32)); // Inline filter buttons are slightly shorter
        searchBtn.addActionListener(e -> applyFilters());

        categoryFilter = createStyledCombo(
                new String[] { "All Categories", "Electronics", "Clothing", "Books", "Home" });
        sortFilter = createStyledCombo(new String[] { "Newest", "Price: Low to High", "Price: High to Low" });

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
        gridPanel = new JPanel(new GridLayout(0, 2, UIConstants.GRID_GAP, UIConstants.GRID_GAP));
        gridPanel.setBackground(UIConstants.BG_COLOR);
        gridPanel.setBorder(
                new EmptyBorder(UIConstants.GUTTER, UIConstants.GUTTER, UIConstants.GUTTER, UIConstants.GUTTER));

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(navContainer, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void styleSearchField(JTextField field) {
        field.setFont(UIConstants.INPUT_FONT);
        field.setPreferredSize(new Dimension(200, 32));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
    }

    private JComboBox<String> createStyledCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(UIConstants.CAPTION_FONT);
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(150, 32));
        combo.addActionListener(e -> applyFilters());
        return combo;
    }

    public void refresh() {
        var user = manager.getAuthService().getCurrentUser();
        if (user instanceof com.mall.model.Customer) {
            BigDecimal balance = ((com.mall.model.Customer) user).getBalance();
            balanceLabel.setText("Balance: $" + String.format("%.2f", balance));
        }
        applyFilters();
    }

    private void applyFilters() {
        gridPanel.removeAll();

        List<Product> results = manager.getCatalog().searchByName(searchField.getText());
        String cat = (String) categoryFilter.getSelectedItem();

        if (!cat.equals("All Categories")) {
            results = results.stream()
                    .filter(p -> p.getCategory().equalsIgnoreCase(cat))
                    .collect(Collectors.toList());
        }

        if (results.isEmpty()) {
            JLabel emptyLabel = new JLabel("No products found in this category");
            emptyLabel.setFont(UIConstants.CAPTION_FONT);
            emptyLabel.setForeground(UIConstants.TEXT_SECONDARY);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            gridPanel.setLayout(new BorderLayout());
            gridPanel.add(emptyLabel, BorderLayout.CENTER);
        } else {
            gridPanel.setLayout(new GridLayout(0, 2, UIConstants.GRID_GAP, UIConstants.GRID_GAP));
            for (Product p : results) {
                gridPanel.add(new ProductCard(p, manager, parent));
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }
}