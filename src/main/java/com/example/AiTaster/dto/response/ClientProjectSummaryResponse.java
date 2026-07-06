package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.ProjectStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientProjectSummaryResponse {

    Long projectId;

    String projectTitle;

    String projectDescription;

    ProjectStatus projectStatus;

    LocalDateTime createAt;

    LocalDateTime updateAt;
}