package com.mall.ui;

import com.mall.model.Product;
import com.mall.service.MallManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class AdminDashboardPanel extends JPanel {
    private MainFrame parent;
    private MallManager manager;
    private JPanel gridPanel;
    private JTextField searchField;

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

        JLabel adminTitle = new JLabel("ADMIN PANEL");
        adminTitle.setFont(UIConstants.H2_FONT);
        adminTitle.setForeground(UIConstants.PRIMARY_COLOR);

        // Search Component
        searchField = new JTextField(15);
        styleSearchField(searchField);
        ModernButton searchBtn = new ModernButton("Search");
        searchBtn.setPreferredSize(new Dimension(90, 32));
        searchBtn.addActionListener(e -> refreshProductGrid());

        leftWrapper.add(adminTitle);
        leftWrapper.add(searchField);
        leftWrapper.add(searchBtn);

        // Right Side: Global Actions
        JPanel actionsWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        actionsWrapper.setOpaque(false);

        ModernButton addProdBtn = new ModernButton("+ Add Product");
        addProdBtn.setPreferredSize(new Dimension(150, 45));
        addProdBtn.addActionListener(e -> parent.showView("ADD_PRODUCT"));

        ModernButton salesHistoryBtn = new ModernButton("Sales History");
        salesHistoryBtn.setPreferredSize(new Dimension(120, 45));
        salesHistoryBtn.addActionListener(e -> parent.showView("SALES_HISTORY"));

        ModernButton logoutBtn = new ModernButton("Logout");
        logoutBtn.setPreferredSize(new Dimension(100, 45));
        logoutBtn.addActionListener(e -> parent.showView("LOGIN"));

        actionsWrapper.add(addProdBtn);
        actionsWrapper.add(salesHistoryBtn);
        actionsWrapper.add(logoutBtn);

        navBar.add(leftWrapper, BorderLayout.WEST);
        navBar.add(actionsWrapper, BorderLayout.EAST);

        // --- 2. Main Content (Product Grid) ---
        gridPanel = new JPanel(new GridLayout(0, 3, UIConstants.GRID_GAP, UIConstants.GRID_GAP));
        gridPanel.setBackground(UIConstants.BG_COLOR);
        gridPanel.setBorder(
                new EmptyBorder(UIConstants.GUTTER, UIConstants.GUTTER, UIConstants.GUTTER, UIConstants.GUTTER));

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(navBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void styleSearchField(JTextField field) {
        field.setFont(UIConstants.INPUT_FONT);
        field.setPreferredSize(new Dimension(180, 32));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
    }

    public void refreshProductGrid() {
        gridPanel.removeAll();
        List<Product> products = manager.getCatalog().searchByName(searchField.getText());

        if (products.isEmpty()) {
            JLabel emptyMsg = new JLabel("No products found for this search.");
            emptyMsg.setFont(UIConstants.CAPTION_FONT);
            gridPanel.add(emptyMsg);
        } else {
            for (Product p : products) {
                gridPanel.add(new ProductCard(p, manager, parent));
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }
}