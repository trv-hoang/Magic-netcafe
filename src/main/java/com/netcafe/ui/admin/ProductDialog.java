package com.netcafe.ui.admin;

import com.netcafe.model.Product;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ProductDialog extends JDialog {
    private final JTextField txtName = new JTextField(20);
    private final JComboBox<Product.Category> cbCategory = new JComboBox<>(Product.Category.values());
    private final JTextField txtPrice = new JTextField(10);
    private final JTextField txtStock = new JTextField(10);
    private final JLabel lblImagePreview = new JLabel("No Image", SwingConstants.CENTER);
    private File selectedImageFile;
    private boolean succeeded;

    public ProductDialog(Frame parent, Product product) {
        super(parent, product == null ? "Add Product" : "Edit Product", true);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(txtName, gbc);

        // Category
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        panel.add(cbCategory, gbc);

        // Price
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Price (VND):"), gbc);
        gbc.gridx = 1;
        panel.add(txtPrice, gbc);

        // Stock
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Stock:"), gbc);
        gbc.gridx = 1;
        panel.add(txtStock, gbc);

        // Image
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Image:"), gbc);

        JPanel imgPanel = new JPanel(new BorderLayout(10, 0));
        lblImagePreview.setPreferredSize(new Dimension(100, 100));
        lblImagePreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JButton btnSelectImage = new JButton("Select Image");
        btnSelectImage.addActionListener(e -> selectImage());

        imgPanel.add(lblImagePreview, BorderLayout.CENTER);
        imgPanel.add(btnSelectImage, BorderLayout.EAST);

        gbc.gridx = 1;
        panel.add(imgPanel, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save");
        btnSave.setBackground(com.netcafe.ui.ThemeConfig.PRIMARY);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);
        btnSave.putClientProperty("JButton.buttonType", "roundRect");

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(com.netcafe.ui.ThemeConfig.FONT_BODY_BOLD);

        btnSave.addActionListener(e -> {
            if (validateInput()) {
                succeeded = true;
                dispose();
            }
        });
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(btnPanel, BorderLayout.SOUTH);

        // Pre-fill if editing
        if (product != null) {
            txtName.setText(product.getName());
            cbCategory.setSelectedItem(product.getCategory());
            txtPrice.setText(String.valueOf(product.getPrice()));
            txtStock.setText(String.valueOf(product.getStock()));
            // Try to load existing image preview
            try {
                java.net.URL imgURL = getClass().getResource("/images/" + product.getName() + ".jpg");
                if (imgURL != null) {
                    ImageIcon icon = new ImageIcon(imgURL);
                    Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    lblImagePreview.setIcon(new ImageIcon(img));
                    lblImagePreview.setText("");
                }
            } catch (Exception ignored) {
            }
        }

        pack();
        setLocationRelativeTo(parent);
    }

    private void selectImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("JPG Images", "jpg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = chooser.getSelectedFile();
            ImageIcon icon = new ImageIcon(selectedImageFile.getAbsolutePath());
            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            lblImagePreview.setIcon(new ImageIcon(img));
            lblImagePreview.setText("");
        }
    }

    private boolean validateInput() {
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required.");
            return false;
        }
        try {
            long price = Long.parseLong(txtPrice.getText().trim());
            if (price < 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Price.");
            return false;
        }
        try {
            int stock = Integer.parseInt(txtStock.getText().trim());
            if (stock < 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Stock.");
            return false;
        }
        return true;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public String getName() {
        return txtName.getText().trim();
    }

    public Product.Category getCategory() {
        return (Product.Category) cbCategory.getSelectedItem();
    }

    public long getPrice() {
        return Long.parseLong(txtPrice.getText().trim());
    }

    public int getStock() {
        return Integer.parseInt(txtStock.getText().trim());
    }

    public void saveImage() {
        if (selectedImageFile != null) {
            try {
                // Target: src/main/resources/images/[Name].jpg
                // Note: This works for source code but running app might need to copy to
                // target/classes too for immediate effect
                String name = getName();
                File resourcesDir = new File("src/main/resources/images");
                if (!resourcesDir.exists())
                    resourcesDir.mkdirs();

                File destFile = new File(resourcesDir, name + ".jpg");
                Files.copy(selectedImageFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Also copy to target/classes/images so it appears without rebuild if possible
                File targetDir = new File("target/classes/images");
                if (targetDir.exists()) {
                    File targetFile = new File(targetDir, name + ".jpg");
                    Files.copy(selectedImageFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving image: " + e.getMessage());
            }
        }
    }
}
