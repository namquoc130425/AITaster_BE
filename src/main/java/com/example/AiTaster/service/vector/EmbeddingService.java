package com.example.AiTaster.service.vector;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
// hàm convert Text -> Vettor dùng EmbeddingModel
// dùng 2 lần nên tách service luôn :)))
public class EmbeddingService {
    private final EmbeddingModel embeddingModel;

    public float[] converTextToVector(String text) {
        if(text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text cannot be null or blank");
        }
        return embeddingModel.embed(text);
    }
}
