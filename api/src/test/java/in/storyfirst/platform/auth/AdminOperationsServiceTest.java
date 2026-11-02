package in.storyfirst.platform.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import in.storyfirst.platform.billing.CreditPurchaseRepository;
import in.storyfirst.platform.chat.ChatMessageRepository;
import in.storyfirst.platform.connection.ConnectionRequestRepository;
import in.storyfirst.platform.profile.Profile;
import in.storyfirst.platform.profile.ProfileRepository;
import in.storyfirst.platform.safety.SafetyReportRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminOperationsServiceTest {
    private ProfileRepository profileRepository;
    private CreditPurchaseRepository purchaseRepository;
    private ConnectionRequestRepository connectionRepository;
    private ChatMessageRepository messageRepository;
    private SafetyReportRepository reportRepository;
    private AdminOperationsService service;

    @BeforeEach
    void setUp() {
        profileRepository = mock(ProfileRepository.class);
        purchaseRepository = mock(CreditPurchaseRepository.class);
        connectionRepository = mock(ConnectionRequestRepository.class);
        messageRepository = mock(ChatMessageRepository.class);
        reportRepository = mock(SafetyReportRepository.class);
        service = new AdminOperationsService(
                profileRepository, purchaseRepository, connectionRepository, messageRepository, reportRepository);
    }

    @Test
    void suspensionIsReflectedInMemberManagementSummary() {
        var member = mock(Profile.class);
        when(member.getId()).thenReturn(7L);
        when(member.getName()).thenReturn("Member");
        when(member.getAccountId()).thenReturn("KANI-MEMBER");
        when(member.getVerificationStatus()).thenReturn("APPROVED");
        when(member.getGender()).thenReturn("WOMAN");
        when(member.getCity()).thenReturn("Pune");
        when(member.isSuspended()).thenReturn(true);
        when(profileRepository.findById(7L)).thenReturn(Optional.of(member));

        var summary = service.setSuspended(7L, true);

        assertTrue(summary.suspended());
        verify(member).setSuspended(true);
    }

    @Test
    void analyticsCombinesMemberAndEngagementTotals() {
        var approved = mock(Profile.class);
        when(approved.getVerificationStatus()).thenReturn("APPROVED");
        when(approved.isSubscriptionActive()).thenReturn(true);
        var pending = mock(Profile.class);
        when(pending.getVerificationStatus()).thenReturn("PENDING");
        when(profileRepository.findByAdminFalseOrderByIdDesc()).thenReturn(List.of(approved, pending));
        when(purchaseRepository.countByStatus("PENDING")).thenReturn(2L);
        when(purchaseRepository.countByStatus("APPROVED")).thenReturn(4L);
        when(purchaseRepository.sumApprovedAmountRupees()).thenReturn(2244L);
        when(connectionRepository.countByStatus("ACCEPTED")).thenReturn(3L);
        when(connectionRepository.countByStatus("PENDING")).thenReturn(5L);
        when(messageRepository.count()).thenReturn(12L);
        when(reportRepository.countByStatus("PENDING")).thenReturn(1L);

        var analytics = service.getAnalytics();

        assertEquals(2, analytics.totalUsers());
        assertEquals(1, analytics.approvedProfiles());
        assertEquals(1, analytics.pendingVerifications());
        assertEquals(1, analytics.activeSubscriptions());
        assertEquals(3, analytics.acceptedConnections());
        assertEquals(12, analytics.totalMessages());
        assertEquals(1, analytics.pendingReports());
        assertEquals(2244, analytics.approvedPaymentRevenueRupees());
    }
}
