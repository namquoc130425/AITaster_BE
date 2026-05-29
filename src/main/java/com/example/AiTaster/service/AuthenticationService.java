package com.example.AiTaster.service;

import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.*;
import com.example.AiTaster.dto.response.ClientProfileResponse;
import com.example.AiTaster.dto.response.ExpertProfileResponse;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ClientProfileMapper;
import com.example.AiTaster.mapper.ExpertProfileMapper;
import com.example.AiTaster.mapper.UserMapper;
import com.example.AiTaster.repository.UserRepo;
import com.example.AiTaster.service.imp.IAuthentication;
import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service

public class AuthenticationService implements UserDetailsService, IAuthentication {

    @Autowired
    UserRepo userRepo;
    @Autowired
    UserMapper userMapper;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    TokenService tokenService;

    @Autowired
    ClientProfileService clientProfileService;
    @Autowired
    private ExpertProfileService expertProfileService;
    @Autowired
    private ClientProfileMapper clientProfileMapper;
    @Autowired
    private ExpertProfileMapper expertProfileMapper;


    // đăng ký client
    @Transactional
    public ClientProfileResponse registerClient(ClientRegisterRequest request) {

        validateRegister(request.getEmail(), request.getPhone());

        //tạo User
        User user = userMapper.clientRegisterToUser(request);
        //setRole , mã hóa Password, setStatus
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CLIENT);
        user.setUserStatus(UserStatus.ACTIVE);
        // lưu xuống db


        ClientProfile clientProfile = ClientProfile.builder()
                .user(user)
                .companyName(request.getCompanyName())
                .contactName(request.getContactName())
                .description(request.getDescription())
                .bussinessField(request.getBusinessField())
                .address(request.getAddress()).build();

        user.setClientProfile(clientProfile);
        userRepo.save(user);


        //chuyển User vừa lưu thành ClientProfile và lưu xuống db
//        ClientProfileResponse response = clientProfileService.createForRegister(savedUser, request);

        return clientProfileMapper.toResponse(clientProfile);
    }


    // đăng ký expert
    @Transactional
    public ExpertProfileResponse registerExpert(ExpertRegisterRequest request) {
        validateRegister(request.getEmail(), request.getPhone());

        // tạo user
        User user = userMapper.expertRegisterToUser(request);
        //setRole , mã hóa pass , setStatus
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.EXPERT);
        user.setUserStatus(UserStatus.ACTIVE);

        ExpertProfile expertProfile = ExpertProfile.builder()
                .user(user)
                .bio(request.getBio())
                .yearOfExperience(request.getYearOfExperience())
                .rating(BigDecimal.ZERO)
                .completedProjects(0)
                .portfolioUrl(request.getPortfolioUrl())

                .build();
        // lưu xuong db
        user.setExpertProfile(expertProfile);
        userRepo.save(user);

        // chuyen use vừa lưu qua cho tk ExpertProfile và lưu xuong dbb
//        ExpertProfileResponse response = expertProfileService.createForRegister(savedUser, request);


        return expertProfileMapper.toResponse(expertProfile);
    }


    private void validateRegister(String email, String phone) {

        if (userRepo.existsByPhone(phone)) {
            throw new GlobalException(400, "Duplicate phone number");
        }

        if (userRepo.existsByEmail(email)) {
            throw new GlobalException(400, "Duplicate email");
        }
    }


    //login
    @Override
    public UserResponse login(LoginRequest loginRequest) {
        try {
            // xác thực bằng Spring security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            // ép kiểu từ Authen -) User
            User user = (User) authentication.getPrincipal();
            //generate token access token
            String accessToken = tokenService.generateAccessToken(user);
            // trả về cho FE
            UserResponse response = userMapper.toResponser(user);
            response.setAccessToken(accessToken);
            return response;

        } catch (LockedException e) {
            throw new GlobalException(403, "Account is locked");
        } catch (DisabledException e) {
            throw new GlobalException(403, "Account is disabled");
        } catch (BadCredentialsException e) {
            throw new GlobalException(400, "Invalid username or password");
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new GlobalException(e.getMessage());
        }

    }

    //lấy username
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(userName);

        if (user == null) {
            throw new UsernameNotFoundException(
                    "User not found: " + userName
            );
        }

        return user;
    }


}
