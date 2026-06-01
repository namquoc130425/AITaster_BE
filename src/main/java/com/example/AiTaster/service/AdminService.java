package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import com.example.AiTaster.dto.request.AdminRequest;
import com.example.AiTaster.dto.response.AdminResponse;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.UserMapper;
import com.example.AiTaster.repository.UserRepo;
import com.example.AiTaster.service.imp.IAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService implements IAdminService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<AdminResponse> getAllUsers(
            Role role,
            UserStatus userStatus,
            String keyword
    ) {
        List<User> users;

        if (role != null && userStatus != null) {
            users = userRepo.findByRoleAndUserStatus(role, userStatus);
        } else if (role != null) {
            users = userRepo.findByRole(role);
        } else if (userStatus != null) {
            users = userRepo.findByUserStatus(userStatus);
        } else if (keyword != null && !keyword.isBlank()) {
            users = userRepo.findByFullNameContainingIgnoreCase(keyword);
        } else {
            users = userRepo.findAll();
        }

        return users.stream()
                .map(userMapper::toAdminResponse)
                .toList();
    }

    @Override
    public AdminResponse getUserById(Long userId) {
        User user = findUserById(userId);
        return userMapper.toAdminResponse(user);
    }

    @Override
    public AdminResponse createUser(AdminRequest request) {

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throwError(ErrorCode.PASSWORD_REQUIRED);
        }

        if (userRepo.existsByEmail(request.getEmail())) {
            throwError(ErrorCode.DUPLICATE_EMAIL);
        }

        if (request.getPhone() != null && userRepo.existsByPhone(request.getPhone())) {
            throwError(ErrorCode.DUPLICATE_PHONE);
        }

        User user = userMapper.adminRequestToUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        if (user.getUserStatus() == null) {
            user.setUserStatus(UserStatus.ACTIVE);
        }

        User savedUser = userRepo.save(user);
        return userMapper.toAdminResponse(savedUser);
    }

    @Override
    public AdminResponse updateUser(Long userId, AdminRequest request) {
        User user = findUserById(userId);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepo.existsByEmail(request.getEmail())) {
                throwError(ErrorCode.DUPLICATE_EMAIL);
            }
        }

        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (userRepo.existsByPhone(request.getPhone())) {
                throwError(ErrorCode.DUPLICATE_PHONE);
            }
        }

        userMapper.updateUserFromAdminRequest(request, user);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        User savedUser = userRepo.save(user);
        return userMapper.toAdminResponse(savedUser);
    }

    @Override
    public AdminResponse banUser(Long userId) {
        User user = findUserById(userId);
        user.setUserStatus(UserStatus.BANNED);

        User savedUser = userRepo.save(user);
        return userMapper.toAdminResponse(savedUser);
    }

    @Override
    public AdminResponse activateUser(Long userId) {
        User user = findUserById(userId);
        user.setUserStatus(UserStatus.ACTIVE);

        User savedUser = userRepo.save(user);
        return userMapper.toAdminResponse(savedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = findUserById(userId);
        userRepo.delete(user);
    }

    private User findUserById(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new GlobalException(
                        ErrorCode.USER_NOT_FOUND.getCode(),
                        ErrorCode.USER_NOT_FOUND.getMessage()
                ));
    }

    private void throwError(ErrorCode errorCode) {
        throw new GlobalException(
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }
}