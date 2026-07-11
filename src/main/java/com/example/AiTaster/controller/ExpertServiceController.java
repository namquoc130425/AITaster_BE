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
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
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
                        "Lấy và lọc dịch vụ AI công khai thành công",
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
                        "Mua dịch vụ AI thành công",
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
                        "Tạo thanh toán SePay cho dịch vụ AI thành công",
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
                        "Gửi dịch vụ AI chờ duyệt thành công",
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
                        "Cập nhật dịch vụ AI thành công",
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
                        "Gửi lại dịch vụ AI đã bị từ chối thành công",
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
                        "Duyệt dịch vụ AI thành công",
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
                        "Từ chối dịch vụ AI thành công",
                        response
                )
        );
    }

    @GetMapping("/admin/review-queue")
    public ResponseEntity<APIResponse<List<ExpertServiceResponse>>> getReviewQueueServices() {
        List<ExpertServiceResponse> response =
                expertProductService.getReviewQueueServices();

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Lấy danh sách dịch vụ AI chờ duyệt thành công",
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
                        "Xóa dịch vụ AI thành công",
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
                        "Lấy dịch vụ AI của tôi thành công",
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
                        "Lấy chi tiết dịch vụ AI của tôi thành công",
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
                        "Lấy dịch vụ AI đã mua thành công",
                        responses
                )
        );
    }

    @GetMapping("/{serviceId}/files/{serviceFileId}/download")
    public ResponseEntity<Resource> downloadServiceFile(
            @PathVariable Long serviceId,
            @PathVariable Long serviceFileId,
            @RequestParam(defaultValue = "product") String kind
    ) {
        ExpertProductService.ServiceFileDownload download =
                expertProductService.downloadServiceFile(serviceId, serviceFileId, kind);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .contentLength(download.contentLength())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(download.fileName(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(download.resource());
    }

    @GetMapping("/public")
    public ResponseEntity<APIResponse<List<ExpertServiceResponse>>> getAllPublicServices() {
        List<ExpertServiceResponse> responses =
                expertProductService.getAllPublicServices();

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Lấy dịch vụ AI công khai thành công",
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
                        "Lấy chi tiết dịch vụ AI công khai thành công",
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
                        "Lấy dịch vụ AI nháp thành công",
                        response
                )
        );
    }
}
