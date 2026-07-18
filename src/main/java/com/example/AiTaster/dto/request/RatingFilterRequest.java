package com.example.AiTaster.dto.request;

import com.example.AiTaster.constant.RatingSortType;
import com.example.AiTaster.constant.RatingTargetType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingFilterRequest {

    RatingTargetType targetType;

    Long expertServiceId;

    Long expertProfileId;

    Long projectId;

    @Min(value = 1, message = "RATING_INVALID")
    @Max(value = 5, message = "RATING_INVALID")
    Integer minRating;

    @Min(value = 1, message = "RATING_INVALID")
    @Max(value = 5, message = "RATING_INVALID")
    Integer maxRating;

    Boolean hasReview;

    @Builder.Default
    RatingSortType sortType = RatingSortType.NEWEST;

    @Builder.Default
    Integer page = 0;

    @Builder.Default
    Integer size = 10;
}
