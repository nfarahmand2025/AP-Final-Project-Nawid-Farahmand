package com.mall.ui;

import com.mall.service.MallManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginPanel extends JPanel {
    private JTextField userField;
    private JPasswordField passField;
    private JButton showPassBtn;
    private MainFrame parent;
    private MallManager manager;
    private boolean isPassVisible = false;

    public LoginPanel(MainFrame parent, MallManager manager) {
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

        // Logo/Header
        JLabel title = new JLabel("Welcome Back");
        title.setFont(UIConstants.H1_FONT);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitle = new JLabel("Enter your details to access your account");
        subtitle.setFont(UIConstants.CAPTION_FONT);
        subtitle.setForeground(UIConstants.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username section
        userField = new JTextField(20);
        styleTextField(userField);

        // Password section with Eye Toggle
        passField = new JPasswordField(20);
        styleTextField(passField);

        JPanel passWrapper = new JPanel(new BorderLayout());
        passWrapper.setOpaque(false);
        passWrapper.setMaximumSize(new Dimension(300, 60));
        passWrapper.add(passField, BorderLayout.CENTER);
        passWrapper.setPreferredSize(new Dimension(passWrapper.getPreferredSize().width, 45));

        showPassBtn = new JButton("👁");
        showPassBtn.setFocusPainted(false);
        showPassBtn.setContentAreaFilled(false);
        showPassBtn.setBorder(new EmptyBorder(0, 10, 0, 10));
        showPassBtn.setForeground(UIConstants.TEXT_SECONDARY);
        showPassBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPassBtn.addActionListener(e -> togglePassword());
        passWrapper.add(showPassBtn, BorderLayout.EAST);

        // Buttons
        ModernButton loginBtn = new ModernButton("Sign In");
        configureButtonLayout(loginBtn);

        ModernButton registerBtn = new ModernButton("Create an Account");
        configureButtonLayout(registerBtn);

        // Layout Assembly
        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, UIConstants.GRID_UNIT)));
        card.add(subtitle);

        card.add(Box.createRigidArea(new Dimension(0, UIConstants.SECTION_SPACING)));

        card.add(createLabel("Username"));
        card.add(userField);

        card.add(Box.createRigidArea(new Dimension(0, 24)));

        card.add(createLabel("Password"));
        card.add(passWrapper);

        card.add(Box.createRigidArea(new Dimension(0, UIConstants.SECTION_SPACING)));
        card.add(loginBtn);

        card.add(Box.createRigidArea(new Dimension(0, 16)));
        card.add(registerBtn);

        // Center the card
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);

        loginBtn.addActionListener(e -> handleLogin());
        registerBtn.addActionListener(e -> parent.showView("REGISTER"));
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

    private void handleLogin() {
        String user = userField.getText();
        String pass = new String(passField.getPassword());
        if (manager.getAuthService().login(user, pass)) {
            parent.onLoginSuccess();
        } else {
            JOptionPane.showMessageDialog(this, "Insufficient Balance or Invalid Credentials", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}