package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.ForgotPasswordRequest;
import com.example.AiTaster.dto.request.ResetPasswordRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.service.ForgotPasswordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class ForgotPasswordController {

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @PostMapping("/forgot-password")
    public APIResponse<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        forgotPasswordService.forgotPassword(request);

        return APIResponse.response(
                200,
                "Gửi OTP đến email thành công",
                null
        );
    }

    @PostMapping("/reset-password")
    public APIResponse<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        forgotPasswordService.resetPassword(request);

        return APIResponse.response(
                200,
                "Đặt lại mật khẩu thành công",
                null
        );
    }
}
