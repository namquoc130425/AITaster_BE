package com.example.AiTaster.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
<<<<<<< HEAD
import org.springframework.cglib.core.Local;

=======
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SkillResponse {

<<<<<<< HEAD
    long SkillId;
    String SkillName;
=======
    long skillId;
    String skillName;
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
    String slug;
    String description;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
