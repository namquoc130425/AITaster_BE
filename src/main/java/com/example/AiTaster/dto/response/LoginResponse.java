package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginResponse {

    Long userId;
    String email;
    String fullName;
    String username;
    String phone;
    String avatarUrl;
    Role role;
    UserStatus userStatus;
    String accessToken;

    ClientProfileInfo clientProfile;
    ExpertProfileInfo expertProfile;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ClientProfileInfo {
        Long clientProfileId;
        String companyName;
        String contactName;
        String description;
        String businessField;
        String address;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ExpertProfileInfo {
        Long expertProfileId;
        String bio;
        String category;
        String skills;
        String yearsOfExperience;
        String portfolioUrl;
        BigDecimal rating;
        Integer completedProjects;
    }
}