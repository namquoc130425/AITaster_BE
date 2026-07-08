package com.example.AiTaster.controller;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.constant.Role;
import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.dto.request.AdminRegisterRequest;
import com.example.AiTaster.dto.request.ClientRegisterRequest;
import com.example.AiTaster.dto.request.ExpertRegisterRequest;
import com.example.AiTaster.dto.request.LoginRequest;
import com.example.AiTaster.dto.request.TokenRequest;

import com.example.AiTaster.dto.response.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
// Dùng Swagger cần cấu hình security requirement.
@CrossOrigin("*")
@SecurityRequirement(name = "api")
// Controller chứa API về authentication.
public class AuthController {
    @Autowired
    AuthenticationService authenticationService;
    // API đăng ký client.
    @PostMapping("/register/client")
    public ResponseEntity<APIResponse<ClientProfileResponse>> register (@RequestBody @Valid ClientRegisterRequest request ) {
             ClientProfileResponse response = authenticationService.registerClient(request);
            return ResponseEntity.status(201).body
                    (APIResponse.response(201,"Register sucessfully",response)
                    );

    }

    @PostMapping("/register/expert")
    public ResponseEntity<APIResponse<ExpertProfileResponse>> registerExpert(@Valid @RequestBody ExpertRegisterRequest expertRegisterRequest
    ){
        ExpertProfileResponse response = authenticationService.registerExpert(expertRegisterRequest);
        return ResponseEntity.status(201).body
                (APIResponse.response(201,"Register Expert sucessfully",response)
                );
    }
    // API đăng nhập.
    @PostMapping("login")
    public ResponseEntity<APIResponse<AuthenticationResponse>> login (@RequestBody @Valid LoginRequest request) {
        AuthenticationResponse response = authenticationService.login(request);
        return ResponseEntity.status(201).body
                (APIResponse.response(201,"Login sucessfully",response)
                );
    }

    @PostMapping("/refresh")
    @Operation(summary = "refresh token")
    public ResponseEntity<APIResponse<AuthResponse>> refresh(@RequestBody @Valid TokenRequest request){
        AuthResponse response = authenticationService.refresh(request);
        return ResponseEntity.ok(APIResponse.response(200, "Refresh token sucessfully", response));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<APIResponse<UserResponse>> registerAdmin(@RequestBody AdminRegisterRequest request) {
        UserResponse response= authenticationService.registerAdmin(request);
        return ResponseEntity.status(201).body(APIResponse.response(201,"Register Admin sucessfully",response));
    }
}
