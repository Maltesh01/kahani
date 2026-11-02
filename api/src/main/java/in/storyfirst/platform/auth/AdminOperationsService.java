package in.storyfirst.platform.auth;

import in.storyfirst.platform.billing.CreditPurchaseRepository;
import in.storyfirst.platform.chat.ChatMessageRepository;
import in.storyfirst.platform.connection.ConnectionRequestRepository;
import in.storyfirst.platform.profile.Profile;
import in.storyfirst.platform.profile.ProfileRepository;
import in.storyfirst.platform.safety.SafetyReportRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminOperationsService {
    private final ProfileRepository profileRepository;
    private final CreditPurchaseRepository purchaseRepository;
    private final ConnectionRequestRepository connectionRepository;
    private final ChatMessageRepository messageRepository;
    private final SafetyReportRepository reportRepository;

    public AdminOperationsService(
            ProfileRepository profileRepository,
            CreditPurchaseRepository purchaseRepository,
            ConnectionRequestRepository connectionRepository,
            ChatMessageRepository messageRepository,
            SafetyReportRepository reportRepository) {
        this.profileRepository = profileRepository;
        this.purchaseRepository = purchaseRepository;
        this.connectionRepository = connectionRepository;
        this.messageRepository = messageRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminUserSummary> getUsers() {
        return profileRepository.findByAdminFalseOrderByIdDesc().stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public AdminUserSummary setSuspended(Long profileId, boolean suspended) {
        var profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found."));
        if (profile.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin accounts cannot be moderated here.");
        }
        profile.setSuspended(suspended);
        return toSummary(profile);
    }

    @Transactional(readOnly = true)
    public AdminAnalyticsSummary getAnalytics() {
        var users = profileRepository.findByAdminFalseOrderByIdDesc();
        return new AdminAnalyticsSummary(
                users.size(),
                users.stream().filter(profile -> "APPROVED".equals(profile.getVerificationStatus())).count(),
                users.stream().filter(profile -> "PENDING".equals(profile.getVerificationStatus())).count(),
                users.stream().filter(Profile::isSuspended).count(),
                users.stream().filter(Profile::isSubscriptionActive).count(),
                purchaseRepository.countByStatus("PENDING"),
                purchaseRepository.countByStatus("APPROVED"),
                connectionRepository.countByStatus("ACCEPTED"),
                connectionRepository.countByStatus("PENDING"),
                messageRepository.count(),
                reportRepository.countByStatus("PENDING"),
                purchaseRepository.sumApprovedAmountRupees());
    }

    private AdminUserSummary toSummary(Profile profile) {
        return new AdminUserSummary(
                profile.getId(),
                profile.getName(),
                profile.getAccountId(),
                profile.getEmail(),
                profile.getPhoneNumber(),
                profile.getGender(),
                profile.getCity(),
                profile.getVerificationStatus(),
                profile.isSuspended(),
                profile.isSubscriptionActive(),
                profile.getCreditsBalance());
    }
}
