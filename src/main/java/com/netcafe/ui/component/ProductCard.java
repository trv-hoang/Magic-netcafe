package com.netcafe.ui.component;

import com.netcafe.ui.ThemeConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * A reusable product card component with image, name, price, and action button.
 * Features blur effect on hover with overlay button.
 * Design: Large image on top, name and price below. Hover shows blue border +
 * blur + button.
 */
public class ProductCard extends JPanel {

    private static final int CARD_WIDTH = 180;
    private static final int CARD_HEIGHT = 220;
    private static final int IMAGE_SIZE = 150; // Square image
    private static final Color HOVER_BORDER_COLOR = new Color(52, 152, 219); // Cyan/Blue

    private final JLabel lblImage;
    private final JLabel lblName;
    private final JLabel lblPrice;
    private final JButton btnAction;
    private final JPanel btnWrapper;
    private final JPanel imagePanel;
    private boolean isHovering = false;
    private BufferedImage blurredImage = null;

    public ProductCard(String name, long price, String buttonText, Runnable onAction) {
        setLayout(new BorderLayout(0, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));

        // Image panel with custom painting for blur
        imagePanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (isHovering && blurredImage != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                    // Center the blurred image
                    int x = (getWidth() - blurredImage.getWidth()) / 2;
                    int y = (getHeight() - blurredImage.getHeight()) / 2;
                    g2.drawImage(blurredImage, x, y, null);
                    g2.dispose();
                }
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Draw rounded border on hover
                if (isHovering) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(HOVER_BORDER_COLOR);
                    g2.setStroke(new BasicStroke(3));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
                    g2.dispose();
                }
            }
        };
        imagePanel.setPreferredSize(new Dimension(IMAGE_SIZE, IMAGE_SIZE));
        imagePanel.setBackground(new Color(248, 249, 250)); // Light gray bg
        imagePanel.setOpaque(true);

        // Image Label - fills the image panel
        lblImage = new JLabel();
        lblImage.setBounds(0, 0, IMAGE_SIZE, IMAGE_SIZE);
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblImage.setVerticalAlignment(SwingConstants.CENTER);
        ImageLoader.loadIntoLabel(lblImage, name, IMAGE_SIZE - 10, IMAGE_SIZE - 10);
        imagePanel.add(lblImage);

        // Button (centered overlay) - icon style like reference
        btnAction = new JButton(buttonText);
        btnAction.putClientProperty("JButton.buttonType", "roundRect");
        btnAction.setBackground(new Color(52, 152, 219)); // Cyan blue
        btnAction.setForeground(Color.WHITE);
        btnAction.setFocusPainted(false);
        btnAction.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnAction.setPreferredSize(new Dimension(IMAGE_SIZE - 30, 40));
        btnAction.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAction.addActionListener(e -> {
            if (onAction != null)
                onAction.run();
        });

        btnWrapper = new JPanel(new GridBagLayout());
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnAction);
        btnWrapper.setBounds(0, 0, IMAGE_SIZE, IMAGE_SIZE);
        btnWrapper.setVisible(false);
        imagePanel.add(btnWrapper);

        // Mouse listener for hover effect
        MouseAdapter hoverAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isHovering) {
                    createBlurredImage();
                    setHovering(true);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), ProductCard.this);
                if (!ProductCard.this.contains(p)) {
                    setHovering(false);
                }
            }
        };

        imagePanel.addMouseListener(hoverAdapter);
        lblImage.addMouseListener(hoverAdapter);
        btnWrapper.addMouseListener(hoverAdapter);
        btnAction.addMouseListener(hoverAdapter);
        addMouseListener(hoverAdapter);

        // Center the image panel
        JPanel imageCenterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        imageCenterPanel.setBackground(Color.WHITE);
        imageCenterPanel.add(imagePanel);
        add(imageCenterPanel, BorderLayout.CENTER);

        // Details panel (Name + Price)
        JPanel details = new JPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.setBackground(Color.WHITE);
        details.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblName = new JLabel(name);
        lblName.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblName.setForeground(ThemeConfig.TEXT_PRIMARY);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblPrice = new JLabel(formatPrice(price));
        lblPrice.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblPrice.setForeground(ThemeConfig.SUCCESS);
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);

        details.add(lblName);
        details.add(Box.createVerticalStrut(3));
        details.add(lblPrice);

        JPanel detailsWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        detailsWrapper.setBackground(Color.WHITE);
        detailsWrapper.add(details);
        add(detailsWrapper, BorderLayout.SOUTH);
    }

    private void createBlurredImage() {
        if (lblImage.getIcon() != null && lblImage.getIcon() instanceof ImageIcon) {
            Image original = ((ImageIcon) lblImage.getIcon()).getImage();
            int w = lblImage.getIcon().getIconWidth();
            int h = lblImage.getIcon().getIconHeight();

            if (w > 0 && h > 0) {
                BufferedImage buffered = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = buffered.createGraphics();
                g2.drawImage(original, 0, 0, null);
                g2.dispose();

                // Smoother blur - scale to 1/3 instead of 1/6
                int blurW = Math.max(1, w / 3);
                int blurH = Math.max(1, h / 3);

                BufferedImage small = new BufferedImage(blurW, blurH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gSmall = small.createGraphics();
                gSmall.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                gSmall.drawImage(buffered, 0, 0, blurW, blurH, null);
                gSmall.dispose();

                blurredImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gBlur = blurredImage.createGraphics();
                gBlur.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                gBlur.drawImage(small, 0, 0, w, h, null);
                gBlur.dispose();
            }
        }
    }

    private void setHovering(boolean hovering) {
        if (isHovering != hovering) {
            isHovering = hovering;
            btnWrapper.setVisible(hovering);
            lblImage.setVisible(!hovering);
            imagePanel.repaint();
        }
    }

    public ProductCard(String name, String buttonText, Runnable onAction) {
        this(name, -1, buttonText, onAction);
        lblPrice.setVisible(false);
    }

    public void setProductName(String name) {
        lblName.setText(name);
    }

    public void setPrice(long price) {
        lblPrice.setText(formatPrice(price));
        lblPrice.setVisible(price >= 0);
    }

    public void setButtonText(String text) {
        btnAction.setText(text);
    }

    public void setImage(String name) {
        ImageLoader.loadIntoLabel(lblImage, name, IMAGE_SIZE - 10, IMAGE_SIZE - 10);
        blurredImage = null;
    }

    private static String formatPrice(long price) {
        if (price < 0)
            return "";
        return String.format("%,dđ", price).replace(",", ".");
    }
}
