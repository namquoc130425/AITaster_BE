package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.request.ExpertServiceRequest;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.ExpertService;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ExpertServiceMapperTest {

    private final ExpertServiceMapper mapper = Mappers.getMapper(ExpertServiceMapper.class);

    @Test
    void toEntity_doesNotInheritRatingAggregatesFromExpertProfile() {
        ExpertProfile expertProfile = ExpertProfile.builder()
                .expertProfileId(4L)
                .rating(new BigDecimal("3.00"))
                .ratingCount(3)
                .build();
        ExpertServiceRequest request = new ExpertServiceRequest();
        request.setServiceName("New AI service");
        request.setServiceDescription("A newly created AI service");
        request.setServiceFee(new BigDecimal("100000"));

        ExpertService service = mapper.toEntity(request, expertProfile);
        service.prePersist();

        assertThat(service.getRating()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(service.getRatingCount()).isZero();
    }
}
