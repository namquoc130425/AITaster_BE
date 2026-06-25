package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.MessageRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.MessageResponse;
import com.example.AiTaster.service.MessageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<APIResponse<MessageResponse>> sendMessageRest(
            @RequestBody @Valid MessageRequest request
    ) {
        return ResponseEntity.status(201).body(
                APIResponse.response(
                        201,
                        "Send message successfully",
                        messageService.sendMessage(request)
                )
        );
    }

    @MessageMapping("/messages/send")
    public void sendMessageSocket(@Valid MessageRequest request) {
        messageService.sendMessage(request);
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<APIResponse<List<MessageResponse>>> getMessages(
            @PathVariable Long conversationId
    ) {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get messages successfully",
                        messageService.getMessages(conversationId)
                )
        );
    }

    @PatchMapping("/{messageId}/read")
    public ResponseEntity<APIResponse<Void>> markAsRead(
            @PathVariable Long messageId
    ) {
        messageService.markAsRead(messageId);

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Message marked as read",
                        null
                )
        );
    }
}