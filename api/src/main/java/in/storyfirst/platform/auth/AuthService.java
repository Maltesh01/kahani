package in.storyfirst.platform.auth;

import in.storyfirst.platform.profile.Profile;
import in.storyfirst.platform.profile.ProfileCard;
import in.storyfirst.platform.profile.ProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private static final String SESSION_PROFILE_ID = "profileId";
    private static final String DEMO_CODE = "123456";
    private static final Pattern E164 = Pattern.compile("^\\+[1-9]\\d{7,14}$");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern INSTAGRAM_HANDLE = Pattern.compile("^[A-Za-z0-9._]{1,30}$");

    private final ConcurrentHashMap<String, PendingSignup> pendingSignups = new ConcurrentHashMap<>();
    private final ProfileRepository profileRepository;
    private final boolean demoMode;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            ProfileRepository profileRepository,
            @Value("${kahani.auth.demo-mode:false}") boolean demoMode,
            PasswordEncoder passwordEncoder) {
        this.profileRepository = profileRepository;
        this.demoMode = demoMode;
        this.passwordEncoder = passwordEncoder;
    }

    public DemoCodeResponse sendCode(PhoneCodeRequest request) {
        requireDemoMode();
        var phone = normalizePhone(request.phoneNumber());
        if (!E164.matcher(phone).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use an international phone number, such as +919876543210");
        }
        var email = normalizeEmail(request.email());
        if (!EMAIL.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid email address");
        }
        var password = request.password() == null ? "" : request.password();
        if (password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters and no more than 72 bytes");
        }
        if (profileRepository.findByPhoneNumber(phone).isPresent()
                || profileRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That phone number or email already has an account. Sign in instead.");
        }

        var name = request.name() == null ? "" : request.name().trim();
        var gender = request.gender() == null ? "" : request.gender().trim().toUpperCase(Locale.ROOT);
        if (name.length() < 2 || name.length() > 80) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a name between 2 and 80 characters");
        }
        if (!gender.equals("MAN") && !gender.equals("WOMAN")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose man or woman to continue");
        }
        var handle = normalizeInstagramHandle(request.instagramHandle());
        if (!INSTAGRAM_HANDLE.matcher(handle).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid Instagram username is required for verification");
        }
        var story = request.story() == null ? "" : request.story().trim();
        if (story.length() < 40 || story.length() > 1200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your story must be between 40 and 1,200 characters");
        }
        pendingSignups.put(phone, new PendingSignup(
                email,
                passwordEncoder.encode(password),
                name,
                gender,
                cleanField(request.headline(), "A story from me", 100),
                cleanField(request.city(), "Not shared", 100),
                story,
                cleanField(request.interests(), "", 300),
                handle));

        return new DemoCodeResponse("Use this demo code to verify your phone number.", DEMO_CODE);
    }

    @Transactional
    public AccountSummary verify(VerifyPhoneRequest request, HttpSession session, HttpServletRequest servletRequest) {
        requireDemoMode();
        var phone = normalizePhone(request.phoneNumber());
        if (!E164.matcher(phone).matches() || !DEMO_CODE.equals(request.code())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The phone number or verification code is incorrect");
        }

        var signup = pendingSignups.get(phone);
        if (signup == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No signup is waiting for phone verification.");
        }
        if (profileRepository.findByPhoneNumber(phone).isPresent()
                || profileRepository.findByEmailIgnoreCase(signup.email()).isPresent()) {
            pendingSignups.remove(phone);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That phone number or email already has an account. Sign in instead.");
        }
        var profile = new Profile(
                signup.name(),
                signup.headline(),
                signup.city(),
                signup.story(),
                signup.interests(),
                signup.gender(),
                phone,
                signup.instagramHandle(),
                "PENDING",
                false,
                false,
                0,
                false);
        profile.setCredentials(
                generateAccountId(),
                signup.email(),
                signup.passwordHash());
        profileRepository.save(profile);
        pendingSignups.remove(phone);

        servletRequest.changeSessionId();
        session.setAttribute(SESSION_PROFILE_ID, profile.getId());
        return toSummary(profile);
    }

    @Transactional(readOnly = true)
    public AccountSummary login(LoginRequest request, HttpSession session, HttpServletRequest servletRequest) {
        var identifier = request.identifier() == null ? "" : request.identifier().trim();
        var password = request.password() == null ? "" : request.password();
        var profile = identifier.contains("@")
                ? profileRepository.findByEmailIgnoreCase(normalizeEmail(identifier))
                : profileRepository.findByPhoneNumber(normalizePhone(identifier));
        var candidate = profile.orElse(null);
        if (candidate == null
                || candidate.getPasswordHash() == null
                || !passwordEncoder.matches(password, candidate.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Phone/email or password is incorrect");
        }
        servletRequest.changeSessionId();
        session.setAttribute(SESSION_PROFILE_ID, candidate.getId());
        return toSummary(candidate);
    }

    @Transactional(readOnly = true)
    public AccountSummary current(HttpSession session) {
        var profileId = currentProfileId(session);
        var profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account no longer exists"));
        return toSummary(profile);
    }

    @Transactional(readOnly = true)
    public Profile requireProfile(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }

    @Transactional(readOnly = true)
    public Long currentProfileId(HttpSession session) {
        var profileId = session.getAttribute(SESSION_PROFILE_ID);
        if (profileId instanceof Long id) {
            return id;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in to continue");
    }

    public boolean isAdmin(HttpSession session) {
        return requireProfile(currentProfileId(session)).isAdmin();
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    private AccountSummary toSummary(Profile profile) {
        var interests = List.of(profile.getInterests().split(",")).stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
        var card = new ProfileCard(
                profile.getId(),
                profile.getName(),
                profile.getHeadline(),
                profile.getCity(),
                profile.getStory(),
                interests,
                0,
                List.of());
        var status = !"APPROVED".equals(profile.getVerificationStatus())
                ? "PENDING_VERIFICATION"
                : "MAN".equals(profile.getGender()) && !profile.isSubscriptionActive()
                        ? "SUBSCRIPTION_REQUIRED"
                        : "ACTIVE";
        return new AccountSummary(
                card,
                profile.getGender(),
                status,
                profile.getVerificationStatus(),
                profile.getInstagramHandle(),
                profile.isInstagramVerified(),
                profile.isVoiceRecordingSubmitted(),
                profile.isSubscriptionActive(),
                profile.getCreditsBalance(),
                profile.getPhoneNumber(),
                profile.getAccountId(),
                profile.isAdmin());
    }

    private String generateAccountId() {
        String accountId;
        do {
            accountId = "KANI-" + UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 10).toUpperCase(Locale.ROOT);
        } while (profileRepository.existsByAccountId(accountId));
        return accountId;
    }

    private String normalizePhone(String phoneNumber) {
        return phoneNumber == null ? "" : phoneNumber.replaceAll("[\\s()-]", "").trim();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeInstagramHandle(String handle) {
        return handle == null ? "" : handle.trim().replaceFirst("^@", "");
    }

    private String cleanField(String value, String fallback, int maxLength) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        var clean = value.trim();
        return clean.length() <= maxLength ? clean : clean.substring(0, maxLength);
    }

    private void requireDemoMode() {
        if (!demoMode) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Phone verification is not configured. Configure a verification provider before enabling signup.");
        }
    }

    private record PendingSignup(
            String email,
            String passwordHash,
            String name,
            String gender,
            String headline,
            String city,
            String story,
            String interests,
            String instagramHandle) {
    }
}
