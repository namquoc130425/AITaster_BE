package com.example.AiTaster.dto.response;

import com.example.AiTaster.dto.UserResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ExpertProfileResponse {
    Long expertProfileId;
    String bio;
    String category;
    String skills;
    Integer yearOfExperience;
    String portfolioUrl;
    BigDecimal rating;
    Integer completedProjects;
    LocalDateTime createAt;
    LocalDateTime updateAt;
    ExpertVerificationResponse verification;
}
