package com.example.AiTaster.dto.response.Ai;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class VectorSkillResult {
    Long skillId;

    String skillName;

    Double score; // độ liên quan đến Qdrant trả về


}
