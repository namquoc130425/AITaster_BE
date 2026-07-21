package com.example.AiTaster.service;

import com.example.AiTaster.constant.*;
import com.example.AiTaster.dto.request.RatingFilterRequest;
import com.example.AiTaster.dto.request.RatingRequest;
import com.example.AiTaster.dto.response.PageResponse;
import com.example.AiTaster.dto.response.RatingResponse;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.RatingMapper;
import com.example.AiTaster.repository.*;
import com.example.AiTaster.specification.RatingSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class RatingService {

    private static final int MAX_PAGE_SIZE = 50;

    private final RatingRepo ratingRepo;
    private final ExpertServiceRepo expertServiceRepo;
    private final ExpertProfileRepo expertProfileRepo;
    private final ProjectRepo projectRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final ClientProfileRepo clientProfileRepo;
    private final CurrentUserService currentUserService;
    private final RatingMapper ratingMapper;

    @Transactional
    public RatingResponse createExpertServiceRating(Long serviceId, RatingRequest request) {
        ClientProfile clientProfile = getCurrentClientProfile();
        ExpertService expertService = expertServiceRepo.findById(serviceId)
                .orElseThrow(() -> new GlobalException(404, "Expert service not found"));

        validateServicePurchased(clientProfile, serviceId);

        if (ratingRepo.existsByClientProfile_ClientProfileIdAndExpertService_ServiceId(
                clientProfile.getClientProfileId(),
                serviceId
        )) {
            throw new GlobalException(409, "You already rated this AI service");
        }

        Rating rating = Rating.builder()
                .clientProfile(clientProfile)
                .expertProfile(expertService.getExpertProfile())
                .expertService(expertService)
                .targetType(RatingTargetType.EXPERT_SERVICE)
                .rating(request.getRating())
                .review(normalizeReview(request.getReview()))
                .build();

        Rating saved = saveNewRating(rating);
        refreshExpertServiceRating(expertService);

        return ratingMapper.toResponse(saved);
    }

    @Transactional
    public RatingResponse createProjectExpertRating(Long projectId, RatingRequest request) {
        ClientProfile clientProfile = getCurrentClientProfile();
        Project project = projectRepo.findWithDetailByProjectId(projectId)
                .orElseThrow(() -> new GlobalException(404, "Project not found"));

        validateProjectCanBeRatedByClient(clientProfile, project);

        if (ratingRepo.existsByClientProfile_ClientProfileIdAndProject_ProjectId(
                clientProfile.getClientProfileId(),
                projectId
        )) {
            throw new GlobalException(409, "You already rated this project");
        }

        ExpertProfile expertProfile = resolveProjectExpertProfile(project);
        Rating rating = Rating.builder()
                .clientProfile(clientProfile)
                .expertProfile(expertProfile)
                .project(project)
                .targetType(RatingTargetType.PROJECT_EXPERT)
                .rating(request.getRating())
                .review(normalizeReview(request.getReview()))
                .build();

        Rating saved = saveNewRating(rating);
        refreshExpertProfileRating(expertProfile);

        return ratingMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public RatingResponse getRating(Long ratingId) {
        return ratingMapper.toResponse(getRatingById(ratingId));
    }

    @Transactional(readOnly = true)
    public RatingResponse getMyExpertServiceRating(Long serviceId) {
        ClientProfile clientProfile = getCurrentClientProfile();

        Rating rating = ratingRepo.findByClientProfile_ClientProfileIdAndExpertService_ServiceId(
                clientProfile.getClientProfileId(),
                serviceId
        ).orElseThrow(() -> new GlobalException(404, "Rating not found"));

        return ratingMapper.toResponse(rating);
    }

    @Transactional(readOnly = true)
    public RatingResponse getMyProjectRating(Long projectId) {
        ClientProfile clientProfile = getCurrentClientProfile();

        Rating rating = ratingRepo.findByClientProfile_ClientProfileIdAndProject_ProjectId(
                clientProfile.getClientProfileId(),
                projectId
        ).orElseThrow(() -> new GlobalException(404, "Rating not found"));

        return ratingMapper.toResponse(rating);
    }

    @Transactional
    public RatingResponse updateRating(Long ratingId, RatingRequest request) {
        ClientProfile clientProfile = getCurrentClientProfile();
        Rating rating = getRatingById(ratingId);
        ensureRatingOwner(clientProfile, rating);

        rating.setRating(request.getRating());
        rating.setReview(normalizeReview(request.getReview()));

        Rating saved = ratingRepo.save(rating);
        refreshAggregateForRating(saved);

        return ratingMapper.toResponse(saved);
    }

    @Transactional
    public void deleteRating(Long ratingId) {
        ClientProfile clientProfile = getCurrentClientProfile();
        Rating rating = getRatingById(ratingId);
        ensureRatingOwner(clientProfile, rating);

        ExpertService expertService = rating.getExpertService();
        ExpertProfile expertProfile = rating.getExpertProfile();
        RatingTargetType targetType = rating.getTargetType();

        ratingRepo.delete(rating);
        ratingRepo.flush();

        if (RatingTargetType.EXPERT_SERVICE.equals(targetType) && expertService != null) {
            refreshExpertServiceRating(expertService);
        }

        if (RatingTargetType.PROJECT_EXPERT.equals(targetType) && expertProfile != null) {
            refreshExpertProfileRating(expertProfile);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<RatingResponse> filterRatings(RatingFilterRequest request) {
        RatingFilterRequest normalizedRequest = normalizeFilterRequest(request);
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                normalizedRequest.getPage(),
                normalizedRequest.getSize(),
                resolveSort(normalizedRequest.getSortType())
        );

        Page<RatingResponse> page = ratingRepo
                .findAll(RatingSpecification.filter(normalizedRequest), pageable)
                .map(ratingMapper::toResponse);

        return PageResponse.fromPage(page);
    }

    private ClientProfile getCurrentClientProfile() {
        User user = currentUserService.getCurrentUser();

        return clientProfileRepo.findByUser(user)
                .orElseThrow(() -> new GlobalException(403, "Only client can use this rating API"));
    }

    private Rating getRatingById(Long ratingId) {
        return ratingRepo.findById(ratingId)
                .orElseThrow(() -> new GlobalException(404, "Rating not found"));
    }

    private void validateServicePurchased(ClientProfile clientProfile, Long serviceId) {
        boolean purchased = paymentTransactionRepo
                .existsBySenderIdAndTransactionTypeAndPaymentReferenceTypeAndPaymentStatusAndReferenceId(
                        clientProfile.getUser().getUserId(),
                        TransactionType.EXPERT_SERVICE_PURCHASE,
                        PaymentReferenceType.EXPERT_SERVICE,
                        PaymentStatus.SUCCESS,
                        serviceId
                );

        if (!purchased) {
            throw new GlobalException(403, "You can only rate this AI service after purchase");
        }
    }

    private void validateProjectCanBeRatedByClient(ClientProfile clientProfile, Project project) {
        Long projectClientProfileId = resolveProjectClientProfileId(project);

        if (!clientProfile.getClientProfileId().equals(projectClientProfileId)) {
            throw new GlobalException(403, "You can only rate your own project");
        }

        if (!ProjectStatus.COMPLETED.equals(project.getProjectStatus())) {
            throw new GlobalException(403, "You can only rate after project is completed");
        }
    }

    private void ensureRatingOwner(ClientProfile clientProfile, Rating rating) {
        if (rating.getClientProfile() == null
                || !clientProfile.getClientProfileId().equals(rating.getClientProfile().getClientProfileId())) {
            throw new GlobalException(403, "You can only change your own rating");
        }
    }

    private Long resolveProjectClientProfileId(Project project) {
        if (project.getInvitation() == null
                || project.getInvitation().getExpertApplication() == null
                || project.getInvitation().getExpertApplication().getJobpost() == null
                || project.getInvitation().getExpertApplication().getJobpost().getClientProfile() == null) {
            throw new GlobalException(400, "Project client information is incomplete");
        }

        return project.getInvitation()
                .getExpertApplication()
                .getJobpost()
                .getClientProfile()
                .getClientProfileId();
    }

    private ExpertProfile resolveProjectExpertProfile(Project project) {
        if (project.getInvitation() == null
                || project.getInvitation().getExpertApplication() == null
                || project.getInvitation().getExpertApplication().getExpertProfile() == null) {
            throw new GlobalException(400, "Project expert information is incomplete");
        }

        return project.getInvitation()
                .getExpertApplication()
                .getExpertProfile();
    }

    private Rating saveNewRating(Rating rating) {
        try {
            return ratingRepo.save(rating);
        } catch (DataIntegrityViolationException exception) {
            throw new GlobalException(409, "You already rated this item");
        }
    }

    private void refreshAggregateForRating(Rating rating) {
        if (RatingTargetType.EXPERT_SERVICE.equals(rating.getTargetType()) && rating.getExpertService() != null) {
            refreshExpertServiceRating(rating.getExpertService());
        }

        if (RatingTargetType.PROJECT_EXPERT.equals(rating.getTargetType()) && rating.getExpertProfile() != null) {
            refreshExpertProfileRating(rating.getExpertProfile());
        }
    }

    private void refreshExpertServiceRating(ExpertService expertService) {
        Long serviceId = expertService.getServiceId();
        long count = ratingRepo.countByExpertService_ServiceIdAndTargetType(
                serviceId,
                RatingTargetType.EXPERT_SERVICE
        );
        Double average = ratingRepo.averageByExpertServiceIdAndTargetType(
                serviceId,
                RatingTargetType.EXPERT_SERVICE
        );

        expertService.setRating(toRatingAverage(average));
        expertService.setRatingCount(Math.toIntExact(count));
        expertServiceRepo.save(expertService);
    }

    private void refreshExpertProfileRating(ExpertProfile expertProfile) {
        Long expertProfileId = expertProfile.getExpertProfileId();
        long count = ratingRepo.countByExpertProfile_ExpertProfileIdAndTargetType(
                expertProfileId,
                RatingTargetType.PROJECT_EXPERT
        );
        Double average = ratingRepo.averageByExpertProfileIdAndTargetType(
                expertProfileId,
                RatingTargetType.PROJECT_EXPERT
        );

        expertProfile.setRating(toRatingAverage(average));
        expertProfile.setRatingCount(Math.toIntExact(count));
        expertProfileRepo.save(expertProfile);
    }

    private BigDecimal toRatingAverage(Double average) {
        if (average == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
    }

    private RatingFilterRequest normalizeFilterRequest(RatingFilterRequest request) {
        RatingFilterRequest normalized = request == null
                ? RatingFilterRequest.builder().build()
                : request;

        if (normalized.getMinRating() != null
                && normalized.getMaxRating() != null
                && normalized.getMinRating() > normalized.getMaxRating()) {
            throw new GlobalException(400, "minRating cannot be greater than maxRating");
        }

        if (normalized.getPage() == null || normalized.getPage() < 0) {
            normalized.setPage(0);
        }

        if (normalized.getSize() == null || normalized.getSize() <= 0) {
            normalized.setSize(10);
        }

        if (normalized.getSize() > MAX_PAGE_SIZE) {
            normalized.setSize(MAX_PAGE_SIZE);
        }

        if (normalized.getSortType() == null) {
            normalized.setSortType(RatingSortType.NEWEST);
        }

        return normalized;
    }

    private Sort resolveSort(RatingSortType sortType) {
        return switch (sortType) {
            case OLDEST -> Sort.by(Sort.Direction.ASC, "createAt");
            case HIGHEST -> Sort.by(Sort.Direction.DESC, "rating")
                    .and(Sort.by(Sort.Direction.DESC, "createAt"));
            case LOWEST -> Sort.by(Sort.Direction.ASC, "rating")
                    .and(Sort.by(Sort.Direction.DESC, "createAt"));
            case NEWEST -> Sort.by(Sort.Direction.DESC, "createAt");
        };
    }

    private String normalizeReview(String review) {
        if (review == null || review.isBlank()) {
            return null;
        }

        return review.trim();
    }
}
