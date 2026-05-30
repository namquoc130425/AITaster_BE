package com.example.AiTaster.controller;

import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.AdminRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.ClientProfileResponse;
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
    public ResponseEntity<APIResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> responses = adminService.getAllUsers();
        return ResponseEntity.ok(APIResponse.response(200, "Get all users successfully", responses));
    }

    @GetMapping("/{userId}")
    public  ResponseEntity<APIResponse<UserResponse>> getUserById(@PathVariable Long userId) {
        UserResponse response = adminService.getUserById(userId);
        return ResponseEntity.ok(APIResponse.response(200, "Get user successfully", response)) ;
    }

    @PostMapping
    public ResponseEntity<APIResponse<UserResponse>> createUser(@RequestBody AdminRequest request) {
        UserResponse userResponse = adminService.createUser(request);
        return ResponseEntity.ok(APIResponse.response(200,"Create user successfully" ,userResponse));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<APIResponse<UserResponse>> updateUser(@PathVariable Long userId, @RequestBody AdminRequest request) {
        UserResponse userResponse = adminService.updateUser(userId,request);
        return ResponseEntity.ok(APIResponse.response(200,"Update user successfully",userResponse));
    }

    @PatchMapping("/{userId}/ban")
    public ResponseEntity<APIResponse<UserResponse>>banUser(@PathVariable Long userId) {
        UserResponse userResponse = adminService.banUser(userId);
        return ResponseEntity.ok(APIResponse.response(200,"Ban user successfully",userResponse));
    }

    @PatchMapping("/{userId}/activate")
    public ResponseEntity<APIResponse<UserResponse>> activateUser(@PathVariable Long userId) {
        UserResponse userResponse = adminService.activateUser(userId);
        return ResponseEntity.ok(APIResponse.response(200,"Activate user successfully",userResponse));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<APIResponse<UserResponse>> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok(APIResponse.response(200,"Delete user successfully",null));
    }
}