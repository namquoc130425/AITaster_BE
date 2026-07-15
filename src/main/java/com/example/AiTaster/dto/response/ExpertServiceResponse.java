package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.ServiceStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpertServiceResponse {

    Long serviceId;
    Long expertProfileId;
    Long expertUserId;
    String expertName;
    String expertEmail;

    String serviceName;

    String serviceDescription;

    BigDecimal serviceFee;

    String currency;

    String serviceImage;

    String videoDemo;

    ServiceStatus serviceStatus;

    String rejectionReason;

    LocalDateTime submittedAt;

    LocalDateTime reviewedAt;

    Long reviewedById;

    String reviewedByName;

    Integer reviewCount;

    CategoryResponse category;

    List<SkillResponse> skills;

    ServiceFileResponse serviceFileResponse;
    Long paymentTransactionId;

    LocalDateTime receivedAt;

    InvoiceResponse invoice;
}
