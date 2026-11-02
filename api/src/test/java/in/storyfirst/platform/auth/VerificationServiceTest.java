package in.storyfirst.platform.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import in.storyfirst.platform.profile.Profile;
import in.storyfirst.platform.profile.ProfileRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class VerificationServiceTest {
    private ProfileRepository profileRepository;
    private VoiceVerificationRecordingRepository recordingRepository;
    private VerificationService service;

    @BeforeEach
    void setUp() {
        profileRepository = mock(ProfileRepository.class);
        recordingRepository = mock(VoiceVerificationRecordingRepository.class);
        service = new VerificationService(profileRepository, recordingRepository);
    }

    @Test
    void requiresVoiceRecordingBeforeApprovingWomanAccount() {
        var profile = profile("WOMAN");
        when(profileRepository.findById(8L)).thenReturn(Optional.of(profile));

        var error = assertThrows(ResponseStatusException.class, () -> service.review(8L, true));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals("PENDING", profile.getVerificationStatus());
        verifyNoInteractions(recordingRepository);
    }

    @Test
    void approvesWomanWithSubmittedVoiceRecording() {
        var profile = profile("WOMAN");
        profile.markVoiceRecordingSubmitted();
        when(profileRepository.findById(8L)).thenReturn(Optional.of(profile));

        service.review(8L, true);

        assertEquals("APPROVED", profile.getVerificationStatus());
        verify(recordingRepository).deleteByProfileId(8L);
    }

    @Test
    void voiceRecordingIsOptionalForMen() {
        var profile = profile("MAN");
        when(profileRepository.findById(8L)).thenReturn(Optional.of(profile));

        service.review(8L, true);

        assertEquals("APPROVED", profile.getVerificationStatus());
        verify(recordingRepository).deleteByProfileId(8L);
    }

    private Profile profile(String gender) {
        return new Profile(
                "Test Member",
                "A short headline",
                "Pune",
                "A story",
                "",
                gender,
                null,
                "SINGLE",
                "+919876543210",
                "test_member",
                "PENDING",
                false,
                false,
                0,
                false);
    }
}
