package com.example.AiTaster.dto.request.AdminDashboard;

import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubAdminRecentAccountFilterRequest {

    Role role;

    UserStatus userStatus;


     // null  -> mặc định 30 phút
     // <= 0  -> lấy tất cả account, không filter theo thời gian
     // > 0   -> lấy account tạo trong số phút gần nhất
    Integer minutes;
}