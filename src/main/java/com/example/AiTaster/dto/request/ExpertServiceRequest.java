package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
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

    MultipartFile docFile;

    MultipartFile sourceFile;
}