package in.storyfirst.platform.safety;

import java.time.LocalDateTime;

public record SafetyReportCard(
        Long id,
        Long reporterProfileId,
        String reporterName,
        String reporterAccountId,
        Long reportedProfileId,
        String reportedName,
        String reportedAccountId,
        String category,
        String details,
        String status,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt) {
}
