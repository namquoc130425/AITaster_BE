package com.example.AiTaster.dto.response.Ai;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VectorSkillResult {
    Long skillId;

    String skillName;

    Double scoure; // độ liên quan đến Qdrant trả về

    boolean selectedByUser;      // đúng nếu skill đc chọn  chọn bên  fe

}
