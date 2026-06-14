package com.example.AiTaster.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.query.SortDirection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
public class PageRequest {
    @Builder.Default
    int page = 0;
    @Builder.Default
    int size = 10;
    @Builder.Default
    String sortBy = "createAt"; //Nên để là createdAt

    @Builder.Default
    SortDirection sortDirection = SortDirection.DESCENDING;

    String search;
}
