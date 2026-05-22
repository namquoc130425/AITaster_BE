package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.LoginRequest;
import com.example.AiTaster.dto.request.RegisterRequest;

public interface IAuthentication {
    UserResponse register(RegisterRequest registerRequest);
    UserResponse login(LoginRequest loginRequest);


}
