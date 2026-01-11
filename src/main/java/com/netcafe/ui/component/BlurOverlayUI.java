package com.netcafe.ui.component;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * A LayerUI that applies a blur effect to a target component and toggles
 * the visibility of an overlay component (e.g., a button) on hover.
 * 
 * Usage:
 * 
 * <pre>
 * JPanel content = new JPanel();
 * JLabel image = new JLabel(icon);
 * JButton overlayBtn = new JButton("Add to Cart");
 * content.add(image);
 * 
 * BlurOverlayUI ui = new BlurOverlayUI(image, overlayBtn);
 * JLayer<JComponent> layer = new JLayer<>(content, ui);
 * </pre>
 */
public class BlurOverlayUI extends LayerUI<JComponent> {
    private boolean isHovering = false;
    private final Component blurTarget;
    private final Component overlayComponent;

    /**
     * Creates a BlurOverlayUI with both blur target and overlay component.
     * 
     * @param blurTarget       The component to blur on hover (e.g., image label)
     * @param overlayComponent The component to show/hide on hover (e.g., button)
     */
    public BlurOverlayUI(Component blurTarget, Component overlayComponent) {
        this.blurTarget = blurTarget;
        this.overlayComponent = overlayComponent;
        if (this.overlayComponent != null) {
            this.overlayComponent.setVisible(false);
        }
    }

    /**
     * Creates a BlurOverlayUI with only blur target (no overlay toggle).
     */
    public BlurOverlayUI(Component blurTarget) {
        this(blurTarget, null);
    }

    /**
     * Creates a BlurOverlayUI that blurs the entire content (no specific target).
     */
    public BlurOverlayUI() {
        this(null, null);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        if (!isHovering) {
            super.paint(g, c);
            return;
        }

        int w = c.getWidth();
        int h = c.getHeight();

        if (w == 0 || h == 0) {
            return;
        }

        // 1. Render the entire component (sharp) to an image
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setClip(g.getClip());
        super.paint(g2, c);
        g2.dispose();

        // 2. Determine blur region
        Rectangle blurRect;
        if (blurTarget != null && blurTarget.isVisible()) {
            try {
                blurRect = SwingUtilities.convertRectangle(
                        blurTarget.getParent(), blurTarget.getBounds(), c);
            } catch (Exception e) {
                blurRect = new Rectangle(0, 0, w, h);
            }
        } else {
            blurRect = new Rectangle(0, 0, w, h);
        }

        // 3. Draw the sharp image first
        g.drawImage(img, 0, 0, null);

        // 4. Apply blur to the target region only
        Rectangle effectiveRect = blurRect.intersection(new Rectangle(0, 0, w, h));

        if (!effectiveRect.isEmpty()) {
            BufferedImage toBlur = img.getSubimage(
                    effectiveRect.x, effectiveRect.y,
                    effectiveRect.width, effectiveRect.height);

            // Scale down then up for blur effect
            int scaledW = Math.max(1, effectiveRect.width / 4);
            int scaledH = Math.max(1, effectiveRect.height / 4);

            BufferedImage scaled = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gScaled = scaled.createGraphics();
            gScaled.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            gScaled.drawImage(toBlur, 0, 0, scaledW, scaledH, null);
            gScaled.dispose();

            Graphics2D gOut = (Graphics2D) g.create();
            gOut.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            gOut.drawImage(scaled, effectiveRect.x, effectiveRect.y,
                    effectiveRect.width, effectiveRect.height, null);
            gOut.dispose();
        }
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        JLayer<?> jlayer = (JLayer<?>) c;
        jlayer.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    @Override
    public void uninstallUI(JComponent c) {
        JLayer<?> jlayer = (JLayer<?>) c;
        jlayer.setLayerEventMask(0);
        super.uninstallUI(c);
    }

    @Override
    protected void processMouseEvent(MouseEvent e, JLayer<? extends JComponent> l) {
        if (e.getID() == MouseEvent.MOUSE_ENTERED) {
            setHovering(true, l);
        } else if (e.getID() == MouseEvent.MOUSE_EXITED) {
            Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), l);
            if (!l.contains(p)) {
                setHovering(false, l);
            }
        }
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JComponent> l) {
        Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), l);
        boolean contains = l.contains(p);
        if (contains != isHovering) {
            setHovering(contains, l);
        }
    }

    private void setHovering(boolean hovering, JLayer<? extends JComponent> l) {
        isHovering = hovering;
        if (overlayComponent != null) {
            overlayComponent.setVisible(hovering);
        }
        l.repaint();
    }
}
