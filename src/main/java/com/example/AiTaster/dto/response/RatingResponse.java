package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.RatingTargetType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RatingResponse {

    Long ratingId;

    Long clientProfileId;

    String clientName;

    String clientAvatarUrl;

    Long expertProfileId;

    String expertName;

    Long expertServiceId;

    String expertServiceName;

    Long projectId;

    String projectTitle;

    RatingTargetType targetType;

    Integer rating;

    String review;

    LocalDateTime createAt;

    LocalDateTime updateAt;
}
