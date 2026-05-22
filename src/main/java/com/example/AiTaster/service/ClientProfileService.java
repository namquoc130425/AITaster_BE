package com.example.AiTaster.service;

import com.example.AiTaster.dto.request.ClientProfileRequest;
import com.example.AiTaster.dto.response.ClientProfileResponse;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.mapper.ClientProfileMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.UserRepo;

import com.example.AiTaster.service.imp.IClientProfile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientProfileService implements IClientProfile {

    private final ClientProfileRepo clientProfileRepo;
    private final UserRepo userRepo;
    private final ClientProfileMapper clientProfileMapper;

    public ClientProfileService(
            ClientProfileRepo clientProfileRepo,
            UserRepo userRepo,
            ClientProfileMapper clientProfileMapper
    ) {
        this.clientProfileRepo = clientProfileRepo;
        this.userRepo = userRepo;
        this.clientProfileMapper = clientProfileMapper;
    }

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
        ClientProfile profile = clientProfileRepo.findByClientProfileId(userId)
                .orElseThrow(() -> new RuntimeException("Client profile not found"));

        return clientProfileMapper.toResponse(profile);
    }

    @Override
    public ClientProfileResponse create(ClientProfileRequest request) {
        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (clientProfileRepo.existsByClientProfileId(request.getUserId())) {
            throw new RuntimeException("This user already has a client profile");
        }

        ClientProfile profile = clientProfileMapper.toEntity(request);
        profile.setUser(user);

        ClientProfile savedProfile = clientProfileRepo.save(profile);

        return clientProfileMapper.toResponse(savedProfile);
    }

    @Override
    public ClientProfileResponse update(Long id, ClientProfileRequest request) {
        ClientProfile profile = clientProfileRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Client profile not found"));

        clientProfileMapper.updateEntity(request, profile);

        ClientProfile updatedProfile = clientProfileRepo.save(profile);

        return clientProfileMapper.toResponse(updatedProfile);
    }

    @Override
    public void delete(Long id) {
        ClientProfile profile = clientProfileRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Client profile not found"));

        clientProfileRepo.delete(profile);
    }
}