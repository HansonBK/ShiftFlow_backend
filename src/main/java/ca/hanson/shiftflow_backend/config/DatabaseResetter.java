package ca.hanson.shiftflow_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseResetter implements org.springframework.boot.CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final boolean resetOnStartup;

    public DatabaseResetter(JdbcTemplate jdbcTemplate,
                            @Value("${app.db.reset-on-startup:false}") boolean resetOnStartup) {
        this.jdbcTemplate = jdbcTemplate;
        this.resetOnStartup = resetOnStartup;
    }

    @Override
    public void run(String... args) {
        if (!resetOnStartup) {
            return;
        }

        List<String> tables = jdbcTemplate.queryForList(
                "SELECT tablename FROM pg_tables WHERE schemaname='public' AND tablename <> 'flyway_schema_history'",
                String.class
        );

        if (tables.isEmpty()) {
            return;
        }

        String joined = tables.stream()
                .map(t -> "\"" + t + "\"")
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        jdbcTemplate.execute("TRUNCATE TABLE " + joined + " CASCADE");
    }
}
