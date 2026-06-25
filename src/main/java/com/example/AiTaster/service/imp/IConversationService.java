package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.request.ConversationCreateRequest;
import com.example.AiTaster.dto.response.ConversationResponse;

import java.util.List;

public interface IConversationService {

    ConversationResponse createConversation(ConversationCreateRequest request);

    List<ConversationResponse> getMyConversations();

    ConversationResponse getConversationDetail(Long conversationId);
}