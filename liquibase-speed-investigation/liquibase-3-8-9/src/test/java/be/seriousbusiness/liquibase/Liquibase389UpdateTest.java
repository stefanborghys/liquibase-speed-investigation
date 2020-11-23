package be.seriousbusiness.liquibase;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;

import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Liquibase389UpdateTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(Liquibase389UpdateTest.class);

    @BeforeEach
    public void clear() {
        TestLiquibase.clearDatabase();
    }

    @Test
    public void measureUpdate() throws SQLException, LiquibaseException {
        final Temporal start = LocalDateTime.now();
        TestLiquibase.updateDatabase();
        final Duration duration = Duration.between(start, LocalDateTime.now());
        LOGGER.info("Migration took: {} millis or rounded {} sec (rounded)", duration.toMillis(), duration.toSeconds());
    }
}
