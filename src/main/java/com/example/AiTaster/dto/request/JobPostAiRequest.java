package com.example.AiTaster.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobPostAiRequest {
    @NotBlank(message = "KEYWORD_REQUIRED")
    @Size(min = 3, max = 500, message = "KEYWORD_LENGTH_INVALID")
    String keyword; // Từ khóa client nhập để AI sinh JobPost
}
