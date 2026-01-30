package com.mall.ui;

import com.mall.model.*;
import com.mall.service.MallManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

public class CartPanel extends JPanel {
    private JPanel itemsContainer;
    private JLabel totalLabel;
    private JLabel subtotalLabel;
    private MallManager manager;
    private MainFrame parent;

    public CartPanel(MainFrame parent, MallManager manager) {
        this.parent = parent;
        this.manager = manager;
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_COLOR);

        // --- 1. Header Section ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.SURFACE_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_LIGHT),
                new EmptyBorder(24, UIConstants.GUTTER, 24, UIConstants.GUTTER)));

        JLabel header = new JLabel("Shopping Cart");
        header.setFont(UIConstants.H1_FONT);
        header.setForeground(UIConstants.TEXT_PRIMARY);

        ModernButton backBtn = new ModernButton("← Continue Shopping");
        backBtn.setPreferredSize(new Dimension(200, 45));
        backBtn.addActionListener(e -> parent.showView("CATALOG_CUSTOMER"));

        headerPanel.add(header, BorderLayout.WEST);
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. Main Content Split (List + Summary) ---
        JPanel contentSplit = new JPanel(new BorderLayout(UIConstants.GRID_GAP, 0));
        contentSplit.setOpaque(false);
        contentSplit.setBorder(
                new EmptyBorder(UIConstants.GUTTER, UIConstants.GUTTER, UIConstants.GUTTER, UIConstants.GUTTER));

        // Left Side: Items List
        itemsContainer = new JPanel();
        itemsContainer.setLayout(new BoxLayout(itemsContainer, BoxLayout.Y_AXIS));
        itemsContainer.setBackground(UIConstants.BG_COLOR);

        JScrollPane scroll = new JScrollPane(itemsContainer);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // Right Side: Summary Card
        JPanel summaryWrapper = new JPanel(new BorderLayout());
        summaryWrapper.setOpaque(false);
        summaryWrapper.setPreferredSize(new Dimension(350, 0));
        summaryWrapper.add(createSummaryCard(), BorderLayout.NORTH);

        contentSplit.add(scroll, BorderLayout.CENTER);
        contentSplit.add(summaryWrapper, BorderLayout.EAST);

        add(contentSplit, BorderLayout.CENTER);
    }

    private JPanel createSummaryCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UIConstants.SURFACE_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1),
                new EmptyBorder(32, 24, 32, 24)));

        JLabel title = new JLabel("Order Summary");
        title.setFont(UIConstants.H2_FONT);

        subtotalLabel = new JLabel("Subtotal: $0.00");
        subtotalLabel.setFont(UIConstants.LABEL_FONT);
        subtotalLabel.setForeground(UIConstants.TEXT_SECONDARY);

        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(UIConstants.PRICE_FONT);
        totalLabel.setForeground(UIConstants.TEXT_PRIMARY);

        ModernButton checkoutBtn = new ModernButton("Confirm & Pay");
        checkoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        checkoutBtn.addActionListener(e -> handleCheckout());

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 24)));
        card.add(subtotalLabel);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(totalLabel);
        card.add(Box.createRigidArea(new Dimension(0, 32)));
        card.add(checkoutBtn);

        return card;
    }

    public void refresh() {
        itemsContainer.removeAll();
        Customer c = manager.getAuthService().getCurrentCustomer();

        if (c == null || c.getCart().getItems().isEmpty()) {
            showEmptyState();
        } else {
            for (CartItem item : c.getCart().getItems()) {
                itemsContainer.add(createItemRow(item));
                itemsContainer.add(Box.createRigidArea(new Dimension(0, 16)));
            }
            updateTotals(c);
        }
        revalidate();
        repaint();
    }

    private void showEmptyState() {
        JLabel emptyMsg = new JLabel("Your cart is empty");
        emptyMsg.setFont(UIConstants.H2_FONT);
        emptyMsg.setForeground(UIConstants.TEXT_SECONDARY);
        emptyMsg.setAlignmentX(Component.CENTER_ALIGNMENT);

        itemsContainer.add(Box.createVerticalGlue());
        itemsContainer.add(emptyMsg);
        itemsContainer.add(Box.createVerticalGlue());

        subtotalLabel.setText("Subtotal: $0.00");
        totalLabel.setText("Total: $0.00");
    }

    private void updateTotals(Customer c) {
        String formattedTotal = String.format("%.2f", c.getCart().calculateTotal());
        subtotalLabel.setText("Subtotal: $" + formattedTotal);
        totalLabel.setText("Total: $" + formattedTotal);
    }

    private JPanel createItemRow(CartItem item) {
        JPanel row = new JPanel(new BorderLayout(20, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        row.setBackground(UIConstants.SURFACE_COLOR);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1),
                new EmptyBorder(16, 20, 16, 20)));

        // Product Info
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 4));
        info.setOpaque(false);
        JLabel name = new JLabel(item.getProduct().getName());
        name.setFont(UIConstants.BUTTON_FONT);
        JLabel price = new JLabel("$" + String.format("%.2f", item.getProduct().getPrice()));
        price.setFont(UIConstants.CAPTION_FONT);
        price.setForeground(UIConstants.PRIMARY_COLOR);
        info.add(name);
        info.add(price);

        // Interaction Controls (Add/Remove/Qty)
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        controls.setOpaque(false);

        JButton minusBtn = createQtyBtn("-");
        JLabel qtyLbl = new JLabel(String.valueOf(item.getQuantity()));
        qtyLbl.setFont(UIConstants.BUTTON_FONT);
        JButton plusBtn = createQtyBtn("+");

        JButton removeBtn = new JButton("Remove");
        removeBtn.setFont(UIConstants.LABEL_FONT);
        removeBtn.setForeground(new Color(239, 68, 68)); // Red color for danger actions
        removeBtn.setBorder(null);
        removeBtn.setContentAreaFilled(false);
        removeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Listeners for modifications
        minusBtn.addActionListener(e -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                manager.saveData();
                refresh();
            }
        });
        plusBtn.addActionListener(e -> {
            item.setQuantity(item.getQuantity() + 1);
            manager.saveData();
            refresh();
        });
        removeBtn.addActionListener(e -> {
            manager.getAuthService().getCurrentCustomer().getCart().remove(item);
            manager.saveData();
            refresh();
        });

        controls.add(removeBtn);
        controls.add(Box.createRigidArea(new Dimension(10, 0)));
        controls.add(minusBtn);
        controls.add(qtyLbl);
        controls.add(plusBtn);

        row.add(info, BorderLayout.CENTER);
        row.add(controls, BorderLayout.EAST);
        return row;
    }

    private JButton createQtyBtn(String text) {
        JButton b = new JButton(text);
        b.setPreferredSize(new Dimension(32, 32));
        b.setFont(UIConstants.BUTTON_FONT);
        b.setFocusPainted(false);
        b.setBackground(UIConstants.BG_COLOR);
        b.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void handleCheckout() {
        Customer c = manager.getAuthService().getCurrentCustomer();
        if (c == null || c.getCart().getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal total = c.getCart().calculateTotal();

        // 1. Check Balance
        if (c.getBalance().compareTo(total) < 0) {
            JOptionPane.showMessageDialog(this, "Insufficient balance! Please top up your account.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Confirmation Dialog
        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm purchase for $" + String.format("%.2f", total) + "?",
                "Checkout", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION)
            return;

        // 3. Generate a shared Transaction ID for this specific session
        String tid = "T" + Long.toHexString(System.currentTimeMillis()).toUpperCase();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // 4. Create Flat Records for EACH item in the cart
        java.util.List<CartItem> itemsToProcess = new java.util.ArrayList<>(c.getCart().getItems());

        for (CartItem item : itemsToProcess) {
            BigDecimal itemTotal = item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity()));

            SaleRecord record = new SaleRecord(
                    tid, // Shared session ID
                    c.getUsername(), // Customer name
                    item.getProduct().getName(), // Product name
                    item.getQuantity(), // Units
                    itemTotal, // Money paid for this line
                    now // Purchase time
            );

            manager.getSaleService().addSale(record);
        }

        // 5. Update User Balance and Clear Cart
        if (c.getCart().checkout()) {
            c.setBalance(c.getBalance().subtract(total));

            // 6. Persistence
            manager.saveData();

            JOptionPane.showMessageDialog(this,
                    "Order Placed Successfully!\nTransaction ID: " + tid,
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            parent.showView("CATALOG_CUSTOMER");
        } else {
            JOptionPane.showMessageDialog(this, "Checkout failed. One or more items might be out of stock.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}