package com.example.AiTaster.controller;

import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.LoginRequest;
import com.example.AiTaster.dto.request.RegisterRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.service.AuthenticationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
// dùng swagger phải co hai tk nay
@CrossOrigin("*")
@SecurityRequirement(name = "api")
// hàm chứa API  về Authentication
public class AuthController {
    @Autowired
    AuthenticationService authenticationService;
    // tạo API register
    @PostMapping("register")
    public ResponseEntity<APIResponse<UserResponse>> register (@RequestBody @Valid RegisterRequest request) {
        UserResponse response = authenticationService.register(request);
        return ResponseEntity.status(201).body
                (APIResponse.response(201,"Register sucessfully",response)
                );
    }
    // API login
    @PostMapping("login")
    public ResponseEntity<APIResponse<UserResponse>> login (@RequestBody @Valid LoginRequest request) {
        UserResponse response = authenticationService.login(request);
        return ResponseEntity.status(201).body
                (APIResponse.response(201,"Login sucessfully",response)
                );
    }
}
