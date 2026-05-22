package com.example.AiTaster.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientProfileResponse {
    Long clientProfileId;
    Long userId;
    String companyName;
    String contactName;
    String description;
    String businessField;
    String address;
    LocalDateTime createAt;
    LocalDateTime updateAt;
}
