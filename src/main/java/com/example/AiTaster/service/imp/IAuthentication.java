package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.ClientRegisterRequest;
import com.example.AiTaster.dto.request.ExpertRegisterRequest;
import com.example.AiTaster.dto.request.LoginRequest;
import com.example.AiTaster.dto.request.RegisterRequest;
import com.example.AiTaster.dto.response.ClientProfileResponse;
import com.example.AiTaster.dto.response.ExpertProfileResponse;

public interface IAuthentication {
    ClientProfileResponse registerClient(ClientRegisterRequest request);

    ExpertProfileResponse registerExpert(ExpertRegisterRequest request);

    UserResponse login(LoginRequest loginRequest);


}
