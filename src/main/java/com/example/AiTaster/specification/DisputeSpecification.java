package com.example.AiTaster.specification;

import com.example.AiTaster.dto.request.DisputeFilterRequest;
import com.example.AiTaster.entity.Dispute;
import com.example.AiTaster.entity.Project;
import com.example.AiTaster.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class DisputeSpecification {
    private DisputeSpecification() {


    }

    public static Specification<Dispute> filter(DisputeFilterRequest request) {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("project", JoinType.LEFT);
                root.fetch("deliverable", JoinType.LEFT);
                root.fetch("reporter", JoinType.LEFT);
                root.fetch("reportedAgainst", JoinType.LEFT);
                query.distinct(true);
            }

            Join<Dispute, Project> project = root.join("project", JoinType.LEFT);
            Join<Dispute, User> reporter = root.join("reporter", JoinType.LEFT);
            Join<Dispute, User> reportedAgainst = root.join("reportedAgainst", JoinType.LEFT);

            var predicate = cb.conjunction();

            if (request.getDisputeStatus() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("disputeStatus"), request.getDisputeStatus()));
            }

            if (request.getDisputeDecision() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("disputeDecision"), request.getDisputeDecision()));
            }

            if (request.getProjectId() != null) {
                predicate = cb.and(predicate, cb.equal(project.get("projectId"), request.getProjectId()));
            }

            if (request.getReporterId() != null) {
                predicate = cb.and(predicate, cb.equal(reporter.get("userId"), request.getReporterId()));
            }

            String search = request.getSearch();
            if (search != null && !search.isBlank()) {
                String keyword = "%" + search.toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("reason")), keyword),
                        cb.like(cb.lower(project.get("title")), keyword),
                        cb.like(cb.lower(reporter.get("fullName")), keyword),
                        cb.like(cb.lower(reportedAgainst.get("fullName")), keyword)
                ));
            }

            return predicate;
        };
    }

}
