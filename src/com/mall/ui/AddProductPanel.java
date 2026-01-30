package com.mall.ui;

import com.mall.model.Product;
import com.mall.service.MallManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class AddProductPanel extends JPanel {
    private JTextField nameField, priceField, stockField, categoryField;
    private JTextArea descArea;
    private JLabel imagePreview;
    private File tempSelectedFile;
    private Product existingProduct;

    public AddProductPanel(MainFrame parent, MallManager manager, Product productToEdit) {
        this.existingProduct = productToEdit;
        boolean isEditMode = (existingProduct != null);

        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_COLOR);

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(UIConstants.SURFACE_COLOR);
        header.setBorder(new EmptyBorder(16, UIConstants.GUTTER, 16, UIConstants.GUTTER));
        JLabel title = new JLabel(isEditMode ? "EDIT PRODUCT: " + existingProduct.getId() : "ADD NEW PRODUCT");
        title.setFont(UIConstants.H2_FONT);
        header.add(title);

        // Form Layout
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(UIConstants.SURFACE_COLOR);
        body.setBorder(new EmptyBorder(32, 40, 32, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Fields
        nameField = createStyledTextField();
        categoryField = createStyledTextField();
        priceField = createStyledTextField();
        stockField = createStyledTextField();
        descArea = new JTextArea(3, 20);
        descArea.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT));

        // Image Section
        imagePreview = new JLabel("No Image Selected", SwingConstants.CENTER);
        imagePreview.setPreferredSize(new Dimension(150, 150));
        imagePreview.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT));

        ModernButton pickImgBtn = new ModernButton("Select Image");
        pickImgBtn.addActionListener(e -> selectImage());

        // Pre-fill Logic
        if (isEditMode) {
            nameField.setText(existingProduct.getName());
            categoryField.setText(existingProduct.getCategory());
            priceField.setText(existingProduct.getPrice().toString());
            stockField.setText(String.valueOf(existingProduct.getStockQty()));
            descArea.setText(existingProduct.getDescription());
            updatePreview(new File(existingProduct.getImagePath()));
        }

        // Add to layout (Simplified GridBag approach)
        gbc.gridy = 0;
        addLabeledField(body, "Product Name", nameField, gbc);
        gbc.gridy = 2;
        addLabeledField(body, "Category", categoryField, gbc);
        gbc.gridy = 4;
        addLabeledField(body, "Price", priceField, gbc);
        gbc.gridy = 6;
        addLabeledField(body, "Stock", stockField, gbc);

        // Image UI placement
        gbc.gridy = 8;
        body.add(new JLabel("Product Image"), gbc);
        gbc.gridy = 9;
        JPanel imgPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        imgPanel.setOpaque(false);
        imgPanel.add(imagePreview);
        imgPanel.add(pickImgBtn);
        body.add(imgPanel, gbc);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 20));
        footer.setOpaque(false);
        ModernButton saveBtn = new ModernButton(isEditMode ? "Save Updates" : "Add Product");
        saveBtn.addActionListener(e -> handleSave(manager, parent, isEditMode));

        ModernButton backBtn = new ModernButton("Cancel");
        backBtn.addActionListener(e -> parent.showView("CATALOG_ADMIN"));

        footer.add(backBtn);
        footer.add(saveBtn);

        add(header, BorderLayout.NORTH);
        add(new JScrollPane(body), BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private void selectImage() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            tempSelectedFile = chooser.getSelectedFile();
            updatePreview(tempSelectedFile);
        }
    }

    private void updatePreview(File file) {
        if (file != null && file.exists()) {
            ImageIcon icon = new ImageIcon(file.getPath());
            Image scaled = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            imagePreview.setIcon(new ImageIcon(scaled));
            imagePreview.setText("");
        }
    }

    private void handleSave(MallManager manager, MainFrame parent, boolean isEdit) {
        try {
            String id = isEdit ? existingProduct.getId() : UUID.randomUUID().toString().substring(0, 8);
            String finalPath = existingProduct != null ? existingProduct.getImagePath() : "";

            // Handle Image Saving/Replacing
            if (tempSelectedFile != null) {
                File dir = new File("resources/images/products/");
                if (!dir.exists())
                    dir.mkdirs();

                String extension = getFileExtension(tempSelectedFile);
                File destination = new File(dir, id + extension);

                Files.copy(tempSelectedFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                finalPath = destination.getPath();
            }

            BigDecimal price = new BigDecimal(priceField.getText());
            int stock = Integer.parseInt(stockField.getText());

            if (isEdit) {
                existingProduct.setName(nameField.getText());
                existingProduct.setCategory(categoryField.getText());
                existingProduct.setPrice(price);
                existingProduct.setStockQty(stock);
                existingProduct.setDescription(descArea.getText());
            } else {
                Product p = new Product(id, nameField.getText(), categoryField.getText(),
                        price, stock, descArea.getText(), finalPath);
                manager.getCatalog().addProduct(p);
                manager.saveData();
            }

            JOptionPane.showMessageDialog(this, "Success!");
            parent.showView("CATALOG_ADMIN");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return (lastDot == -1) ? ".jpg" : name.substring(lastDot);
    }

    private void addLabeledField(JPanel p, String label, JComponent field, GridBagConstraints gbc) {
        gbc.insets = new Insets(0, 0, 4, 0);
        p.add(new JLabel(label), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 16, 0);
        p.add(field, gbc);
    }

    private JTextField createStyledTextField() {
        JTextField f = new JTextField();
        f.setPreferredSize(new Dimension(0, 40));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT), new EmptyBorder(0, 10, 0, 10)));
        return f;
    }
}