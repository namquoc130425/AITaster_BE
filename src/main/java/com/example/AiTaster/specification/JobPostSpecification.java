package com.example.AiTaster.specification;

import com.example.AiTaster.constant.JobpostStatus;
import com.example.AiTaster.dto.request.JobPost.JobPostFilterRequest;
import com.example.AiTaster.dto.request.JobPost.SubJobPostFilterRequest;
import com.example.AiTaster.entity.JobPost;
import com.example.AiTaster.entity.Skill;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class JobPostSpecification {
    private JobPostSpecification() {
    }

    public static Specification<JobPost> filter(JobPostFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            SubJobPostFilterRequest filter = null;

            if (request != null) {
                filter = request.getFilter();
            }

            predicates.add(cb.equal(root.get("jobPostStatus"), JobpostStatus.OPEN));

            if (request != null && request.getSearch() != null && !request.getSearch().isBlank()) {
                String keyword = "%" + request.getSearch().trim().toLowerCase() + "%";

                Predicate searchByTitle = cb.like(cb.lower(root.get("title")), keyword);
                Predicate searchByRequirementDescription = cb.like(cb.lower(root.get("requirementDescription")), keyword);
                Predicate searchByBusinessGoal = cb.like(cb.lower(root.get("businessGoal")), keyword);
                Predicate searchByMainFeatures = cb.like(cb.lower(root.get("mainFeatures")), keyword);
                Join<JobPost, Skill> searchSkillJoin = root.join("skills", JoinType.LEFT);
                Predicate searchBySkillName = cb.like(cb.lower(searchSkillJoin.get("skillName")), keyword);
                Predicate searchBySkillDescription = cb.like(cb.lower(searchSkillJoin.get("description")), keyword);

                predicates.add(cb.or(
                        searchByTitle,
                        searchByRequirementDescription,
                        searchByBusinessGoal,
                        searchByMainFeatures,
                        searchBySkillName,
                        searchBySkillDescription
                ));
                query.distinct(true);
            }

            if (filter != null) {
                if (filter.getSkillIds() != null && !filter.getSkillIds().isEmpty()) {
                    Join<JobPost, Skill> skillJoin = root.join("skills", JoinType.INNER);
                    predicates.add(skillJoin.get("skillId").in(filter.getSkillIds()));
                    query.distinct(true);
                }

                if (filter.getBudgetFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("budgets"), filter.getBudgetFrom()));
                }

                if (filter.getBudgetTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("budgets"), filter.getBudgetTo()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
