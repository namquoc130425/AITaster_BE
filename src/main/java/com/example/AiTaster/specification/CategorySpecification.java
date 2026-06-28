package com.example.AiTaster.specification;

import com.example.AiTaster.dto.request.Category.CategoryFilterRequest;
import com.example.AiTaster.dto.request.Category.SubCategoryFilterRequest;
import com.example.AiTaster.entity.Category;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CategorySpecification {

    private CategorySpecification() {
    }

    public static Specification<Category> filter(CategoryFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            SubCategoryFilterRequest filter = null;

            if (request != null) {
                filter = request.getFilter();
            }

            if (request != null && request.getSearch() != null && !request.getSearch().isBlank()) {
                String keyword = "%" + request.getSearch().trim().toLowerCase() + "%";

                Predicate searchByName =
                        cb.like(cb.lower(root.get("categoryName")), keyword);

                Predicate searchBySlug =
                        cb.like(cb.lower(root.get("slug")), keyword);

                Predicate searchByDescription =
                        cb.like(cb.lower(root.get("description")), keyword);

                predicates.add(cb.or(
                        searchByName,
                        searchBySlug,
                        searchByDescription
                ));
            }

            if (filter != null) {
                if (filter.getCategoryName() != null && !filter.getCategoryName().isBlank()) {
                    predicates.add(
                            cb.like(
                                    cb.lower(root.get("categoryName")),
                                    "%" + filter.getCategoryName().trim().toLowerCase() + "%"
                            )
                    );
                }

                if (filter.getSlug() != null && !filter.getSlug().isBlank()) {
                    predicates.add(
                            cb.equal(
                                    cb.lower(root.get("slug")),
                                    filter.getSlug().trim().toLowerCase()
                            )
                    );
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}