package com.example.AiTaster.service;

import com.example.AiTaster.constant.*;
import com.example.AiTaster.dto.request.RatingFilterRequest;
import com.example.AiTaster.dto.request.RatingRequest;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.RatingMapper;
import com.example.AiTaster.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepo ratingRepo;

    @Mock
    private ExpertServiceRepo expertServiceRepo;

    @Mock
    private ExpertProfileRepo expertProfileRepo;

    @Mock
    private ProjectRepo projectRepo;

    @Mock
    private PaymentTransactionRepo paymentTransactionRepo;

    @Mock
    private ClientProfileRepo clientProfileRepo;

    @Mock
    private CurrentUserService currentUserService;

    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        ratingService = new RatingService(
                ratingRepo,
                expertServiceRepo,
                expertProfileRepo,
                projectRepo,
                paymentTransactionRepo,
                clientProfileRepo,
                currentUserService,
                new RatingMapper()
        );
    }

    @Test
    void createExpertServiceRating_requiresSuccessfulPurchase() {
        User clientUser = User.builder().userId(10L).build();
        ClientProfile clientProfile = ClientProfile.builder()
                .clientProfileId(3L)
                .user(clientUser)
                .build();
        ExpertService expertService = ExpertService.builder()
                .serviceId(4L)
                .expertProfile(ExpertProfile.builder().expertProfileId(2L).build())
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        when(clientProfileRepo.findByUser(clientUser)).thenReturn(Optional.of(clientProfile));
        when(expertServiceRepo.findById(4L)).thenReturn(Optional.of(expertService));
        when(paymentTransactionRepo.existsBySenderIdAndTransactionTypeAndPaymentReferenceTypeAndPaymentStatusAndReferenceId(
                10L,
                TransactionType.EXPERT_SERVICE_PURCHASE,
                PaymentReferenceType.EXPERT_SERVICE,
                PaymentStatus.SUCCESS,
                4L
        )).thenReturn(false);

        assertThatThrownBy(() -> ratingService.createExpertServiceRating(
                4L,
                RatingRequest.builder().rating(5).build()
        ))
                .isInstanceOf(GlobalException.class)
                .hasMessage("You can only rate this AI service after purchase");
    }

    @Test
    void createExpertServiceRating_savesIntegerRatingAndRefreshesAverage() {
        User clientUser = User.builder()
                .userId(10L)
                .fullName("Client")
                .build();
        ClientProfile clientProfile = ClientProfile.builder()
                .clientProfileId(3L)
                .user(clientUser)
                .build();
        User expertUser = User.builder()
                .userId(20L)
                .fullName("DienExpert")
                .build();
        ExpertProfile expertProfile = ExpertProfile.builder()
                .expertProfileId(2L)
                .user(expertUser)
                .build();
        ExpertService expertService = ExpertService.builder()
                .serviceId(4L)
                .serviceName("AI Service")
                .expertProfile(expertProfile)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        when(clientProfileRepo.findByUser(clientUser)).thenReturn(Optional.of(clientProfile));
        when(expertServiceRepo.findById(4L)).thenReturn(Optional.of(expertService));
        when(paymentTransactionRepo.existsBySenderIdAndTransactionTypeAndPaymentReferenceTypeAndPaymentStatusAndReferenceId(
                anyLong(),
                eq(TransactionType.EXPERT_SERVICE_PURCHASE),
                eq(PaymentReferenceType.EXPERT_SERVICE),
                eq(PaymentStatus.SUCCESS),
                eq(4L)
        )).thenReturn(true);
        when(ratingRepo.existsByClientProfile_ClientProfileIdAndExpertService_ServiceId(3L, 4L))
                .thenReturn(false);
        when(ratingRepo.save(any(Rating.class))).thenAnswer(invocation -> {
            Rating rating = invocation.getArgument(0);
            rating.setRatingId(99L);
            return rating;
        });
        when(ratingRepo.countByExpertService_ServiceIdAndTargetType(4L, RatingTargetType.EXPERT_SERVICE))
                .thenReturn(2L);
        when(ratingRepo.averageByExpertServiceIdAndTargetType(4L, RatingTargetType.EXPERT_SERVICE))
                .thenReturn(4.5);

        var response = ratingService.createExpertServiceRating(
                4L,
                RatingRequest.builder()
                        .rating(5)
                        .review("  Great service  ")
                        .build()
        );

        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getReview()).isEqualTo("Great service");
        assertThat(expertService.getRating()).isEqualByComparingTo(BigDecimal.valueOf(4.50));
        assertThat(expertService.getRatingCount()).isEqualTo(2);
        verify(expertServiceRepo).save(expertService);
    }

    @Test
    void createProjectExpertRating_requiresCompletedProject() {
        User clientUser = User.builder().userId(10L).build();
        ClientProfile clientProfile = ClientProfile.builder()
                .clientProfileId(3L)
                .user(clientUser)
                .build();
        Project project = project(ProjectStatus.ACTIVE, clientProfile, ExpertProfile.builder().build());

        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        when(clientProfileRepo.findByUser(clientUser)).thenReturn(Optional.of(clientProfile));
        when(projectRepo.findWithDetailByProjectId(7L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> ratingService.createProjectExpertRating(
                7L,
                RatingRequest.builder().rating(4).build()
        ))
                .isInstanceOf(GlobalException.class)
                .hasMessage("You can only rate after project is completed");
    }

    @Test
    void filterRatings_usesHighestSortWithNewestTieBreaker() {
        when(ratingRepo.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        ratingService.filterRatings(RatingFilterRequest.builder()
                .sortType(RatingSortType.HIGHEST)
                .page(0)
                .size(10)
                .build());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(ratingRepo).findAll(any(Specification.class), pageableCaptor.capture());

        Sort.Order ratingOrder = pageableCaptor.getValue().getSort().getOrderFor("rating");
        Sort.Order createAtOrder = pageableCaptor.getValue().getSort().getOrderFor("createAt");

        assertThat(ratingOrder).isNotNull();
        assertThat(ratingOrder.getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(createAtOrder).isNotNull();
        assertThat(createAtOrder.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    private Project project(
            ProjectStatus status,
            ClientProfile clientProfile,
            ExpertProfile expertProfile
    ) {
        JobPost jobPost = JobPost.builder()
                .clientProfile(clientProfile)
                .build();
        ExpertApplication application = ExpertApplication.builder()
                .jobpost(jobPost)
                .expertProfile(expertProfile)
                .build();
        Invitation invitation = Invitation.builder()
                .expertApplication(application)
                .build();

        return Project.builder()
                .projectId(7L)
                .projectStatus(status)
                .invitation(invitation)
                .build();
    }
}
