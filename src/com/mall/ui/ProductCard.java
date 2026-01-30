package com.mall.ui;

import com.mall.model.Administrator;
import com.mall.model.Product;
import com.mall.service.MallManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

public class ProductCard extends JPanel {
    private final Product product;
    private final MallManager manager;
    private final MainFrame parent;

    private static final int WIDTH = 280;
    private static final int HEIGHT = 400;
    private static final int IMAGE_HEIGHT = (int) (HEIGHT * 0.6);

    public ProductCard(Product p, MallManager manager, MainFrame parent) {
        this.product = p;
        this.manager = manager;
        this.parent = parent;

        Dimension dimensions = new Dimension(WIDTH, HEIGHT);
        setPreferredSize(dimensions);
        setMaximumSize(dimensions);
        setMinimumSize(dimensions);

        setLayout(new BorderLayout(0, 16));
        setBackground(UIConstants.SURFACE_COLOR);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1),
                new EmptyBorder(24, 24, 24, 24)));

        // --- Left: Image Thumbnail ---
        JLabel thumbLabel = new JLabel();
        thumbLabel.setPreferredSize(new Dimension(WIDTH, IMAGE_HEIGHT));
        thumbLabel.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT));
        thumbLabel.setHorizontalAlignment(SwingConstants.CENTER);

        String no_image_path = "resources/images/ui/no_image.jpg";
        String actualPath = p.getImagePath();
        String path = (actualPath != null && !actualPath.isEmpty()) ? actualPath : no_image_path;

        try {
            ImageIcon icon = new ImageIcon(path);
            Image scaled = icon.getImage().getScaledInstance(WIDTH, IMAGE_HEIGHT, Image.SCALE_SMOOTH);
            thumbLabel.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            thumbLabel.setText("No Image Available");
            thumbLabel.setFont(UIConstants.CAPTION_FONT);
            thumbLabel.setForeground(UIConstants.TEXT_SECONDARY);
        }

        // --- Center: Info ---
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(p.getName());
        nameLabel.setFont(UIConstants.H2_FONT);
        nameLabel.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel priceLabel = new JLabel("$" + String.format("%.2f", p.getPrice()));
        priceLabel.setFont(UIConstants.PRICE_FONT);
        priceLabel.setForeground(UIConstants.PRIMARY_COLOR);

        JLabel stockLabel = new JLabel("Stock Count: " + p.getStockQty());
        stockLabel.setFont(UIConstants.CAPTION_FONT);
        stockLabel.setForeground(UIConstants.TEXT_SECONDARY);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        infoPanel.add(stockLabel);

        // --- Right: Dynamic Action Button ---
        boolean isAdmin = manager.getAuthService().getCurrentUser() instanceof Administrator;
        ModernButton actionBtn = new ModernButton(isAdmin ? "Edit" : "Add to Cart");
        actionBtn.setPreferredSize(new Dimension(isAdmin ? 80 : 110, 35));

        actionBtn.addActionListener(e -> {
            if (isAdmin) {
                parent.showEditProductView(p);
            } else {
                showQuantityDialog();
            }
        });

        // assembly:
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        contentPanel.add(infoPanel, BorderLayout.CENTER);
        contentPanel.add(actionBtn, BorderLayout.SOUTH);

        add(thumbLabel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
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
                var customer = manager.getAuthService().getCurrentCustomer();
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
}