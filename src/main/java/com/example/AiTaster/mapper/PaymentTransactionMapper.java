package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.response.ProjectPaymentResponse;
import com.example.AiTaster.dto.response.SepayCheckoutFormResponse;
import com.example.AiTaster.dto.response.SepayPurchasePaymentResponse;
import com.example.AiTaster.dto.response.WalletDepositPaymentResponse;
import com.example.AiTaster.entity.PaymentTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentTransactionMapper {
    @Mapping(target = "invitationId", source = "invitationId")
    @Mapping(target = "checkoutForm", source = "checkoutForm")
    @Mapping(target = "amount", source = "paymentTransaction.grossAmount")
    ProjectPaymentResponse  toInvitationPaymentResponse(
            PaymentTransaction paymentTransaction,
            Long invitationId,
            SepayCheckoutFormResponse checkoutForm
    );


    @Mapping(target = "walletId", source = "paymentTransaction.targetWalletId")
    @Mapping(target = "userId", source = "paymentTransaction.receiverId")
    @Mapping(target = "checkoutForm", source = "checkoutForm")
    WalletDepositPaymentResponse toWalletDepositPaymentResponse(
            PaymentTransaction paymentTransaction , SepayCheckoutFormResponse checkoutForm
    );


    @Mapping(target = "checkoutForm", source = "checkoutForm")
    @Mapping(target = "amount", source = "paymentTransaction.grossAmount")
    SepayPurchasePaymentResponse toSepayPurchasePaymentResponse(PaymentTransaction paymentTransaction, SepayCheckoutFormResponse checkoutForm);




}
