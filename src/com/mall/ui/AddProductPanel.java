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
    // Input fields for product properties
    private JTextField nameField, priceField, stockField, categoryField;
    // Multi-line description area
    private JTextArea descArea;
    // Label used to preview selected image
    private JLabel imagePreview;
    // Temporarily holds a user-selected image file before saving
    private File tempSelectedFile;
    // When editing an existing product, this holds the product being edited
    private Product existingProduct;

    // Constructor: builds the UI and optionally pre-fills fields for edit mode
    public AddProductPanel(MainFrame parent, MallManager manager, Product productToEdit) {
        this.existingProduct = productToEdit;
        boolean isEditMode = (existingProduct != null);

        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_COLOR);

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(UIConstants.SURFACE_COLOR);
        header.setBorder(new EmptyBorder(16, UIConstants.GUTTER, 16, UIConstants.GUTTER));
        // Title reflects whether we are adding a new product or editing an existing one
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
        // createStyledTextField() centralizes visual style for all single-line fields
        nameField = createStyledTextField();
        categoryField = createStyledTextField();
        priceField = createStyledTextField();
        stockField = createStyledTextField();

        // Description area: multi-line with wrapping and scroll pane
        descArea = new JTextArea(4, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        // Match JTextField font to keep visual consistency
        descArea.setFont(new JTextField().getFont()); // Match JTextField font
        JScrollPane descScrollPane = new JScrollPane(descArea);
        descScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT), new EmptyBorder(5, 10, 5, 10)));

        // Image Section
        // imagePreview shows the currently selected image or a placeholder text
        imagePreview = new JLabel("No Image Selected", SwingConstants.CENTER);
        imagePreview.setPreferredSize(new Dimension(150, 150));
        imagePreview.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT));

        // Button to open file chooser for selecting product image
        ModernButton pickImgBtn = new ModernButton("Select Image");
        pickImgBtn.addActionListener(e -> selectImage());

        // Pre-fill Logic: when editing fill fields with existing product values
        if (isEditMode) {
            nameField.setText(existingProduct.getName());
            categoryField.setText(existingProduct.getCategory());
            priceField.setText(existingProduct.getPrice().toString());
            stockField.setText(String.valueOf(existingProduct.getStockQty()));
            descArea.setText(existingProduct.getDescription());
            // Show existing product image in preview if available
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
        gbc.gridy = 8;
        addLabeledField(body, "Description", descScrollPane, gbc);

        // Image UI placement: label and button grouped together
        gbc.gridy = 10;
        body.add(new JLabel("Product Image"), gbc);
        gbc.gridy = 11;
        JPanel imgPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        imgPanel.setOpaque(false);
        imgPanel.add(imagePreview);
        imgPanel.add(pickImgBtn);
        body.add(imgPanel, gbc);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 20));
        footer.setOpaque(false);
        ModernButton saveBtn = new ModernButton(isEditMode ? "Save Updates" : "Add Product");
        // Save action handles both add and edit flows and persists changes
        saveBtn.addActionListener(e -> handleSave(manager, parent, isEditMode));

        ModernButton backBtn = new ModernButton("Cancel");
        backBtn.addActionListener(e -> parent.showView("CATALOG_ADMIN"));

        footer.add(backBtn);
        footer.add(saveBtn);

        add(header, BorderLayout.NORTH);
        add(new JScrollPane(body), BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    // Opens a JFileChooser to let the user pick an image file for the product
    private void selectImage() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            tempSelectedFile = chooser.getSelectedFile();
            updatePreview(tempSelectedFile);
        }
    }

    // Update the imagePreview label with a scaled image when a file is selected
    private void updatePreview(File file) {
        if (file != null && file.exists()) {
            ImageIcon icon = new ImageIcon(file.getPath());
            Image scaled = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            imagePreview.setIcon(new ImageIcon(scaled));
            imagePreview.setText("");
        }
    }

    // Handles saving a new product or applying updates to an existing product
    private void handleSave(MallManager manager, MainFrame parent, boolean isEdit) {
        try {
            // Generate a new id for new products; reuse existing id in edit mode
            String id = isEdit ? existingProduct.getId() : UUID.randomUUID().toString().substring(0, 8);
            // Default to existing product image path when editing; otherwise empty until image saved
            String finalPath = existingProduct != null ? existingProduct.getImagePath() : "";

            // Handle Image Saving/Replacing
            if (tempSelectedFile != null) {
                File dir = new File("resources/images/products/");
                if (!dir.exists())
                    dir.mkdirs();

                String extension = getFileExtension(tempSelectedFile);
                File destination = new File(dir, id + extension);

                // Copy the selected file into the application's product images directory
                Files.copy(tempSelectedFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                finalPath = destination.getPath();
            }

            // Parse numeric fields from text inputs; may throw NumberFormatException which is caught below
            BigDecimal price = new BigDecimal(priceField.getText());
            int stock = Integer.parseInt(stockField.getText());

            if (isEdit) {
                // Update fields of the existing product instance and persist
                existingProduct.setName(nameField.getText());
                existingProduct.setCategory(categoryField.getText());
                existingProduct.setPrice(price);
                existingProduct.setStockQty(stock);
                existingProduct.setDescription(descArea.getText());
                manager.saveData();
            } else {
                // Create a new Product and add it to the product service
                Product p = new Product(id, nameField.getText(), categoryField.getText(),
                        price, stock, descArea.getText(), finalPath);
                manager.getProductService().addProduct(p);
                manager.saveData();
            }

            // Inform the user of success and navigate back to the admin catalog view
            JOptionPane.showMessageDialog(this, "Success!");
            parent.showView("CATALOG_ADMIN");
        } catch (Exception ex) {
            // Show an error dialog with the exception message if anything goes wrong
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Returns the file extension (including dot) for the provided file name; defaults to .jpg
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return (lastDot == -1) ? ".jpg" : name.substring(lastDot);
    }

    // Helper that adds a label and the given field to the panel using provided GridBagConstraints
    private void addLabeledField(JPanel p, String label, JComponent field, GridBagConstraints gbc) {
        gbc.insets = new Insets(0, 0, 4, 0);
        p.add(new JLabel(label), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 16, 0);
        p.add(field, gbc);
    }

    // Factory for creating styled single-line text fields used across the form
    private JTextField createStyledTextField() {
        JTextField f = new JTextField();
        f.setPreferredSize(new Dimension(0, 40));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT), new EmptyBorder(0, 10, 0, 10)));
        return f;
    }
}
