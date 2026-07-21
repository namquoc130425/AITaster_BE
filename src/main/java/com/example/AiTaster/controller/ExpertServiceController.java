package com.example.AiTaster.controller;

<<<<<<< HEAD
import com.example.AiTaster.constant.ServiceStatus;
import com.example.AiTaster.dto.request.ExpertProduct.ExpertServiceFillerRequest;
=======
import com.example.AiTaster.dto.request.ExpertProduct.ExpertServiceFillerRequest;
import com.example.AiTaster.dto.request.ExpertServiceRejectRequest;
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
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
<<<<<<< HEAD
=======
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

<<<<<<< HEAD
=======
import java.nio.charset.StandardCharsets;
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
import java.util.List;

@RestController
@RequestMapping("/api/expert-Service")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class ExpertServiceController {
<<<<<<< HEAD
=======

>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
    @Autowired
    ExpertProductService expertProductService;

    @Autowired
    ExpertServicePurchaseService expertServicePurchaseService;

<<<<<<< HEAD

    // Lọc, tìm kiếm và phân trang service public.
    @PostMapping("/public/filter")
    public ResponseEntity<APIResponse<PageResponse<ExpertServiceResponse>>> getAllPublicServicesPage(@RequestBody @Valid ExpertServiceFillerRequest expertServiceFillerRequest) {
        PageResponse<ExpertServiceResponse> expertServiceResponse = expertProductService.getAllPublicServicesPage(expertServiceFillerRequest);
      return ResponseEntity.ok(APIResponse.response(200, "get All and Filter and Search Success", expertServiceResponse));
    }


    // Thanh toán AI service bằng ví.
=======
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

>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
    @PostMapping("/{serviceId}/purchase")
    public ResponseEntity<APIResponse<PaymentTransaction>> purchaseService(
            @PathVariable Long serviceId
    ) {
<<<<<<< HEAD
        PaymentTransaction paymentTransaction = expertServicePurchaseService.purchaseService(serviceId);
        return ResponseEntity.ok(
                APIResponse.response(200, "Purchase service successfully", paymentTransaction)
        );
    }

    // Thanh toán AI service bằng SePay.
=======
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

>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
    @PostMapping("/{serviceId}/purchase/sepay")
    public ResponseEntity<APIResponse<SepayPurchasePaymentResponse>> createServiceSepayPayment(
            @PathVariable Long serviceId
    ) {
        SepayPurchasePaymentResponse response =
                expertServicePurchaseService.createServiceSepayPayment(serviceId);

        return ResponseEntity.ok(
<<<<<<< HEAD
                APIResponse.response(200, "SePay service payment created", response)
        );
    }





=======
                APIResponse.response(
                        200,
                        "SePay service payment created",
                        response
                )
        );
    }

>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
    @PostMapping(
            value = "/Creatservice",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<APIResponse<ExpertServiceResponse>> creatAiservice(
            @ModelAttribute @Valid ExpertServiceRequest request
    ) {
<<<<<<< HEAD

=======
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
        ExpertServiceResponse response =
                expertProductService.CreatService(request);

        return ResponseEntity.ok(
                APIResponse.response(
                        201,
<<<<<<< HEAD
                        "Create service successfully",
=======
                        "Submit AI service for review successfully",
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
                        response
                )
        );
    }

<<<<<<< HEAD
    // Expert cập nhật bài đăng của mình.
    @PutMapping(value = "/{serviceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
=======
    @PutMapping(
            value = "/{serviceId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
    public ResponseEntity<APIResponse<ExpertServiceResponse>> updateService(
            @PathVariable Long serviceId,
            @ModelAttribute @Valid ExpertServiceRequest expertServiceRequest
    ) {
<<<<<<< HEAD
        ExpertServiceResponse response = expertProductService.updateService(serviceId, expertServiceRequest);

        return ResponseEntity.ok(
                APIResponse.response(200, "Update AI service successfully", response)
        );
    }

    // Expert xóa mềm bài đăng của mình.
=======
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

    @GetMapping("/admin/review-queue")
    public ResponseEntity<APIResponse<List<ExpertServiceResponse>>> getReviewQueueServices() {
        List<ExpertServiceResponse> response =
                expertProductService.getReviewQueueServices();

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get AI service review queue successfully",
                        response
                )
        );
    }

>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
    @DeleteMapping("/{serviceId}")
    public ResponseEntity<APIResponse<Void>> deleteService(
            @PathVariable Long serviceId
    ) {
        expertProductService.deleteService(serviceId);

        return ResponseEntity.ok(
<<<<<<< HEAD
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
=======
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

>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
    @GetMapping("/my/{serviceId}")
    public ResponseEntity<APIResponse<ExpertServiceResponse>> getMyServiceDetail(
            @PathVariable Long serviceId
    ) {
<<<<<<< HEAD
        ExpertServiceResponse response = expertProductService.getMyServiceDetail(serviceId);

        return ResponseEntity.ok(
                APIResponse.response(200, "Get my AI service detail successfully", response)
=======
        ExpertServiceResponse response =
                expertProductService.getMyServiceDetail(serviceId);

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Get my AI service detail successfully",
                        response
                )
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
        );
    }

    @GetMapping("/client/my")
    public ResponseEntity<APIResponse<List<ExpertServiceResponse>>> getMyPurchasedServices() {
<<<<<<< HEAD
        List<ExpertServiceResponse> responses = expertProductService.getMyPurchasedServices();

        return ResponseEntity.ok(
                APIResponse.response(200, "Get purchased AI services successfully", responses)
        );
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
=======
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
                        "Get public AI services successfully",
                        responses
                )
        );
    }

>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
    @GetMapping("/public/{serviceId}")
    public ResponseEntity<APIResponse<ExpertServiceResponse>> getPublicServiceDetail(
            @PathVariable Long serviceId
    ) {
<<<<<<< HEAD
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



=======
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
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
}
