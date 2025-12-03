package com.netcafe.ui.user;

import com.netcafe.model.Message;
import com.netcafe.model.User;
import com.netcafe.service.MessageService;
import com.netcafe.util.SwingUtils;
import com.netcafe.ui.ThemeConfig;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ChatPanel extends JPanel {
    private final User user;
    private final MessageService messageService = new MessageService();
    private final com.netcafe.service.AIService aiService = new com.netcafe.service.AIService();
    private final JTextArea chatArea = new JTextArea();
    private Timer chatTimer;

    public ChatPanel(User currentUser) {
        this.user = currentUser;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        chatArea.setEditable(false);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JTextField txtContent = new JTextField();
        txtContent.putClientProperty("JTextField.placeholderText", "Type a message...");
        txtContent.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtContent.setPreferredSize(new Dimension(0, 40));

        JButton btnSend = new JButton("Send");
        btnSend.putClientProperty("JButton.buttonType", "roundRect");
        btnSend.setBackground(ThemeConfig.PRIMARY);
        btnSend.setForeground(Color.WHITE);
        btnSend.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnSend.setPreferredSize(new Dimension(80, 40));

        inputPanel.add(txtContent, BorderLayout.CENTER);
        inputPanel.add(btnSend, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        Runnable loadChat = () -> {
            SwingWorker<List<Message>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<Message> doInBackground() throws Exception {
                    // Chat with Admin (ID 1)
                    return messageService.getConversation(user.getId(), 1);
                }

                @Override
                protected void done() {
                    try {
                        List<Message> list = get();
                        StringBuilder sb = new StringBuilder();
                        for (Message m : list) {
                            String sender = (m.getSenderId() == user.getId()) ? "Me" : "Admin";
                            sb.append(sender).append(": ").append(m.getContent()).append("\n");
                        }
                        if (!chatArea.getText().equals(sb.toString())) {
                            chatArea.setText(sb.toString());
                            chatArea.setCaretPosition(chatArea.getDocument().getLength());
                        }
                    } catch (Exception ex) {
                        System.err.println("Chat polling error: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        };

        Runnable sendMessage = () -> {
            String content = txtContent.getText().trim();
            if (!content.isEmpty()) {
                txtContent.setText(""); // Clear immediately
                txtContent.setEnabled(false); // Prevent double send
                btnSend.setEnabled(false);

                SwingWorker<Void, Boolean> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        // 1. Send User Message
                        messageService.sendMessage(user.getId(), 1, content);
                        publish(true); // Signal that user message is sent

                        // 2. Check for AI Response
                        String aiResponse = aiService.getResponse(content);
                        if (aiResponse != null) {
                            // Simulate thinking time (longer for realism)
                            Thread.sleep(1000);
                            // Send AI response as Admin (ID 1)
                            messageService.sendMessage(1, user.getId(), "[AI] " + aiResponse);
                        }
                        return null;
                    }

                    @Override
                    protected void process(List<Boolean> chunks) {
                        // Refresh UI to show user's message immediately
                        loadChat.run();
                    }

                    @Override
                    protected void done() {
                        try {
                            get(); // Check for exceptions
                        } catch (Exception ex) {
                            SwingUtils.showError(ChatPanel.this, "Error sending message", ex);
                        } finally {
                            loadChat.run(); // Refresh to show AI message
                            txtContent.setEnabled(true);
                            btnSend.setEnabled(true);
                            txtContent.requestFocus();
                        }
                    }
                };
                worker.execute();
            }
        };

        btnSend.addActionListener(e -> sendMessage.run());

        // Add KeyListener for Enter key
        txtContent.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    sendMessage.run();
                }
            }
        });

        // Real-time polling
        chatTimer = new Timer(3000, e -> {
            if (isShowing()) {
                loadChat.run();
            }
        });
        chatTimer.start();

        loadChat.run();
    }
}
