package com.example.AiTaster.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GoogleLoginUserInfoResponse {
    String supabaseUserId;

    String email;

    String fullName;

    String avatarUrl;
}
