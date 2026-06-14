package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import com.example.AiTaster.controller.AuthController;
import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.*;
import com.example.AiTaster.dto.response.ClientProfileResponse;
import com.example.AiTaster.dto.response.ExpertProfileResponse;
import com.example.AiTaster.dto.response.AuthResponse;
import com.example.AiTaster.dto.response.AuthenticationResponse;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.RefreshToken;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ClientProfileMapper;
import com.example.AiTaster.mapper.CurrentUserResponseMapper;
import com.example.AiTaster.mapper.ExpertProfileMapper;
import com.example.AiTaster.mapper.UserMapper;
import com.example.AiTaster.repository.UserRepo;
import com.example.AiTaster.service.imp.IAuthentication;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthenticationService implements UserDetailsService, IAuthentication {

    UserRepo userRepo;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    AuthenticationManager authenticationManager;
    TokenService tokenService;
    ClientProfileMapper clientProfileMapper;
    ExpertProfileMapper expertProfileMapper;
    RefreshTokenService refreshTokenService;
    CurrentUserResponseMapper currentUserResponseMapper;

//code quá bẩn

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


        //builder:mapper thẳng
        ClientProfile clientProfile = ClientProfile.builder()
                .user(user)
                .companyName(request.getCompanyName())
                .contactName(request.getContactName())
                .description(request.getDescription())
                .businessField(request.getBusinessField())
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

        // tạo user
        User user = userMapper.expertRegisterToUser(request);
        //setRole , mã hóa pass , setStatus
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.EXPERT);
        user.setUserStatus(UserStatus.ACTIVE);

        ExpertProfile expertProfile = ExpertProfile.builder()
                .user(user)
                .bio(request.getBio())
                .yearOfExperience(request.getYearsOfExperience())
                .rating(BigDecimal.ZERO)
                .completedProjects(0)
                .portfolioUrl(request.getPortfolioUrl())

                .build();
        // lưu xuong db
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


    //login
    @Override
    @Transactional
    public AuthenticationResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();
//            principal   -> user là ai <=>  new UsernamePasswordAuthenticationToken
//            credentials -> mật khẩu/token
//            authorities -> quyền/role
            String accessToken = tokenService.generateAccessToken(user);

            String refreshToken = refreshTokenService.createRefreshToken(user).getToken();


            return buildAuthenticationResponse(user, accessToken, refreshToken);

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

    //lấy username
    @Override
    public UserDetails loadUserByUsername(String userName)
            throws UsernameNotFoundException {

        User user = userRepo.findByUsername(userName).orElseThrow(() -> new GlobalException(
                ErrorCode.USER_NOT_FOUND.getCode(),
                ErrorCode.USER_NOT_FOUND.getMessage() + ": " + userName
        ));

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

    @Override
    @Transactional
    public AuthResponse refresh(TokenRequest tokenRequest) {

        RefreshToken validToken =
                refreshTokenService.verifyToken(tokenRequest.getToken())
                        .orElseThrow(() -> new GlobalException(
                                ErrorCode.INVALID_REFRESH_TOKEN.getCode(),
                                ErrorCode.INVALID_REFRESH_TOKEN.getMessage()
                        ));

        User user = userRepo.findById(validToken.getUserId())
                .orElseThrow(() -> new GlobalException(
                        ErrorCode.USER_NOT_FOUND.getCode(),
                        ErrorCode.USER_NOT_FOUND.getMessage()
                ));

        //rotation: revoke cũ + cấp mới
        validateRefreshUserStatus(user);

        RefreshToken refreshToken = refreshTokenService.rotateToken(tokenRequest.getToken(), user);
        String accessToken = tokenService.generateAccessToken(user);
        return buildAuthResponse(accessToken, refreshToken.getToken());
    }

    private void validateRefreshUserStatus(User user) {
        if (!user.isAccountNonLocked()) {
            throw new GlobalException(
                    ErrorCode.ACCOUNT_LOCKED.getCode(),
                    ErrorCode.ACCOUNT_LOCKED.getMessage()
            );
        }

        if (!user.isEnabled()) {
            throw new GlobalException(
                    ErrorCode.ACCOUNT_DISABLED.getCode(),
                    ErrorCode.ACCOUNT_DISABLED.getMessage()
            );
        }
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private AuthenticationResponse buildAuthenticationResponse(User user, String accessToken, String refreshToken) {
        return AuthenticationResponse.builder()
                .auth(buildAuthResponse(accessToken, refreshToken))
                .currentUser(currentUserResponseMapper.toResponse(user))
                .build();
    }


}
