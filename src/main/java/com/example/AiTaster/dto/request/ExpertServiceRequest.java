package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
    @Size(min = 5, max = 100, message = "Tên dịch vụ phải có từ 5 đến 100 ký tự")
    String serviceName;

    @NotBlank(message = "FIELD_REQUIRED")
    @Size(min = 20, max = 2000, message = "Mô tả dịch vụ phải có từ 20 đến 2.000 ký tự")
    String serviceDescription;

    @NotNull(message = "SERVICE_FEE_INVALID")
    @DecimalMin(value = "10000", message = "SERVICE_FEE_INVALID")
    @DecimalMax(value = "1000000000", message = "Phí dịch vụ không được vượt quá 1.000.000.000 VND")
    BigDecimal serviceFee;

    String serviceImage;

    String videoDemo;

    @NotNull(message = "SERVICE_CATEGORY_REQUIRED")
    @Positive(message = "SERVICE_CATEGORY_REQUIRED")
    Long selectedCategoryId;

    @NotEmpty(message = "SERVICE_SKILL_REQUIRED")
    List<@Positive(message = "Kỹ năng đã chọn không hợp lệ") Long> selectedSkillIds;

    MultipartFile docFile;

    MultipartFile sourceFile;
}
