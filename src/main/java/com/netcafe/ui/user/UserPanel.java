package com.netcafe.ui.user;

import com.netcafe.config.AppConfig;
import com.netcafe.dao.ProductDAO;
import com.netcafe.model.Product;
import com.netcafe.model.Session;
import com.netcafe.model.User;
import com.netcafe.service.BillingService;
import com.netcafe.service.SessionService;
import com.netcafe.util.TimeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class UserPanel extends JPanel {
    private final User user;
    private final SessionService sessionService = new SessionService();
    private final BillingService billingService = new BillingService();
    private final ProductDAO productDAO = new ProductDAO();

    // Persistent Header Components
    private final JLabel lblBalance = new JLabel("Balance: Loading...");
    private final JLabel lblTimeRemaining = new JLabel("Time Remaining: --:--:--");
    private final JLabel lblStatus = new JLabel("Status: IDLE");

    private Timer timer;
    private Session currentSession;
    private long currentBalance;
    private int ratePerHour;

    // Cart Data
    private static class CartItem {
        Product product;
        int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }

    private final List<CartItem> cartItems = new java.util.ArrayList<>();
    // Columns: Name, Price, Qty
    private final DefaultTableModel cartModel = new DefaultTableModel(new String[] { "Name", "Price", "Qty" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Handle clicks via MouseListener
        }
    };
    private final JLabel lblCartTotal = new JLabel("Total: 0 VND");

    public UserPanel(User user) {
        this.user = user;
        this.ratePerHour = AppConfig.getInt("rate.vnd_per_hour", 10000);

        setLayout(new BorderLayout());

        // 1. Top Header Panel
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Main Content (Tabs Center, Cart Right)
        JPanel mainContent = new JPanel(new BorderLayout());

        // Left: Tabs (Games, Foods, Drinks, Topup)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Games", createPlayPanel());
        tabbedPane.addTab("Foods", createProductCategoryPanel(Product.Category.FOOD));
        tabbedPane.addTab("Drinks", createProductCategoryPanel(Product.Category.DRINK));
        tabbedPane.addTab("Topup", createTopupPanel());
        tabbedPane.addTab("Chat", createChatPanel());

        mainContent.add(tabbedPane, BorderLayout.CENTER);

        // Right: Cart
        mainContent.add(createCartPanel(), BorderLayout.EAST);

        add(mainContent, BorderLayout.CENTER);

        // Initial Data Load
        refreshBalance();
        checkActiveSession();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setBackground(Color.WHITE);
        // Bottom border separator + Padding
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        // Left: Welcome & Status
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        leftPanel.setBackground(Color.WHITE);

        JLabel lblWelcome = new JLabel("Welcome, " + user.getFullName());
        lblWelcome.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblWelcome.setForeground(new Color(50, 50, 50));

        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblStatus.setForeground(Color.GRAY);

        leftPanel.add(lblWelcome);
        leftPanel.add(lblStatus);
        panel.add(leftPanel, BorderLayout.WEST);

        // Center: Balance & Time
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        centerPanel.setBackground(Color.WHITE);

        lblBalance.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblBalance.setForeground(new Color(39, 174, 96)); // Green

        lblTimeRemaining.setFont(new Font("Monospaced", Font.BOLD, 18));
        lblTimeRemaining.setForeground(Color.BLACK);

        centerPanel.add(lblBalance);
        centerPanel.add(lblTimeRemaining);
        panel.add(centerPanel, BorderLayout.CENTER);

        // Right: Logout Button
        JButton btnLogout = new JButton("Logout");
        btnLogout.putClientProperty("JButton.buttonType", "roundRect");
        btnLogout.setBackground(new Color(231, 76, 60)); // Red for logout
        btnLogout.setForeground(Color.WHITE);
        btnLogout.addActionListener(e -> logout());

        panel.add(btnLogout, BorderLayout.EAST);

        return panel;
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            stopSession();
        }
    }

    private JPanel createPlayPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        String[] games = { "League of Legends", "Dota 2", "PUBG", "Valorant", "FIFA Online 4", "CS2", "Minecraft",
                "Roblox" };
        for (String game : games) {
            JButton btnGame = new JButton(game);
            btnGame.setPreferredSize(new Dimension(180, 100));
            btnGame.setFont(new Font("SansSerif", Font.BOLD, 14));
            btnGame.addActionListener(e -> JOptionPane.showMessageDialog(this, "Launching " + game + "..."));
            panel.add(btnGame);
        }
        return panel;
    }

    private JPanel createProductCategoryPanel(Product.Category category) {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel gridPanel = new JPanel(new GridLayout(0, 3, 10, 10)); // 3 columns
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Load products
        SwingWorker<List<Product>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Product> doInBackground() throws Exception {
                return productDAO.findAll().stream()
                        .filter(p -> p.getCategory() == category)
                        .collect(Collectors.toList());
            }

            @Override
            protected void done() {
                try {
                    List<Product> products = get();
                    gridPanel.removeAll();
                    for (Product p : products) {
                        gridPanel.add(createProductCard(p));
                    }
                    gridPanel.revalidate();
                    gridPanel.repaint();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UserPanel.this, "Error loading products: " + ex.getMessage());
                }
            }
        };
        worker.execute();

        return panel;
    }

    private JPanel createProductCard(Product p) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        // Subtle border with padding
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        card.setPreferredSize(new Dimension(180, 240)); // Slightly larger

        // Preview Picture
        JLabel lblImage = new JLabel();
        lblImage.setPreferredSize(new Dimension(180, 120));
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblImage.setBackground(Color.WHITE);
        lblImage.setOpaque(true);

        // Try to load image
        // Try to load image
        String imagePath = "/images/" + p.getName() + ".jpg"; // Assuming JPG for now
        java.net.URL imgURL = getClass().getResource(imagePath);

        ImageIcon icon = null;
        if (imgURL != null) {
            icon = new ImageIcon(imgURL);
        } else {
            // Fallback: Try loading from source directory (for dev mode)
            java.io.File devFile = new java.io.File("src/main/resources/images/" + p.getName() + ".jpg");
            if (devFile.exists()) {
                icon = new ImageIcon(devFile.getAbsolutePath());
            }
        }

        if (icon != null) {
            // Scale to fit while preserving aspect ratio
            Image img = icon.getImage();
            int originalWidth = icon.getIconWidth();
            int originalHeight = icon.getIconHeight();

            if (originalWidth > 0 && originalHeight > 0) {
                double ratio = Math.min(150.0 / originalWidth, 110.0 / originalHeight); // Max 150x110
                int newWidth = (int) (originalWidth * ratio);
                int newHeight = (int) (originalHeight * ratio);

                Image scaledImg = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                lblImage.setIcon(new ImageIcon(scaledImg));
            } else {
                // Fallback if image load fails
                lblImage.setText(String.valueOf(p.getName().charAt(0)));
                lblImage.setFont(new Font("SansSerif", Font.BOLD, 48));
                lblImage.setForeground(new Color(200, 200, 200));
            }
        } else {
            // Fallback to placeholder
            lblImage.setText(String.valueOf(p.getName().charAt(0)));
            lblImage.setFont(new Font("SansSerif", Font.BOLD, 48));
            lblImage.setForeground(new Color(200, 200, 200));
        }

        card.add(lblImage, BorderLayout.CENTER);

        // Details
        JPanel details = new JPanel(new GridLayout(3, 1, 0, 4)); // Increased gap
        details.setBackground(Color.WHITE);
        details.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JLabel lblName = new JLabel(p.getName(), SwingConstants.CENTER);
        lblName.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblName.setForeground(new Color(30, 30, 30));

        JLabel lblPrice = new JLabel(String.format("%,d VND", p.getPrice()), SwingConstants.CENTER);
        lblPrice.setForeground(new Color(39, 174, 96)); // Green
        lblPrice.setFont(new Font("SansSerif", Font.BOLD, 13));

        JButton btnAdd = new JButton("Add to Cart");
        btnAdd.putClientProperty("JButton.buttonType", "roundRect");
        btnAdd.putClientProperty("Component.arc", 999); // Pill shape
        btnAdd.setBackground(new Color(52, 152, 219));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnAdd.setPreferredSize(new Dimension(0, 30)); // Fixed height

        details.add(lblName);
        details.add(lblPrice);
        details.add(btnAdd);
        card.add(details, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> addToCart(p));

        return card;
    }

    private void addToCart(Product p) {
        boolean found = false;
        for (CartItem item : cartItems) {
            // Match by ID for normal products, or by Price/Name for Topups (ID -1)
            boolean sameId = (p.getId() != -1 && item.product.getId() == p.getId());
            boolean sameTopup = (p.getId() == -1 && item.product.getId() == -1
                    && item.product.getPrice() == p.getPrice());

            if (sameId || sameTopup) {
                item.quantity++;
                found = true;
                break;
            }
        }
        if (!found) {
            cartItems.add(new CartItem(p, 1));
        }
        refreshCartTable();
    }

    private void refreshCartTable() {
        cartModel.setRowCount(0);
        for (CartItem item : cartItems) {
            cartModel.addRow(new Object[] {
                    item.product.getName(),
                    item.product.getPrice(),
                    item.quantity // Integer, rendered as panel
            });
        }
        updateCartTotal();
    }

    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(380, 0)); // Increased width
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel lblTitle = new JLabel("Your Cart");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panel.add(lblTitle, BorderLayout.NORTH);

        JTable table = new JTable(cartModel);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(45); // Taller rows
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setSelectionBackground(new Color(240, 248, 255));
        table.setSelectionForeground(Color.BLACK);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(140); // Name
        table.getColumnModel().getColumn(1).setPreferredWidth(80); // Price
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Quantity Panel

        // Custom Renderer for Quantity Column
        table.getColumnModel().getColumn(2).setCellRenderer(new QuantityCellRenderer());

        // Click Listener for Quantity Buttons
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());

                if (row >= 0 && col == 2) { // Quantity column
                    Rectangle cellRect = table.getCellRect(row, col, false);
                    int x = e.getX() - cellRect.x;
                    int width = cellRect.width;
                    int oneThird = width / 3;

                    if (x < oneThird) { // Clicked first third (Minus)
                        CartItem item = cartItems.get(row);
                        item.quantity--;
                        if (item.quantity <= 0) {
                            cartItems.remove(row);
                        }
                        refreshCartTable();
                    } else if (x >= 2 * oneThird) { // Clicked last third (Plus)
                        cartItems.get(row).quantity++;
                        refreshCartTable();
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 15));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        lblCartTotal.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblCartTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        bottomPanel.add(lblCartTotal, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        JButton btnClear = new JButton("Clear");
        JButton btnCheckout = new JButton("Checkout");

        btnClear.putClientProperty("JButton.buttonType", "roundRect");
        btnClear.setBackground(new Color(231, 76, 60));
        btnClear.setForeground(Color.WHITE);
        btnClear.setFont(new Font("SansSerif", Font.BOLD, 14));

        btnCheckout.putClientProperty("JButton.buttonType", "roundRect");
        btnCheckout.setBackground(new Color(46, 204, 113));
        btnCheckout.setForeground(Color.WHITE);
        btnCheckout.setFont(new Font("SansSerif", Font.BOLD, 14));

        btnPanel.add(btnClear);
        btnPanel.add(btnCheckout);
        bottomPanel.add(btnPanel, BorderLayout.CENTER);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        btnClear.addActionListener(e -> {
            cartItems.clear();
            refreshCartTable();
        });

        btnCheckout.addActionListener(e -> checkout());

        return panel;
    }

    // Custom Renderer for Quantity Column
    class QuantityCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JButton btnMinus = new JButton("-");
        private final JLabel lblQty = new JLabel("0");
        private final JButton btnPlus = new JButton("+");

        public QuantityCellRenderer() {
            setLayout(new GridLayout(1, 3, 2, 0)); // 1 row, 3 cols, 2px gap
            setOpaque(true);
            setBackground(Color.WHITE);

            btnMinus.setFocusable(false);
            btnPlus.setFocusable(false);
            lblQty.setHorizontalAlignment(SwingConstants.CENTER);
            lblQty.setFont(new Font("SansSerif", Font.BOLD, 12));

            add(btnMinus);
            add(lblQty);
            add(btnPlus);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(Color.WHITE);
            }
            lblQty.setText(value.toString());
            return this;
        }
    }

    private void updateCartTotal() {
        long total = cartItems.stream().mapToLong(i -> i.product.getPrice() * i.quantity).sum();
        lblCartTotal.setText("Total: " + total + " VND");
    }

    private void checkout() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty!");
            return;
        }

        long total = cartItems.stream().mapToLong(i -> i.product.getPrice() * i.quantity).sum();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Pay " + total + " VND for items?", "Confirm Payment",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    for (CartItem item : cartItems) {
                        Product p = item.product;
                        if (p.getCategory() == Product.Category.TOPUP) {
                            // Topup request for total amount of this item (price * qty)
                            billingService.requestTopup(user.getId(), p.getPrice() * item.quantity);
                        } else {
                            billingService.placeOrder(user.getId(), p.getId(), item.quantity, p.getPrice());
                        }
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(UserPanel.this,
                                "Order placed successfully! Admin will serve/approve your requests.");
                        cartItems.clear();
                        refreshCartTable();
                        refreshBalance();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(UserPanel.this, "Payment failed: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        }
    }

    private JPanel createTopupPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        panel.setBackground(Color.WHITE);

        // 1. Info Panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setBackground(Color.WHITE);
        JLabel lblInfo = new JLabel("Select an amount or enter a custom value to request top-up.");
        lblInfo.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lblInfo.setForeground(Color.GRAY);
        infoPanel.add(lblInfo);
        panel.add(infoPanel, BorderLayout.NORTH);

        // 2. Quick Select Buttons (Grid)
        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 20, 20)); // 2 rows, 3 cols
        gridPanel.setBackground(Color.WHITE);
        long[] amounts = { 10000, 20000, 50000, 100000, 200000, 500000 };

        for (long amount : amounts) {
            JButton btn = new JButton(String.format("%,d VND", amount));
            btn.setFont(new Font("SansSerif", Font.BOLD, 16));
            btn.setFocusPainted(false);
            btn.putClientProperty("JButton.buttonType", "roundRect");
            btn.setBackground(new Color(236, 240, 241));
            btn.setForeground(new Color(44, 62, 80));
            btn.addActionListener(e -> requestTopup(amount));
            gridPanel.add(btn);
        }
        panel.add(gridPanel, BorderLayout.CENTER);

        // 3. Custom Amount Panel
        JPanel customPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        customPanel.setBackground(Color.WHITE);
        customPanel.setBorder(BorderFactory.createTitledBorder("Custom Amount"));

        JTextField txtCustom = new JTextField(12);
        txtCustom.putClientProperty("JTextField.placeholderText", "Enter amount...");

        JButton btnRequest = new JButton("Request Top-up");
        btnRequest.putClientProperty("JButton.buttonType", "roundRect");
        btnRequest.setBackground(new Color(52, 152, 219));
        btnRequest.setForeground(Color.WHITE);
        btnRequest.setFont(new Font("SansSerif", Font.BOLD, 14));

        btnRequest.addActionListener(e -> {
            try {
                String text = txtCustom.getText().trim().replace(",", "").replace(".", "");
                long amount = Long.parseLong(text);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Amount must be positive.");
                    return;
                }
                requestTopup(amount);
                txtCustom.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format.");
            }
        });

        customPanel.add(new JLabel("Amount (VND):"));
        customPanel.add(txtCustom);
        customPanel.add(btnRequest);

        panel.add(customPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void requestTopup(long amount) {
        Product p = new Product();
        p.setId(-1); // Dummy ID
        p.setName("Topup " + amount);
        p.setCategory(Product.Category.TOPUP);
        p.setPrice(amount);
        p.setStock(1);
        addToCart(p);
        JOptionPane.showMessageDialog(this, "Topup added to cart. Please checkout to confirm.");
    }

    private void refreshBalance() {
        SwingWorker<Long, Void> worker = new SwingWorker<>() {
            @Override
            protected Long doInBackground() throws Exception {
                return billingService.getBalance(user.getId());
            }

            @Override
            protected void done() {
                try {
                    currentBalance = get();
                    lblBalance.setText("Balance: " + currentBalance + " VND");
                } catch (Exception ex) {
                    System.err.println("Balance refresh error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void checkActiveSession() {
        SwingWorker<Session, Void> worker = new SwingWorker<>() {
            @Override
            protected Session doInBackground() throws Exception {
                return sessionService.getActiveSession(user.getId()).orElse(null);
            }

            @Override
            protected void done() {
                try {
                    Session session = get();
                    if (session != null) {
                        resumeSession(session);
                    }
                } catch (Exception ex) {
                    System.err.println("Session check error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void resumeSession(Session session) {
        this.currentSession = session;
        lblStatus.setText("Status: ACTIVE on " + session.getMachineName());
        startTimer();
    }

    private void startTimer() {
        if (timer != null && timer.isRunning())
            return;

        timer = new Timer(1000, e -> {
            if (currentSession == null)
                return;

            currentSession.setTimeConsumedSeconds(currentSession.getTimeConsumedSeconds() + 1);

            // Calc remaining
            long totalSecondsAvailable = (currentBalance * 3600) / ratePerHour;
            long remaining = totalSecondsAvailable - currentSession.getTimeConsumedSeconds();

            if (remaining <= 0) {
                stopSession();
                JOptionPane.showMessageDialog(this, "Time is up!");
            } else {
                lblTimeRemaining.setText("Time Remaining: " + TimeUtil.formatDuration(remaining));
                // Red if under 15 mins (900 seconds)
                if (remaining < 900) {
                    lblTimeRemaining.setForeground(new Color(231, 76, 60));
                } else {
                    lblTimeRemaining.setForeground(Color.BLACK);
                }
            }

            // Persist every 60s
            if (currentSession.getTimeConsumedSeconds() % 60 == 0) {
                persistSession();
            }
        });
        timer.start();
    }

    private void persistSession() {
        if (currentSession == null)
            return;
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                sessionService.updateConsumedTime(currentSession.getId(), currentSession.getTimeConsumedSeconds());
                return null;
            }
        };
        worker.execute();
    }

    private void stopSession() {
        if (timer != null)
            timer.stop();
        if (currentSession == null) {
            closeApp();
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                sessionService.endSession(currentSession.getId(), user.getId(),
                        currentSession.getTimeConsumedSeconds());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    closeApp();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UserPanel.this, "Error stopping session: " + ex.getMessage());
                    closeApp();
                }
            }
        };
        worker.execute();
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JTextField txtContent = new JTextField();
        txtContent.putClientProperty("JTextField.placeholderText", "Type a message...");
        txtContent.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtContent.setPreferredSize(new Dimension(0, 40));

        JButton btnSend = new JButton("Send");
        btnSend.putClientProperty("JButton.buttonType", "roundRect");
        btnSend.setBackground(new Color(52, 152, 219));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnSend.setPreferredSize(new Dimension(80, 40));

        inputPanel.add(txtContent, BorderLayout.CENTER);
        inputPanel.add(btnSend, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.SOUTH);

        // Logic
        com.netcafe.service.MessageService messageService = new com.netcafe.service.MessageService();
        Runnable loadChat = () -> {
            SwingWorker<List<com.netcafe.model.Message>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<com.netcafe.model.Message> doInBackground() throws Exception {
                    // Chat with Admin (ID 1)
                    return messageService.getConversation(user.getId(), 1);
                }

                @Override
                protected void done() {
                    try {
                        List<com.netcafe.model.Message> list = get();
                        StringBuilder sb = new StringBuilder();
                        for (com.netcafe.model.Message m : list) {
                            String sender = (m.getSenderId() == user.getId()) ? "Me" : "Admin";
                            sb.append(sender).append(": ").append(m.getContent()).append("\n");
                        }
                        // Only update if text changed to avoid flickering/scrolling issues
                        if (!chatArea.getText().equals(sb.toString())) {
                            chatArea.setText(sb.toString());
                            chatArea.setCaretPosition(chatArea.getDocument().getLength());
                        }
                    } catch (Exception ex) {
                        // Silent failure for chat polling to avoid spamming alerts
                        System.err.println("Chat polling error: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        };

        btnSend.addActionListener(e -> {
            String content = txtContent.getText();
            if (!content.isEmpty()) {
                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        messageService.sendMessage(user.getId(), 1, content);
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            get();
                            loadChat.run();
                            txtContent.setText("");
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(UserPanel.this, "Error sending message: " + ex.getMessage());
                        }
                    }
                };
                worker.execute();
            }
        });

        // Real-time polling
        Timer chatTimer = new Timer(3000, e -> {
            if (panel.isShowing()) {
                loadChat.run();
            }
        });
        chatTimer.start();

        loadChat.run();
        return panel;
    }

    private void closeApp() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
        new com.netcafe.ui.login.LoginFrame().setVisible(true);
    }
}
