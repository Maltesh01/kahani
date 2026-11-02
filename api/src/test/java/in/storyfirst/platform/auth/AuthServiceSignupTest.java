package in.storyfirst.platform.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import in.storyfirst.platform.profile.Profile;
import in.storyfirst.platform.profile.ProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

class AuthServiceSignupTest {
    private ProfileRepository profileRepository;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        profileRepository = mock(ProfileRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        when(profileRepository.findByEmailIgnoreCase("person@example.com")).thenReturn(Optional.empty());
        when(profileRepository.findByPhoneNumber("+919876543210")).thenReturn(Optional.empty());
        when(profileRepository.existsByAccountId(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);
        when(passwordEncoder.encode("SafePassword123")).thenReturn("encoded-password");
        authService = new AuthService(profileRepository, true, passwordEncoder);
    }

    @Test
    void requiresEmailVerificationBeforePurposeStep() {
        var error = assertThrows(
                ResponseStatusException.class,
                () -> authService.startSignup(startRequest(null, LocalDate.now().minusYears(25), "SINGLE")));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void rejectsSignupForAnyoneUnderEighteen() {
        var token = verifiedEmailToken();

        var error = assertThrows(
                ResponseStatusException.class,
                () -> authService.startSignup(startRequest(token, LocalDate.now().minusYears(17), "SINGLE")));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void rejectsRelationshipStatusesOutsideTheSignupOptions() {
        var token = verifiedEmailToken();

        var error = assertThrows(
                ResponseStatusException.class,
                () -> authService.startSignup(startRequest(token, LocalDate.now().minusYears(25), "MARRIED")));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void generatesUniqueLookingAccountIdAtPurposeStep() {
        var token = verifiedEmailToken();

        var response = authService.startSignup(startRequest(token, LocalDate.now().minusYears(25), "SINGLE"));

        assertTrue(response.accountId().matches("KANI-[A-F0-9]{10}"));
        assertTrue(!response.signupToken().isBlank());
    }

    @Test
    void requiresAtLeastTwoHundredFiftyStoryWordsBeforePhoneVerification() {
        var signup = startSignup();
        var request = new PhoneCodeRequest(
                signup.signupToken(),
                "SafePassword123",
                "Designer",
                "Pune",
                List.of("HINDI", "MARATHI"),
                List.of("BOOKS_READING"),
                Map.of("BOOKS_READING", "I enjoy discovering new authors."),
                "I value kindness and honesty in all relationships.",
                "Too short.",
                "https://www.instagram.com/story_person/");

        var error = assertThrows(ResponseStatusException.class, () -> authService.sendCode(request));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void validatesEverySelectedInterestHasANote() {
        var signup = startSignup();
        var request = new PhoneCodeRequest(
                signup.signupToken(),
                "SafePassword123",
                "Designer",
                "Pune",
                List.of("HINDI", "MARATHI"),
                List.of("BOOKS_READING", "TRAVEL"),
                Map.of("BOOKS_READING", "I love a good book."),
                "I value kindness and honesty in all relationships.",
                longStory(),
                "https://www.instagram.com/story_person/");

        var error = assertThrows(ResponseStatusException.class, () -> authService.sendCode(request));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void acceptsCompleteProfileForPhoneVerification() {
        var signup = startSignup();

        var response = authService.sendCode(validProfileRequest(signup.signupToken()));

        assertEquals("123456", response.demoCode());
    }

    @Test
    void preservesStoryAndProfileFieldMappingAfterPhoneVerification() {
        var signup = startSignup();
        authService.sendCode(validProfileRequest(signup.signupToken()));

        var account = authService.verify(
                new VerifyPhoneRequest("+919876543210", "123456"),
                mock(HttpSession.class),
                mock(HttpServletRequest.class));
        var profile = account.profile();

        assertEquals(List.of("DEEP_CONVERSATION", "FRIENDSHIP"), profile.whatHereFor());
        assertEquals("Designer", profile.occupation());
        assertEquals("Pune", profile.nativeCity());
        assertEquals(List.of("HINDI", "MARATHI"), profile.languages());
        assertEquals(longStory(), profile.story());
        assertEquals(List.of("Books and reading"), profile.interests());
        assertEquals(
                List.of("Books and reading: I enjoy discovering new authors."),
                profile.interestNotes());
        assertEquals("I value kindness and honesty in all relationships.", profile.valuesNote());
    }

    @Test
    void signsInUsingKahaniId() {
        var profile = new Profile(
                "A Person", "Designer", "Pune", longStory(), "Books", "WOMAN",
                null, null, "+919876543210", "story_person", "APPROVED",
                true, false, 0, false);
        profile.setCredentials("KANI-ABCDEF1234", "person@example.com", "encoded-password");
        when(profileRepository.findByAccountIdIgnoreCase("KANI-ABCDEF1234")).thenReturn(Optional.of(profile));
        when(passwordEncoder.matches("SafePassword123", "encoded-password")).thenReturn(true);
        var session = mock(HttpSession.class);
        var servletRequest = mock(HttpServletRequest.class);

        var account = authService.login(
                new LoginRequest("KANI-ABCDEF1234", "SafePassword123"),
                session,
                servletRequest);

        assertEquals("KANI-ABCDEF1234", account.accountId());
        verify(session).setAttribute("profileId", profile.getId());
        verify(servletRequest).changeSessionId();
    }

    @Test
    void revealsAccountIdOnlyAfterRecoveryEmailCodeVerification() {
        var profile = mock(Profile.class);
        when(profile.getAccountId()).thenReturn("KANI-ABCDEF1234");
        when(profileRepository.findByEmailIgnoreCase("person@example.com")).thenReturn(Optional.of(profile));
        authService.sendAccountIdRecoveryCode(new EmailCodeRequest("person@example.com"));

        var response = authService.recoverAccountId(
                new VerifyEmailRequest("person@example.com", "123456"));

        assertEquals("KANI-ABCDEF1234", response.accountId());
    }

    @Test
    void givesSameRecoveryMessageWhenEmailHasNoAccount() {
        when(profileRepository.findByEmailIgnoreCase("person@example.com")).thenReturn(Optional.empty());
        when(profileRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());
        authService.sendAccountIdRecoveryCode(new EmailCodeRequest("person@example.com"));
        var knownResponse = authService.recoverAccountId(
                new VerifyEmailRequest("person@example.com", "123456"));
        authService.sendAccountIdRecoveryCode(new EmailCodeRequest("unknown@example.com"));

        var unknownResponse = authService.recoverAccountId(
                new VerifyEmailRequest("unknown@example.com", "123456"));

        assertEquals(knownResponse.message(), unknownResponse.message());
        assertNull(unknownResponse.accountId());
    }

    private SignupStartResponse startSignup() {
        var token = verifiedEmailToken();
        return authService.startSignup(startRequest(token, LocalDate.now().minusYears(25), "SINGLE"));
    }

    private String verifiedEmailToken() {
        authService.sendEmailCode(new EmailCodeRequest("person@example.com"));
        return authService.verifyEmailCode(new VerifyEmailRequest("person@example.com", "123456"))
                .verificationToken();
    }

    private SignupStartRequest startRequest(String token, LocalDate dateOfBirth, String relationshipStatus) {
        return new SignupStartRequest(
                token,
                "A Person",
                "+919876543210",
                dateOfBirth,
                "WOMAN",
                relationshipStatus,
                "Mumbai",
                "person@example.com",
                List.of("DEEP_CONVERSATION", "FRIENDSHIP"));
    }

    private PhoneCodeRequest validProfileRequest(String token) {
        return new PhoneCodeRequest(
                token,
                "SafePassword123",
                "Designer",
                "Pune",
                List.of("HINDI", "MARATHI"),
                List.of("BOOKS_READING"),
                Map.of("BOOKS_READING", "I enjoy discovering new authors."),
                "I value kindness and honesty in all relationships.",
                longStory(),
                "https://www.instagram.com/story_person/");
    }

    private String longStory() {
        return ("I learned that kindness, curiosity, and honest conversations can change an ordinary day. "
                .repeat(50)).trim();
    }
}
