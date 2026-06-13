package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurrentUserResponse {
    Long userId;
    String email;
    String fullName;
    String username;
    String phone;
    String avatarUrl;
    Role role;
    UserStatus userStatus;
    ClientProfileResponse clientProfile;
    ExpertProfileResponse expertProfile;
}
