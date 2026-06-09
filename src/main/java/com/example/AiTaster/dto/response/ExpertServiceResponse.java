package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.ServiceStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ExpertServiceResponse {


    String serviceName;

    String serviceDescription;

    BigDecimal serviceFee;

    String currency;

    String serviceImage;
    String videoDemo;

    ServiceStatus serviceStatus;

    Long selectedCategoryId;

    List<Long> selectedSkillIds;
}
