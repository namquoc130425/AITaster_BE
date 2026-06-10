package com.example.AiTaster.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.query.SortDirection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class PageRequest {
    @Builder.Default
    int page = 0;
    @Builder.Default
    int size = 10;
    @Builder.Default
    String sortBy = "createAt";

    @Builder.Default
    SortDirection sortDirection = SortDirection.DESCENDING;
    String search;
}
