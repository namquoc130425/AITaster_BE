package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.ClientProfileRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.ClientProfileResponse;
import com.example.AiTaster.dto.response.CurrentUserResponse;


import com.example.AiTaster.service.ClientProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client-profiles")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class ClientProfileController {

@Autowired
ClientProfileService clientProfileService;



    @GetMapping
    public ResponseEntity<APIResponse<List<ClientProfileResponse>>> getAll() {
        List<ClientProfileResponse> responses = clientProfileService.getAll();
        return ResponseEntity.ok(APIResponse.response(201, "Get all client successfully", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<ClientProfileResponse>> getById( @PathVariable Long id) {

          ClientProfileResponse response =  clientProfileService.getByClientId(id);
        return ResponseEntity.status(201).body(APIResponse.response(201,"Get successfully",response));

    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<CurrentUserResponse>> update(@Valid@PathVariable Long id, @RequestBody ClientProfileRequest request
    ) {
        CurrentUserResponse response =  clientProfileService.update(id,request);
        return ResponseEntity.status(201).body(APIResponse.response(201,"Update Client successfully",response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<ClientProfileResponse>> delete(@PathVariable Long id) {
        clientProfileService.delete(id);
        return ResponseEntity.status(201).body(APIResponse.response(201,"Deleted Client successfully",null));
    }
}
