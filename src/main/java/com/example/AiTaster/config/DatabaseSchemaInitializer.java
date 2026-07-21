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
        normalizeInvoiceTypeColumn();
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

    private void normalizeInvoiceTypeColumn() {
        String dataType =
                jdbcTemplate.query(
                        """
                                SELECT data_type
                                FROM information_schema.columns
                                WHERE table_schema = DATABASE()
                                  AND table_name = 'invoices'
                                  AND column_name = 'invoice_type'
                                """,
                        resultSet -> resultSet.next() ? resultSet.getString(1) : null
                );

        if (dataType == null
                || dataType.equalsIgnoreCase("varchar")
                || dataType.equalsIgnoreCase("character varying")) {
            return;
        }

        jdbcTemplate.execute(
                "ALTER TABLE invoices MODIFY COLUMN invoice_type VARCHAR(50) NOT NULL"
        );
        log.info("Normalized invoices.invoice_type column");
    }
}
