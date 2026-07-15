package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.constant.NotificationType;
import com.example.AiTaster.constant.ProjectStatus;
import com.example.AiTaster.constant.ReferenceType;
import com.example.AiTaster.dto.request.MessageRequest;
import com.example.AiTaster.dto.response.MessageResponse;
import com.example.AiTaster.dto.response.ReadReceiptResponse;
import com.example.AiTaster.entity.Conversation;
import com.example.AiTaster.entity.Message;
import com.example.AiTaster.entity.Project;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.MessageMapper;
import com.example.AiTaster.repository.MessageRepo;
import com.example.AiTaster.repository.ProjectRepo;
import com.example.AiTaster.repository.UserRepo;
import com.example.AiTaster.service.imp.IMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService implements IMessageService {

    private final MessageRepo messageRepo;
    private final UserRepo userRepo;
    private final ProjectRepo projectRepo;

    private final ConversationService conversationService;
    private final CurrentUserService currentUserService;
    private final ContentManagerService contentManagerService;

    private final MessageMapper messageMapper;

    private final SimpMessagingTemplate messagingTemplate;
    private final RealtimeService realtimeService;
    private final NotificationService notificationService;

    /*
     * Gửi tin nhắn bằng REST.
     *
     * REST lấy user từ SecurityContextHolder thông qua CurrentUserService.
     */
    @Override
    @Transactional
    public MessageResponse sendMessage(
            MessageRequest request
    ) {
        User sender =
                currentUserService.getCurrentUser();

        return sendMessageInternal(
                request,
                sender
        );
    }

    /*
     * Gửi tin nhắn bằng WebSocket.
     *
     * Không dùng CurrentUserService vì WebSocket chạy trên thread khác.
     * User được lấy từ Principal của STOMP session.
     */
    @Transactional
    public MessageResponse sendMessageSocket(
            MessageRequest request,
            Principal principal
    ) {
        User sender =
                getUserFromPrincipal(principal);

        return sendMessageInternal(
                request,
                sender
        );
    }

    private MessageResponse sendMessageInternal(
            MessageRequest request,
            User sender
    ) {
        if (request == null) {
            throw new GlobalException(
                    ErrorCode.FIELD_REQUIRED
            );
        }

        if (request.getConversationId() == null) {
            throw new GlobalException(
                    ErrorCode.CONVERSATION_NOT_FOUND
            );
        }

        if (request.getContent() == null
                || request.getContent().isBlank()) {
            throw new GlobalException(
                    ErrorCode.MESSAGE_CONTENT_REQUIRED
            );
        }

        Conversation conversation =
                conversationService.getConversationEntity(
                        request.getConversationId()
                );

        conversationService.checkConversationMember(
                conversation,
                sender
        );
        ensureConversationCanReceiveMessage(conversation);

        /*
     * Chặn thêm ở tầng service:
     * expert không được gửi tin nhắn đầu tiên.
         *
     * Thông thường conversation đã được tạo cùng tin nhắn đầu tiên
     * của client, nhưng vẫn kiểm tra để tránh dữ liệu sai.
         */
        boolean hasMessage =
                messageRepo.existsByConversation(conversation);

        boolean senderIsExpert =
                conversation.getExpert()
                        .getUserId()
                        .equals(sender.getUserId());

        if (!hasMessage && senderIsExpert) {
            throw new GlobalException(
                    ErrorCode.CLIENT_MUST_SEND_FIRST_MESSAGE
            );
        }

        /*
         * Chặn link, số điện thoại, email,
         * prompt injection và blocked keywords.
         */
        contentManagerService.validateKeywordInput(
                request.getContent()
        );

        User receiver =
                resolveReceiver(
                        conversation,
                        sender
                );

        Message message =
                Message.builder()
                        .conversation(conversation)
                        .sender(sender)
                        .receiver(receiver)
                        .content(request.getContent().trim())
                        .isRead(false)
                        .build();

        Message savedMessage =
                messageRepo.save(message);

        MessageResponse response =
                messageMapper.toResponse(savedMessage);

        /*
     * Cả client và expert đang lắng nghe conversation
     * đều nhận được tin nhắn.
         */
        messagingTemplate.convertAndSend(
                "/topic/conversations/"
                        + conversation.getConversationId(),
                response
        );

        /*
     * Kênh notification riêng cho người nhận.
         */
        realtimeService.pushUserDashboardEvent(
                sender,
                "MESSAGE_SENT",
                ReferenceType.CONVERSATION,
                conversation.getConversationId(),
                "Message sent"
        );
        realtimeService.pushUserMessage(receiver, response);
        notificationService.notify(
                receiver,
                NotificationType.SYSTEM,
                ReferenceType.CONVERSATION,
                conversation.getConversationId(),
                "New message",
                buildMessageNotificationContent(sender, response.getContent())
        );

        return response;
    }

    /*
     * REST lấy lịch sử conversation.
     *
     * Khi user mở chat:
     * 1. Đánh dấu tin nhắn gửi đến user hiện tại là đã đọc.
     * 2. Gửi read receipt cho người gửi.
     * 3. Query và trả danh sách tin nhắn.
     */
    @Override
    @Transactional
    public List<MessageResponse> getMessages(
            Long conversationId
    ) {
        User currentUser =
                currentUserService.getCurrentUser();

        Conversation conversation =
                conversationService.getConversationEntity(
                        conversationId
                );

        conversationService.checkConversationMember(
                conversation,
                currentUser
        );

        ReadReceiptResponse readReceipt =
                markConversationAsReadInternal(
                        conversation,
                        currentUser
                );

        if (readReceipt.getReadCount() > 0) {
            sendReadReceipt(
                    conversation,
                    readReceipt
            );
            realtimeService.pushUserDashboardEvent(
                    currentUser,
                    "CONVERSATION_READ",
                    ReferenceType.CONVERSATION,
                    conversation.getConversationId(),
                    "Conversation read"
            );
        }

        return messageRepo
                .findByConversationOrderBySendAtAsc(conversation)
                .stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    /*
     * Có thể dùng nội bộ cho REST nếu cần.
     * Không cần mở PATCH endpoint.
     */
    @Override
    @Transactional
    public ReadReceiptResponse markConversationAsRead(
            Long conversationId
    ) {
        User currentUser =
                currentUserService.getCurrentUser();

        Conversation conversation =
                conversationService.getConversationEntity(
                        conversationId
                );

        conversationService.checkConversationMember(
                conversation,
                currentUser
        );

        ReadReceiptResponse response =
                markConversationAsReadInternal(
                        conversation,
                        currentUser
                );

        if (response.getReadCount() > 0) {
            sendReadReceipt(
                    conversation,
                    response
            );
            realtimeService.pushUserDashboardEvent(
                    currentUser,
                    "CONVERSATION_READ",
                    ReferenceType.CONVERSATION,
                    conversation.getConversationId(),
                    "Conversation read"
            );
        }

        return response;
    }

    /*
     * WebSocket đánh dấu đã đọc.
     *
     * FE chỉ gọi khi:
     * - đang mở đúng conversation;
     * - tab hiện tại đang active;
     * - vừa nhận tin nhắn gửi đến user hiện tại.
     */
    @Transactional
    public ReadReceiptResponse markConversationAsReadSocket(
            Long conversationId,
            Principal principal
    ) {
        User currentUser =
                getUserFromPrincipal(principal);

        Conversation conversation =
                conversationService.getConversationEntity(
                        conversationId
                );

        conversationService.checkConversationMember(
                conversation,
                currentUser
        );

        ReadReceiptResponse response =
                markConversationAsReadInternal(
                        conversation,
                        currentUser
                );

        if (response.getReadCount() > 0) {
            sendReadReceipt(
                    conversation,
                    response
            );
            realtimeService.pushUserDashboardEvent(
                    currentUser,
                    "CONVERSATION_READ",
                    ReferenceType.CONVERSATION,
                    conversation.getConversationId(),
                    "Conversation read"
            );
        }

        return response;
    }

    private ReadReceiptResponse markConversationAsReadInternal(
            Conversation conversation,
            User currentUser
    ) {
        /*
         * Query chỉ update tin nhắn:
         * receiver = user hiện tại
         * isRead = false
         *
         * Không update tin nhắn do chính user hiện tại gửi.
         */
        int readCount =
                messageRepo.markConversationAsRead(
                        conversation,
                        currentUser
                );

        return ReadReceiptResponse.builder()
                .conversationId(
                        conversation.getConversationId()
                )
                .readerId(currentUser.getUserId())
                .readCount(readCount)
                .readAt(LocalDateTime.now())
                .build();
    }

    private void sendReadReceipt(
            Conversation conversation,
            ReadReceiptResponse response
    ) {
        messagingTemplate.convertAndSend(
                "/topic/conversations/"
                        + conversation.getConversationId()
                        + "/read",
                response
        );
    }

    /*
     * Không kiểm tra cụ thể UsernamePasswordAuthenticationToken.
     *
     * Principal được Spring truyền vào thường là Authentication,
     * nhưng dùng Authentication interface sẽ ổn định hơn.
     */
    private User getUserFromPrincipal(
            Principal principal
    ) {
        if (principal == null) {
            log.warn("WebSocket Principal is null");

            throw new GlobalException(
                    ErrorCode.INVALID_TOKEN
            );
        }

        if (principal instanceof Authentication authentication) {
            Object authenticatedPrincipal =
                    authentication.getPrincipal();

            if (authenticatedPrincipal instanceof User user) {
                return user;
            }

            String username =
                    authentication.getName();

            return findUserByUsername(username);
        }

        /*
         * Dự phòng nếu Principal không phải Authentication
         * nhưng vẫn có username.
         */
        return findUserByUsername(
                principal.getName()
        );
    }

    private User findUserByUsername(
            String username
    ) {
        if (username == null || username.isBlank()) {
            throw new GlobalException(
                    ErrorCode.INVALID_TOKEN
            );
        }

        return userRepo.findByUsername(username)
                .orElseThrow(() ->
                        new GlobalException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );
    }

    private User resolveReceiver(
            Conversation conversation,
            User sender
    ) {
        if (conversation.getClient()
                .getUserId()
                .equals(sender.getUserId())) {

            return conversation.getExpert();
        }

        if (conversation.getExpert()
                .getUserId()
                .equals(sender.getUserId())) {

            return conversation.getClient();
        }

        throw new GlobalException(
                ErrorCode.NOT_CONVERSATION_MEMBER
        );
    }

    private void ensureConversationCanReceiveMessage(Conversation conversation) {
        Long projectId = conversation.getProjectId();

        if (projectId == null) {
            return;
        }

        Project project = projectRepo.findById(projectId).orElse(null);

        if (project != null && ProjectStatus.DISPUTED.equals(project.getProjectStatus())) {
            throw new GlobalException(400, "Messaging is paused while project is under dispute");
        }
    }

    private String buildMessageNotificationContent(
            User sender,
            String content
    ) {
        String senderName = safeName(sender);
        String preview = content == null ? "" : content.trim();

        if (preview.length() > 120) {
            preview = preview.substring(0, 117) + "...";
        }

        return senderName + ": " + preview;
    }

    private String safeName(User user) {
        if (user == null) {
            return "User";
        }

        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }

        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }

        return "User";
    }
}
