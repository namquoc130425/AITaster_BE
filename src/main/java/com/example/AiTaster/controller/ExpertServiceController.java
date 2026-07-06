package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.ExpertProduct.ExpertServiceFillerRequest;
import com.example.AiTaster.dto.request.ExpertServiceRejectRequest;
import com.example.AiTaster.dto.request.ExpertServiceRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.ExpertServiceResponse;
import com.example.AiTaster.dto.response.PageResponse;
import com.example.AiTaster.dto.response.SepayPurchasePaymentResponse;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.service.ExpertProductService;
import com.example.AiTaster.service.payment.ExpertServicePurchaseService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expert-Service")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class ExpertServiceController {

    @Autowired
    ExpertProductService expertProductService;

    @Autowired
    ExpertServicePurchaseService expertServicePurchaseService;

    @PostMapping("/public/filter")
    public ResponseEntity<APIResponse<PageResponse<ExpertServiceResponse>>> getAllPublicServicesPage(
            @RequestBody @Valid ExpertServiceFillerRequest expertServiceFillerRequest
    ) {
        PageResponse<ExpertServiceResponse> response =
                expertProductService.getAllPublicServicesPage(expertServiceFillerRequest);

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "get All and Filter and Search Success",
                        response
                )
        );
    }

    @PostMapping("/{serviceId}/purchase")
    public ResponseEntity<APIResponse<PaymentTransaction>> purchaseService(
            @PathVariable Long serviceId
    ) {
        PaymentTransaction paymentTransaction =
                expertServicePurchaseService.purchaseService(serviceId);

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Purchase service successfully",
                        paymentTransaction
                )
        );
    }

    @PostMapping("/{serviceId}/purchase/sepay")
    public ResponseEntity<APIResponse<SepayPurchasePaymentResponse>> createServiceSepayPayment(
            @PathVariable Long serviceId
    ) {
        SepayPurchasePaymentResponse response =
                expertServicePurchaseService.createServiceSepayPayment(serviceId);

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "SePay service payment created",
                        response
                )
        );
    }

    @PostMapping(
            value = "/Creatservice",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<APIResponse<ExpertServiceResponse>> creatAiservice(
            @ModelAttribute @Valid ExpertServiceRequest request
    ) {
        ExpertServiceResponse response =
                expertProductService.CreatService(request);

        return ResponseEntity.ok(
                APIResponse.response(
                        201,
                        "Create service draft successfully",
                        response
                )
        );
    }

    @PutMapping(
            value = "/{serviceId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<APIResponse<ExpertServiceResponse>> updateService(
            @PathVariable Long serviceId,
            @ModelAttribute @Valid ExpertServiceRequest expertServiceRequest
    ) {
        ExpertServiceResponse response =
                expertProductService.updateService(
                        serviceId,
                        expertServiceRequest
                );

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Update AI service successfully",
                        response
                )
        );
    }

    @PatchMapping("/{serviceId}/resubmit")
    public ResponseEntity<APIResponse<ExpertServiceResponse>> resubmitService(
            @PathVariable Long serviceId
    ) {
        ExpertServiceResponse response =
                expertProductService.resubmitRejectedService(serviceId);

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Resubmit rejected AI service successfully",
                        response
                )
        );
    }

    @PatchMapping("/admin/{serviceId}/accept")
    public ResponseEntity<APIResponse<ExpertServiceResponse>> acceptService(
            @PathVariable Long serviceId
    ) {
        ExpertServiceResponse response =
                expertProductService.acceptService(serviceId);

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Accept AI service successfully",
                        response
                )
        );
    }

    @PatchMapping("/admin/{serviceId}/reject")
    public ResponseEntity<APIResponse<ExpertServiceResponse>> rejectService(
            @PathVariable Long serviceId,
            @RequestBody @Valid ExpertServiceRejectRequest request
    ) {
        ExpertServiceResponse response =
                expertProductService.rejectService(serviceId, request);

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Reject AI service successfully",
                        response
                )
        );
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<APIResponse<Void>> deleteService(
            @PathVariable Long serviceId
    ) {
        expertProductService.deleteService(serviceId);

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Delete AI service successfully",
                        null
                )
        );
    }

    @GetMapping("/my")
    public ResponseEntity<APIResponse<List<ExpertServiceResponse>>> getAllMyServices() {
        List<ExpertServiceResponse> responses =
                expertProductService.getAllMyServiceByOpend();

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get my AI services successfully",
                        responses
                )
        );
    }

    @GetMapping("/my/{serviceId}")
    public ResponseEntity<APIResponse<ExpertServiceResponse>> getMyServiceDetail(
            @PathVariable Long serviceId
    ) {
        ExpertServiceResponse response =
                expertProductService.getMyServiceDetail(serviceId);

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get my AI service detail successfully",
                        response
                )
        );
    }

    @GetMapping("/client/my")
    public ResponseEntity<APIResponse<List<ExpertServiceResponse>>> getMyPurchasedServices() {
        List<ExpertServiceResponse> responses =
                expertProductService.getMyPurchasedServices();

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get purchased AI services successfully",
                        responses
                )
        );
    }

    @GetMapping("/public")
    public ResponseEntity<APIResponse<List<ExpertServiceResponse>>> getAllPublicServices() {
        List<ExpertServiceResponse> responses =
                expertProductService.getAllPublicServices();

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get public AI services successfully",
                        responses
                )
        );
    }

    @GetMapping("/public/{serviceId}")
    public ResponseEntity<APIResponse<ExpertServiceResponse>> getPublicServiceDetail(
            @PathVariable Long serviceId
    ) {
        ExpertServiceResponse response =
                expertProductService.getPublicServiceDetail(serviceId);

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get public AI service detail successfully",
                        response
                )
        );
    }

    @GetMapping("/admin/drafts")
    public ResponseEntity<APIResponse<List<ExpertServiceResponse>>> getDraftServices() {
        List<ExpertServiceResponse> response =
                expertProductService.getDraftServices();

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get draft AI services successfully",
                        response
                )
        );
    }
}