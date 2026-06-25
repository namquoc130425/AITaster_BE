package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.request.MessageRequest;
import com.example.AiTaster.dto.response.MessageResponse;

import java.util.List;

public interface IMessageService {

    MessageResponse sendMessage(MessageRequest request);

    List<MessageResponse> getMessages(Long conversationId);

    void markAsRead(Long messageId);
}