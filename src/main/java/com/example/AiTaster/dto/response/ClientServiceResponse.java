package com.example.AiTaster.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientServiceResponse {
    Long clientServiceId;
    Long serviceId;
    Long invoiceId;
    Long paymentTransactionId;
    String serviceName;
    String serviceType;
    String description;
    String serviceFile;
    String videoDemo;
    String instructionFile;
    Integer version;
    LocalDateTime receivedAt;
    LocalDateTime expiredAt;
    LocalDateTime createdAt;
    ExpertServiceResponse expertService;
}
