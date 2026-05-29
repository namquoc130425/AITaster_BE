package com.example.AiTaster.controller.skillsController;

import com.example.AiTaster.dto.request.skillsRequest.SkillRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.skillsResponse.SkillResponse;

import com.example.AiTaster.service.skillsSevice.SkillService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    SkillService skillService;

    @PostMapping()
    public ResponseEntity<APIResponse<SkillResponse>> create(@Valid @RequestBody SkillRequest skillRequest) {

        SkillResponse skillResponse = skillService.create(skillRequest);

        return ResponseEntity.status(201).body(APIResponse.response(201, "created skill", skillResponse));

    }

    @GetMapping
    public ResponseEntity<APIResponse<List<SkillResponse>>> getAllSkills() {

        List<SkillResponse> responseList = skillService.getAll();
        return ResponseEntity.ok(APIResponse.response(200, "Get all skills successful", responseList));

    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<SkillResponse>> getSkill(@PathVariable long id) {

        SkillResponse skillResponse = skillService.getSkillById(id);

        return ResponseEntity.ok(APIResponse.response(200, "Get skill successful", skillResponse));
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<SkillResponse>> updateSkill(@PathVariable long id,
                                                                  @Valid @RequestBody SkillRequest skillRequest) {

        SkillResponse skillResponse = skillService.updateEnity(skillRequest, id);

        return ResponseEntity.ok((APIResponse.response(200, "updated skill successful", skillResponse)));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<SkillResponse>> deleteSkill(@PathVariable long id) {
    SkillResponse skillResponse = skillService.deleteSkillById(id);

    return ResponseEntity.ok(APIResponse.response(200, "deleted skill successful", skillResponse));

    }

}
