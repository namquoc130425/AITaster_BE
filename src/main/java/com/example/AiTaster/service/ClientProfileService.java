package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.dto.request.ClientProfileRequest;
import com.example.AiTaster.dto.request.ClientRegisterRequest;
import com.example.AiTaster.dto.response.ClientProfileResponse;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ClientProfileMapper;
import com.example.AiTaster.mapper.UserMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.UserRepo;

import com.example.AiTaster.service.imp.IClientProfile;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.control.MappingControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Slf4j
@Service
public class ClientProfileService implements IClientProfile {
    @Autowired
    UserRepo userRepo;
    @Autowired
    ClientProfileRepo clientProfileRepo;
    @Autowired
    UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
     ClientProfileMapper clientProfileMapper;



    @Override
    public List<ClientProfileResponse> getAll() {
        return clientProfileRepo.findAll()
                .stream()
                .map(clientProfileMapper::toResponse)
                .toList();
    }

    @Override
    public ClientProfileResponse getByClientId(Long id) {
        ClientProfile profile = clientProfileRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Client profile not found"));

        return clientProfileMapper.toResponse(profile);
    }

    @Override
    public ClientProfileResponse getByUserId(Long userId) {
        ClientProfile profile = clientProfileRepo.findByUser_UserId(userId).orElseThrow(() -> new RuntimeException("Client profile not found"));

        return clientProfileMapper.toResponse(profile);
    }



    @Transactional
    public ClientProfileResponse update(Long id, ClientProfileRequest request) {

        ClientProfile profile = clientProfileRepo.findById(id)
                .orElseThrow(() -> new GlobalException(
                        ErrorCode.NOT_FOUND.getCode(),
                        "Client profile " + ErrorCode.NOT_FOUND.getMessage()
                ));

        User user = profile.getUser();

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepo.existsByEmail(request.getEmail())) {
                throw new GlobalException(
                        ErrorCode.DUPLICATE_EMAIL.getCode(),
                        ErrorCode.DUPLICATE_EMAIL.getMessage()
                );
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (userRepo.existsByPhone(request.getPhone())) {
                throw new GlobalException(
                        ErrorCode.DUPLICATE_PHONE.getCode(),
                        ErrorCode.DUPLICATE_PHONE.getMessage()
                );
            }
            user.setPhone(request.getPhone());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        clientProfileMapper.updateEntity(request, profile);

        ClientProfile updatedProfile = clientProfileRepo.save(profile);

        return clientProfileMapper.toResponse(updatedProfile);
    }

    @Override
    public void delete(Long clientProfileId) {
       ClientProfile profile = clientProfileRepo.findById(clientProfileId).orElseThrow(() -> new RuntimeException("Client profile not found"));

        // muốn xóa profie của tk nào đó thì phải cắt quan hệ của User --- Profile . còn muốn xóa User mà đi kèm profile thì qua user làm
        User user = profile.getUser();

        if(user != null) {
            user.setClientProfile(null);
        }
            profile.setUser(null);
        clientProfileRepo.delete(profile);

    }

    public ClientProfileResponse createForRegister(User savedUser, ClientRegisterRequest request) {
        if (clientProfileRepo.existsByUser_UserId(savedUser.getUserId())) {
            throw new RuntimeException("This user already has a client profile");
        }
         // request mapper qua entity
        ClientProfile profile = clientProfileMapper.registerToEntity(request);

        // lưu user
        profile.setUser(savedUser);

        // lưu database
        ClientProfile saved = clientProfileRepo.save(profile);

        return clientProfileMapper.toResponse(saved);
    }
}