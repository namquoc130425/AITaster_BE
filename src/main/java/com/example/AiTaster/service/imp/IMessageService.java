package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.request.MessageRequest;
import com.example.AiTaster.dto.response.MessageResponse;
import com.example.AiTaster.dto.response.ReadReceiptResponse;

import java.util.List;

public interface IMessageService {

    MessageResponse sendMessage(
            MessageRequest request
    );

    List<MessageResponse> getMessages(
            Long conversationId
    );

    ReadReceiptResponse markConversationAsRead(
            Long conversationId
    );
}