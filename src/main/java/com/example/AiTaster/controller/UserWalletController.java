package com.example.AiTaster.controller;

import com.example.AiTaster.constant.UserWalletStatus;
import com.example.AiTaster.dto.request.UserWalletRequest;
import com.example.AiTaster.dto.request.WalletBalanceRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.UserWalletResponse;
import com.example.AiTaster.dto.response.WalletDepositPaymentResponse;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.service.UserWalletService;
import com.example.AiTaster.service.payment.WalletDepositService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class UserWalletController {

    private final UserWalletService userWalletService;
    private final WalletDepositService walletDepositService;

    @PostMapping
    public ResponseEntity<APIResponse<UserWalletResponse>>
    createWallet(
            @RequestBody UserWalletRequest request
    ) {

        return ResponseEntity.ok(
                APIResponse.response(
                        201,
                        "Wallet created",
                        userWalletService.createWallet(request)
                )
        );
    }

    @GetMapping("/my")
    public ResponseEntity<APIResponse<UserWalletResponse>>
    getMyWallet() {

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Success",
                        userWalletService.getMyWallet()
                )
        );
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<APIResponse<UserWalletResponse>>
    getWallet(
            @PathVariable Long walletId
    ) {

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Success",
                        userWalletService.getWalletById(walletId)
                )
        );
    }

    @GetMapping
    public ResponseEntity<APIResponse<List<UserWalletResponse>>>
    getAllWallets() {

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Success",
                        userWalletService.getAllWallets()
                )
        );
    }

    @PostMapping("/{walletId}/deposit/sepay")
    public ResponseEntity<APIResponse<WalletDepositPaymentResponse>>
    createSepayDeposit(
            @PathVariable Long walletId,
            @RequestBody WalletBalanceRequest request
    ) {

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "SePay wallet deposit created",
                        walletDepositService.createWalletDeposit(walletId, request)
                )
        );
    }

    // User gửi yêu cầu rút tiền.
    // Bước này chỉ lưu yêu cầu: requestWithdrawal = true, amountRequestWithdrawal = amount.
    // Chưa trừ tiền trong ví và chưa tạo PaymentTransaction SUCCESS.
    @PostMapping("/{walletId}/withdraw/request")
    public ResponseEntity<APIResponse<UserWalletResponse>>
    requestWithdraw(
            @PathVariable Long walletId,
            @RequestBody WalletBalanceRequest request
    ) {

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Withdrawal requested",
                        userWalletService.requestWithdraw(
                                walletId,
                                request.getAmount()
                        )
                )
        );
    }

    // Admin duyệt yêu cầu rút tiền.
    // Admin không nhập lại amount, service sẽ lấy amountRequestWithdrawal đã lưu trong ví.
    // Bước này mới gọi MoneyMovementService để trừ ví user và tạo transaction USER_WITHDRAW SUCCESS.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{walletId}/withdraw/approve")
    public ResponseEntity<APIResponse<PaymentTransaction>>
    approveWithdraw(
            @PathVariable Long walletId
    ) {

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Withdrawal approved",
                        userWalletService.approveWithdraw(walletId)
                )
        );
    }

    @PatchMapping("/{walletId}/status")
    public ResponseEntity<APIResponse<UserWalletResponse>>
    changeStatus(
            @PathVariable Long walletId,
            @RequestParam UserWalletStatus status
    ) {

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Status changed",
                        userWalletService.changeStatus(
                                walletId,
                                status
                        )
                )
        );
    }

    @DeleteMapping("/{walletId}")
    public ResponseEntity<APIResponse<Void>>
    deleteWallet(
            @PathVariable Long walletId
    ) {

        userWalletService.deleteWallet(walletId);

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Wallet deleted",
                        null
                )
        );
    }
}
