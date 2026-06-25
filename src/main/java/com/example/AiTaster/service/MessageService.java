package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.constant.MessageType;
import com.example.AiTaster.dto.request.MessageRequest;
import com.example.AiTaster.dto.response.MessageResponse;
import com.example.AiTaster.entity.Conversation;
import com.example.AiTaster.entity.Message;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.MessageMapper;
import com.example.AiTaster.repository.MessageRepo;
import com.example.AiTaster.service.imp.IMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService implements IMessageService {

    private final MessageRepo messageRepo;
    private final ConversationService conversationService;
    private final CurrentUserService currentUserService;
    private final MessageMapper messageMapper;
    private final ContentManagerService contentManagerService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public MessageResponse sendMessage(MessageRequest request) {
        Conversation conversation = conversationService.getConversation(request.getConversationId());
        conversationService.checkConversationMember(conversation);

        User sender = currentUserService.getCurrentUser();
        User receiver = resolveReceiver(conversation, sender);

        MessageType messageType = request.getMessageType() == null
                ? MessageType.TEXT
                : request.getMessageType();

        if (messageType == MessageType.TEXT) {
            if (request.getContent() == null || request.getContent().isBlank()) {
                throw new GlobalException(ErrorCode.MESSAGE_CONTENT_REQUIRED);
            }

            contentManagerService.validateKeywordInput(request.getContent());
        }

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .receiver(receiver)
                .content(request.getContent())
                .fileUrl(request.getFileUrl())
                .messageType(messageType)
                .isRead(false)
                .build();

        Message saved = messageRepo.save(message);
        MessageResponse response = messageMapper.toResponse(saved);

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversation.getConversationId(),
                response
        );

        messagingTemplate.convertAndSend(
                "/topic/users/" + receiver.getUserId() + "/messages",
                response
        );

        return response;
    }

    @Override
    public List<MessageResponse> getMessages(Long conversationId) {
        Conversation conversation = conversationService.getConversation(conversationId);
        conversationService.checkConversationMember(conversation);

        return messageRepo.findByConversationOrderBySendAtAsc(conversation)
                .stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    @Override
    public void markAsRead(Long messageId) {
        Message message = messageRepo.findByMessageId(messageId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MESSAGE_NOT_FOUND));

        Conversation conversation = message.getConversation();
        conversationService.checkConversationMember(conversation);

        User currentUser = currentUserService.getCurrentUser();

        if (!message.getReceiver().getUserId().equals(currentUser.getUserId())) {
            throw new GlobalException(ErrorCode.NOT_CONVERSATION_MEMBER);
        }

        message.setIsRead(true);
        messageRepo.save(message);
    }

    private User resolveReceiver(Conversation conversation, User sender) {
        if (conversation.getClient().getUserId().equals(sender.getUserId())) {
            return conversation.getExpert();
        }

        if (conversation.getExpert().getUserId().equals(sender.getUserId())) {
            return conversation.getClient();
        }

        throw new GlobalException(ErrorCode.NOT_CONVERSATION_MEMBER);
    }
}