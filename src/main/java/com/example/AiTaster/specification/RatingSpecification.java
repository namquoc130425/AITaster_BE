package com.example.AiTaster.specification;

import com.example.AiTaster.dto.request.RatingFilterRequest;
import com.example.AiTaster.entity.Rating;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class RatingSpecification {

    private RatingSpecification() {
    }

    public static Specification<Rating> filter(RatingFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            if (request.getTargetType() != null) {
                predicates.add(cb.equal(root.get("targetType"), request.getTargetType()));
            }

            if (request.getExpertServiceId() != null && request.getExpertServiceId() > 0) {
                predicates.add(cb.equal(
                        root.get("expertService").get("serviceId"),
                        request.getExpertServiceId()
                ));
            }

            if (request.getExpertProfileId() != null && request.getExpertProfileId() > 0) {
                predicates.add(cb.equal(
                        root.get("expertProfile").get("expertProfileId"),
                        request.getExpertProfileId()
                ));
            }

            if (request.getProjectId() != null && request.getProjectId() > 0) {
                predicates.add(cb.equal(
                        root.get("project").get("projectId"),
                        request.getProjectId()
                ));
            }

            if (request.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), request.getMinRating()));
            }

            if (request.getMaxRating() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rating"), request.getMaxRating()));
            }

            if (Boolean.TRUE.equals(request.getHasReview())) {
                predicates.add(cb.isNotNull(root.get("review")));
                predicates.add(cb.notEqual(cb.trim(root.get("review")), ""));
            }

            if (Boolean.FALSE.equals(request.getHasReview())) {
                predicates.add(cb.or(
                        cb.isNull(root.get("review")),
                        cb.equal(cb.trim(root.get("review")), "")
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
