package com.example.AiTaster.dto.response.skillsResponse;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;

public class SkillResponse {

    long skillId;


    String skillName;

    String slug;

    String description;

}
