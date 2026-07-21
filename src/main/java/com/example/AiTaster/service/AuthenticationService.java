package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
<<<<<<< HEAD
=======
import com.example.AiTaster.constant.ExpertVerificationStatus;
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import com.example.AiTaster.controller.AuthController;
import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.*;
<<<<<<< HEAD
import com.example.AiTaster.dto.response.ClientProfileResponse;
import com.example.AiTaster.dto.response.ExpertProfileResponse;
import com.example.AiTaster.dto.response.AuthResponse;
import com.example.AiTaster.dto.response.AuthenticationResponse;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.RefreshToken;
import com.example.AiTaster.entity.User;
=======
import com.example.AiTaster.dto.response.*;
import com.example.AiTaster.entity.*;
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ClientProfileMapper;
import com.example.AiTaster.mapper.CurrentUserResponseMapper;
import com.example.AiTaster.mapper.ExpertProfileMapper;
import com.example.AiTaster.mapper.UserMapper;
import com.example.AiTaster.repository.CategoryRepo;
import com.example.AiTaster.repository.SkillRepo;
import com.example.AiTaster.repository.UserRepo;
import com.example.AiTaster.service.imp.IAuthentication;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
<<<<<<< HEAD
import lombok.RequiredArgsConstructor;
=======
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
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
<<<<<<< HEAD
=======
import java.util.ArrayList;
import java.util.List;
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
<<<<<<< HEAD

=======
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
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
    ClientProfileMapper clientProfileMapper;
<<<<<<< HEAD

    @Autowired
    ExpertProfileMapper expertProfileMapper;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    CurrentUserResponseMapper currentUserResponseMapper;

    @Autowired
    UserWalletService userWalletService;

// Service xử lý đăng ký, đăng nhập và refresh token.

    // Đăng ký client.
=======

    @Autowired
    ExpertProfileMapper expertProfileMapper;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    CurrentUserResponseMapper currentUserResponseMapper;

    @Autowired
    UserWalletService userWalletService;

    @Autowired
    SkillRepo skillRepo;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    SupabaseService supabaseService;

    @Autowired
    EmailService emailService;

    @Transactional
    public ClientProfileResponse registerClient(ClientRegisterRequest request) {
        validateRegister(request.getEmail(), request.getPhone());

<<<<<<< HEAD
        // Tạo User.
        User user = userMapper.clientRegisterToUser(request);

        // Set role, mã hóa password và set status.
=======
        User user = userMapper.clientRegisterToUser(request);
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CLIENT);
        user.setUserStatus(UserStatus.ACTIVE);

<<<<<<< HEAD

        // Tạo ClientProfile từ dữ liệu yêu cầu.
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

=======
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
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
        userWalletService.createdUserWallet(user);
        sendWelcomeEmail(user);

        return clientProfileMapper.toResponse(clientProfile);
    }

    @Override
    @Transactional
    public ExpertProfileResponse registerExpert(ExpertRegisterRequest request) {
        validateRegister(request.getEmail(), request.getPhone());

<<<<<<< HEAD
        // Tạo user.
        User user = userMapper.expertRegisterToUser(request);
        // Set role, mã hóa password và set status.
=======
        User user = userMapper.expertRegisterToUser(request);
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.EXPERT);
        user.setUserStatus(UserStatus.ACTIVE);

<<<<<<< HEAD
        ExpertProfile expertProfile = ExpertProfile.builder()
                .user(user)
                .bio(request.getBio())
                .yearOfExperience(request.getYearOfExperience())
                .rating(BigDecimal.ZERO)
                .completedProjects(0)
                .portfolioUrl(request.getPortfolioUrl())

                .build();
        // Lưu xuống DB.
=======
        Category category = getCategoryByCategoryId(request.getCategoryId());
        List<Skill> skills = getSkillBySkillId(request.getSkillIds());

        ExpertProfile expertProfile = ExpertProfile.builder()
                .user(user)
                .bio(request.getBio())
                .category(category)
                .skills(skills)
                .yearOfExperience(request.getYearOfExperience())
                .rating(BigDecimal.ZERO)
                .completedProjects(0)
                .portfolioUrl(request.getPortfolioUrl())
                .build();

        ExpertVerification verification = ExpertVerification.builder()
                .expertProfile(expertProfile)
                .certificateUrl(request.getCertificateUrl())
                .verificationStatus(ExpertVerificationStatus.SUBMITTED)
                .build();

        expertProfile.setVerification(verification);

>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
        user.setExpertProfile(expertProfile);
        userRepo.save(user);
        userWalletService.createdUserWallet(user);
        sendWelcomeEmail(user);

        return expertProfileMapper.toResponse(expertProfile);
    }

<<<<<<< HEAD

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


    // Đăng nhập.
=======
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
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
<<<<<<< HEAD
            String accessToken = tokenService.generateAccessToken(user);

            String refreshToken = refreshTokenService.createRefreshToken(user).getToken();


            return buildAuthenticationResponse(user, accessToken, refreshToken);

=======

            String accessToken = tokenService.generateAccessToken(user);
            String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

            return buildAuthenticationResponse(user, accessToken, refreshToken);

>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
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

<<<<<<< HEAD
    // Lấy user theo username.
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
=======
    /*
     * Login bằng Google thông qua Supabase.
     *
     * Không tạo user mới.
     * Không lưu supabaseUserId vào bảng User.
     * Chỉ verify Supabase token rồi tìm user trong hệ thống bằng email.
     */
    @Transactional
    public AuthenticationResponse loginWithSupabaseGoogle(
            GoogleLoginRequest request
    ) {
        GoogleLoginUserInfoResponse supabaseUserInfo =
                supabaseService.verifyGoogleAccessToken(
                        request.getAccessToken()
                );

        User user = userRepo.findByEmail(supabaseUserInfo.getEmail())
                .orElseThrow(() -> new GlobalException(
                        ErrorCode.SUPABASE_ACCOUNT_NOT_REGISTERED
                ));

        validateLoginUserStatus(user);

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        return buildAuthenticationResponse(user, accessToken, refreshToken);
    }

    @Override
    public UserDetails loadUserByUsername(String userName)
            throws UsernameNotFoundException {

        return userRepo.findByUsername(userName)
                .orElseThrow(() -> new GlobalException(
                        ErrorCode.USER_NOT_FOUND.getCode(),
                        ErrorCode.USER_NOT_FOUND.getMessage() + ": " + userName
                ));
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
        userWalletService.createdUserWallet(savedUser);
        sendWelcomeEmail(savedUser);

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

        validateRefreshUserStatus(user);

        RefreshToken refreshToken =
                refreshTokenService.rotateToken(tokenRequest.getToken(), user);

        String accessToken =
                tokenService.generateAccessToken(user);

        return buildAuthResponse(accessToken, refreshToken.getToken());
    }

    @Transactional
    public AuthenticationResponse registerClientWithSupabaseGoogle(
            GoogleClientRegisterRequest request
    ) {
        GoogleLoginUserInfoResponse supabaseUserInfo =
                supabaseService.verifyGoogleAccessToken(
                        request.getAccessToken()
                );

        validateGoogleRegister(
                supabaseUserInfo.getEmail(),
                request.getPhone(),
                request.getUsername()
        );

        User user = User.builder()
                .email(supabaseUserInfo.getEmail())
                .fullName(request.getFullName())
                .username(request.getUsername())
                .phone(request.getPhone())
                .avatarUrl(supabaseUserInfo.getAvatarUrl())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.CLIENT)
                .userStatus(UserStatus.ACTIVE)
                .build();

        ClientProfile clientProfile = ClientProfile.builder()
                .user(user)
                .companyName(request.getCompanyName())
                .contactName(request.getContactName())
                .description(request.getDescription())
                .businessField(request.getBusinessField())
                .address(request.getAddress())
                .build();

        user.setClientProfile(clientProfile);

        User savedUser = userRepo.save(user);

        userWalletService.createdUserWallet(savedUser);
        sendWelcomeEmail(savedUser);

        String accessToken =
                tokenService.generateAccessToken(savedUser);

        String refreshToken =
                refreshTokenService
                        .createRefreshToken(savedUser)
                        .getToken();

        return buildAuthenticationResponse(
                savedUser,
                accessToken,
                refreshToken
        );
    }

    @Transactional
    public AuthenticationResponse registerExpertWithSupabaseGoogle(
            GoogleExpertRegisterRequest request
    ) {
        GoogleLoginUserInfoResponse supabaseUserInfo =
                supabaseService.verifyGoogleAccessToken(
                        request.getAccessToken()
                );

        validateGoogleRegister(
                supabaseUserInfo.getEmail(),
                request.getPhone(),
                request.getUsername()
        );

        Category category =
                getCategoryByCategoryId(request.getCategoryId());

        List<Skill> skills =
                getSkillBySkillId(request.getSkillIds());

        User user = User.builder()
                .email(supabaseUserInfo.getEmail())
                .fullName(request.getFullName())
                .username(request.getUsername())
                .phone(request.getPhone())
                .avatarUrl(supabaseUserInfo.getAvatarUrl())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.EXPERT)
                .userStatus(UserStatus.ACTIVE)
                .build();

        ExpertProfile expertProfile = ExpertProfile.builder()
                .user(user)
                .bio(request.getBio())
                .category(category)
                .skills(skills)
                .yearOfExperience(request.getYearOfExperience())
                .rating(BigDecimal.ZERO)
                .completedProjects(0)
                .portfolioUrl(request.getPortfolioUrl())
                .build();

        ExpertVerification verification = ExpertVerification.builder()
                .expertProfile(expertProfile)
                .certificateUrl(request.getCertificateUrl())
                .verificationStatus(ExpertVerificationStatus.SUBMITTED)
                .build();

        expertProfile.setVerification(verification);
        user.setExpertProfile(expertProfile);

        User savedUser = userRepo.save(user);

        userWalletService.createdUserWallet(savedUser);
        sendWelcomeEmail(savedUser);

        String accessToken =
                tokenService.generateAccessToken(savedUser);

        String refreshToken =
                refreshTokenService
                        .createRefreshToken(savedUser)
                        .getToken();

        return buildAuthenticationResponse(
                savedUser,
                accessToken,
                refreshToken
        );
    }

    private void sendWelcomeEmail(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }

        try {
            emailService.sendWelcomeEmail(
                    user.getEmail(),
                    getDisplayName(user),
                    user.getRole() == null ? null : user.getRole().name()
            );
        } catch (RuntimeException e) {
            log.warn("Could not send welcome email to {}", user.getEmail(), e);
        }
    }

    private String getDisplayName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }

        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }

        return user.getEmail();
    }

    private void validateRegister(String email, String phone) {
        if (userRepo.existsByPhone(phone)) {
            throw new GlobalException(
                    ErrorCode.DUPLICATE_PHONE.getCode(),
                    ErrorCode.DUPLICATE_PHONE.getMessage()
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
            );
        }

        if (userRepo.existsByEmail(email)) {
            throw new GlobalException(
                    ErrorCode.DUPLICATE_EMAIL.getCode(),
                    ErrorCode.DUPLICATE_EMAIL.getMessage()
            );
        }

        // TODO: Hoàn thiện điều kiện kiểm tra bổ sung nếu cần; dòng nháp trước đó làm project không compile.
    }

<<<<<<< HEAD
    @Override
    @Transactional
    public UserResponse registerAdmin(AdminRegisterRequest request) {

        validateRegister(request.getEmail(), request.getPhone());

        User user = userMapper.adminRegisterToUser(request);

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ADMIN);
        user.setUserStatus(UserStatus.ACTIVE);

        User savedUser = userRepo.save(user);
        userWalletService.createdUserWallet(savedUser);

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

        // Xoay vòng token: thu hồi token cũ và cấp token mới.
        validateRefreshUserStatus(user);

        RefreshToken refreshToken = refreshTokenService.rotateToken(tokenRequest.getToken(), user);
        String accessToken = tokenService.generateAccessToken(user);
        return buildAuthResponse(accessToken, refreshToken.getToken());
    }

    private void validateRefreshUserStatus(User user) {
=======
    private void validateLoginUserStatus(User user) {
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
        if (!user.isAccountNonLocked()) {
            throw new GlobalException(
                    ErrorCode.ACCOUNT_LOCKED.getCode(),
                    ErrorCode.ACCOUNT_LOCKED.getMessage()
            );
        }
<<<<<<< HEAD

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

=======
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384

        if (!user.isEnabled()) {
            throw new GlobalException(
                    ErrorCode.ACCOUNT_DISABLED.getCode(),
                    ErrorCode.ACCOUNT_DISABLED.getMessage()
            );
        }

        if (!UserStatus.ACTIVE.equals(user.getUserStatus())) {
            throw new GlobalException(
                    ErrorCode.ACCOUNT_DISABLED.getCode(),
                    ErrorCode.ACCOUNT_DISABLED.getMessage()
            );
        }
    }

    private void validateRefreshUserStatus(User user) {
        validateLoginUserStatus(user);
    }

    private AuthResponse buildAuthResponse(
            String accessToken,
            String refreshToken
    ) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private AuthenticationResponse buildAuthenticationResponse(
            User user,
            String accessToken,
            String refreshToken
    ) {
        return AuthenticationResponse.builder()
                .auth(buildAuthResponse(accessToken, refreshToken))
                .currentUser(currentUserResponseMapper.toResponse(user))
                .build();
    }

    private Category getCategoryByCategoryId(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new GlobalException(400, "Category is required");
        }

        return categoryRepo.getCategoriesByCategoryId(categoryId)
                .orElseThrow(() -> new GlobalException(400, "Category Not Found"));
    }

    private List<Skill> getSkillBySkillId(List<Long> selectedSkillIds) {
        if (selectedSkillIds == null || selectedSkillIds.isEmpty()) {
            throw new GlobalException(400, "selectedSkillIds is required");
        }

        List<Long> checkSkillIds = new ArrayList<>();

        for (Long skillId : selectedSkillIds) {
            if (skillId == null) {
                continue;
            }

            if (skillId <= 0) {
                continue;
            }

            if (!checkSkillIds.contains(skillId)) {
                checkSkillIds.add(skillId);
            }
        }

        if (checkSkillIds.isEmpty()) {
            throw new GlobalException(400, "Skill is required");
        }

        List<Skill> skills = skillRepo.findAllById(checkSkillIds);

        if (skills.size() != checkSkillIds.size()) {
            throw new GlobalException(400, "Some skills not found");
        }

        return skills;
    }

    private void validateGoogleRegister(
            String email,
            String phone,
            String username
    ) {
        if (email == null || email.isBlank()) {
            throw new GlobalException(ErrorCode.SUPABASE_EMAIL_REQUIRED);
        }

        if (phone == null || phone.isBlank()) {
            throw new GlobalException(400, "Phone is required");
        }

        if (username == null || username.isBlank()) {
            throw new GlobalException(400, "Username is required");
        }

        if (userRepo.existsByEmail(email)) {
            throw new GlobalException(
                    ErrorCode.DUPLICATE_EMAIL.getCode(),
                    ErrorCode.DUPLICATE_EMAIL.getMessage()
            );
        }

        if (userRepo.existsByPhone(phone)) {
            throw new GlobalException(
                    ErrorCode.DUPLICATE_PHONE.getCode(),
                    ErrorCode.DUPLICATE_PHONE.getMessage()
            );
        }

    }
}
