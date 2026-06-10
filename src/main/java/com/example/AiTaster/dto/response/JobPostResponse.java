package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.JobpostStatus;
import com.example.AiTaster.entity.ClientProfile;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter // Tự sinh getter
@Setter // Tự sinh setter
@NoArgsConstructor // Constructor rỗng
@AllArgsConstructor // Constructor đầy đủ
@Builder // Cho phép build response
@FieldDefaults(level = AccessLevel.PRIVATE) // Field mặc định private
public class JobPostResponse {

    Long jobPostId;

    Long clientId;

    String title;

    String requirementDescription;

    String businessGoal;

    String mainFeatures;


    BigDecimal budgets;

    String timeLine;

    JobpostStatus jobPostStatus;
    List<SkillResponse> skills;

   // LocalDateTime createAt;

   // LocalDateTime updateAt;
}
