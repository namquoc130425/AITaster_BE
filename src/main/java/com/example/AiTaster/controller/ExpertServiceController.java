package com.example.AiTaster.controller;

import com.example.AiTaster.constant.ServiceStatus;
import com.example.AiTaster.dto.request.ExpertProduct.ExpertServiceFillerRequest;
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


    // Lọc, tìm kiếm và phân trang service public.
    @PostMapping("/public/filter")
    public ResponseEntity<APIResponse<PageResponse<ExpertServiceResponse>>> getAllPublicServicesPage(@RequestBody @Valid ExpertServiceFillerRequest expertServiceFillerRequest) {
        PageResponse<ExpertServiceResponse> expertServiceResponse = expertProductService.getAllPublicServicesPage(expertServiceFillerRequest);
      return ResponseEntity.ok(APIResponse.response(200, "get All and Filter and Search Success", expertServiceResponse));
    }


    // Thanh toán AI service bằng ví.
    @PostMapping("/{serviceId}/purchase")
    public ResponseEntity<APIResponse<PaymentTransaction>> purchaseService(
            @PathVariable Long serviceId
    ) {
        PaymentTransaction paymentTransaction = expertServicePurchaseService.purchaseService(serviceId);
        return ResponseEntity.ok(
                APIResponse.response(200, "Purchase service successfully", paymentTransaction)
        );
    }

    // Thanh toán AI service bằng SePay.
    @PostMapping("/{serviceId}/purchase/sepay")
    public ResponseEntity<APIResponse<SepayPurchasePaymentResponse>> createServiceSepayPayment(
            @PathVariable Long serviceId
    ) {
        SepayPurchasePaymentResponse response =
                expertServicePurchaseService.createServiceSepayPayment(serviceId);

        return ResponseEntity.ok(
                APIResponse.response(200, "SePay service payment created", response)
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
                        "Create service successfully",
                        response
                )
        );
    }

    // Expert cập nhật bài đăng của mình.
    @PutMapping(value = "/{serviceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<ExpertServiceResponse>> updateService(
            @PathVariable Long serviceId,
            @ModelAttribute @Valid ExpertServiceRequest expertServiceRequest
    ) {
        ExpertServiceResponse response = expertProductService.updateService(serviceId, expertServiceRequest);

        return ResponseEntity.ok(
                APIResponse.response(200, "Update AI service successfully", response)
        );
    }

    // Expert xóa mềm bài đăng của mình.
    @DeleteMapping("/{serviceId}")
    public ResponseEntity<APIResponse<Void>> deleteService(
            @PathVariable Long serviceId
    ) {
        expertProductService.deleteService(serviceId);

        return ResponseEntity.ok(
                APIResponse.response(200, "Delete AI service successfully", null)
        );
    }

    // Expert xem tất cả bài đăng của mình.
    @GetMapping("/my")
    public ResponseEntity<APIResponse<List<ExpertServiceResponse>>> getAllMyServices() {

        List<ExpertServiceResponse> responses = expertProductService.getAllMyServiceByOpend();

        return ResponseEntity.ok(
                APIResponse.response(200, "Get my AI services successfully", responses)
        );
    }

    // Expert xem chi tiết một bài đăng của mình.
    @GetMapping("/my/{serviceId}")
    public ResponseEntity<APIResponse<ExpertServiceResponse>> getMyServiceDetail(
            @PathVariable Long serviceId
    ) {
        ExpertServiceResponse response = expertProductService.getMyServiceDetail(serviceId);

        return ResponseEntity.ok(
                APIResponse.response(200, "Get my AI service detail successfully", response)
        );
    }

    @GetMapping("/client/my")
    public ResponseEntity<APIResponse<List<ExpertServiceResponse>>> getMyPurchasedServices() {
        List<ExpertServiceResponse> responses = expertProductService.getMyPurchasedServices();

        return ResponseEntity.ok(
                APIResponse.response(200, "Get purchased AI services successfully", responses)
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

    // Client xem tất cả bài đăng đang OPEN của toàn hệ thống.
    @GetMapping("/public")
    public ResponseEntity<APIResponse<List<ExpertServiceResponse>>> getAllPublicServices() {

        List<ExpertServiceResponse> responses = expertProductService.getAllPublicServices();

        return ResponseEntity.ok(
                APIResponse.response(200, "Get public AI services successfully", responses)
        );
    }

    // Client xem chi tiết một bài đăng public.
    @GetMapping("/public/{serviceId}")
    public ResponseEntity<APIResponse<ExpertServiceResponse>> getPublicServiceDetail(
            @PathVariable Long serviceId
    ) {
        ExpertServiceResponse response = expertProductService.getPublicServiceDetail(serviceId);

        return ResponseEntity.ok(
                APIResponse.response(200, "Get public AI service detail successfully", response)
        );
    }

    // FE truyền serviceId và status mới để đổi trạng thái service.


    @PatchMapping("/{serviceId}/status")
    public ResponseEntity<APIResponse<ExpertServiceResponse>> changeServiceStatus(
            @PathVariable Long serviceId,
            @RequestParam ServiceStatus serviceStatus
    ) {
        ExpertServiceResponse response = expertProductService.changeServiceStatus(serviceId, serviceStatus);

        return ResponseEntity.ok(
                APIResponse.response(200, "Change AI service status successfully", response)
        );
    }



}
