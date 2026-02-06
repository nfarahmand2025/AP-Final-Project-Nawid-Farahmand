package com.mall.ui;

import com.mall.model.Administrator;
import com.mall.model.Product;
import com.mall.model.Customer;
import com.mall.service.MallManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;

public class ProductCard extends JPanel {
    private final Product product;
    private final MallManager manager;
    private final MainFrame parent;

    private JPanel ratingPanel;

    private static final int WIDTH = 280;
    private static final int SQUARE_SIZE = WIDTH;
    private static final int BUTTON_ROW_HEIGHT = 60;
    private static final int HEIGHT = (SQUARE_SIZE * 2) + BUTTON_ROW_HEIGHT;

    public ProductCard(Product p, MallManager manager, MainFrame parent) {
        this.product = p;
        this.manager = manager;
        this.parent = parent;

        boolean isAdmin = manager.getAuthService().getCurrentUser() instanceof Administrator;

        Dimension dimensions = new Dimension(WIDTH, HEIGHT);
        setPreferredSize(dimensions);
        setMaximumSize(dimensions);
        setMinimumSize(dimensions);

        setLayout(new BorderLayout());
        setBackground(UIConstants.SURFACE_COLOR);
        setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1));

        // --- Upper Part: Image Showcase (Square) ---
        JLabel thumbLabel = new JLabel();
        thumbLabel.setHorizontalAlignment(SwingConstants.CENTER);
        thumbLabel.setOpaque(true);
        thumbLabel.setBackground(Color.WHITE);
        // thumbLabel.setPreferredSize(new Dimension(WIDTH, IMAGE_HEIGHT));
        // thumbLabel.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT));

        String no_image_path = "resources/images/ui/no_image.jpg";
        String actualPath = p.getImagePath();
        String path = (actualPath != null && !actualPath.isEmpty()) ? actualPath : no_image_path;

        try {
            ImageIcon icon = new ImageIcon(path);
            Image scaled = icon.getImage().getScaledInstance(WIDTH, SQUARE_SIZE, Image.SCALE_SMOOTH);
            thumbLabel.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            thumbLabel.setText("No Image Available");
            thumbLabel.setFont(UIConstants.CAPTION_FONT);
            thumbLabel.setForeground(UIConstants.TEXT_SECONDARY);
        }

        // --- Lower Part: Product Info ---
        JPanel lowerSquare = new JPanel(new GridBagLayout());
        lowerSquare.setOpaque(false);
        lowerSquare.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        // Top Row (25% Height): Name (Left) and Price (Right)
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel nameLabel = new JLabel(p.getName());
        nameLabel.setFont(UIConstants.H2_FONT);
        nameLabel.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel priceLabel = new JLabel("Price: $" + String.format("%.2f", p.getPrice()));
        priceLabel.setFont(UIConstants.PRICE_FONT);
        priceLabel.setForeground(UIConstants.PRIMARY_COLOR);

        topRow.add(nameLabel, BorderLayout.WEST);
        topRow.add(priceLabel, BorderLayout.EAST);

        gbc.gridy = 0;
        gbc.weighty = 0.25;
        lowerSquare.add(topRow, gbc);

        // Middle Row (50% Height): Description
        JTextArea descLabel = new JTextArea(p.getDescription());
        descLabel.setFont(UIConstants.CAPTION_FONT);
        descLabel.setForeground(UIConstants.TEXT_SECONDARY);
        descLabel.setLineWrap(true);
        descLabel.setWrapStyleWord(true);
        descLabel.setEditable(false);
        descLabel.setOpaque(false);

        gbc.gridy = 1;
        gbc.weighty = 0.50;
        gbc.insets = new Insets(8, 0, 8, 0);
        lowerSquare.add(descLabel, gbc);

        // Lower Row (25% Height): Rating (Left) and Stock (Right)
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);

        // Rating UI (Stars + Count)
        ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        ratingPanel.setOpaque(false);
        
        if (!isAdmin) {
            ratingPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            ratingPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (manager.getAuthService().getCurrentUser() instanceof Customer) {
                        showRatingDialog();
                    } else {
                        JOptionPane.showMessageDialog(parent, "Only customers can rate products.");
                    }
                }
            });
        }
        updateRating();

        JLabel stockLabel = new JLabel("Stock: " + p.getStockQty());
        stockLabel.setFont(UIConstants.CAPTION_FONT);
        stockLabel.setForeground(UIConstants.TEXT_PRIMARY);

        bottomRow.add(ratingPanel, BorderLayout.WEST);
        bottomRow.add(stockLabel, BorderLayout.EAST);

        gbc.gridy = 2;
        gbc.weighty = 0.25;
        gbc.insets = new Insets(0, 0, 0, 0);
        lowerSquare.add(bottomRow, gbc);

        // --- Final row: Dynamic Action Button ---
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.setOpaque(false);
        buttonWrapper.setBorder(new EmptyBorder(0, 16, 16, 16));

        ModernButton actionBtn = new ModernButton(isAdmin ? "Edit" : "Add to Cart");
        actionBtn.setPreferredSize(new Dimension(WIDTH - 40, 40));
        if (!isAdmin && p.getStockQty() == 0){
            actionBtn.disable();
        } else {

            actionBtn.addActionListener(e -> {
                if (isAdmin) {
                    parent.showEditProductView(p);
                } else {
                    showQuantityDialog();
                }
            });

        }

        buttonWrapper.add(actionBtn);

        // assembly:
        add(thumbLabel, BorderLayout.NORTH);
        add(lowerSquare, BorderLayout.CENTER);
        add(buttonWrapper, BorderLayout.SOUTH);
    }

    private void showRatingDialog() {
        JDialog ratingDialog = new JDialog(parent, "Rate Product", true);
        ratingDialog.setLayout(new BorderLayout(10, 20));
        ((JPanel) ratingDialog.getContentPane()).setBorder(new EmptyBorder(20, 20, 20, 20));
        ratingDialog.setBackground(Color.WHITE);

        JLabel title = new JLabel("Rate: " + product.getName(), SwingConstants.CENTER);
        title.setFont(UIConstants.H2_FONT);
        ratingDialog.add(title, BorderLayout.NORTH);

        JPanel starContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        starContainer.setOpaque(false);

        Customer currentCustomer = (Customer) manager.getAuthService().getCurrentUser();
        Integer existingRating = product.getRatings().get(currentCustomer);
        int initialValue = (existingRating != null) ? existingRating : 0;

        JLabel[] stars = new JLabel[5];
        final int[] selectedRating = { initialValue };

        for (int i = 0; i < 5; i++) {
            final int index = i + 1;
            stars[i] = new JLabel(index <= initialValue ? "★" : "☆");
            stars[i].setFont(new Font("Serif", Font.BOLD, 40));
            stars[i].setForeground(new Color(255, 193, 7));
            stars[i].setCursor(new Cursor(Cursor.HAND_CURSOR));

            stars[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedRating[0] = index;
                    for (int j = 0; j < 5; j++) {
                        stars[j].setText(j < index ? "★" : "☆");
                    }
                }
            });
            starContainer.add(stars[i]);
        }

        ModernButton submitBtn = new ModernButton("Submit Rating");
        submitBtn.addActionListener(e -> {
            if (selectedRating[0] == 0) {
                JOptionPane.showMessageDialog(ratingDialog, "Please select a star rating.");
                return;
            }
            product.addOrUpdateRating(currentCustomer, selectedRating[0]);
            manager.saveData();
            ratingDialog.dispose();
            updateRating();
        });

        ratingDialog.add(starContainer, BorderLayout.CENTER);
        ratingDialog.add(submitBtn, BorderLayout.SOUTH);

        ratingDialog.pack();
        ratingDialog.setLocationRelativeTo(parent);
        ratingDialog.setVisible(true);
    }

    private void showQuantityDialog() {
        JDialog dialog = new JDialog(parent, "Select Quantity", true);
        dialog.setLayout(new BorderLayout(20, 20));
        ((JPanel) dialog.getContentPane()).setBorder(new EmptyBorder(24, 24, 24, 24));
        dialog.setBackground(Color.WHITE);

        // Row 1: Title
        JLabel titleLabel = new JLabel(product.getName(), SwingConstants.CENTER);
        titleLabel.setFont(UIConstants.H2_FONT);
        dialog.add(titleLabel, BorderLayout.NORTH);

        // Row 2: Counter [Minus - Number - Plus]
        JPanel counterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        counterPanel.setOpaque(false);

        final int[] quantity = { 1 };
        JLabel qtyLabel = new JLabel("1");
        qtyLabel.setFont(UIConstants.H1_FONT);

        ModernButton minusBtn = new ModernButton("-");
        minusBtn.setPreferredSize(new Dimension(45, 45));

        ModernButton plusBtn = new ModernButton("+");
        plusBtn.setPreferredSize(new Dimension(45, 45));

        // Row 3: Pricing
        JLabel priceDisplay = new JLabel();
        priceDisplay.setFont(UIConstants.LABEL_FONT);

        Runnable updatePricing = () -> {
            qtyLabel.setText(String.valueOf(quantity[0]));
            BigDecimal total = product.getPrice().multiply(new BigDecimal(quantity[0]));
            priceDisplay.setText(String.format("$%.2f/unit  -----  Total: $%.2f", product.getPrice(), total));
        };

        minusBtn.addActionListener(e -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                updatePricing.run();
            }
        });

        plusBtn.addActionListener(e -> {
            if (quantity[0] < product.getStockQty()) {
                quantity[0]++;
                updatePricing.run();
            }
        });

        counterPanel.add(minusBtn);
        counterPanel.add(qtyLabel);
        counterPanel.add(plusBtn);

        // Bottom Section: Pricing + Confirm
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        bottomPanel.setOpaque(false);
        bottomPanel.add(priceDisplay);

        ModernButton confirmBtn = new ModernButton("Add to Cart");
        confirmBtn.addActionListener(e -> {
            try {
                Customer customer = (Customer) manager.getAuthService().getCurrentUser();
                customer.getCart().addProduct(product, quantity[0]);
                manager.saveData();
                dialog.dispose();
                JOptionPane.showMessageDialog(parent, "Added " + quantity[0] + " units to cart.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        bottomPanel.add(confirmBtn);

        dialog.add(counterPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        updatePricing.run();
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private void updateRating() {
        if (ratingPanel == null)
            return;

        ratingPanel.removeAll();

        int avgRating = (int) Math.round(product.getAverageRating());
        for (int i = 1; i <= 5; i++) {
            JLabel star = new JLabel(i <= avgRating ? "★" : "☆");
            star.setForeground(new Color(255, 193, 7));
            star.setFont(new Font("Serif", Font.BOLD, 16));
            ratingPanel.add(star);
        }

        JLabel ratingCount = new JLabel("(" + product.getRatings().size() + ")");
        ratingCount.setFont(UIConstants.CAPTION_FONT);
        ratingPanel.add(ratingCount);

        ratingPanel.revalidate();
        ratingPanel.repaint();
    }
}