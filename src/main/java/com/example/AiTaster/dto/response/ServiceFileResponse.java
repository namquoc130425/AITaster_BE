package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceFileResponse {

    Long serviceFileId;

    String fileContent;

    ProductType productType;

    String productFile;

    String fileName;

    Boolean isActive;
}
