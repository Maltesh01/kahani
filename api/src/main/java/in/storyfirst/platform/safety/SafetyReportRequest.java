package in.storyfirst.platform.safety;

public record SafetyReportRequest(Long profileId, String category, String details) {
}
