package com.example.AiTaster.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL) //  null ko truyền đi
@Builder
public class MetaData {

    int page;

    int size;

    int currentElements;

    Boolean hasNext;

    boolean hasPrevious;
}
