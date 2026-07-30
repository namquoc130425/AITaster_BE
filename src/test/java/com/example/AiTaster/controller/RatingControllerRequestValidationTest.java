package com.example.AiTaster.controller;

import com.example.AiTaster.exception.GlobalExceptionHander;
import com.example.AiTaster.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RatingControllerRequestValidationTest {

    @Mock
    private RatingService ratingService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RatingController(ratingService))
                .setControllerAdvice(new GlobalExceptionHander())
                .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"rating\":4.5}",
            "{\"rating\":\"5\"}"
    })
    void createRating_rejectsNonIntegerJsonValues(String requestBody) throws Exception {
        mockMvc.perform(post("/api/ratings/expert-services/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.messages").value("Invalid request body"));

        verifyNoInteractions(ratingService);
    }

    @Test
    void createRating_returnsJsonErrorForMalformedBody() throws Exception {
        mockMvc.perform(post("/api/ratings/expert-services/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.messages").value("Invalid request body"));

        verifyNoInteractions(ratingService);
    }

    @Test
    void filterRatings_returnsJsonErrorForInvalidEnum() throws Exception {
        mockMvc.perform(post("/api/ratings/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sortType\":\"NOT_A_SORT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.messages").value("Invalid request body"));

        verifyNoInteractions(ratingService);
    }
}
