package com.example.AiTaster.controller;

import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.service.PendingPaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class PaymentController {
    private final PendingPaymentService pendingPaymentService;

    @PostMapping("/sepay/{paymentCode}/cancel")
    public ResponseEntity<APIResponse<Void>> cancelSepayPayment(
            @PathVariable String paymentCode
    ) {
        pendingPaymentService.cancelPendingPayment(paymentCode);

        return ResponseEntity.ok(
                APIResponse.response(200, "Đã hủy giao dịch SePay", null)
        );
    }
}
