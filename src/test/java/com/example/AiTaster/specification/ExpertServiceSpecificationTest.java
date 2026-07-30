package com.example.AiTaster.specification;

import com.example.AiTaster.constant.ServiceStatus;
import com.example.AiTaster.dto.request.ExpertProduct.ExpertServiceFillerRequest;
import com.example.AiTaster.dto.request.ExpertProduct.SubExpertServiceFilterRequest;
import com.example.AiTaster.entity.Category;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.ExpertService;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.repository.ExpertServiceRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExpertServiceSpecificationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExpertServiceRepo expertServiceRepo;

    private Category category;
    private ExpertProfile expertProfile;

    @BeforeEach
    void setUp() {
        category = entityManager.persist(Category.builder()
                .categoryName("AI")
                .slug("ai")
                .build());
        User expertUser = entityManager.persist(User.builder()
                .email("expert-filter@example.com")
                .username("expert-filter")
                .build());
        expertProfile = entityManager.persist(ExpertProfile.builder()
                .user(expertUser)
                .category(category)
                .build());

        persistService("Unrated", BigDecimal.ZERO, 0);
        persistService("Ba sao", new BigDecimal("3.00"), 2);
        persistService("Four stars", new BigDecimal("4.00"), 3);
        persistService("Five stars", new BigDecimal("5.00"), 4);
        entityManager.flush();
    }

    @Test
    void minRating_filtersInDatabaseBeforePaginationAndExcludesUnratedServices() {
        ExpertServiceFillerRequest request = requestWithMinRating(4);

        Page<ExpertService> page = expertServiceRepo.findAll(
                ExpertServiceSpecification.filter(request),
                PageRequest.of(0, 1, Sort.by("serviceId").ascending())
        );

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(ExpertService::getServiceName)
                .containsExactly("Four stars");
        assertThat(page.getContent().getFirst().getRatingCount()).isPositive();
    }

    @Test
    void nullOrZeroMinRating_returnsRatedAndUnratedServices() {
        Page<ExpertService> withoutRatingFilter = expertServiceRepo.findAll(
                ExpertServiceSpecification.filter(requestWithMinRating(null)),
                PageRequest.of(0, 10)
        );
        Page<ExpertService> clearedRatingFilter = expertServiceRepo.findAll(
                ExpertServiceSpecification.filter(requestWithMinRating(0)),
                PageRequest.of(0, 10)
        );

        assertThat(withoutRatingFilter.getTotalElements()).isEqualTo(4);
        assertThat(clearedRatingFilter.getTotalElements()).isEqualTo(4);
    }

    @Test
    void minRatingFive_returnsOnlyFiveStarServices() {
        Page<ExpertService> page = expertServiceRepo.findAll(
                ExpertServiceSpecification.filter(requestWithMinRating(5)),
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent())
                .extracting(ExpertService::getServiceName)
                .containsExactly("Five stars");
    }

    private ExpertServiceFillerRequest requestWithMinRating(Integer minRating) {
        return ExpertServiceFillerRequest.builder()
                .filter(SubExpertServiceFilterRequest.builder()
                        .minRating(minRating)
                        .build())
                .build();
    }

    private void persistService(String name, BigDecimal rating, int ratingCount) {
        entityManager.persist(ExpertService.builder()
                .serviceName(name)
                .serviceDescription(name)
                .serviceFee(BigDecimal.TEN)
                .serviceStatus(ServiceStatus.OPEN)
                .rating(rating)
                .ratingCount(ratingCount)
                .category(category)
                .expertProfile(expertProfile)
                .build());
    }
}
