package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.*;
import com.example.AiTaster.dto.response.ClientProfileResponse;
import com.example.AiTaster.dto.response.ExpertProfileResponse;
import com.example.AiTaster.dto.response.LoginResponse;

public interface IAuthentication {
    ClientProfileResponse registerClient(ClientRegisterRequest request);

    ExpertProfileResponse registerExpert(ExpertRegisterRequest request);

    LoginResponse login(LoginRequest loginRequest);

    UserResponse registerAdmin(AdminRegisterRequest request);
}
