package in.storyfirst.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("integration")
@EnabledIfEnvironmentVariable(named = "KAHANI_TEST_DATABASE_URL", matches = "jdbc:postgresql:.*")
class PostgresSchemaIntegrationTest {
    @Autowired
    private Flyway flyway;

    @Test
    void migratesPostgresSchemaAndMatchesHibernateEntities() {
        assertEquals("5", flyway.info().current().getVersion().getVersion());
    }
}
