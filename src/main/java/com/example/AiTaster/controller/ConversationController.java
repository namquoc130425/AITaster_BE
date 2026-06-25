package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.ConversationCreateRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.ConversationResponse;
import com.example.AiTaster.service.ConversationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    public ResponseEntity<APIResponse<ConversationResponse>> createConversation(
            @RequestBody @Valid ConversationCreateRequest request
    ) {
        return ResponseEntity.status(201).body(
                APIResponse.response(
                        201,
                        "Create conversation successfully",
                        conversationService.createConversation(request)
                )
        );
    }

    @GetMapping("/my")
    public ResponseEntity<APIResponse<List<ConversationResponse>>> getMyConversations() {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get my conversations successfully",
                        conversationService.getMyConversations()
                )
        );
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<APIResponse<ConversationResponse>> getConversationDetail(
            @PathVariable Long conversationId
    ) {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get conversation successfully",
                        conversationService.getConversationDetail(conversationId)
                )
        );
    }
}