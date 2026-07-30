package com.example.AiTaster.controller;

import com.example.AiTaster.exception.GlobalExceptionHander;
import com.example.AiTaster.mapper.JobPostMapper;
import com.example.AiTaster.repository.JobPostRepo;
import com.example.AiTaster.service.JobPostAiService;
import com.example.AiTaster.service.JobPostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class JobPostControllerNotFoundTest {

    @Mock
    private JobPostAiService jobPostAiService;
    @Mock
    private JobPostService jobPostService;
    @Mock
    private JobPostRepo jobPostRepo;
    @Mock
    private JobPostMapper jobPostMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        JobPostController controller = new JobPostController(
                jobPostAiService,
                jobPostService,
                jobPostRepo,
                jobPostMapper
        );
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHander())
                .build();
    }

    @Test
    void getJobPostById_returns404JsonWhenJobPostDoesNotExist() throws Exception {
        when(jobPostRepo.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/job-posts/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.messages").value("Job Post Not Found"));
    }
}
