package com.example.AiTaster.dto.request.Admin;

import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SubUserFilterRequest {
    Role role;

    UserStatus userStatus;
}
