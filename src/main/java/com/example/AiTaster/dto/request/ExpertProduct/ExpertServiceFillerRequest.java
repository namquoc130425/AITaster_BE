package com.example.AiTaster.dto.request.ExpertProduct;

import com.example.AiTaster.dto.request.PageRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data // gộp chung của getter,setter,toString,...
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ExpertServiceFillerRequest extends PageRequest {
    SubExpertServiceFilterRequest filter;
}
