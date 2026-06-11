package com.example.AiTaster.dto.request;

import com.example.AiTaster.constant.ServiceStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ExpertServiceRequest {




    @NotBlank(message = "FIELD_REQUIRED")
    String serviceName;
    @NotBlank(message = "FIELD_REQUIRED")
    String serviceDescription;


    BigDecimal serviceFee;


    String serviceImage;


    String videoDemo;

    Long selectedCategoryId;

    List<Long> selectedSkillIds;


}
