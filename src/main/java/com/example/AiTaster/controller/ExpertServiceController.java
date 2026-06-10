package com.example.AiTaster.controller;

import com.example.AiTaster.constant.ServiceStatus;
import com.example.AiTaster.dto.request.ExpertServiceRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.ExpertServiceResponse;
import com.example.AiTaster.service.ExpertProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/expert-Service")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class ExpertServiceController {
    @Autowired
    ExpertProductService expertProductService;

    @PostMapping("/Creatservice")
    public ResponseEntity<APIResponse<ExpertServiceResponse>> creatAiservice(@RequestBody @Valid ExpertServiceRequest expertServiceRequest) {
        ExpertServiceResponse responses = expertProductService.CreatService(expertServiceRequest);
        return ResponseEntity.ok(APIResponse.response(201, "Create job post successfully", responses));
    }

    // EXPERT: update bài đăng của mình
    @PutMapping("/{serviceId}")
    public ResponseEntity<APIResponse<ExpertServiceResponse>> updateService(
            @PathVariable Long serviceId,
            @RequestBody @Valid ExpertServiceRequest expertServiceRequest
    ) {
        ExpertServiceResponse response = expertProductService.updateService(serviceId, expertServiceRequest);

        return ResponseEntity.ok(
                APIResponse.response(200, "Update AI service successfully", response)
        );
    }

    // EXPERT xóa mềm bài đăng của mình
    @DeleteMapping("/{serviceId}")
    public ResponseEntity<APIResponse<Void>> deleteService(
            @PathVariable Long serviceId
    ) {
        expertProductService.deleteService(serviceId);

        return ResponseEntity.ok(
                APIResponse.response(200, "Delete AI service successfully", null)
        );
    }

    // EXPERT xem tất cả bài đăng của mình
    @GetMapping("/my")
    public ResponseEntity<APIResponse<List<ExpertServiceResponse>>> getAllMyServices() {

        List<ExpertServiceResponse> responses = expertProductService.getAllMyServiceByOpend();

        return ResponseEntity.ok(
                APIResponse.response(200, "Get my AI services successfully", responses)
        );
    }

    // EXPERT xem chi tiết 1 bài đăng của  mình
    @GetMapping("/my/{serviceId}")
    public ResponseEntity<APIResponse<ExpertServiceResponse>> getMyServiceDetail(
            @PathVariable Long serviceId
    ) {
        ExpertServiceResponse response = expertProductService.getMyServiceDetail(serviceId);

        return ResponseEntity.ok(
                APIResponse.response(200, "Get my AI service detail successfully", response)
        );
    }

    // CLIENT xem tất cả bài đăng đang OPEN của toàn hệ thống
    @GetMapping("/public")
    public ResponseEntity<APIResponse<List<ExpertServiceResponse>>> getAllPublicServices() {

        List<ExpertServiceResponse> responses = expertProductService.getAllPublicServices();

        return ResponseEntity.ok(
                APIResponse.response(200, "Get public AI services successfully", responses)
        );
    }

    // CLIENT xem chi tiết 1 bài đăng public
    @GetMapping("/public/{serviceId}")
    public ResponseEntity<APIResponse<ExpertServiceResponse>> getPublicServiceDetail(
            @PathVariable Long serviceId
    ) {
        ExpertServiceResponse response = expertProductService.getPublicServiceDetail(serviceId);

        return ResponseEntity.ok(
                APIResponse.response(200, "Get public AI service detail successfully", response)
        );
    }

    // nếu client mua thì fe truyền status với serviceid veef dde doi trang thaui


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