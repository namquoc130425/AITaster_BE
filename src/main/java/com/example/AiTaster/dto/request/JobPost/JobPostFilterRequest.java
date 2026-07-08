package com.example.AiTaster.dto.request.JobPost;

import com.example.AiTaster.dto.request.PageRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class JobPostFilterRequest extends PageRequest {
    SubJobPostFilterRequest filter;
}
