package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.dto.request.ConversationCreateRequest;
import com.example.AiTaster.dto.response.ConversationResponse;
import com.example.AiTaster.entity.Conversation;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ConversationMapper;
import com.example.AiTaster.repository.ConversationRepo;
import com.example.AiTaster.repository.UserRepo;
import com.example.AiTaster.service.imp.IConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService implements IConversationService {

    private final ConversationRepo conversationRepo;
    private final UserRepo userRepo;
    private final CurrentUserService currentUserService;
    private final ConversationMapper conversationMapper;

    @Override
    public ConversationResponse createConversation(ConversationCreateRequest request) {
        User client = userRepo.findById(request.getClientId())
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        User expert = userRepo.findById(request.getExpertId())
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        if (client.getUserId().equals(expert.getUserId())) {
            throw new GlobalException(ErrorCode.INVALID_CONVERSATION_PARTICIPANTS);
        }

        Conversation conversation = conversationRepo
                .findByClientAndExpertAndProjectId(client, expert, request.getProjectId())
                .orElseGet(() -> conversationRepo.save(
                        Conversation.builder()
                                .client(client)
                                .expert(expert)
                                .projectId(request.getProjectId())
                                .conversationType(request.getConversationType())
                                .build()
                ));

        return conversationMapper.toResponse(conversation);
    }

    @Override
    public List<ConversationResponse> getMyConversations() {
        User currentUser = currentUserService.getCurrentUser();

        return conversationRepo.findByClientOrExpert(currentUser, currentUser)
                .stream()
                .map(conversationMapper::toResponse)
                .toList();
    }

    @Override
    public ConversationResponse getConversationDetail(Long conversationId) {
        Conversation conversation = getConversation(conversationId);
        checkConversationMember(conversation);

        return conversationMapper.toResponse(conversation);
    }

    public Conversation getConversation(Long conversationId) {
        return conversationRepo.findByConversationId(conversationId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CONVERSATION_NOT_FOUND));
    }

    public void checkConversationMember(Conversation conversation) {
        User currentUser = currentUserService.getCurrentUser();

        boolean isClient = conversation.getClient().getUserId().equals(currentUser.getUserId());
        boolean isExpert = conversation.getExpert().getUserId().equals(currentUser.getUserId());

        if (!isClient && !isExpert) {
            throw new GlobalException(ErrorCode.NOT_CONVERSATION_MEMBER);
        }
    }
}