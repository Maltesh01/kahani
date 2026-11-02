package in.storyfirst.platform;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class ProductionProfileGuardTest {
    @Test
    void allowsLocalDemoProfileOutsideProduction() {
        var environment = new MockEnvironment();
        environment.setActiveProfiles("demo", "local");

        assertDoesNotThrow(() -> new ProductionProfileGuard(environment, true));
    }

    @Test
    void rejectsDemoProfileInProductionEvenWhenDemoModeIsDisabled() {
        var environment = productionEnvironment("prod", "demo");

        assertThrows(IllegalStateException.class, () -> new ProductionProfileGuard(environment, false));
    }

    @Test
    void rejectsDemoAuthenticationInProduction() {
        var environment = productionEnvironment("prod");

        assertThrows(IllegalStateException.class, () -> new ProductionProfileGuard(environment, true));
    }

    @Test
    void allowsProductionWhenDemoModeIsDisabled() {
        var environment = productionEnvironment("prod");

        assertDoesNotThrow(() -> new ProductionProfileGuard(environment, false));
    }

    @Test
    void rejectsEphemeralDatabaseInProduction() {
        var environment = productionEnvironment("prod")
                .withProperty("spring.datasource.url", "jdbc:h2:mem:kahani");

        assertThrows(IllegalStateException.class, () -> new ProductionProfileGuard(environment, false));
    }

    @Test
    void rejectsDestructiveSchemaModeInProduction() {
        var environment = productionEnvironment("prod")
                .withProperty("spring.jpa.hibernate.ddl-auto", "create-drop");

        assertThrows(IllegalStateException.class, () -> new ProductionProfileGuard(environment, false));
    }

    private MockEnvironment productionEnvironment(String... profiles) {
        var environment = new MockEnvironment();
        environment.setActiveProfiles(profiles);
        environment.setProperty("spring.datasource.url", "jdbc:postgresql://db.example.test/kahani?sslmode=verify-full");
        environment.setProperty("spring.jpa.hibernate.ddl-auto", "validate");
        environment.setProperty("spring.flyway.enabled", "true");
        environment.setProperty("server.servlet.session.cookie.secure", "true");
        return environment;
    }
}
