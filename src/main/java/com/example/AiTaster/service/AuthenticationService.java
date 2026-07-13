package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.constant.ExpertVerificationStatus;
import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.*;
import com.example.AiTaster.dto.response.*;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ClientProfileMapper;
import com.example.AiTaster.mapper.CurrentUserResponseMapper;
import com.example.AiTaster.mapper.ExpertProfileMapper;
import com.example.AiTaster.mapper.UserMapper;
import com.example.AiTaster.repository.CategoryRepo;
import com.example.AiTaster.repository.SkillRepo;
import com.example.AiTaster.repository.UserRepo;
import com.example.AiTaster.service.imp.IAuthentication;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
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
                .businessField(request.getBusinessField())
                .address(request.getAddress())
                .build();

        user.setClientProfile(clientProfile);

        userRepo.save(user);
        userWalletService.createdUserWallet(user);

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

        user.setExpertProfile(expertProfile);
        userRepo.save(user);
        userWalletService.createdUserWallet(user);

        return expertProfileMapper.toResponse(expertProfile);
    }

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

        // TODO: Hoàn thiện điều kiện kiểm tra bổ sung nếu cần; dòng nháp trước đó làm project không compile.
    }

    private void validateLoginUserStatus(User user) {
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
