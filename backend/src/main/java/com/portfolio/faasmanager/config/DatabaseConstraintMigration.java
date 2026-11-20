package com.portfolio.faasmanager.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConstraintMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseConstraintMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String sql = """
            DO $$
            DECLARE
                r RECORD;
            BEGIN
                FOR r IN (
                    SELECT c.conname, t.relname
                    FROM pg_constraint c
                    JOIN pg_class t ON c.conrelid = t.oid
                    JOIN pg_namespace n ON n.oid = t.relnamespace
                    WHERE n.nspname = current_schema()
                      AND t.relname IN ('tb_device', 'tb_function', 'tb_function_instance')
                      AND c.contype = 'u'
                ) LOOP
                    EXECUTE format('ALTER TABLE %I DROP CONSTRAINT IF EXISTS %I', r.relname, r.conname);
                END LOOP;
            END $$;
            """;

        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            System.err.println("Aviso ao migrar constraints UNIQUE legadas: " + e.getMessage());
        }
    }
}
