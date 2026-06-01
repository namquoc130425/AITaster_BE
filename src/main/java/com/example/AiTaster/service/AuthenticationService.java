package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.*;
import com.example.AiTaster.dto.response.ClientProfileResponse;
import com.example.AiTaster.dto.response.ExpertProfileResponse;
import com.example.AiTaster.dto.response.LoginResponse;
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
    private ClientProfileMapper clientProfileMapper;

    @Autowired
    private ExpertProfileMapper expertProfileMapper;

    @Override
    @Transactional
    public ClientProfileResponse registerClient(ClientRegisterRequest request) {

        validateRegister(request.getEmail(), request.getPhone());

        User user = userMapper.clientRegisterToUser(request);

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CLIENT);
        user.setUserStatus(UserStatus.ACTIVE);

        ClientProfile clientProfile = ClientProfile.builder()
                .user(user)
                .companyName(request.getCompanyName())
                .contactName(request.getContactName())
                .description(request.getDescription())
                .bussinessField(request.getBusinessField())
                .address(request.getAddress())
                .build();

        user.setClientProfile(clientProfile);

        userRepo.save(user);

        return clientProfileMapper.toResponse(clientProfile);
    }

    @Override
    @Transactional
    public ExpertProfileResponse registerExpert(ExpertRegisterRequest request) {

        validateRegister(request.getEmail(), request.getPhone());

        User user = userMapper.expertRegisterToUser(request);

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

        user.setExpertProfile(expertProfile);

        userRepo.save(user);

        return expertProfileMapper.toResponse(expertProfile);
    }

    private void validateRegister(String email, String phone) {

        if (userRepo.existsByPhone(phone)) {
            throw new GlobalException(
                    ErrorCode.DUPLICATE_PHONE.getCode(),
                    ErrorCode.DUPLICATE_PHONE.getMessage()
            );
        }

        if (userRepo.existsByEmail(email)) {
            throw new GlobalException(
                    ErrorCode.DUPLICATE_EMAIL.getCode(),
                    ErrorCode.DUPLICATE_EMAIL.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();

            String accessToken = tokenService.generateAccessToken(user);

            LoginResponse.LoginResponseBuilder responseBuilder = LoginResponse.builder()
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .phone(user.getPhone())
                    .avatarUrl(user.getAvatarUrl())
                    .role(user.getRole())
                    .userStatus(user.getUserStatus())
                    .accessToken(accessToken);

            if (user.getClientProfile() != null) {
                ClientProfile clientProfile = user.getClientProfile();

                responseBuilder.clientProfile(
                        LoginResponse.ClientProfileInfo.builder()
                                .clientProfileId(clientProfile.getClientProfileId())
                                .companyName(clientProfile.getCompanyName())
                                .contactName(clientProfile.getContactName())
                                .description(clientProfile.getDescription())
                                .businessField(clientProfile.getBussinessField())
                                .address(clientProfile.getAddress())
                                .build()
                );
            }

            if (user.getExpertProfile() != null) {
                ExpertProfile expertProfile = user.getExpertProfile();

                responseBuilder.expertProfile(
                        LoginResponse.ExpertProfileInfo.builder()
                                .expertProfileId(expertProfile.getExpertProfileId())
                                .bio(expertProfile.getBio())
                                .category(expertProfile.getCategory())
                                .skills(expertProfile.getSkills())
                                .yearsOfExperience(expertProfile.getYearOfExperience())
                                .portfolioUrl(expertProfile.getPortfolioUrl())
                                .rating(expertProfile.getRating())
                                .completedProjects(expertProfile.getCompletedProjects())
                                .build()
                );
            }

            return responseBuilder.build();

        } catch (LockedException e) {
            throw new GlobalException(
                    ErrorCode.ACCOUNT_LOCKED.getCode(),
                    ErrorCode.ACCOUNT_LOCKED.getMessage()
            );
        } catch (DisabledException e) {
            throw new GlobalException(
                    ErrorCode.ACCOUNT_DISABLED.getCode(),
                    ErrorCode.ACCOUNT_DISABLED.getMessage()
            );
        } catch (BadCredentialsException e) {
            throw new GlobalException(
                    ErrorCode.INVALID_LOGIN.getCode(),
                    ErrorCode.INVALID_LOGIN.getMessage()
            );
        }
    }

    @Override
    public UserDetails loadUserByUsername(String userName)
            throws UsernameNotFoundException {

        User user = userRepo.findByEmail(userName);

        if (user == null) {
            throw new UsernameNotFoundException(
                    ErrorCode.USER_NOT_FOUND.getMessage() + ": " + userName
            );
        }

        return user;
    }

    @Override
    @Transactional
    public UserResponse registerAdmin(AdminRegisterRequest request) {

        validateRegister(request.getEmail(), request.getPhone());

        User user = userMapper.adminRegisterToUser(request);

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ADMIN);
        user.setUserStatus(UserStatus.ACTIVE);

        User savedUser = userRepo.save(user);

        return userMapper.toResponser(savedUser);
    }
}