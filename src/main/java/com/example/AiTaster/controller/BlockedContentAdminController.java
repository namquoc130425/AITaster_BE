package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.BlockedContentRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.entity.BlockedContent;
import com.example.AiTaster.service.BlockedContentAdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Controller trả JSON API.
@RequestMapping("/api/admin/blocked-contents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN được quản lý blacklist.
@SecurityRequirement(name = "api")

public class BlockedContentAdminController {
    private final BlockedContentAdminService service;

    @GetMapping("/getAllBlockContent")
    public ResponseEntity<APIResponse<List<BlockedContent>>> getAll() {
        return ResponseEntity.ok(
                APIResponse.response(200, "Lấy nội dung bị chặn thành công", service.getAll())
        );
    }

    @PostMapping("/createBlockContent")
    public ResponseEntity<APIResponse<BlockedContent>> create(
            @RequestBody @Valid BlockedContentRequest request
    ) {
        return ResponseEntity.status(201).body(
                APIResponse.response(201, "Tạo nội dung bị chặn thành công", service.create(request))
        );
    }

    @PutMapping("/updateBlockConten{id}")
    public ResponseEntity<APIResponse<BlockedContent>> update(
            @PathVariable Long id,
            @RequestBody @Valid BlockedContentRequest request
    ) {
        return ResponseEntity.ok(
                APIResponse.response(200, "Cập nhật nội dung bị chặn thành công", service.update(id, request))
        );
    }

    @DeleteMapping("/deletedBlockConten/{id}")
    public ResponseEntity<APIResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(
                APIResponse.response(200, "Xóa nội dung bị chặn thành công", null)
        );
    }
}
