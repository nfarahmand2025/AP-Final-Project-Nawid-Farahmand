package com.mall.ui;

import com.mall.model.Administrator;
import com.mall.service.MallManager;
import javax.swing.*;
import java.awt.*;

/**
 * Main application window that holds all views using a CardLayout.
 * - Initializes panels and shared services
 * - Provides a centralized showView(...) method to switch between screens
 * - Routes post-login to admin or customer catalog based on user role
 */
public class MainFrame extends JFrame {
    // Core application manager that provides services (auth, persistence, etc.)
    private MallManager manager;

    // CardLayout used to swap visible panels in the main content area
    private CardLayout cardLayout = new CardLayout();
    // Container panel that holds all cards managed by cardLayout
    private JPanel cards = new JPanel(cardLayout);

    // Primary feature panels (created at startup)
    private ProductCatalogPanel catalogPanel;
    private SalesHistoryPanel salesPanel;
    private AdminDashboardPanel adminDashboard;
    private CartPanel cartPanel;
    private PaymentPanel paymentPanel;
    // Add/Edit product panel is created lazily when user navigates to product form
    private AddProductPanel addEditProductPanel;

    /**
     * Construct the main application frame.
     * Responsibilities:
     * - instantiate the MallManager
     * - create and register all application panels
     * - configure window properties and show the login view
     */
    public MainFrame() {
        // Initialize the application-level manager (auth, services, data access)
        manager = new MallManager();

        // Window Setup: title, default size, close behavior and background color
        setTitle("Amirkabir Mall System");
        setSize(1100, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(UIConstants.BG_COLOR);

        // Initialize all Panels and pass shared references (this frame and manager)
        catalogPanel = new ProductCatalogPanel(this, manager);
        salesPanel = new SalesHistoryPanel(this, manager);
        adminDashboard = new AdminDashboardPanel(this, manager);
        cartPanel = new CartPanel(this, manager);
        paymentPanel = new PaymentPanel(this, manager);

        // --- Layout Assembly ---
        // Keep the cards panel transparent so the main background shows through
        cards.setOpaque(false);
        // Add login and register as fresh instances so resetForm() can clear them
        cards.add(new LoginPanel(this, manager), "LOGIN");
        cards.add(new RegisterPanel(this, manager), "REGISTER");
        // Add main feature panels with their card keys
        cards.add(catalogPanel, "CATALOG_CUSTOMER");
        cards.add(adminDashboard, "CATALOG_ADMIN");
        cards.add(salesPanel, "SALES_HISTORY");
        cards.add(paymentPanel, "PAYMENT");
        cards.add(cartPanel, "CART");

        // Add the cards container to the frame
        add(cards);

        // Interaction Logic: start the UI on the login screen, center window, and show it
        cardLayout.show(cards, "LOGIN");
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Centralized view switcher that enforces the design system's refresh/reset rules.
     *
     * The method:
     * - resets form state for Login/Register when navigating to those screens
     * - triggers panel-specific refresh logic when needed (cart, sales, catalog, payment)
     * - lazily creates the add/edit product form when requested
     *
     * @param key card name to show
     */
    public void showView(String key) {
        // Reset input forms for views that should start empty each time they are shown.
        for (Component comp : cards.getComponents()) {
            // If switching to LOGIN, reset the LoginPanel fields
            if (key.equals("LOGIN") && comp instanceof LoginPanel) {
                ((LoginPanel) comp).resetForm();
                // If switching to REGISTER, reset the RegisterPanel fields
            } else if (key.equals("REGISTER") && comp instanceof RegisterPanel) {
                ((RegisterPanel) comp).resetForm();
            }
        }

        // Perform view-specific pre-show actions (refresh data or create forms)
        switch (key) {
            case "CART":
                // Ensure the cart UI reflects the latest cart contents before showing
                cartPanel.refresh();
                break;
            case "SALES_HISTORY":
                // Refresh sales history list from the service layer
                salesPanel.refresh();
                break;
            case "ADD_PRODUCT":
                // Lazily create the AddProductPanel for creating a new product (null = new)
                addEditProductPanel = new AddProductPanel(this, manager, null);
                cards.add(addEditProductPanel, "PRODUCT_FORM");
                // Show the newly added product form card immediately
                cardLayout.show(cards, "PRODUCT_FORM");
                break;
            case "CATALOG_CUSTOMER":
                // Refresh the product catalog for customer-facing view
                catalogPanel.refresh();
                break;
            case "CATALOG_ADMIN":
                // Admin catalog/dashboard may need to reload product data or grid state
                adminDashboard.refreshProductGrid();
                break;
            case "PAYMENT":
                // Prepare the payment panel (update totals, available methods, etc.)
                paymentPanel.refresh();
                break;
            // default: no pre-action needed for other keys
        }

        // Finally, show the requested card. Safe even if a case already called show.
        cardLayout.show(cards, key);
    }

    /**
     * Convenience method to externally trigger a catalog refresh.
     * Useful when other flows (add/edit product) change product data.
     */
    public void refreshCatalog() {
        catalogPanel.refresh();
    }

    /**
     * Open the Add/Edit Product form populated with an existing product for editing.
     * - Creates the AddProductPanel lazily with the provided product
     * - Shows the form using the "PRODUCT_FORM" card key
     *
     * @param p product to edit; pass null to open form for creating a new product
     */
    public void showEditProductView(com.mall.model.Product p) {
        addEditProductPanel = new AddProductPanel(this, manager, p);
        cards.add(addEditProductPanel, "PRODUCT_FORM");
        cardLayout.show(cards, "PRODUCT_FORM");
    }

    /**
     * Logic for routing after authentication success.
     * - Admin users are routed to the admin catalog/dashboard
     * - Non-admin (customer) users are routed to the customer catalog
     *
     * This method is called by the LoginPanel when login succeeds.
     */
    public void onLoginSuccess() {
        if (manager.getAuthService().getCurrentUser() instanceof Administrator) {
            // Route administrators to the admin view
            showView("CATALOG_ADMIN");
        } else {
            // Route regular customers to the customer catalog
            showView("CATALOG_CUSTOMER");
        }
    }
}
