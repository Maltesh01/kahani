package in.storyfirst.platform;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProductionProfileGuard {
    public ProductionProfileGuard(
            Environment environment,
            @Value("${kahani.auth.demo-mode:false}") boolean demoMode) {
        var profiles = environment.getActiveProfiles();
        var productionProfile = Arrays.asList(profiles).contains("prod");
        var demoProfile = Arrays.asList(profiles).contains("demo");
        if (!productionProfile) {
            return;
        }
        if (demoMode || demoProfile) {
            throw new IllegalStateException("The demo profile and demo authentication are prohibited in production.");
        }
        if (!environment.getProperty("spring.datasource.url", "").startsWith("jdbc:postgresql:")) {
            throw new IllegalStateException("Production requires a PostgreSQL datasource.");
        }
        if (!"validate".equals(environment.getProperty("spring.jpa.hibernate.ddl-auto"))
                || !environment.getProperty("spring.flyway.enabled", Boolean.class, false)) {
            throw new IllegalStateException("Production requires Flyway migrations and Hibernate schema validation.");
        }
        if (!environment.getProperty("server.servlet.session.cookie.secure", Boolean.class, false)) {
            throw new IllegalStateException("Production session cookies must be Secure.");
        }
    }
}
