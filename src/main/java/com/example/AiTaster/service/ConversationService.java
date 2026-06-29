package com.example.AiTaster.service;

import com.example.AiTaster.constant.ConversationType;
import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.dto.request.ConversationStartRequest;
import com.example.AiTaster.dto.response.ConversationResponse;
import com.example.AiTaster.dto.response.ConversationStartResponse;
import com.example.AiTaster.dto.response.MessageResponse;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ConversationMapper;
import com.example.AiTaster.mapper.MessageMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.ConversationRepo;
import com.example.AiTaster.repository.ExpertApplicationRepo;
import com.example.AiTaster.repository.MessageRepo;
import com.example.AiTaster.service.imp.IConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService implements IConversationService {

    private final ConversationRepo conversationRepo;
    private final MessageRepo messageRepo;
    private final ExpertApplicationRepo expertApplicationRepo;
    private final ClientProfileRepo clientProfileRepo;

    private final CurrentUserService currentUserService;
    private final ContentManagerService contentManagerService;

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public ConversationStartResponse startConversation(
            Long applicationId,
            ConversationStartRequest request
    ) {
        User currentUser = currentUserService.getCurrentUser();

        ClientProfile currentClient = clientProfileRepo.findByUser(currentUser)
                .orElseThrow(() -> new GlobalException(ErrorCode.ONLY_CLIENT_CAN_START_CONVERSATION));

        ExpertApplication application = expertApplicationRepo.findWithDetailByApplicationId(applicationId)
                .orElseThrow(() -> new GlobalException(ErrorCode.APPLICATION_NOT_FOUND));

        checkApplicationOwner(application, currentClient);

        if (conversationRepo.existsByExpertApplication_ApplicationId(applicationId)) {
            throw new GlobalException(ErrorCode.CONVERSATION_ALREADY_EXISTS);
        }

        contentManagerService.validateKeywordInput(request.getContent());

        User clientUser = application.getJobpost()
                .getClientProfile()
                .getUser();

        User expertUser = application.getExpertProfile()
                .getUser();

        Conversation conversation = Conversation.builder()
                .expertApplication(application)
                .client(clientUser)
                .expert(expertUser)
                .projectId(null)
                .conversationType(ConversationType.APPLICATION)
                .build();

        Conversation savedConversation = conversationRepo.save(conversation);

        Message firstMessage = Message.builder()
                .conversation(savedConversation)
                .sender(clientUser)
                .receiver(expertUser)
                .content(request.getContent().trim())
                .isRead(false)
                .build();

        Message savedMessage = messageRepo.save(firstMessage);

        ConversationResponse conversationResponse =
                conversationMapper.toResponse(savedConversation);

        MessageResponse messageResponse =
                messageMapper.toResponse(savedMessage);

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + savedConversation.getConversationId(),
                messageResponse
        );

        messagingTemplate.convertAndSend(
                "/topic/users/" + expertUser.getUserId() + "/messages",
                messageResponse
        );

        return ConversationStartResponse.builder()
                .conversation(conversationResponse)
                .firstMessage(messageResponse)
                .build();
    }

    @Override
    public List<ConversationResponse> getMyConversations() {
        User currentUser = currentUserService.getCurrentUser();

        return conversationRepo
                .findByClientOrExpertOrderByUpdateAtDesc(currentUser, currentUser)
                .stream()
                .map(conversation -> toResponseWithUnreadCount(conversation, currentUser))
                .toList();
    }

    @Override
    public ConversationResponse getConversationDetail(Long conversationId) {
        Conversation conversation = getConversationEntity(conversationId);

        User currentUser = currentUserService.getCurrentUser();

        checkConversationMember(conversation, currentUser);

        return toResponseWithUnreadCount(conversation, currentUser);
    }

    public Conversation getConversationEntity(Long conversationId) {
        return conversationRepo.findWithDetailByConversationId(conversationId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CONVERSATION_NOT_FOUND));
    }

    public void checkConversationMember(Conversation conversation, User user) {
        boolean isClient = conversation.getClient()
                .getUserId()
                .equals(user.getUserId());

        boolean isExpert = conversation.getExpert()
                .getUserId()
                .equals(user.getUserId());

        if (!isClient && !isExpert) {
            throw new GlobalException(ErrorCode.NOT_CONVERSATION_MEMBER);
        }
    }

    private void checkApplicationOwner(
            ExpertApplication application,
            ClientProfile currentClient
    ) {
        Long ownerClientId = application.getJobpost()
                .getClientProfile()
                .getClientProfileId();

        if (!ownerClientId.equals(currentClient.getClientProfileId())) {
            throw new GlobalException(ErrorCode.NOT_APPLICATION_OWNER);
        }
    }

    // Sau này ProjectService gọi hàm này sau khi tạo Project.
    @Transactional
    public void attachProject(Long applicationId, Long projectId) {
        Conversation conversation = conversationRepo
                .findByExpertApplication_ApplicationId(applicationId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CONVERSATION_NOT_FOUND));

        conversation.setProjectId(projectId);
        conversation.setConversationType(ConversationType.PROJECT);
        conversation.setConvertedToProjectAt(LocalDateTime.now());

        conversationRepo.save(conversation);
    }

    private ConversationResponse toResponseWithUnreadCount(
            Conversation conversation,
            User currentUser
    ) {
        ConversationResponse response = conversationMapper.toResponse(conversation);
        response.setUnreadCount(
                messageRepo.countByConversationAndReceiverAndIsReadFalse(
                        conversation,
                        currentUser
                )
        );
        return response;
    }
}
