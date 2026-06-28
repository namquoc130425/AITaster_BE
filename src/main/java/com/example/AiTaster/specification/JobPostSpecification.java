package com.example.AiTaster.specification;

import com.example.AiTaster.constant.InvitationStatus;
import com.example.AiTaster.constant.JobpostStatus;
import com.example.AiTaster.dto.request.JobPost.JobPostFilterRequest;
import com.example.AiTaster.dto.request.JobPost.SubJobPostFilterRequest;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.JobPost;
import com.example.AiTaster.entity.Skill;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class JobPostSpecification {
    private JobPostSpecification() {
    }

    public static Specification<JobPost> filter(JobPostFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            SubJobPostFilterRequest filter = request != null ? request.getFilter() : null;

            predicates.add(cb.equal(root.get("jobPostStatus"), JobpostStatus.OPEN));

            Subquery<Long> reservedInvitationSubquery = query.subquery(Long.class);
            Root<Invitation> invitationRoot = reservedInvitationSubquery.from(Invitation.class);
            reservedInvitationSubquery.select(invitationRoot.get("invitationId"));
            reservedInvitationSubquery.where(
                    cb.equal(invitationRoot.get("expertApplication").get("jobpost"), root),
                    invitationRoot.get("invitationStatus").in(List.of(
                            InvitationStatus.PENDING,
                            InvitationStatus.ACCEPTED,
                            InvitationStatus.PAYMENT_EXPIRED
                    ))
            );
            predicates.add(cb.not(cb.exists(reservedInvitationSubquery)));

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
                    List<Long> validSkillIds = filter.getSkillIds()
                            .stream()
                            .filter(skillId -> skillId != null && skillId > 0)
                            .distinct()
                            .toList();

                    if (!validSkillIds.isEmpty()) {
                        Join<JobPost, Skill> skillJoin = root.join("skills", JoinType.INNER);
                        predicates.add(skillJoin.get("skillId").in(validSkillIds));
                        query.distinct(true);
                    }
                }

                BigDecimal budgetFrom = normalizePositive(filter.getBudgetFrom());
                BigDecimal budgetTo = normalizePositive(filter.getBudgetTo());

                if (budgetFrom != null && budgetTo != null && budgetFrom.compareTo(budgetTo) > 0) {
                    BigDecimal temp = budgetFrom;
                    budgetFrom = budgetTo;
                    budgetTo = temp;
                }

                if (budgetFrom != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("budgets"), budgetFrom));
                }

                if (budgetTo != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("budgets"), budgetTo));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static BigDecimal normalizePositive(BigDecimal value) {
        if (value == null || value.signum() <= 0) {
            return null;
        }

        return value;
    }
}
