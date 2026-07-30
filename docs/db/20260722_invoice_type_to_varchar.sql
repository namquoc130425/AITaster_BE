-- MySQL ENUM values do not automatically follow additions to the Java enum.
-- VARCHAR keeps invoice types forward-compatible with new application values.
ALTER TABLE invoices
    MODIFY COLUMN invoice_type VARCHAR(50) NOT NULL;
