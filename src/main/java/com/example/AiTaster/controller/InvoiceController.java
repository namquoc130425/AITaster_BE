package com.example.AiTaster.controller;

import com.example.AiTaster.constant.InvoiceType;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.InvoiceResponse;
import com.example.AiTaster.service.InvoiceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;

    @GetMapping("/my")
    public ResponseEntity<APIResponse<List<InvoiceResponse>>> getMyInvoices(
            @RequestParam(name = "type", required = false) InvoiceType invoiceType
    ) {
        return ResponseEntity.ok(
                APIResponse.response(200, "Lấy hóa đơn của tôi thành công", invoiceService.getMyInvoices(invoiceType)
                )
        );
    }

    @GetMapping("/my/{invoiceId}")
    public ResponseEntity<APIResponse<InvoiceResponse>> getMyInvoiceDetail(
            @PathVariable Long invoiceId
    ) {
        return ResponseEntity.ok(
                APIResponse.response(200, "Lấy chi tiết hóa đơn thành công", invoiceService.getMyInvoiceDetail(invoiceId)
                )
        );
    }
}
