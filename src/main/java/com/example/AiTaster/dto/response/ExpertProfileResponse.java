package com.example.AiTaster.dto.response;

import com.example.AiTaster.dto.UserResponse;
import com.example.AiTaster.entity.Category;
import com.example.AiTaster.entity.Skill;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ExpertProfileResponse {
    Long expertProfileId;
    String bio;
    Category category;
    List<Skill> skills;
    Integer yearOfExperience;
    String portfolioUrl;
    BigDecimal rating;
    Integer completedProjects;
    LocalDateTime createAt;
    LocalDateTime updateAt;
    ExpertVerificationResponse verification;
}
