package com.example.AiTaster.service;

import com.example.AiTaster.Util.PageUtil;
import com.example.AiTaster.constant.JobpostStatus;
import com.example.AiTaster.constant.NotificationType;
import com.example.AiTaster.constant.ReferenceType;
import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.ServiceStatus;
import com.example.AiTaster.constant.UserStatus;
import com.example.AiTaster.dto.request.Admin.AdminContentModerationRequest;
import com.example.AiTaster.dto.request.Admin.AdminExpertServiceFilterRequest;
import com.example.AiTaster.dto.request.Admin.AdminJobPostFilterRequest;
import com.example.AiTaster.dto.response.ExpertServiceResponse;
import com.example.AiTaster.dto.response.JobPostResponse;
import com.example.AiTaster.dto.response.PageResponse;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.ExpertService;
import com.example.AiTaster.entity.JobPost;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ExpertServiceMapper;
import com.example.AiTaster.mapper.JobPostMapper;
import com.example.AiTaster.repository.ExpertServiceRepo;
import com.example.AiTaster.repository.JobPostRepo;
import com.example.AiTaster.repository.UserRepo;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminContentModerationService {
    private final CurrentUserService currentUserService;
    private final JobPostRepo jobPostRepo;
    private final ExpertServiceRepo expertServiceRepo;
    private final UserRepo userRepo;
    private final JobPostMapper jobPostMapper;
    private final ExpertServiceMapper expertServiceMapper;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public PageResponse<JobPostResponse> filterJobPosts(AdminJobPostFilterRequest request) {
        checkAdmin();

        if (request == null) {
            request = new AdminJobPostFilterRequest();
        }

        Page<JobPost> page = jobPostRepo.findAll(
                buildJobPostSpec(request),
                PageUtil.createPageable(request)
        );

        return PageResponse.fromPage(page.map(jobPostMapper::toResponse));
    }

    @Transactional
    public JobPostResponse removeJobPost(Long jobPostId, AdminContentModerationRequest request) {
        checkAdmin();

        JobPost jobPost = jobPostRepo.findById(jobPostId)
                .orElseThrow(() -> new GlobalException(404, "Job post not found"));
        User owner = resolveJobPostOwner(jobPost);

        jobPost.setJobPostStatus(JobpostStatus.CANCELED);

        if (shouldBanOwner(request) && owner != null) {
            owner.setUserStatus(UserStatus.BANNED);
            userRepo.save(owner);
        }

        JobPost saved = jobPostRepo.save(jobPost);

        if (owner != null) {
            notificationService.notify(
                    owner,
                    NotificationType.SYSTEM,
                    ReferenceType.JOB_POST,
                    saved.getJobPostId(),
                    "Job post removed by admin",
                    buildModerationMessage("Your job post '" + saved.getTitle() + "' was removed by admin.", request)
            );
        }

        return jobPostMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpertServiceResponse> filterExpertServices(AdminExpertServiceFilterRequest request) {
        checkAdmin();

        if (request == null) {
            request = new AdminExpertServiceFilterRequest();
        }

        Page<ExpertService> page = expertServiceRepo.findAll(
                buildExpertServiceSpec(request),
                PageUtil.createPageable(request)
        );

        return PageResponse.fromPage(page.map(expertServiceMapper::toResponse));
    }

    @Transactional
    public ExpertServiceResponse removeExpertService(Long serviceId, AdminContentModerationRequest request) {
        checkAdmin();

        ExpertService expertService = expertServiceRepo.findById(serviceId)
                .orElseThrow(() -> new GlobalException(404, "AI service not found"));
        User owner = resolveExpertServiceOwner(expertService);

        expertService.setServiceStatus(ServiceStatus.DELETED);

        if (shouldBanOwner(request) && owner != null) {
            owner.setUserStatus(UserStatus.BANNED);
            userRepo.save(owner);
        }

        ExpertService saved = expertServiceRepo.save(expertService);

        if (owner != null) {
            notificationService.notify(
                    owner,
                    NotificationType.EXPERT_SERVICE,
                    ReferenceType.EXPERT_SERVICE,
                    saved.getServiceId(),
                    "AI service removed by admin",
                    buildModerationMessage("Your AI service '" + saved.getServiceName() + "' was removed by admin.", request)
            );
        }

        return expertServiceMapper.toResponse(saved);
    }

    private Specification<JobPost> buildJobPostSpec(AdminJobPostFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<JobPost, ClientProfile> clientProfile = root.join("clientProfile", JoinType.LEFT);
            Join<ClientProfile, User> owner = clientProfile.join("user", JoinType.LEFT);

            if (request.getJobPostStatus() != null) {
                predicates.add(cb.equal(root.get("jobPostStatus"), request.getJobPostStatus()));
            }

            if (request.getOwnerUserId() != null) {
                predicates.add(cb.equal(owner.get("userId"), request.getOwnerUserId()));
            }

            if (request.getSearch() != null && !request.getSearch().isBlank()) {
                String keyword = "%" + request.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), keyword),
                        cb.like(cb.lower(root.get("requirementDescription")), keyword),
                        cb.like(cb.lower(owner.get("fullName")), keyword),
                        cb.like(cb.lower(owner.get("email")), keyword),
                        cb.like(cb.lower(owner.get("username")), keyword)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<ExpertService> buildExpertServiceSpec(AdminExpertServiceFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<ExpertService, ExpertProfile> expertProfile = root.join("expertProfile", JoinType.LEFT);
            Join<ExpertProfile, User> owner = expertProfile.join("user", JoinType.LEFT);

            if (request.getServiceStatus() != null) {
                predicates.add(cb.equal(root.get("serviceStatus"), request.getServiceStatus()));
            }

            if (request.getOwnerUserId() != null) {
                predicates.add(cb.equal(owner.get("userId"), request.getOwnerUserId()));
            }

            if (request.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("categoryId"), request.getCategoryId()));
            }

            if (request.getSearch() != null && !request.getSearch().isBlank()) {
                String keyword = "%" + request.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("serviceName")), keyword),
                        cb.like(cb.lower(root.get("serviceDescription")), keyword),
                        cb.like(cb.lower(owner.get("fullName")), keyword),
                        cb.like(cb.lower(owner.get("email")), keyword),
                        cb.like(cb.lower(owner.get("username")), keyword)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void checkAdmin() {
        User currentUser = currentUserService.getCurrentUser();
        if (!Role.ADMIN.equals(currentUser.getRole())) {
            throw new GlobalException(403, "Only admin can moderate content");
        }
    }

    private boolean shouldBanOwner(AdminContentModerationRequest request) {
        return request != null && Boolean.TRUE.equals(request.getBanOwner());
    }

    private User resolveJobPostOwner(JobPost jobPost) {
        if (jobPost == null || jobPost.getClientProfile() == null) {
            return null;
        }
        return jobPost.getClientProfile().getUser();
    }

    private User resolveExpertServiceOwner(ExpertService expertService) {
        if (expertService == null || expertService.getExpertProfile() == null) {
            return null;
        }
        return expertService.getExpertProfile().getUser();
    }

    private String buildModerationMessage(String baseMessage, AdminContentModerationRequest request) {
        String reason = request == null ? null : request.getReason();
        if (reason == null || reason.isBlank()) {
            return baseMessage;
        }

        return baseMessage + " Reason: " + reason.trim();
    }
}
