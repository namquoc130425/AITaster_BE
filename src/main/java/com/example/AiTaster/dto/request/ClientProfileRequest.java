package com.example.AiTaster.dto.request;

import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientProfileRequest {
    private Long userId;
    private String companyName;
    private String contactName;
    private String description;
    private String businessField;
    private String address;
}
