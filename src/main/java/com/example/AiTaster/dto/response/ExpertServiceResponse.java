package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.ServiceStatus;
<<<<<<< HEAD
import com.example.AiTaster.entity.Category;
import com.example.AiTaster.entity.ServiceFile;
import com.example.AiTaster.entity.Skill;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

=======
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
<<<<<<< HEAD
@Data
=======
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
public class ExpertServiceResponse {

    Long serviceId;
    Long expertProfileId;
    Long expertUserId;
    String expertName;
    String expertEmail;
    String expertAvatarUrl;

    String serviceName;

    String serviceDescription;

    BigDecimal serviceFee;

    String currency;

    String serviceImage;

    String videoDemo;

    ServiceStatus serviceStatus;

<<<<<<< HEAD
=======
    String rejectionReason;

    LocalDateTime submittedAt;

    LocalDateTime reviewedAt;

    Long reviewedById;

    String reviewedByName;

    Integer reviewCount;

    BigDecimal rating;

    Integer ratingCount;

    CategoryResponse category;

    List<SkillResponse> skills;

    ServiceFileResponse serviceFileResponse;
<<<<<<< HEAD
=======
    Long paymentTransactionId;

    LocalDateTime receivedAt;

    InvoiceResponse invoice;
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
}
