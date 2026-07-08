package com.example.AiTaster.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ClientProfileResponse {
    Long clientProfileId;
    String companyName;
    String contactName;
    String description;
    String businessField;
    String address;
    LocalDateTime createAt;
    LocalDateTime updateAt;
}
