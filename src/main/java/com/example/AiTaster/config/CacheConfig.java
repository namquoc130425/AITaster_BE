package com.example.AiTaster.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration // Báo Spring đây là class cấu hình.
@EnableCaching // Bật cơ chế @Cacheable và @CacheEvict.
public class CacheConfig {
}
