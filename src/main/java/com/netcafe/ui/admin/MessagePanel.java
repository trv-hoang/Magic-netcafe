package com.netcafe.ui.admin;

import com.netcafe.model.Message;
import com.netcafe.model.User;
import com.netcafe.service.MessageService;
import com.netcafe.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MessagePanel extends JPanel {
    private final UserService userService = new UserService();
    private final MessageService messageService = new MessageService();

    private JList<User> userList = new JList<>();
    private DefaultListModel<User> userListModel = new DefaultListModel<>();
    private JTextArea chatArea = new JTextArea();
    private User selectedChatUser;
    private Timer chatTimer;

    public MessagePanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left: User List
        userList.setModel(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User) {
                    setText(((User) value).getUsername());
                    setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                }
                return this;
            }
        });
        userList.setFixedCellHeight(30);

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedChatUser = userList.getSelectedValue();
                loadConversation();
            }
        });

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Users"), BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        JButton btnRefreshUsers = new JButton("Refresh Users");
        btnRefreshUsers.addActionListener(e -> loadChatUsers());
        leftPanel.add(btnRefreshUsers, BorderLayout.SOUTH);
        leftPanel.setPreferredSize(new Dimension(200, 0));

        // Right: Chat Area
        JPanel rightPanel = new JPanel(new BorderLayout());
        chatArea.setEditable(false);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        rightPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JTextField txtContent = new JTextField();
        txtContent.putClientProperty("JTextField.placeholderText", "Type a message...");

        JButton btnSend = new JButton("Send");
        btnSend.setBackground(new Color(52, 152, 219));
        btnSend.setForeground(Color.WHITE);
        btnSend.putClientProperty("JButton.buttonType", "roundRect");

        inputPanel.add(txtContent, BorderLayout.CENTER);
        inputPanel.add(btnSend, BorderLayout.EAST);
        rightPanel.add(inputPanel, BorderLayout.SOUTH);

        btnSend.addActionListener(e -> {
            String content = txtContent.getText();
            if (!content.isEmpty() && selectedChatUser != null) {
                sendMessage(selectedChatUser.getId(), content);
                txtContent.setText("");
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerSize(5);
        add(splitPane, BorderLayout.CENTER);

        loadChatUsers();
        startChatTimer();
    }

    private void loadChatUsers() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                return userService.getAllUsers();
            }

            @Override
            protected void done() {
                try {
                    List<User> list = get();
                    userListModel.clear();
                    for (User u : list) {
                        if (u.getRole() == User.Role.USER) { // Only show normal users
                            userListModel.addElement(u);
                        }
                    }
                } catch (Exception ex) {
                    com.netcafe.util.SwingUtils.showError(MessagePanel.this, "Error loading chat users", ex);
                }
            }
        };
        worker.execute();
    }

    private void loadConversation() {
        if (selectedChatUser == null)
            return;
        SwingWorker<List<Message>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Message> doInBackground() throws Exception {
                // Assuming Admin ID is 1. In real app, get current logged in admin ID.
                return messageService.getConversation(1, selectedChatUser.getId());
            }

            @Override
            protected void done() {
                try {
                    List<Message> list = get();
                    StringBuilder sb = new StringBuilder();
                    for (Message m : list) {
                        String sender = (m.getSenderId() == 1) ? "Me" : selectedChatUser.getUsername();
                        sb.append(sender).append(": ").append(m.getContent()).append("\n");
                    }
                    if (!chatArea.getText().equals(sb.toString())) {
                        chatArea.setText(sb.toString());
                        chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    }
                } catch (Exception ex) {
                    // Silent failure for chat polling
                    System.err.println("Chat error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void sendMessage(int receiverId, String content) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                messageService.sendMessage(1, receiverId, content);
                return null;
            }

            @Override
            protected void done() {
                loadConversation();
            }
        };
        worker.execute();
    }

    private void startChatTimer() {
        if (chatTimer != null && chatTimer.isRunning())
            return;
        chatTimer = new Timer(3000, e -> {
            if (selectedChatUser != null && isShowing()) {
                loadConversation();
            }
        });
        chatTimer.start();
    }
}
