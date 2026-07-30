package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.SkillRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.SkillResponse;
import com.example.AiTaster.service.SkillService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/skill")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SkillController {

    SkillService skillService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<SkillResponse>> createSkill(@RequestBody @Valid SkillRequest skillRequest) {

        return ResponseEntity.status(201).body(APIResponse
                .response(201, "Create Skill Success", skillService.create(skillRequest)));

    }

    @GetMapping
    public ResponseEntity<APIResponse<List<SkillResponse>>> getAllSkills() {

        return ResponseEntity.ok(APIResponse
                .response(200, "Get All Skills Success", skillService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<SkillResponse>> getSkillById(@PathVariable @Valid long id) {
        return ResponseEntity.ok(APIResponse
                .response(200, "Get Skill Success", skillService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<SkillResponse>> deleteSkillById(@PathVariable long id) {
        return ResponseEntity.ok(APIResponse
                .response(200, "Delete Skill Success", skillService.delete(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<SkillResponse>> updateSkillById
            (@Valid @RequestBody SkillRequest skillRequest, @PathVariable long id) {
        return ResponseEntity.ok(APIResponse
                .response(200, "Update Skill Success", skillService.update(id, skillRequest)));
    }

}
