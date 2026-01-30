package com.mall.ui;

import com.mall.service.MallManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

public class PaymentPanel extends JPanel {
    private JTextField amountField;
    private MainFrame parent;
    private MallManager manager;

    public PaymentPanel(MainFrame parent, MallManager manager) {
        this.parent = parent;
        this.manager = manager;

        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_COLOR);

        // --- Center Card ---
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UIConstants.SURFACE_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1),
                new EmptyBorder(40, 40, 40, 40)));

        // Header
        JLabel title = new JLabel("Deposit Funds");
        title.setFont(UIConstants.H1_FONT);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitle = new JLabel("Top up your balance to continue shopping");
        subtitle.setFont(UIConstants.CAPTION_FONT);
        subtitle.setForeground(UIConstants.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Amount Input
        amountField = new JTextField();
        styleTextField(amountField);

        // Mimic Credit Card Fields (Visual only)
        JTextField cardNum = new JTextField("**** **** **** 1234");
        styleTextField(cardNum);
        cardNum.setEnabled(false);
        cardNum.setBackground(UIConstants.BG_COLOR);

        // Buttons
        ModernButton payBtn = new ModernButton("Confirm Payment");
        configureButtonLayout(payBtn);

        ModernButton cancelBtn = new ModernButton("Cancel");
        configureButtonLayout(cancelBtn);

        // Layout Assembly
        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, UIConstants.GRID_UNIT))); // 8px
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

        // Center the card
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);

        payBtn.addActionListener(e -> handlePayment());
        cancelBtn.addActionListener(e -> parent.showView("CATALOG_CUSTOMER"));
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIConstants.LABEL_FONT);
        l.setForeground(UIConstants.TEXT_PRIMARY);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 5, 0));
        return l;
    }

    private void styleTextField(JTextField field) {
        field.setMaximumSize(new Dimension(300, 45));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 45));
        field.setFont(UIConstants.INPUT_FONT);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)));
    }

    private void configureButtonLayout(ModernButton btn) {
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(300, 45));
    }

    public void refresh() {
        amountField.setText("");
    }

    private void handlePayment() {
        try {
            BigDecimal amount = new BigDecimal(amountField.getText());
            if (amount.compareTo(BigDecimal.ZERO) <= 0)
                throw new Exception();

            manager.getAuthService().depositBalance(amount);
            manager.saveData();
            String message = "Success! $" + String.format("%.2f", amount) + " added to your account.";
            JOptionPane.showMessageDialog(this, message, "Payment Successful", JOptionPane.INFORMATION_MESSAGE);

            parent.showView("CATALOG_CUSTOMER");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}