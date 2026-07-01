package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.dto.request.ClientProfileRequest;
import com.example.AiTaster.dto.request.ClientRegisterRequest;
import com.example.AiTaster.dto.response.ClientProfileResponse;
import com.example.AiTaster.dto.response.CurrentUserResponse;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ClientProfileMapper;
import com.example.AiTaster.mapper.CurrentUserResponseMapper;
import com.example.AiTaster.mapper.UserMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.UserRepo;

import com.example.AiTaster.service.imp.IClientProfile;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.control.MappingControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    @Autowired
    CurrentUserResponseMapper currentUserResponseMapper;
    @Autowired
    CurrentUserService currentUserService;



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
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND.getCode(),"Client profile"+ ErrorCode.NOT_FOUND.getMessage()));

        return clientProfileMapper.toResponse(profile);
    }

    @Override
    public ClientProfileResponse getByUserId(Long userId) {
        ClientProfile profile = clientProfileRepo.findByUser_UserId(userId).orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND.getCode(),"Client profile"+ ErrorCode.NOT_FOUND.getMessage()));

        return clientProfileMapper.toResponse(profile);
    }



    @Override
    @Transactional
    public CurrentUserResponse update(Long id, ClientProfileRequest request) {

        ClientProfile profile = clientProfileRepo.findById(id)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND.getCode()
                        ,"Client profile "+ ErrorCode.NOT_FOUND.getMessage()));

        ClientProfile currentClientProfile = getCurrentClientProfile();
        checkClientOwner(profile, currentClientProfile);

        User user = profile.getUser();

        clientProfileMapper.updateEntity(request, profile);

        userMapper.updateUserFromClientProfileRequest(request, user); // dirty checking tự động lưu (@Transactional)

        ClientProfile updatedProfile = clientProfileRepo.save(profile);

        return currentUserResponseMapper.toResponse(updatedProfile.getUser());
    }


    @Override
    public void delete(Long clientProfileId) {
       ClientProfile profile = clientProfileRepo.findById(clientProfileId).orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND.getCode(),"Client profile"+ ErrorCode.NOT_FOUND.getMessage()));

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
            throw new GlobalException("This user already has a client profile");
        }
         // Mapper chuyển dữ liệu yêu cầu sang entity.
        ClientProfile profile = clientProfileMapper.registerToEntity(request);

        // Gắn user vào profile.
        profile.setUser(savedUser);

        // Lưu database.
        ClientProfile saved = clientProfileRepo.save(profile);

        return clientProfileMapper.toResponse(saved);
    }
    public ClientProfile getCurrentClientProfile() {
        User currentUser = currentUserService.getCurrentUser();
        return clientProfileRepo.findByUser(currentUser)
                .orElseThrow(() -> new GlobalException("Client Profile Not Found"));
    }

    private void checkClientOwner(ClientProfile profile, ClientProfile currentClientProfile) {
        if (!profile.getClientProfileId().equals(currentClientProfile.getClientProfileId())) {
            throw new GlobalException(403, "You are not owner of this client profile");
        }
    };
}
