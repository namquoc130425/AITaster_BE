package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.dto.request.ExpertProfileRequest;
import com.example.AiTaster.dto.request.ExpertRegisterRequest;
import com.example.AiTaster.dto.response.CurrentUserResponse;
import com.example.AiTaster.dto.response.ExpertProfileResponse;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.CurrentUserResponseMapper;
import com.example.AiTaster.mapper.ExpertProfileMapper;
import com.example.AiTaster.mapper.UserMapper;
import com.example.AiTaster.repository.ExpertProfileRepo;
import com.example.AiTaster.repository.UserRepo;
import com.example.AiTaster.service.imp.IExpertProfile;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ExpertProfileService implements IExpertProfile {
@Autowired
    ExpertProfileMapper expertProfileMapper;
@Autowired
    ExpertProfileRepo expertProfileRepo;
@Autowired
    UserRepo userRepo;
@Autowired
    CurrentUserResponseMapper currentUserResponseMapper;

@Autowired
    UserMapper userMapper;


    @Override
    public List<ExpertProfileResponse> getAll() {
        return expertProfileRepo
                .findAll().
                stream().
                map(expertProfileMapper::toResponse)
                .toList();
    }

@Override
    public ExpertProfileResponse getByExpertId(Long expertId) {
        ExpertProfile profile = expertProfileRepo.findById(expertId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND.getCode(),"Expert profile"+ErrorCode.NOT_FOUND.getMessage()));
        return expertProfileMapper.toResponse(profile);
    }

    @Override
    public ExpertProfileResponse getByUserId(Long userId) {
        ExpertProfile profile = expertProfileRepo.findByUser_UserId(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND.getCode(),"Expert profile"+ErrorCode.NOT_FOUND.getMessage()));
        return expertProfileMapper.toResponse(profile);
    }

    @Override
    public ExpertProfileResponse createForRegister(User user, ExpertRegisterRequest request) {
        // kiểm tra tồn tại ko
        if(expertProfileRepo.existsByUser_UserId(user.getUserId())) {
            throw new GlobalException("This user already has a client profile");
        }

        // Mapper chuyển dữ liệu yêu cầu sang entity.
          ExpertProfile expertProfile = expertProfileMapper.registertoEntity(request);
        //  Gắn User vừa tạo vào ExpertProfile
        expertProfile.setUser(user);

        // Lưu database.
        ExpertProfile save = expertProfileRepo.save(expertProfile);
        return expertProfileMapper.toResponse(save);
    }

    @Override
    @Transactional
    public CurrentUserResponse update(Long id, ExpertProfileRequest request) {
        ExpertProfile profile = expertProfileRepo.findByExpertProfileId(id).orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND.getCode(),"Expert profile"+ErrorCode.NOT_FOUND.getMessage()));
        User user = profile.getUser();

        expertProfileMapper.updateEntity(request,profile);
        userMapper.updateUserFromExpertProfileRequest(request, user);
        ExpertProfile updateProfile = expertProfileRepo.save(profile);

        return currentUserResponseMapper.toResponse(updateProfile.getUser());
    }


    @Transactional
    @Override
    public void  delete(Long id) {
        ExpertProfile profile = expertProfileRepo.findById(id)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND.getCode(),"Expert profile"+ErrorCode.NOT_FOUND.getMessage()));

        // muốn xóa profie của tk nào đó thì phải cắt quan hệ của User --- Profile . còn muốn xóa User mà đi kèm profile thì qua user làm
        User user = profile.getUser();

        if (user != null) {
            user.setExpertProfile(null);
        }
            profile.setUser(null);
        expertProfileRepo.delete(profile);
    }
}
