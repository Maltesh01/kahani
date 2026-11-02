package in.storyfirst.platform.connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import in.storyfirst.platform.billing.CreditTransactionRepository;
import in.storyfirst.platform.profile.Profile;
import in.storyfirst.platform.profile.ProfileRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ConnectionServiceTest {
    private ConnectionRequestRepository requestRepository;
    private ProfileRepository profileRepository;
    private CreditTransactionRepository creditTransactionRepository;
    private ConnectionService service;

    @BeforeEach
    void setUp() {
        requestRepository = mock(ConnectionRequestRepository.class);
        profileRepository = mock(ProfileRepository.class);
        creditTransactionRepository = mock(CreditTransactionRepository.class);
        service = new ConnectionService(requestRepository, profileRepository, creditTransactionRepository);
        when(requestRepository.findByRequesterIdAndRecipientId(1L, 2L)).thenReturn(Optional.empty());
        when(requestRepository.findByRequesterIdAndRecipientId(2L, 1L)).thenReturn(Optional.empty());
        when(requestRepository.save(any(ConnectionRequest.class))).thenAnswer(invocation -> {
            var request = (ConnectionRequest) invocation.getArgument(0);
            return request;
        });
    }

    @Test
    void manNeedsOneCreditToRequestConnectionWithWoman() {
        var man = profile(1L, "MAN", true, 1);
        var woman = profile(2L, "WOMAN", false, 0);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(man));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(woman));

        var result = service.requestConnection(1L, 2L, "I enjoyed reading your story.");

        assertEquals(0, man.getCreditsBalance());
        assertEquals("I enjoyed reading your story.", result.introNote());
        verify(creditTransactionRepository).save(any());
    }

    @Test
    void manCannotRequestConnectionWithWomanWithoutCredit() {
        var man = profile(1L, "MAN", true, 0);
        var woman = profile(2L, "WOMAN", false, 0);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(man));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(woman));

        var error = assertThrows(ResponseStatusException.class, () ->
                service.requestConnection(1L, 2L, "I enjoyed reading your story."));

        assertEquals(HttpStatus.PAYMENT_REQUIRED, error.getStatusCode());
        verify(requestRepository, never()).save(any());
    }

    @Test
    void womanCanRequestConnectionWithoutCredits() {
        var woman = profile(1L, "WOMAN", false, 0);
        var man = profile(2L, "MAN", false, 0);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(woman));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(man));

        service.requestConnection(1L, 2L, "I enjoyed reading your story.");

        verify(creditTransactionRepository, never()).save(any());
    }

    @Test
    void requiresMeaningfulHelloNoteBeforeSendingRequest() {
        var woman = profile(1L, "WOMAN", false, 0);
        var man = profile(2L, "MAN", false, 0);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(woman));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(man));

        var error = assertThrows(ResponseStatusException.class, () -> service.requestConnection(1L, 2L, "  "));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(requestRepository, never()).save(any());
    }

    @Test
    void updatesNoteWithoutChargingAgainWhenHelloIsAlreadyPending() {
        var sender = profile(1L, "WOMAN", false, 0);
        var recipient = profile(2L, "MAN", false, 0);
        var existing = new ConnectionRequest(1L, 2L, "My first hello note.");
        when(profileRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(requestRepository.findByRequesterIdAndRecipientId(1L, 2L)).thenReturn(Optional.of(existing));

        var result = service.requestConnection(1L, 2L, "A new note after reading your story.");

        assertEquals("A new note after reading your story.", result.introNote());
        verify(requestRepository, never()).save(any());
        verify(creditTransactionRepository, never()).save(any());
    }

    private Profile profile(Long id, String gender, boolean subscription, int credits) {
        var profile = new Profile(
                "Member " + id,
                "A little about me",
                "Pune",
                "My story",
                "Books,Music",
                gender,
                null,
                "SINGLE",
                "+919876543210",
                "member_" + id,
                "APPROVED",
                false,
                subscription,
                credits,
                false);
        when(profileRepository.findById(id)).thenReturn(Optional.of(profile));
        return profile;
    }
}
