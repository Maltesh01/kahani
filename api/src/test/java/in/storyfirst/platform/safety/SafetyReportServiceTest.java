package in.storyfirst.platform.safety;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import in.storyfirst.platform.profile.Profile;
import in.storyfirst.platform.profile.ProfileRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class SafetyReportServiceTest {
    private SafetyReportRepository reportRepository;
    private ProfileRepository profileRepository;
    private SafetyReportService service;

    @BeforeEach
    void setUp() {
        reportRepository = mock(SafetyReportRepository.class);
        profileRepository = mock(ProfileRepository.class);
        service = new SafetyReportService(reportRepository, profileRepository);
    }

    @Test
    void memberCanSubmitAReportForAnotherMember() {
        var reporter = profile(1L, "Reporter", "KANI-REPORTER");
        var target = profile(2L, "Target", "KANI-TARGET");
        when(profileRepository.findById(1L)).thenReturn(Optional.of(reporter));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(target));
        when(reportRepository.save(any(SafetyReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.submit(1L, new SafetyReportRequest(2L, "harassment", "Repeated unwanted messages."));

        assertEquals("HARASSMENT", result.category());
        assertEquals("Repeated unwanted messages.", result.details());
        assertEquals("KANI-REPORTER", result.reporterAccountId());
        assertEquals("KANI-TARGET", result.reportedAccountId());
        assertEquals("PENDING", result.status());
        verify(reportRepository).save(any(SafetyReport.class));
    }

    @Test
    void memberCannotReportTheirOwnProfile() {
        var error = assertThrows(ResponseStatusException.class, () ->
                service.submit(1L, new SafetyReportRequest(1L, "OTHER", "This is my own profile.")));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(reportRepository, never()).save(any(SafetyReport.class));
    }

    @Test
    void reportCanOnlyBeReviewedOnce() {
        var reporter = profile(1L, "Reporter", "KANI-REPORTER");
        var target = profile(2L, "Target", "KANI-TARGET");
        var report = new SafetyReport(1L, 2L, "FAKE_PROFILE", "The profile is impersonating someone.");
        when(reportRepository.findById(3L)).thenReturn(Optional.of(report));
        when(profileRepository.findById(1L)).thenReturn(Optional.of(reporter));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(target));

        var reviewed = service.review(3L, "RESOLVED");

        assertEquals("RESOLVED", reviewed.status());
        var error = assertThrows(ResponseStatusException.class, () -> service.review(3L, "DISMISSED"));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }

    private Profile profile(Long id, String name, String accountId) {
        var profile = mock(Profile.class);
        when(profile.getId()).thenReturn(id);
        when(profile.getName()).thenReturn(name);
        when(profile.getAccountId()).thenReturn(accountId);
        when(profile.isAdmin()).thenReturn(false);
        return profile;
    }
}
