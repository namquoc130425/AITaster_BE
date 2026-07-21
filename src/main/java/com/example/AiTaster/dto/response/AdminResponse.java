package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminResponse {

    Long userId;

<<<<<<< HEAD
=======
    String username;

>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
    String email;

    String fullName;

    String avatarUrl;

    String phone;

    Role role;

    UserStatus userStatus;

    LocalDateTime createAt;

    LocalDateTime updateAt;
<<<<<<< HEAD
}
=======
}
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
