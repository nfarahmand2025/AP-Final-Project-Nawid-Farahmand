package com.mall.ui;

import com.mall.service.MallManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginPanel extends JPanel {
    // Text field for entering username/email
    private JTextField userField;
    // Password field (masked) with toggle to show/hide
    private JPasswordField passField;
    // Button that toggles password visibility (eye icon)
    private JButton showPassBtn;
    // Reference to main application frame used for navigation callbacks
    private MainFrame parent;
    // Central manager providing services like authentication and persistence
    private MallManager manager;
    // Tracks whether the password is currently visible
    private boolean isPassVisible = false;

    // Constructor: builds the login UI and wires up event handlers
    public LoginPanel(MainFrame parent, MallManager manager) {
        this.parent = parent;
        this.manager = manager;

        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_COLOR);

        // --- Center Card ---
        // Card panel holds form elements and is centered by a wrapper below
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

        // Subtitle explaining the form purpose
        JLabel subtitle = new JLabel("Enter your details to access your account");
        subtitle.setFont(UIConstants.CAPTION_FONT);
        subtitle.setForeground(UIConstants.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username section: single-line input styled consistently
        userField = new JTextField(20);
        styleTextField(userField);

        // Password section with Eye Toggle: masked input that can be revealed
        passField = new JPasswordField(20);
        styleTextField(passField);

        // Wrapper used to place the password field and the toggle button in one row
        JPanel passWrapper = new JPanel(new BorderLayout());
        passWrapper.setOpaque(false);
        passWrapper.setMaximumSize(new Dimension(300, 60));
        passWrapper.add(passField, BorderLayout.CENTER);
        passWrapper.setPreferredSize(new Dimension(passWrapper.getPreferredSize().width, 45));

        // Toggle button shows an eye icon; clicking it reveals or masks the password
        showPassBtn = new JButton("👁");
        showPassBtn.setFocusPainted(false);
        showPassBtn.setContentAreaFilled(false);
        showPassBtn.setBorder(new EmptyBorder(0, 10, 0, 10));
        showPassBtn.setForeground(UIConstants.TEXT_SECONDARY);
        showPassBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPassBtn.addActionListener(e -> togglePassword());
        passWrapper.add(showPassBtn, BorderLayout.EAST);

        // Buttons: Sign In and Create Account actions
        ModernButton loginBtn = new ModernButton("Sign In");
        configureButtonLayout(loginBtn);

        ModernButton registerBtn = new ModernButton("Create an Account");
        configureButtonLayout(registerBtn);

        // Layout Assembly: add components to the card with spacing
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

        // Center the card inside a wrapper so it stays centered in the panel
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);

        // Event handlers for buttons
        loginBtn.addActionListener(e -> handleLogin());
        registerBtn.addActionListener(e -> parent.showView("REGISTER"));
    }

    // Helper to create a consistently styled label for inputs
    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIConstants.LABEL_FONT);
        l.setForeground(UIConstants.TEXT_PRIMARY);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 5, 0));
        return l;
    }

    // Apply consistent sizing, font and border to text fields
    private void styleTextField(JTextField field) {
        field.setMaximumSize(new Dimension(300, 45));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 45));
        field.setFont(UIConstants.INPUT_FONT);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)));
    }

    // Configure layout constraints used by form action buttons
    private void configureButtonLayout(ModernButton btn) {
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(300, 45));
    }

    // Toggle password visibility: flip state and update echo char and icon
    private void togglePassword() {
        isPassVisible = !isPassVisible;
        if (isPassVisible) {
            passField.setEchoChar((char) 0); // show characters
            showPassBtn.setText("🔒"); // indicate locked state visually
        } else {
            passField.setEchoChar('•'); // mask characters
            showPassBtn.setText("👁"); // eye icon when masked
        }
    }

    // Reset the login form to its initial empty state (used on view enter/logout)
    public void resetForm() {
        userField.setText("");
        passField.setText("");
        passField.setEchoChar('•');
        isPassVisible = false;
        showPassBtn.setText("👁");
    }

    // Handle login action: authenticate via AuthService and notify parent on success
    private void handleLogin() {
        String user = userField.getText();
        String pass = new String(passField.getPassword());
        if (manager.getAuthService().login(user, pass)) {
            // Notify MainFrame that login succeeded so it can switch views or load user state
            parent.onLoginSuccess();
        } else {
            // Show an error dialog when credentials are invalid
            JOptionPane.showMessageDialog(this, "Invalid Credentials", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}