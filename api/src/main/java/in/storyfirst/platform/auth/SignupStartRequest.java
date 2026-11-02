package in.storyfirst.platform.auth;

import java.time.LocalDate;
import java.util.List;

public record SignupStartRequest(
        String emailVerificationToken,
        String name,
        String phoneNumber,
        LocalDate dateOfBirth,
        String gender,
        String relationshipStatus,
        String city,
        String email,
        List<String> purposes) {
}
