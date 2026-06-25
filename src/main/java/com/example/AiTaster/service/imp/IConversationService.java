package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.request.ConversationStartRequest;
import com.example.AiTaster.dto.response.ConversationResponse;
import com.example.AiTaster.dto.response.ConversationStartResponse;

import java.util.List;

public interface IConversationService {

    ConversationStartResponse startConversation(
            Long applicationId,
            ConversationStartRequest request
    );

    List<ConversationResponse> getMyConversations();

    ConversationResponse getConversationDetail(Long conversationId);
}