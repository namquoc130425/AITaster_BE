package com.example.AiTaster.dto.request.JobPost;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SubJobPostFilterRequest {
    List<Long> skillIds;

    @JsonAlias({"minBudget", "budgetMin", "minPrice", "priceFrom"})
    BigDecimal budgetFrom;

    @JsonAlias({"maxBudget", "budgetMax", "maxPrice", "priceTo"})
    BigDecimal budgetTo;
}
