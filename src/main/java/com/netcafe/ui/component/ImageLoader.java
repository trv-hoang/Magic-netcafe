package com.netcafe.ui.component;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;

/**
 * Utility class for loading and scaling images with fallback support.
 */
public class ImageLoader {

    /**
     * Loads an image from resources or dev directory, scales it, and returns the
     * icon.
     * 
     * @param name      The name of the image (without extension)
     * @param maxWidth  Maximum width for scaling
     * @param maxHeight Maximum height for scaling
     * @return ImageIcon or null if not found
     */
    public static ImageIcon loadImage(String name, int maxWidth, int maxHeight) {
        return loadImage(name, ".jpg", maxWidth, maxHeight);
    }

    /**
     * Loads an image with specified extension.
     */
    public static ImageIcon loadImage(String name, String extension, int maxWidth, int maxHeight) {
        String imagePath = "/images/" + name + extension;
        URL imgURL = ImageLoader.class.getResource(imagePath);

        ImageIcon icon = null;
        if (imgURL != null) {
            icon = new ImageIcon(imgURL);
        } else {
            // Fallback: Try loading from source directory (for dev mode)
            File devFile = new File("src/main/resources/images/" + name + extension);
            if (devFile.exists()) {
                icon = new ImageIcon(devFile.getAbsolutePath());
            }
        }

        if (icon != null) {
            return scaleImage(icon, maxWidth, maxHeight);
        }
        return null;
    }

    /**
     * Scales an ImageIcon to fit within max dimensions while preserving aspect
     * ratio.
     */
    public static ImageIcon scaleImage(ImageIcon icon, int maxWidth, int maxHeight) {
        int originalWidth = icon.getIconWidth();
        int originalHeight = icon.getIconHeight();

        if (originalWidth <= 0 || originalHeight <= 0) {
            return icon;
        }

        double ratio = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        Image scaledImg = icon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImg);
    }

    /**
     * Creates a placeholder label with the first letter of the name.
     */
    public static void setPlaceholder(JLabel label, String name) {
        label.setIcon(null);
        label.setText(name != null && !name.isEmpty() ? String.valueOf(name.charAt(0)) : "?");
        label.setFont(new Font("SansSerif", Font.BOLD, 48));
        label.setForeground(new Color(200, 200, 200));
        label.setHorizontalAlignment(SwingConstants.CENTER);
    }

    /**
     * Loads an image into a JLabel, with automatic placeholder fallback.
     * 
     * @param label     The JLabel to populate
     * @param name      The image name
     * @param maxWidth  Max width
     * @param maxHeight Max height
     */
    public static void loadIntoLabel(JLabel label, String name, int maxWidth, int maxHeight) {
        ImageIcon icon = loadImage(name, maxWidth, maxHeight);
        if (icon != null) {
            label.setIcon(icon);
            label.setText("");
        } else {
            setPlaceholder(label, name);
        }
    }
}
