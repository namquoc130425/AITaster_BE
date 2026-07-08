package com.example.AiTaster.service.payment;

import com.example.AiTaster.constant.NotificationType;
import com.example.AiTaster.constant.ReferenceType;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.repository.UserRepo;
import com.example.AiTaster.service.NotificationService;
import com.example.AiTaster.service.RealtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ExpertServicePurchaseEventService {

    private final UserRepo userRepo;
    private final RealtimeService realtimeService;
    private final NotificationService notificationService;

    public void publishAfterPaymentSuccess(
            Long clientUserId,
            Long expertUserId,
            Long serviceId,
            String serviceName,
            Long sourceWalletId,
            Long targetWalletId,
            BigDecimal expertAmount
    ) {
        runAfterCommit(() -> publishNow(
                clientUserId,
                expertUserId,
                serviceId,
                serviceName,
                sourceWalletId,
                targetWalletId,
                expertAmount
        ));
    }

    private void publishNow(
            Long clientUserId,
            Long expertUserId,
            Long serviceId,
            String serviceName,
            Long sourceWalletId,
            Long targetWalletId,
            BigDecimal expertAmount
    ) {
        User expertUser = findUser(expertUserId);
        String safeServiceName = serviceName == null || serviceName.isBlank()
                ? "AI Service"
                : serviceName;
        String formattedAmount = formatMoney(expertAmount);

        if (expertUser != null) {
            realtimeService.pushUserWalletEvent(
                    expertUser,
                    "WALLET_EXPERT_SERVICE_PURCHASE_RECEIVED",
                    targetWalletId,
                    "AI service payment received: " + formattedAmount
            );
            realtimeService.pushUserDashboardEvent(
                    expertUser,
                    "EXPERT_SERVICE_PURCHASED",
                    ReferenceType.EXPERT_SERVICE,
                    serviceId,
                    "AI service purchased: " + safeServiceName
            );
            notificationService.notify(
                    expertUser,
                    NotificationType.EXPERT_SERVICE,
                    ReferenceType.EXPERT_SERVICE,
                    serviceId,
                    "AI Service purchased",
                    "Client purchased '" + safeServiceName + "'. You received " + formattedAmount + "."
            );
        }

        User clientUser = findUser(clientUserId);

        if (clientUser == null) {
            return;
        }

        realtimeService.pushUserDashboardEvent(
                clientUser,
                "EXPERT_SERVICE_PURCHASED",
                ReferenceType.EXPERT_SERVICE,
                serviceId,
                "AI service purchase completed: " + safeServiceName
        );

        if (sourceWalletId != null) {
            realtimeService.pushUserWalletEvent(
                    clientUser,
                    "WALLET_EXPERT_SERVICE_PURCHASE_PAID",
                    sourceWalletId,
                    "AI service purchase paid: " + safeServiceName
            );
        }
    }

    private User findUser(Long userId) {
        if (userId == null) {
            return null;
        }

        return userRepo.findById(userId).orElse(null);
    }

    private void runAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        action.run();
                    }
                }
        );
    }

    private String formatMoney(BigDecimal amount) {
        BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount;

        return NumberFormat
                .getNumberInstance(Locale.forLanguageTag("vi-VN"))
                .format(safeAmount) + " VND";
    }
}
