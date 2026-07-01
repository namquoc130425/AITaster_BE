package com.example.AiTaster.controller;

import com.example.AiTaster.constant.UserWalletStatus;
import com.example.AiTaster.dto.request.BankAccountOtpVerifyRequest;
import com.example.AiTaster.dto.request.UserBankAccountRequest;
import com.example.AiTaster.dto.request.UserWalletRequest;
import com.example.AiTaster.dto.request.WalletBalanceRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.PaymentTransactionResponse;
import com.example.AiTaster.dto.response.UserBankAccountResponse;
import com.example.AiTaster.dto.response.UserWalletResponse;
import com.example.AiTaster.dto.response.WalletDepositPaymentResponse;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.service.UserBankAccountService;
import com.example.AiTaster.service.UserWalletService;
import com.example.AiTaster.service.payment.WalletDepositService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
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
    private final UserBankAccountService userBankAccountService;

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
    @PreAuthorize("hasRole('ADMIN')")
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

    @GetMapping("/transactions/my")
    public ResponseEntity<APIResponse<List<PaymentTransactionResponse>>>
    getMyTransactions() {

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get wallet transactions successfully",
                        userWalletService.getMyTransactions()
                )
        );
    }

    @GetMapping("/bank-account/my")
    public ResponseEntity<APIResponse<UserBankAccountResponse>>
    getMyBankAccount() {

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get my bank account successfully",
                        userBankAccountService.getMyBankAccount()
                )
        );
    }

    @PostMapping("/bank-account")
    public ResponseEntity<APIResponse<UserBankAccountResponse>>
    requestBankAccountOtp(
            @RequestBody @Valid UserBankAccountRequest request
    ) {

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Bank account OTP sent",
                        userBankAccountService.requestBankAccountOtp(request)
                )
        );
    }

    @PostMapping("/bank-account/verify")
    public ResponseEntity<APIResponse<UserBankAccountResponse>>
    verifyBankAccountOtp(
            @RequestBody @Valid BankAccountOtpVerifyRequest request
    ) {

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Bank account verified",
                        userBankAccountService.verifyBankAccountOtp(request)
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
    // Bước này chỉ lưu requestWithdrawal và amountRequestWithdrawal.
    // Chưa trừ ví cho đến khi admin xác nhận đã chuyển khoản thủ công.
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/withdraw/requests")
    public ResponseEntity<APIResponse<List<UserWalletResponse>>>
    getWithdrawalRequests() {

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get withdrawal requests successfully",
                        userWalletService.getWithdrawalRequests()
                )
        );
    }

    // Admin xác nhận đã chuyển khoản thủ công.
    // Service dùng amountRequestWithdrawal đã lưu, trừ ví,
    // và tạo transaction USER_WITHDRAW SUCCESS.
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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{walletId}/withdraw/reject")
    public ResponseEntity<APIResponse<UserWalletResponse>>
    rejectWithdraw(
            @PathVariable Long walletId
    ) {

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Withdrawal rejected",
                        userWalletService.rejectWithdraw(walletId)
                )
        );
    }

    @PatchMapping("/{walletId}/status")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
