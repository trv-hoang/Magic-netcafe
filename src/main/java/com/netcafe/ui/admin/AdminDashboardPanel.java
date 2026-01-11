package com.netcafe.ui.admin;

import com.netcafe.ui.ThemeConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AdminDashboardPanel extends JPanel {

    public AdminDashboardPanel(JFrame parentFrame) {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(33, 33, 33)); // Keep dark header for contrast
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setFont(ThemeConfig.FONT_HEADER);
        titleLabel.setForeground(Color.WHITE);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(ThemeConfig.DANGER);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            parentFrame.dispose();
            new com.netcafe.ui.login.LoginFrame().setVisible(true);
        });

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(btnLogout, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Main Content with LayeredPane for FAB
        JLayeredPane layeredPane = new JLayeredPane();
        add(layeredPane, BorderLayout.CENTER);

        // Tabs (Layer 0)
        JTabbedPane adminTabs = new JTabbedPane();
        adminTabs.addTab("Order Management", new OrderManagementPanel());
        adminTabs.addTab("Topup Requests", new TopupRequestPanel());
        adminTabs.addTab("User Management", new UserManagementPanel());
        adminTabs.addTab("Messages", new MessagePanel());
        // AI Analyst Tab REMOVED
        adminTabs.addTab("Computer Map", new ComputerMapPanel());
        adminTabs.addTab("Statistics", new StatisticsPanel());

        // Handle resizing
        layeredPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                adminTabs.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
                // Ensure FAB stays within bounds if window shrinks
                Component fab = null;
                for (Component c : layeredPane.getComponentsInLayer(JLayeredPane.PALETTE_LAYER)) {
                    fab = c;
                    break;
                }
                if (fab != null) {
                    int x = Math.min(fab.getX(), layeredPane.getWidth() - fab.getWidth());
                    int y = Math.min(fab.getY(), layeredPane.getHeight() - fab.getHeight());
                    fab.setLocation(x, y);
                }
            }
        });

        layeredPane.add(adminTabs, JLayeredPane.DEFAULT_LAYER);

        // FAB (Layer 1)
        DraggableRoundButton fab = new DraggableRoundButton();
        fab.addActionListener(e -> {
            AIAnalystDialog dialog = new AIAnalystDialog(parentFrame);
            dialog.setVisible(true);
        });

        // Initial Position (Bottom-Right)
        // We need to set this after the frame is realized, or just set a default and
        // let the user move it.
        // Hardcoding a reasonable default for now, assuming typical size.
        fab.setBounds(700, 500, 60, 60);

        layeredPane.add(fab, JLayeredPane.PALETTE_LAYER);
    }

    // Custom Round Draggable Button
    private static class DraggableRoundButton extends JButton {
        private static final int DRAG_THRESHOLD = 5; // Pixels before considered a drag
        private Point initialClick;
        private Point pressPoint; // Track the original press location
        private boolean isDragging = false;

        public DraggableRoundButton() {
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setPreferredSize(new Dimension(60, 60));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Drag handling
            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    initialClick = e.getPoint();
                    pressPoint = e.getPoint();
                    isDragging = false;
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    // Calculate total distance moved from press point
                    double distance = Point.distance(pressPoint.x, pressPoint.y, e.getX(), e.getY());

                    // Only start dragging if we've moved past the threshold
                    if (distance > DRAG_THRESHOLD) {
                        isDragging = true;
                    }

                    // Only move the button if we're actually dragging
                    if (isDragging) {
                        // get location of window
                        int thisX = getLocation().x;
                        int thisY = getLocation().y;

                        // Determine how much the mouse moved since the initial click
                        int xMoved = e.getX() - initialClick.x;
                        int yMoved = e.getY() - initialClick.y;

                        // Move window to this position
                        int X = thisX + xMoved;
                        int Y = thisY + yMoved;

                        // Keep within parent bounds
                        Container parent = getParent();
                        if (parent != null) {
                            X = Math.max(0, Math.min(X, parent.getWidth() - getWidth()));
                            Y = Math.max(0, Math.min(Y, parent.getHeight() - getHeight()));
                        }

                        setLocation(X, Y);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (!isDragging) {
                        fireActionPerformed(new java.awt.event.ActionEvent(DraggableRoundButton.this,
                                java.awt.event.ActionEvent.ACTION_PERFORMED, "click"));
                    }
                    isDragging = false;
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Background
            if (getModel().isArmed()) {
                g2.setColor(ThemeConfig.PRIMARY.darker());
            } else {
                g2.setColor(ThemeConfig.PRIMARY);
            }
            g2.fillOval(0, 0, getWidth(), getHeight());

            // Icon (Simple Robot Face)
            g2.setColor(Color.WHITE);
            // Head
            g2.fillRoundRect(15, 15, 30, 25, 10, 10);
            // Eyes
            g2.setColor(ThemeConfig.PRIMARY);
            g2.fillOval(20, 22, 6, 6);
            g2.fillOval(34, 22, 6, 6);
            // Antenna
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(30, 15, 30, 8);
            g2.fillOval(27, 5, 6, 6);

            g2.dispose();
        }

        @Override
        public boolean contains(int x, int y) {
            int radius = getWidth() / 2;
            return Point.distance(x, y, getWidth() / 2, getHeight() / 2) < radius;
        }
    }
}
