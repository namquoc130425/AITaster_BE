package com.example.AiTaster.dto.request.ExpertProduct;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExpertServiceFillerRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void minRating_acceptsNullZeroAndValuesUpToFive() {
        assertThat(validator.validate(request(null))).isEmpty();
        assertThat(validator.validate(request(0))).isEmpty();
        assertThat(validator.validate(request(3))).isEmpty();
        assertThat(validator.validate(request(5))).isEmpty();
    }

    @Test
    void minRating_rejectsValuesOutsideZeroToFive() {
        assertThat(validator.validate(request(-1)))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("filter.minRating");
        assertThat(validator.validate(request(6)))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("filter.minRating");
    }

    private ExpertServiceFillerRequest request(Integer minRating) {
        return ExpertServiceFillerRequest.builder()
                .filter(SubExpertServiceFilterRequest.builder()
                        .minRating(minRating)
                        .build())
                .build();
    }
}
