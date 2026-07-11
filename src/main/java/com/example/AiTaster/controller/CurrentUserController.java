package com.example.AiTaster.controller;

import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.CurrentUserResponse;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.mapper.CurrentUserResponseMapper;
import com.example.AiTaster.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class CurrentUserController {
    private final CurrentUserService currentUserService;
    private final CurrentUserResponseMapper currentUserResponseMapper;

    @GetMapping("/me")
    public ResponseEntity<APIResponse<CurrentUserResponse>> getCurrentUser() {
        User user = currentUserService.getCurrentUser();
        CurrentUserResponse response = currentUserResponseMapper.toResponse(user);

        return ResponseEntity.ok(APIResponse.response(200, "Lấy người dùng hiện tại thành công", response));
    }
}
