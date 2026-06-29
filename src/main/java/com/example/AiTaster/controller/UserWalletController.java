package com.example.AiTaster.controller;

import com.example.AiTaster.constant.UserWalletStatus;
import com.example.AiTaster.dto.request.UserBankAccountRequest;
import com.example.AiTaster.dto.request.UserWalletRequest;
import com.example.AiTaster.dto.request.WalletBalanceRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.ProjectPaymentResponse;
import com.example.AiTaster.dto.response.PaymentTransactionResponse;
import com.example.AiTaster.dto.response.UserBankAccountResponse;
import com.example.AiTaster.dto.response.UserWalletResponse;
import com.example.AiTaster.service.PaymentTransactionQueryService;
import com.example.AiTaster.service.UserBankAccountService;
import com.example.AiTaster.service.UserWalletService;
import com.example.AiTaster.service.WalletDepositService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private final PaymentTransactionQueryService paymentTransactionQueryService;

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

    @PatchMapping("/{walletId}/deposit")
    public ResponseEntity<APIResponse<UserWalletResponse>>
    deposit(
            @PathVariable Long walletId,
            @RequestBody WalletBalanceRequest request
    ) {

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Deposit success",
                        userWalletService.deposit(
                                walletId,
                                request.getAmount()
                        )
                )
        );
    }

    @PostMapping("/{walletId}/deposit/sepay")
    public ResponseEntity<APIResponse<ProjectPaymentResponse>>
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

    @PatchMapping("/{walletId}/withdraw")
    public ResponseEntity<APIResponse<UserWalletResponse>>
    withdraw(
            @PathVariable Long walletId,
            @RequestBody WalletBalanceRequest request
    ) {

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Withdraw success",
                        userWalletService.withdraw(
                                walletId,
                                request.getAmount()
                        )
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

    @GetMapping("/transactions/my")
    public ResponseEntity<APIResponse<List<PaymentTransactionResponse>>> getMyTransactions() {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get wallet transactions successfully",
                        paymentTransactionQueryService.getMyWalletTransactions()
                )
        );
    }

    @GetMapping("/bank-account/my")
    public ResponseEntity<APIResponse<UserBankAccountResponse>> getMyBankAccount() {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get bank account successfully",
                        userBankAccountService.getMyDefaultBankAccount()
                )
        );
    }

    @PostMapping("/bank-account")
    public ResponseEntity<APIResponse<UserBankAccountResponse>> integrateBankAccount(
            @RequestBody UserBankAccountRequest request
    ) {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Bank account verified",
                        userBankAccountService.integrate(request)
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
