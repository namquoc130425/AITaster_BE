package com.example.AiTaster.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class DatabaseSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        normalizeExpertServiceStatusColumn();
    }

    private void normalizeExpertServiceStatusColumn() {
        Integer columnCount =
                jdbcTemplate.queryForObject(
                        """
                                SELECT COUNT(*)
                                FROM information_schema.columns
                                WHERE table_schema = DATABASE()
                                  AND table_name = 'expert_service'
                                  AND column_name = 'service_status'
                                """,
                        Integer.class
                );

        if (columnCount == null || columnCount == 0) {
            return;
        }

        jdbcTemplate.execute(
                "ALTER TABLE expert_service MODIFY COLUMN service_status VARCHAR(30) NOT NULL"
        );
        log.info("Normalized expert_service.service_status column");
    }
}
