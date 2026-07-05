package com.example.AiTaster.service;

import com.example.AiTaster.constant.ExpertVerificationStatus;
import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import com.example.AiTaster.dto.request.ExpertRegisterRequest;
import com.example.AiTaster.dto.response.ExpertProfileResponse;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.mapper.ClientProfileMapper;
import com.example.AiTaster.mapper.CurrentUserResponseMapper;
import com.example.AiTaster.mapper.ExpertProfileMapper;
import com.example.AiTaster.mapper.UserMapper;
import com.example.AiTaster.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceExpertVerificationTest {

    @Mock
    private UserRepo userRepo;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenService tokenService;
    @Mock
    private ClientProfileMapper clientProfileMapper;
    @Mock
    private ExpertProfileMapper expertProfileMapper;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private CurrentUserResponseMapper currentUserResponseMapper;
    @Mock
    private UserWalletService userWalletService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void registerExpert_createsSubmittedVerificationFromCertificateUrl() {
        ExpertRegisterRequest request = new ExpertRegisterRequest();
        request.setEmail("expert@example.com");
        request.setPhone("0987654321");
        request.setPassword("12345678");
        request.setBio("AI automation expert");
        request.setCategory("AI Automation");
        request.setSkills("Chatbot, RPA");
        request.setYearOfExperience(2);
        request.setPortfolioUrl("https://github.com/expert");
        request.setCertificateUrl("https://supabase.example/cert.pdf");

        User mappedUser = User.builder().build();
        ExpertProfileResponse response = new ExpertProfileResponse();

        when(userRepo.existsByPhone("0987654321")).thenReturn(false);
        when(userRepo.existsByEmail("expert@example.com")).thenReturn(false);
        when(userMapper.expertRegisterToUser(request)).thenReturn(mappedUser);
        when(passwordEncoder.encode("12345678")).thenReturn("encoded-password");
        when(expertProfileMapper.toResponse(org.mockito.ArgumentMatchers.any(ExpertProfile.class))).thenReturn(response);

        authenticationService.registerExpert(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        ExpertProfile expertProfile = savedUser.getExpertProfile();

        assertThat(savedUser.getRole()).isEqualTo(Role.EXPERT);
        assertThat(savedUser.getUserStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(savedUser.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(expertProfile.getCategory()).isEqualTo("AI Automation");
        assertThat(expertProfile.getSkills()).isEqualTo("Chatbot, RPA");
        assertThat(expertProfile.getVerification()).isNotNull();
        assertThat(expertProfile.getVerification().getExpertProfile()).isSameAs(expertProfile);
        assertThat(expertProfile.getVerification().getCertificateUrl()).isEqualTo("https://supabase.example/cert.pdf");
        assertThat(expertProfile.getVerification().getVerificationStatus()).isEqualTo(ExpertVerificationStatus.SUBMITTED);
        verify(userWalletService).createdUserWallet(mappedUser);
    }
}
