package in.storyfirst.platform.safety;

import in.storyfirst.platform.profile.Profile;
import in.storyfirst.platform.profile.ProfileRepository;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SafetyReportService {
    private static final Set<String> CATEGORIES = Set.of(
            "HARASSMENT", "INAPPROPRIATE_CONTENT", "FAKE_PROFILE", "SCAM_OR_SPAM", "OTHER");

    private final SafetyReportRepository reportRepository;
    private final ProfileRepository profileRepository;

    public SafetyReportService(SafetyReportRepository reportRepository, ProfileRepository profileRepository) {
        this.reportRepository = reportRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public SafetyReportCard submit(Long reporterProfileId, SafetyReportRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report details are required.");
        }
        if (request.profileId() == null || request.profileId().equals(reporterProfileId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose another member to report.");
        }
        var target = findProfile(request.profileId());
        var reporter = findProfile(reporterProfileId);
        if (target.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This profile cannot be reported here.");
        }
        var category = request.category() == null ? "" : request.category().trim().toUpperCase(Locale.ROOT);
        if (!CATEGORIES.contains(category)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a valid report reason.");
        }
        var details = request.details() == null ? "" : request.details().trim();
        if (details.length() < 10 || details.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report details must be between 10 and 1,000 characters.");
        }
        return toCard(reportRepository.save(
                new SafetyReport(reporterProfileId, target.getId(), category, details)), reporter, target);
    }

    @Transactional(readOnly = true)
    public List<SafetyReportCard> getReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(report -> toCard(
                        report,
                        findProfile(report.getReporterProfileId()),
                        findProfile(report.getReportedProfileId())))
                .toList();
    }

    @Transactional
    public SafetyReportCard review(Long reportId, String requestedStatus) {
        var status = requestedStatus == null ? "" : requestedStatus.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("RESOLVED", "DISMISSED").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose resolved or dismissed.");
        }
        var report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Safety report not found."));
        if (!"PENDING".equals(report.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This report has already been reviewed.");
        }
        report.review(status);
        return toCard(
                report,
                findProfile(report.getReporterProfileId()),
                findProfile(report.getReportedProfileId()));
    }

    private Profile findProfile(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found."));
    }

    private SafetyReportCard toCard(SafetyReport report, Profile reporter, Profile target) {
        return new SafetyReportCard(
                report.getId(),
                reporter.getId(),
                reporter.getName(),
                reporter.getAccountId(),
                target.getId(),
                target.getName(),
                target.getAccountId(),
                report.getCategory(),
                report.getDetails(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getReviewedAt());
    }
}
