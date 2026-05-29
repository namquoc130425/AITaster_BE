package com.example.AiTaster.service;

import com.example.AiTaster.constant.UserStatus;
import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.AdminRequest;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.UserMapper;
import com.example.AiTaster.repository.UserRepo;
import com.example.AiTaster.service.imp.IAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService implements IAdminService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepo.findAll()
                .stream()
                .map(userMapper::toResponser)
                .toList();
    }

    @Override
    public UserResponse getUserById(Long userId) {
        User user = findUserById(userId);
        return userMapper.toResponser(user);
    }

    @Override
    public UserResponse createUser(AdminRequest request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new GlobalException(400, "Duplicate email");
        }

        if (request.getPhone() != null && userRepo.existsByPhone(request.getPhone())) {
            throw new GlobalException(400, "Duplicate phone number");
        }

        User user = userMapper.adminRequestToUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        if (user.getUserStatus() == null) {
            user.setUserStatus(UserStatus.ACTIVE);
        }

        User savedUser = userRepo.save(user);
        return userMapper.toResponser(savedUser);
    }

    @Override
    public UserResponse updateUser(Long userId, AdminRequest request) {
        User user = findUserById(userId);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepo.existsByEmail(request.getEmail())) {
                throw new GlobalException(400, "Duplicate email");
            }
        }

        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (userRepo.existsByPhone(request.getPhone())) {
                throw new GlobalException(400, "Duplicate phone number");
            }
        }

        userMapper.updateUserFromAdminRequest(request, user);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        User savedUser = userRepo.save(user);
        return userMapper.toResponser(savedUser);
    }

    @Override
    public UserResponse banUser(Long userId) {
        User user = findUserById(userId);
        user.setUserStatus(UserStatus.BANNED);
        return userMapper.toResponser(userRepo.save(user));
    }

    @Override
    public UserResponse activateUser(Long userId) {
        User user = findUserById(userId);
        user.setUserStatus(UserStatus.ACTIVE);
        return userMapper.toResponser(userRepo.save(user));
    }

    @Override
    public void deleteUser(Long userId) {
        User user = findUserById(userId);
        userRepo.delete(user);
    }

    private User findUserById(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new GlobalException(404, "User not found"));
    }
}