package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.response.ProjectPaymentResponse;
import com.example.AiTaster.dto.response.SepayCheckoutFormResponse;
import com.example.AiTaster.entity.PaymentTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentTransactionMapper {
    @Mapping(target = "invitationId", source = "invitationId")
    @Mapping(target = "checkoutForm", source = "checkoutForm")
    ProjectPaymentResponse toInvitationPaymentResponse(
            PaymentTransaction paymentTransaction,
            Long invitationId,
            SepayCheckoutFormResponse checkoutForm
    );

}
