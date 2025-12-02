package com.netcafe.service;

import com.netcafe.dao.MessageDAO;
import com.netcafe.model.Message;

import java.util.List;

public class MessageService {
    private final MessageDAO messageDAO = new MessageDAO();

    public void sendMessage(int senderId, int receiverId, String content) throws Exception {
        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        messageDAO.create(msg);
    }

    public List<Message> getAllMessages() throws Exception {
        return messageDAO.findAll();
    }

    public List<Message> getConversation(int userId1, int userId2) throws Exception {
        return messageDAO.findConversation(userId1, userId2);
    }
}
