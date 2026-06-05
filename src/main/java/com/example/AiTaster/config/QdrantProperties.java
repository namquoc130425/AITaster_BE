package com.example.AiTaster.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.stereotype.Component;



@Getter
@Setter
@ConfigurationProperties(prefix = "app.qdrant")
public class QdrantProperties {
    private String url;
    private int vectorSize;

    private String distance;
    private CollectionConfig collection = new CollectionConfig();


    @Getter
    @Setter
   public static class CollectionConfig {
        private String skills;
        
    }
}
