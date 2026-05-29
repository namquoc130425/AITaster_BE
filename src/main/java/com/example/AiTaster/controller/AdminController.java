package com.example.AiTaster.controller;

import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.AdminRequest;
import com.example.AiTaster.service.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
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
    public List<UserResponse> getAllUsers() {
        return adminService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public UserResponse getUserById(@PathVariable Long userId) {
        return adminService.getUserById(userId);
    }

    @PostMapping
    public UserResponse createUser(@RequestBody AdminRequest request) {
        return adminService.createUser(request);
    }

    @PutMapping("/{userId}")
    public UserResponse updateUser(
            @PathVariable Long userId,
            @RequestBody AdminRequest request
    ) {
        return adminService.updateUser(userId, request);
    }

    @PatchMapping("/{userId}/ban")
    public UserResponse banUser(@PathVariable Long userId) {
        return adminService.banUser(userId);
    }

    @PatchMapping("/{userId}/activate")
    public UserResponse activateUser(@PathVariable Long userId) {
        return adminService.activateUser(userId);
    }

    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return "User deleted successfully";
    }
}