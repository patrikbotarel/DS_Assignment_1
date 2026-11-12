package ro.tuc.ds2020.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseConstraintCleaner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConstraintCleaner.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseConstraintCleaner(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(final String... args) {
        try {
            LOGGER.info("Verifying database schema: Ensuring 1-to-N relationship on devices.user_id...");

            // Query MySQL INFORMATION_SCHEMA for any UNIQUE index on column 'user_id' in table 'devices'
            List<String> uniqueIndexes = jdbcTemplate.query(
                    "SELECT DISTINCT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'devices' "
                            + "AND COLUMN_NAME = 'user_id' AND NON_UNIQUE = 0 AND INDEX_NAME != 'PRIMARY'",
                    (rs, rowNum) -> rs.getString("INDEX_NAME")
            );

            for (String indexName : uniqueIndexes) {
                try {
                    LOGGER.info("Dropping UNIQUE index '{}' on devices.user_id to allow One-to-Many associations", indexName);
                    jdbcTemplate.execute("ALTER TABLE devices DROP INDEX " + indexName);
                    LOGGER.info("Successfully dropped UNIQUE index '{}'", indexName);
                } catch (Exception e) {
                    LOGGER.warn("Could not drop index {}: {}", indexName, e.getMessage());
                }
            }

            // Also check for any UNIQUE CONSTRAINT in TABLE_CONSTRAINTS
            List<String> uniqueConstraints = jdbcTemplate.query(
                    "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'devices' "
                            + "AND CONSTRAINT_TYPE = 'UNIQUE' AND CONSTRAINT_NAME != 'PRIMARY'",
                    (rs, rowNum) -> rs.getString("CONSTRAINT_NAME")
            );

            for (String constraintName : uniqueConstraints) {
                try {
                    LOGGER.info("Dropping UNIQUE constraint '{}' on table devices", constraintName);
                    jdbcTemplate.execute("ALTER TABLE devices DROP INDEX " + constraintName);
                    LOGGER.info("Successfully dropped constraint '{}'", constraintName);
                } catch (Exception e) {
                    LOGGER.warn("Could not drop constraint {}: {}", constraintName, e.getMessage());
                }
            }

            LOGGER.info("Schema verification completed. One-to-Many (1:N) relationship on devices.user_id is active.");
        } catch (Exception ex) {
            LOGGER.warn("Database constraint check notice (safe to continue): {}", ex.getMessage());
        }
    }
}
