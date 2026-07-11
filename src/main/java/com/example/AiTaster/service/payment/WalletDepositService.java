package com.example.AiTaster.service.payment;


import com.example.AiTaster.constant.PaymentReferenceType;

import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.constant.UserWalletStatus;
import com.example.AiTaster.dto.request.WalletBalanceRequest;
import com.example.AiTaster.dto.response.SepayCheckoutFormResponse;
import com.example.AiTaster.dto.response.WalletDepositPaymentResponse;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.entity.UserWallet;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.PaymentTransactionMapper;

import com.example.AiTaster.repository.UserWalletRepo;
import com.example.AiTaster.service.CurrentUserService;
import com.example.AiTaster.service.PendingPaymentService;
import com.example.AiTaster.service.SepayGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class WalletDepositService {

    private final CurrentUserService currentUserService;
    private final UserWalletRepo userWalletRepo;

    private final PaymentTransactionMapper paymentTransactionMapper;
    private final SepayGateway sepayGateway;
    private final PendingPaymentService pendingPaymentService;

    @Transactional
    public WalletDepositPaymentResponse createWalletDeposit(Long userWalletId, WalletBalanceRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        UserWallet userWallet = findUserWalletById(userWalletId);

        checkWalletDeposit(userWallet, currentUser, request);

        PaymentTransaction paymentTransaction = pendingPaymentService.createPendingPaymentTransaction(
                currentUser.getUserId(),
                currentUser.getUserId(),
                null,
                userWallet.getUserWalletId(),
                 null,
                null,
                TransactionType.USER_DEPOSIT,
                userWallet.getUserWalletId(),
                PaymentReferenceType.USER_WALLET
                , request.getAmount(),
                "Nạp ví qua SePay",
                LocalDateTime.now().plusHours(1)
        );
        SepayCheckoutFormResponse checkoutForm = sepayGateway.createCheckoutForm(paymentTransaction);
        return paymentTransactionMapper.toWalletDepositPaymentResponse(paymentTransaction, checkoutForm);
    }

    private void checkWalletDeposit(UserWallet userWallet, User currentUser, WalletBalanceRequest request) {
        if (request == null || request.getAmount() == null) {
            throw new GlobalException(400, "Số tiền là bắt buộc");
        }

        if (!userWallet.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new GlobalException(403, "Bạn không phải chủ sở hữu ví này");
        }

        if (userWallet.getStatus() != UserWalletStatus.ACTIVE) {
            throw new GlobalException(400, "Ví chưa hoạt động");
        }

        if (!"VND".equalsIgnoreCase(userWallet.getCurrency())) {
            throw new GlobalException(400, "Đơn vị tiền tệ của ví không được hỗ trợ");
        }

        if (request.getAmount().compareTo(new BigDecimal("10000")) < 0) {
            throw new GlobalException(400, "Số tiền nạp tối thiểu là 10.000đ");
        }

        if (request.getAmount().compareTo(new BigDecimal("50000000")) > 0) {
            throw new GlobalException(400, "Số tiền nạp tối đa là 50.000.000đ");
        }

        if (request.getAmount().stripTrailingZeros().scale() > 0) {
            throw new GlobalException(400, "Số tiền phải là số nguyên");
        }
    }


    private UserWallet findUserWalletById(Long userWalletId) {
        return userWalletRepo.findByUserWalletId(userWalletId)
                .orElseThrow(() -> new GlobalException(404, "Không tìm thấy ví người dùng"));
    }
}
