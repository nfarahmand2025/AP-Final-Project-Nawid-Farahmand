package com.mall.ui;

import com.mall.service.MallManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

public class PaymentPanel extends JPanel {
    // Input field for entering deposit amount
    private JTextField amountField;
    // Reference to main application frame for navigation
    private MainFrame parent;
    // Manager providing authentication and persistence services
    private MallManager manager;

    public PaymentPanel(MainFrame parent, MallManager manager) {
        this.parent = parent;
        this.manager = manager;

        // Set panel layout and background color
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_COLOR);

        // --- Center Card ---
        // Card panel to hold all UI components in a vertical layout
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UIConstants.SURFACE_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1),
                new EmptyBorder(40, 40, 40, 40)));

        // Header title
        JLabel title = new JLabel("Deposit Funds");
        title.setFont(UIConstants.H1_FONT);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle description
        JLabel subtitle = new JLabel("Top up your balance to continue shopping");
        subtitle.setFont(UIConstants.CAPTION_FONT);
        subtitle.setForeground(UIConstants.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Amount input field
        amountField = new JTextField();
        styleTextField(amountField);

        // Mimic credit card visual input (disabled)
        JTextField cardNum = new JTextField("**** **** **** 1234");
        styleTextField(cardNum);
        cardNum.setEnabled(false);
        cardNum.setBackground(UIConstants.BG_COLOR);

        // Payment and cancel buttons
        ModernButton payBtn = new ModernButton("Confirm Payment");
        configureButtonLayout(payBtn);

        ModernButton cancelBtn = new ModernButton("Cancel");
        configureButtonLayout(cancelBtn);

        // Layout assembly: add components with spacing
        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, UIConstants.GRID_UNIT))); // small vertical gap
        card.add(subtitle);
        card.add(Box.createRigidArea(new Dimension(0, UIConstants.SECTION_SPACING)));
        card.add(createLabel("Amount to Deposit ($)"));
        card.add(amountField);
        card.add(Box.createRigidArea(new Dimension(0, 24)));
        card.add(createLabel("Payment Method"));
        card.add(cardNum);
        card.add(Box.createRigidArea(new Dimension(0, UIConstants.SECTION_SPACING)));
        card.add(payBtn);
        card.add(Box.createRigidArea(new Dimension(0, 16)));
        card.add(cancelBtn);

        // Center the card in the panel
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);

        // Event handling for buttons
        payBtn.addActionListener(e -> handlePayment());
        cancelBtn.addActionListener(e -> parent.showView("CATALOG_CUSTOMER"));
    }

    // Helper method to create consistently styled labels
    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIConstants.LABEL_FONT);
        l.setForeground(UIConstants.TEXT_PRIMARY);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 5, 0));
        return l;
    }

    // Apply standard styling to text fields
    private void styleTextField(JTextField field) {
        field.setMaximumSize(new Dimension(300, 45));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 45));
        field.setFont(UIConstants.INPUT_FONT);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)));
    }

    // Apply layout constraints to buttons
    private void configureButtonLayout(ModernButton btn) {
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(300, 45));
    }

    // Reset input field when refreshing the panel
    public void refresh() {
        amountField.setText("");
    }

    // Handle payment processing
    private void handlePayment() {
        try {
            // Parse entered amount
            BigDecimal amount = new BigDecimal(amountField.getText());
            if (amount.compareTo(BigDecimal.ZERO) <= 0)
                throw new Exception();

            // Deposit amount to user account
            manager.getAuthService().depositBalance(amount);
            manager.saveData();

            // Show success message
            String message = "Success! $" + String.format("%.2f", amount) + " added to your account.";
            JOptionPane.showMessageDialog(this, message, "Payment Successful", JOptionPane.INFORMATION_MESSAGE);

            // Navigate back to customer catalog
            parent.showView("CATALOG_CUSTOMER");
        } catch (Exception e) {
            // Show error message for invalid input
            JOptionPane.showMessageDialog(this, "Please enter a valid amount.", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

}
