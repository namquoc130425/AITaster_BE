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

    /** Selected whole-star bucket; the API field name is retained for compatibility. */
    @Min(value = 0, message = "Số sao phải từ 0 đến 5")
    @Max(value = 5, message = "Số sao phải từ 0 đến 5")
    Integer minRating;

}
