package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.ClientProfileRequest;
import com.example.AiTaster.dto.response.ClientProfileResponse;

import com.example.AiTaster.service.ClientProfileService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client-profiles")
public class ClientProfileController {

    private final ClientProfileService clientProfileService;

    public ClientProfileController(ClientProfileService clientProfileService) {
        this.clientProfileService = clientProfileService;
    }

    @GetMapping
    public List<ClientProfileResponse> getAll() {
        return clientProfileService.getAll();
    }

    @GetMapping("/{id}")
    public ClientProfileResponse getById(@PathVariable Long id) {
        return clientProfileService.getByClientId(id);
    }

    @PostMapping
    public ClientProfileResponse create(@RequestBody ClientProfileRequest request) {
        return clientProfileService.create(request);
    }

    @PutMapping("/{id}")
    public ClientProfileResponse update(
            @PathVariable Long id,
            @RequestBody ClientProfileRequest request
    ) {
        return clientProfileService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        clientProfileService.delete(id);
        return "Deleted successfully";
    }
}
