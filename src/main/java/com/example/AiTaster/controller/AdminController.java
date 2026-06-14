package com.example.AiTaster.controller;

import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.AdminRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.AdminResponse;
import com.example.AiTaster.service.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "api")
public class AdminController {

    private final AdminService adminService;

    @GetMapping


    public List<AdminResponse> getAllUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus userStatus,
            @RequestParam(required = false) String keyword
    ) {
        return adminService.getAllUsers(role, userStatus, keyword);
    }

    @GetMapping("/{userId}")
    public AdminResponse getUserById(@PathVariable Long userId) {
        return adminService.getUserById(userId);
    }

    @PostMapping
    public AdminResponse createUser(@RequestBody AdminRequest request) {
        return adminService.createUser(request);
    }

    @PutMapping("/{userId}")
    public AdminResponse updateUser(
            @PathVariable Long userId,
            @RequestBody AdminRequest request
    ) {
        return adminService.updateUser(userId, request);
    }

    @PatchMapping("/{userId}/ban")
    public AdminResponse banUser(@PathVariable Long userId) {
        return adminService.banUser(userId);
    }

    @PatchMapping("/{userId}/activate")
    public AdminResponse activateUser(@PathVariable Long userId) {
        return adminService.activateUser(userId);

    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<APIResponse<String>> deleteUser(
            @PathVariable Long userId
    ) {
        adminService.deleteUser(userId);

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "User deactivated successfully",
                        "INACTIVE"
                )
        );
    }
}