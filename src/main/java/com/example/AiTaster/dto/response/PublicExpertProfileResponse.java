package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.ExpertVerificationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublicExpertProfileResponse {

    Long expertProfileId;

    Long expertUserId;

    String expertName;

    String username;

    String avatarUrl;

    String bio;

    CategoryResponse category;

    List<SkillResponse> skills;

    Integer yearOfExperience;

    String portfolioUrl;

    BigDecimal rating;

    Integer ratingCount;

    Integer completedProjects;

    Long openAiServiceCount;

    ExpertVerificationStatus verificationStatus;

    LocalDateTime createAt;

    LocalDateTime updateAt;
}
