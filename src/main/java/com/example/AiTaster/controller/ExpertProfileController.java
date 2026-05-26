package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.ClientProfileRequest;
import com.example.AiTaster.dto.request.ExpertProfileRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.ClientProfileResponse;
import com.example.AiTaster.dto.response.ExpertProfileResponse;
import com.example.AiTaster.repository.ExpertProfileRepo;
import com.example.AiTaster.service.ExpertProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expert-profiles")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class ExpertProfileController {
    @Autowired
    ExpertProfileService   expertProfileService;

    @GetMapping
    public ResponseEntity<APIResponse<List<ExpertProfileResponse>>> getAll() {
        List<ExpertProfileResponse> responses = expertProfileService.getAll();
        return ResponseEntity.ok(APIResponse.response(201, "Get all client successfully", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<ExpertProfileResponse>> getById( @PathVariable Long id) {

        ExpertProfileResponse response =  expertProfileService.getByExpertId(id);
        return ResponseEntity.status(201).body(APIResponse.response(201,"Get Expert successfully",response));

    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<ExpertProfileResponse>> update(@Valid @PathVariable Long id, @RequestBody ExpertProfileRequest request
    ) {
        ExpertProfileResponse response =  expertProfileService.update(id,request);
        return ResponseEntity.status(201).body(APIResponse.response(201,"Update Client successfully",response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<ExpertProfileResponse>> delete(@PathVariable Long id) {
        expertProfileService.delete(id);
        return ResponseEntity.status(201).body(APIResponse.response(201,"Deleted Client successfully",null));
    }
}
