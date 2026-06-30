package com.example.AiTaster.service.payment.sepay;

import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.ServiceStatus;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.ExpertServiceRepo;
import com.example.AiTaster.service.CurrentUserService;
import com.example.AiTaster.service.MoneyMovementService;
import com.example.AiTaster.service.PlatformFeeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ExpertServicePurchaseService {
    private final ExpertServiceRepo expertServiceRepo;
    private final ClientProfileRepo clientProfileRepo;
    private final CurrentUserService currentUserService;
    private final MoneyMovementService moneyMovementService;
    private final PlatformFeeCalculator platformFeeCalculator;

    // Client dùng ví nội bộ để mua ExpertService
    // Luồng: ví client → ví expert (trừ phí sàn → ví admin)
    @Transactional
    public PaymentTransaction purchaseService(Long serviceId) {
        User user = currentUserService.getCurrentUser();

        ClientProfile clientProfile = clientProfileRepo.findByUser(user).orElseThrow(() -> new GlobalException(403, "Only client can purchase service"));
        ExpertService expertService = expertServiceRepo.findById(serviceId).orElseThrow(() -> new GlobalException(404, "Expert service not found"));

        if(!expertService.getServiceStatus().equals(ServiceStatus.OPEN)) {
            throw new GlobalException(400, "Service is not available");
        }
        Long clientUserId = clientProfile.getUser().getUserId();
        // expertService → expertProfile → user
        Long expertUserId = expertService.getExpertProfile().getUser().getUserId();


        BigDecimal amount = expertService.getServiceFee();
        BigDecimal balanceAmount = moneyMovementService.calculateFee(amount);

        // Chuyển tiền nội bộ: ví client → ví expert
        // Nếu ví client không đủ tiền → MoneyMovementService tự throw lỗi
        return moneyMovementService.moneyTransactionManagement(
                clientUserId,                            // fromId : client mua -> bị trừ tiền trong ví
                expertUserId,                            // toId : expert mua -> được cộng tiền vào ví
                TransactionType.EXPERT_SERVICE_PURCHASE, // nghiệp vụ client mua sản phẩm có sẵn
                serviceId,                               // referenceId: id của service
                PaymentReferenceType.EXPERT_SERVICE,     // mua service
                "Purchase service " + serviceId + ": " + expertService.getServiceName(),
                amount,
                balanceAmount ,
                null

        );
    }
}
