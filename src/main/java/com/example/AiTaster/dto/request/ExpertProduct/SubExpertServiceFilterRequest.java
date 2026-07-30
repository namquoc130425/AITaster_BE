package com.example.AiTaster.dto.request.ExpertProduct;

import com.example.AiTaster.constant.ServiceStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SubExpertServiceFilterRequest {

    Long categoryId;

    Long expertProfileId;

    List<Long> skillIds;

    BigDecimal feeFrom;

    BigDecimal feeTo;

    @Min(value = 0, message = "minRating must be between 0 and 5")
    @Max(value = 5, message = "minRating must be between 0 and 5")
    Integer minRating;

}
