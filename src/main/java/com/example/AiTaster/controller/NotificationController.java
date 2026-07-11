package com.example.AiTaster.controller;

import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.NotificationResponse;
import com.example.AiTaster.dto.response.UnreadNotificationCountResponse;
import com.example.AiTaster.service.NotificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/my")
    public ResponseEntity<APIResponse<List<NotificationResponse>>>
    getMyNotifications() {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Lấy thông báo thành công",
                        notificationService.getMyNotifications()
                )
        );
    }

    @GetMapping("/my/unread")
    public ResponseEntity<APIResponse<List<NotificationResponse>>>
    getMyUnreadNotifications() {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Lấy thông báo chưa đọc thành công",
                        notificationService.getMyUnreadNotifications()
                )
        );
    }

    @GetMapping("/my/unread-count")
    public ResponseEntity<APIResponse<UnreadNotificationCountResponse>>
    countMyUnreadNotifications() {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Đếm thông báo chưa đọc thành công",
                        notificationService.countMyUnreadNotifications()
                )
        );
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<APIResponse<NotificationResponse>>
    markAsRead(
            @PathVariable Long notificationId
    ) {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Notification marked as read",
                        notificationService.markAsRead(notificationId)
                )
        );
    }

    @PatchMapping("/read-all")
    public ResponseEntity<APIResponse<Integer>>
    markAllAsRead() {
        int updatedCount =
                notificationService.markAllAsRead();

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "All notifications marked as read",
                        updatedCount
                )
        );
    }
}
