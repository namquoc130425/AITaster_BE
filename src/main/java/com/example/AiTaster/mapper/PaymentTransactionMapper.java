package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.response.ProjectPaymentResponse;
import com.example.AiTaster.entity.PaymentTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentTransactionMapper {
    @Mapping(target = "projectId",source = "projectId")
    @Mapping(target = "qrUrl",source = "qrUrl")
      ProjectPaymentResponse toProjectPaymentResponse(PaymentTransaction paymentTransaction, Long projectId, String qrUrl);

}
