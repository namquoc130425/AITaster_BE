package com.example.AiTaster.service.vector;

import com.example.AiTaster.config.QdrantProperties;
import com.example.AiTaster.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
// service sữ lý text người dùng nhập vào
@Slf4j
@RequiredArgsConstructor
public class SkillVectorSearchService {
    private final EmbeddingService embeddingService;
    private final QdrantCollectionService qdrantCollectionService;
    private final RestClient qdrantRestClient;
    private final QdrantProperties qdrantProperties;

}
