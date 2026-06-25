package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.ConversationReadRequest;
import com.example.AiTaster.dto.request.MessageRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.MessageResponse;
import com.example.AiTaster.service.MessageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class MessageController {

    private final MessageService messageService;

    /*
     * REST gửi message.
     * Vẫn giữ để test Swagger hoặc dùng khi WebSocket mất kết nối.
     */
    @PostMapping
    public ResponseEntity<APIResponse<MessageResponse>>
    sendMessageRest(
            @RequestBody @Valid MessageRequest request
    ) {
        MessageResponse response =
                messageService.sendMessage(request);

        return ResponseEntity.status(201).body(
                APIResponse.response(
                        201,
                        "Send message successfully",
                        response
                )
        );
    }

    /*
     * WebSocket gửi message:
     *
     * Client publish:
     * /app/messages/send
     */
    @MessageMapping("/messages/send")
    public void sendMessageSocket(
            @Payload @Valid MessageRequest request,
            Principal principal
    ) {
        messageService.sendMessageSocket(
                request,
                principal
        );
    }

    /*
     * Khi user mở conversation bằng REST:
     * - lấy lịch sử
     * - tự mark read các message gửi đến current user
     */
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<APIResponse<List<MessageResponse>>>
    getMessages(
            @PathVariable Long conversationId
    ) {
        List<MessageResponse> responses =
                messageService.getMessages(conversationId);

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get messages successfully",
                        responses
                )
        );
    }

    /*
     * Khi frontend đang mở đúng cửa sổ chat và nhận message realtime,
     * frontend publish:
     *
     * /app/conversations/read
     */
    @MessageMapping("/conversations/read")
    public void markConversationReadSocket(
            @Payload @Valid ConversationReadRequest request,
            Principal principal
    ) {
        messageService.markConversationAsReadSocket(
                request.getConversationId(),
                principal
        );
    }
}