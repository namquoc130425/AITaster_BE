package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.AdminRequest;

import java.util.List;

public interface IAdminService {
    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long userId);

    UserResponse createUser(AdminRequest request);

    UserResponse updateUser(Long userId, AdminRequest request);

    UserResponse banUser(Long userId);

    UserResponse activateUser(Long userId);

    void deleteUser(Long userId);
}
