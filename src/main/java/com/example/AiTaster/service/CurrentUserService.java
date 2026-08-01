package com.example.AiTaster.service;

import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
// hàm check đăng nhập chưa
@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepo userRepo;
//lấy thông tin user hiện tại đang đăng nhập
    public User getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            throw new GlobalException("Bạn chưa đăng nhập");
        }
        String username = authentication.getName(); // lấy email hoặc user name từ jwt
        return userRepo.findByUsername(username).orElseThrow(() ->  new GlobalException("Không tìm thấy người dùng"));
    }

}
