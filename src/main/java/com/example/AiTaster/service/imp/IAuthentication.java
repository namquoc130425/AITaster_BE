package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.*;
import com.example.AiTaster.dto.response.ClientProfileResponse;
import com.example.AiTaster.dto.response.ExpertProfileResponse;
import com.example.AiTaster.dto.response.AuthResponse;
import com.example.AiTaster.dto.response.AuthenticationResponse;

public interface IAuthentication {
    ClientProfileResponse registerClient(ClientRegisterRequest request);

    ExpertProfileResponse registerExpert(ExpertRegisterRequest request);

    AuthenticationResponse login(LoginRequest loginRequest);

    AuthResponse refresh(TokenRequest tokenRequest);

    UserResponse registerAdmin(AdminRegisterRequest request);
}
