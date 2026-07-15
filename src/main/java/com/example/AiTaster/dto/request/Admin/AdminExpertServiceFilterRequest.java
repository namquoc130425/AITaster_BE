package com.example.AiTaster.dto.request.Admin;

import com.example.AiTaster.constant.ServiceStatus;
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
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminExpertServiceFilterRequest extends PageRequest {
    ServiceStatus serviceStatus;
    Long ownerUserId;
    Long categoryId;
}
