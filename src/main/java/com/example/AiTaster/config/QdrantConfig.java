package com.example.AiTaster.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class QdrantConfig {
    private final QdrantProperties qdrantProperties;


    @Bean
    public RestClient qdrantRestClient() {
        return RestClient.builder().baseUrl(qdrantProperties.getUrl()).build();
    }


}
