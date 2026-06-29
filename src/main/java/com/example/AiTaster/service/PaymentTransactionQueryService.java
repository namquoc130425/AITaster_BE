package com.example.AiTaster.service;

import com.example.AiTaster.dto.response.PaymentTransactionResponse;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.entity.UserWallet;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.repository.UserWalletRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentTransactionQueryService {
    private final CurrentUserService currentUserService;
    private final UserWalletRepo userWalletRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;

    public List<PaymentTransactionResponse> getMyWalletTransactions() {
        User user = currentUserService.getCurrentUser();
        UserWallet wallet = userWalletRepo.findByUser(user)
                .orElseThrow(() -> new GlobalException(404, "Wallet not found"));

        return paymentTransactionRepo
                .findBySourceWalletIdOrTargetWalletIdOrderByCreateAtDesc(wallet.getUserWalletId(), wallet.getUserWalletId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PaymentTransactionResponse toResponse(PaymentTransaction transaction) {
        return PaymentTransactionResponse.builder()
                .paymentTransactionId(transaction.getPaymentTransactionId())
                .projectEscrowId(transaction.getProjectEscrowId())
                .expertServiceId(transaction.getExpertServiceId())
                .senderId(transaction.getSenderId())
                .receiverId(transaction.getReceiverId())
                .sourceWalletId(transaction.getSourceWalletId())
                .targetWalletId(transaction.getTargetWalletId())
                .amount(transaction.getAmount())
                .fromAmount(transaction.getFromAmount())
                .receiveAmount(transaction.getReceiveAmount())
                .currency(transaction.getCurrency())
                .transactionType(transaction.getTransactionType())
                .paymentMethod(transaction.getPaymentMethod())
                .paymentStatus(transaction.getPaymentStatus())
                .referenceId(transaction.getReferenceId())
                .paymentReferenceType(transaction.getPaymentReferenceType())
                .providerName(transaction.getProviderName())
                .providerTransactionCode(transaction.getProviderTransactionCode())
                .paymentCode(transaction.getPaymentCode())
                .providerContent(transaction.getProviderContent())
                .description(transaction.getDescription())
                .paidAt(transaction.getPaidAt())
                .expiredAt(transaction.getExpiredAt())
                .createAt(transaction.getCreateAt())
                .updateAt(transaction.getUpdateAt())
                .build();
    }
}
