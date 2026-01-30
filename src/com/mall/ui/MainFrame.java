package com.mall.ui;

import com.mall.model.Administrator;
import com.mall.service.MallManager;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private MallManager manager;
    private CardLayout cardLayout = new CardLayout();
    private JPanel cards = new JPanel(cardLayout);

    private ProductCatalogPanel catalogPanel;
    private SalesHistoryPanel salesPanel;
    private AdminDashboardPanel adminDashboard;
    private CartPanel cartPanel;
    private PaymentPanel paymentPanel;
    private AddProductPanel addEditProductPanel;

    public MainFrame() {
        manager = new MallManager();

        // Window Setup
        setTitle("Amirkabir Mall System");
        setSize(1100, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(UIConstants.BG_COLOR);

        // Initialize all Panels
        catalogPanel = new ProductCatalogPanel(this, manager);
        salesPanel = new SalesHistoryPanel(this, manager);
        adminDashboard = new AdminDashboardPanel(this, manager);
        cartPanel = new CartPanel(this, manager);
        paymentPanel = new PaymentPanel(this, manager);

        // --- Layout Assembly ---
        cards.setOpaque(false);
        cards.add(new LoginPanel(this, manager), "LOGIN");
        cards.add(new RegisterPanel(this, manager), "REGISTER");
        cards.add(catalogPanel, "CATALOG_CUSTOMER");
        cards.add(adminDashboard, "CATALOG_ADMIN");
        cards.add(salesPanel, "SALES_HISTORY");
        cards.add(paymentPanel, "PAYMENT");
        cards.add(cartPanel, "CART");

        add(cards);

        // Interaction Logic
        cardLayout.show(cards, "LOGIN");
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Centralized view switcher that enforces the Design System's refresh rules.
     */
    public void showView(String key) {
        for (Component comp : cards.getComponents()) {
            if (key.equals("LOGIN") && comp instanceof LoginPanel) {
                ((LoginPanel) comp).resetForm();
            } else if (key.equals("REGISTER") && comp instanceof RegisterPanel) {
                ((RegisterPanel) comp).resetForm();
            }
        }

        switch (key) {
            case "CART":
                cartPanel.refresh();
                break;
            case "SALES_HISTORY":
                salesPanel.refresh();
                break;
            case "ADD_PRODUCT":
                addEditProductPanel = new AddProductPanel(this, manager, null);
                cards.add(addEditProductPanel, "PRODUCT_FORM");
                cardLayout.show(cards, "PRODUCT_FORM");
                break;
            case "CATALOG_CUSTOMER":
                catalogPanel.refresh();
                break;
            case "CATALOG_ADMIN":
                adminDashboard.refreshProductGrid();
                break;
            case "PAYMENT":
                break;
        }

        cardLayout.show(cards, key);
    }

    public void refreshCatalog() {
        catalogPanel.refresh();
    }

    public void showEditProductView(com.mall.model.Product p) {
        addEditProductPanel = new AddProductPanel(this, manager, p);
        cards.add(addEditProductPanel, "PRODUCT_FORM");
        cardLayout.show(cards, "PRODUCT_FORM");
    }

    /**
     * Logic for routing after authentication
     */
    public void onLoginSuccess() {
        if (manager.getAuthService().getCurrentUser() instanceof Administrator) {
            showView("CATALOG_ADMIN");
        } else {
            showView("CATALOG_CUSTOMER");
        }
    }
}