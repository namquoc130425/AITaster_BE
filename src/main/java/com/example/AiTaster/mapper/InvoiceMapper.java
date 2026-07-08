package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.response.InvoiceResponse;
import com.example.AiTaster.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
//map từ source sang target, nếu trong target có field chưa được map, thì bỏ qua, không báo lỗi hoặc warning.
public interface  InvoiceMapper {
    @Mapping(target = "clientId", source = "project.invitation.expertApplication.jobpost.clientProfile.user.userId")
    @Mapping(target = "expertId", source = "project.invitation.expertApplication.expertProfile.user.userId")
    @Mapping(target = "projectId", source = "project.projectId")
    @Mapping(target = "projectEscrowId", source = "escrow.projectEscrowId")
    @Mapping(target = "paymentTransactionId", source = "payment.paymentTransactionId")
    @Mapping(target = "currency", source = "payment.currency")
    @Mapping(target = "paymentMethod", source = "payment.paymentMethod")
    Invoices toProjectCompletionInvoice( Project project,
                                         ProjectEscrow escrow,
                                         PaymentTransaction payment
    );
    @Mapping(target = "clientId", source = "payment.senderId")
    @Mapping(target = "expertId", source = "expertService.expertProfile.user.userId")
    @Mapping(target = "paymentTransactionId", source = "payment.paymentTransactionId")
    @Mapping(target = "currency", source = "payment.currency")
    @Mapping(target = "paymentMethod", source = "payment.paymentMethod")
    Invoices toAiServiceInvoice(
            ExpertService expertService,
            PaymentTransaction payment
    );

    InvoiceResponse toInvoiceResponse(Invoices invoice);
}
