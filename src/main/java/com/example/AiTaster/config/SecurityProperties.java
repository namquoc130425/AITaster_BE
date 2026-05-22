package com.example.AiTaster.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "app.security")
@FieldDefaults(level = AccessLevel.PUBLIC)
public class SecurityProperties {
    List<String> publicEndpoints = new ArrayList<>();
}
