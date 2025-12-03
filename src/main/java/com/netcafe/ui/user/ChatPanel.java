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
                            SwingUtils.showError(ChatPanel.this, "Error sending message", ex);
                        }
                    }
                };
                worker.execute();
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
