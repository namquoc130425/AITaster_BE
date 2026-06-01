package com.example.AiTaster.service.imp;

import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.AdminRequest;
import com.example.AiTaster.dto.response.AdminResponse;

import java.util.List;

public interface IAdminService {
    List<AdminResponse> getAllUsers(
            Role role,
            UserStatus userStatus,
            String keyword
    );

    AdminResponse getUserById(Long userId);

    AdminResponse createUser(AdminRequest request);

    AdminResponse updateUser(
            Long userId,
            AdminRequest request
    );

    AdminResponse banUser(Long userId);

    AdminResponse activateUser(Long userId);

    void deleteUser(Long userId);
}
