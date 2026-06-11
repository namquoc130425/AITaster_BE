package com.example.AiTaster.dto.request.ExpertProduct;

import com.example.AiTaster.constant.ServiceStatus;
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

    List<Long> skillIds;

    BigDecimal feeFrom;

    BigDecimal feeTo;


}
