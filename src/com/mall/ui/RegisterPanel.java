package com.mall.ui;

import com.mall.service.MallManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegisterPanel extends JPanel {
    private JTextField userField;
    private JPasswordField passField;
    private JButton showPassBtn;
    private MainFrame parent;
    private MallManager manager;
    private boolean isPassVisible = false;

    public RegisterPanel(MainFrame parent, MallManager manager) {
        this.parent = parent;
        this.manager = manager;

        setLayout(new BorderLayout());
        // Rule 7.1: Ensure background is BG_COLOR (#F8FAFC)
        setBackground(UIConstants.BG_COLOR);

        // --- Center Card ---
        // Rule 4.1: Encapsulate functional modules in a "Modern Card"
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UIConstants.SURFACE_COLOR);

        // Rule 4.1: Border 1px solid #E2E8F0, Radius 16px, Padding 40px for forms
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1),
                new EmptyBorder(40, 40, 40, 40)));

        // Header
        JLabel title = new JLabel("Create Account");
        // Rule 3.1: H1 (Page Titles) 28pt Bold
        title.setFont(UIConstants.H1_FONT);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitle = new JLabel("Join us to start shopping today");
        // Rule 3.2: Caption 12pt Medium/Plain
        subtitle.setFont(UIConstants.CAPTION_FONT);
        subtitle.setForeground(UIConstants.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username
        userField = new JTextField(20);
        styleTextField(userField);

        // Password with Eye Toggle
        passField = new JPasswordField(20);
        styleTextField(passField);

        JPanel passWrapper = new JPanel(new BorderLayout());
        passWrapper.setOpaque(false);
        // Rule 4.3: Height standardized at 45px
        passWrapper.setMaximumSize(new Dimension(300, 45));
        passWrapper.setPreferredSize(new Dimension(passWrapper.getPreferredSize().width, 45));
        passWrapper.add(passField, BorderLayout.CENTER);

        showPassBtn = new JButton("👁");
        showPassBtn.setFocusPainted(false);
        showPassBtn.setContentAreaFilled(false);
        showPassBtn.setBorder(new EmptyBorder(0, 10, 0, 10));
        showPassBtn.setForeground(UIConstants.TEXT_SECONDARY);
        // Rule 4.2: Always use HAND_CURSOR on hover
        showPassBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPassBtn.addActionListener(e -> togglePassword());
        passWrapper.add(showPassBtn, BorderLayout.EAST);

        // Buttons
        // Rule 7.2: Ensure all buttons use the ModernButton class
        ModernButton regBtn = new ModernButton("Register Now");
        configureButtonLayout(regBtn);

        ModernButton backBtn = new ModernButton("Back to Login");
        configureButtonLayout(backBtn);

        // Layout Assembly (Rule 5.1: 8px Grid system)
        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, UIConstants.GRID_UNIT)));
        card.add(subtitle);

        // Rule 5.1: Section Spacing 40px
        card.add(Box.createRigidArea(new Dimension(0, UIConstants.SECTION_SPACING)));

        card.add(createLabel("Choose Username"));
        card.add(userField);
        // Rule 4.3: 20px vertical spacing between input groups (adjusted to 24px for
        // 8px grid)
        card.add(Box.createRigidArea(new Dimension(0, 24)));

        card.add(createLabel("Choose Password"));
        card.add(passWrapper);
        card.add(Box.createRigidArea(new Dimension(0, UIConstants.SECTION_SPACING)));

        card.add(regBtn);
        // Rule 5.1: Gap 16px (2x8)
        card.add(Box.createRigidArea(new Dimension(0, 16)));
        card.add(backBtn);

        // Center the card
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);

        regBtn.addActionListener(e -> handleRegister());
        backBtn.addActionListener(e -> parent.showView("LOGIN"));
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        // Rule 3.2: Standard Label 12pt Bold
        l.setFont(UIConstants.LABEL_FONT);
        l.setForeground(UIConstants.TEXT_PRIMARY);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Rule 4.3: Label sits 5px above input field
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

    private void togglePassword() {
        isPassVisible = !isPassVisible;
        if (isPassVisible) {
            passField.setEchoChar((char) 0);
            showPassBtn.setText("🔒");
        } else {
            passField.setEchoChar('•');
            showPassBtn.setText("👁");
        }
    }

    public void resetForm() {

        userField.setText("");

        passField.setText("");

        passField.setEchoChar('•');

        isPassVisible = false;

        showPassBtn.setText("👁");

    }

    private void handleRegister() {
        String u = userField.getText().trim();
        String p = new String(passField.getPassword()).trim();

        if (u.isEmpty() || p.isEmpty()) {
            // Rule 6.2: Use JOptionPane.ERROR_MESSAGE for failures
            JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        manager.getAuthService().registerCustomer(u, p);
        manager.saveData();
        // Rule 6.1: Use JOptionPane.INFORMATION_MESSAGE for completed actions
        JOptionPane.showMessageDialog(this, "Account Created Successfully!", "Success",
                JOptionPane.INFORMATION_MESSAGE);
        parent.showView("LOGIN");
    }
}