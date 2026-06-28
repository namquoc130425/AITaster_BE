package com.example.AiTaster.Util;

import com.example.AiTaster.dto.request.PageRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.query.SortDirection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PageUtil {
    public static Pageable createPageable(PageRequest request) {
        if (request == null) {
            request = PageRequest.builder().build();
        }

        int page = Math.max(request.getPage(), 0);

        int size = request.getSize();
        if (size <= 0) {
            size = 10;
        }
        if (size > 50) {
            size = 50;
        }

        String sortBy = normalizeSortBy(request.getSortBy());
        Sort sort = request.getSortDirection() == SortDirection.ASCENDING
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return org.springframework.data.domain.PageRequest.of(page, size, sort);
    }

    private static String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "createAt";
        }

        return switch (sortBy.trim()) {
            case "createdAt" -> "createAt";
            case "updatedAt" -> "updateAt";
            case "budget", "budgetFrom", "budgetTo" -> "budgets";
            case "createAt", "updateAt", "title", "budgets", "timeLine" -> sortBy.trim();
            default -> "createAt";
        };
    }
}
