package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingRequest {

    @NotNull(message = "FIELD_REQUIRED")
    @Min(value = 1, message = "RATING_INVALID")
    @Max(value = 5, message = "RATING_INVALID")
    Integer rating;

    @Size(max = 2000, message = "REVIEW_INVALID_SIZE")
    String review;
}
