package in.storyfirst.platform.auth;

import in.storyfirst.platform.profile.Profile;
import in.storyfirst.platform.profile.ProfileCard;
import in.storyfirst.platform.profile.ProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
    private static final List<String> RELATIONSHIP_STATUSES = List.of(
            "SINGLE", "DATING_NOT_SURE_YET", "RECENT_BREAKUP", "AWAITING_DIVORCE",
            "COMMITTED_RELATIONSHIP", "OPEN_RELATIONSHIP");
    private static final Set<String> PURPOSES = Set.of(
            "SILLY_CONVERSATION", "DEEP_CONVERSATION", "HANGOUT_PARTNER",
            "FRIENDSHIP", "LOVE", "ALL_OF_THE_ABOVE");
    private static final Map<String, String> INTERESTS = Map.ofEntries(
            Map.entry("ARTS", "Arts"),
            Map.entry("BOOKS_READING", "Books and reading"),
            Map.entry("BOARD_GAMES", "Board games"),
            Map.entry("COOKING", "Cooking"),
            Map.entry("CULTURE", "Culture"),
            Map.entry("DANCE", "Dance"),
            Map.entry("DESIGN", "Design"),
            Map.entry("ENVIRONMENT", "Environment"),
            Map.entry("FASHION", "Fashion"),
            Map.entry("FILM_TV", "Film and TV"),
            Map.entry("FITNESS", "Fitness"),
            Map.entry("FOOD", "Food"),
            Map.entry("GAMING", "Gaming"),
            Map.entry("GARDENING", "Gardening"),
            Map.entry("HISTORY", "History"),
            Map.entry("MINDFULNESS", "Mindfulness"),
            Map.entry("MUSIC", "Music"),
            Map.entry("OUTDOORS", "Outdoors and hiking"),
            Map.entry("PETS", "Pets"),
            Map.entry("PHOTOGRAPHY", "Photography"),
            Map.entry("PODCASTS", "Podcasts"),
            Map.entry("SCIENCE", "Science"),
            Map.entry("SPIRITUALITY", "Spirituality"),
            Map.entry("SPORTS", "Sports"),
            Map.entry("TECHNOLOGY", "Technology"),
            Map.entry("TRAVEL", "Travel"),
            Map.entry("VOLUNTEERING", "Volunteering"),
            Map.entry("WRITING", "Writing"),
            Map.entry("COMEDY", "Comedy"),
            Map.entry("ENTREPRENEURSHIP", "Entrepreneurship"),
            Map.entry("PERSONAL_GROWTH", "Personal growth"));
    private static final Set<String> LANGUAGES = Set.of(
            "ASSAMESE", "BENGALI", "BODO", "DOGRI", "GUJARATI", "HINDI", "KANNADA",
            "KASHMIRI", "KONKANI", "MAITHILI", "MALAYALAM", "MANIPURI", "MARATHI",
            "NEPALI", "ODIA", "PUNJABI", "SANSKRIT", "SANTALI", "SINDHI", "TAMIL",
            "TELUGU", "URDU");

    private final ConcurrentHashMap<String, PendingSignup> pendingSignups = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SignupDraft> signupDrafts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, EmailChallenge> emailChallenges = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, EmailChallenge> accountIdRecoveryChallenges = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, VerifiedEmail> verifiedEmails = new ConcurrentHashMap<>();
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
        var token = request.signupToken();
        var draft = token == null ? null : signupDrafts.get(token);
        if (draft == null || draft.expiresAt().isBefore(Instant.now())) {
            if (token != null) {
                signupDrafts.remove(token);
            }
            throw new ResponseStatusException(HttpStatus.GONE, "Your signup expired. Please start again.");
        }
        var password = request.password() == null ? "" : request.password();
        if (password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters and no more than 72 bytes");
        }
        if (profileRepository.findByPhoneNumber(draft.phoneNumber()).isPresent()
                || profileRepository.findByEmailIgnoreCase(draft.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That phone number or email already has an account. Sign in instead.");
        }
        var occupation = requiredField(request.occupation(), "Enter what you do for a living.", 120);
        var nativeCity = requiredField(request.nativeCity(), "Enter your native city.", 100);
        var languages = validateSelections(request.languages(), LANGUAGES, "Choose at least one language.");
        var selectedInterests = validateSelections(request.interests(), INTERESTS.keySet(), "Choose at least one interest.");
        var notes = request.interestNotes() == null ? Map.<String, String>of() : request.interestNotes();
        for (var interest : selectedInterests) {
            requiredField(notes.get(interest), "Add a short note for each selected interest.", 300);
        }
        var interestNotes = selectedInterests.stream()
                .map(interest -> INTERESTS.get(interest) + ": " + notes.get(interest).trim())
                .collect(java.util.stream.Collectors.joining("\n"));
        var valuesNote = requiredField(request.valuesNote(), "Tell us what you value most.", 1000);
        if (valuesNote.length() < 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your values note must be at least 20 characters.");
        }
        var story = request.story() == null ? "" : request.story().trim();
        if (wordCount(story) < 250 || story.length() > 20000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your story must be at least 250 words and no more than 20,000 characters.");
        }
        var handle = normalizeInstagramUrl(request.instagramUrl());
        if (!INSTAGRAM_HANDLE.matcher(handle).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid Instagram profile link.");
        }
        var pending = new PendingSignup(
                draft.email(),
                passwordEncoder.encode(password),
                draft.name(),
                draft.gender(),
                draft.dateOfBirth(),
                draft.relationshipStatus(),
                draft.city(),
                draft.purposes(),
                draft.accountId(),
                occupation,
                nativeCity,
                String.join(",", languages),
                story,
                selectedInterests.stream().map(INTERESTS::get).collect(java.util.stream.Collectors.joining(",")),
                interestNotes,
                valuesNote,
                handle);
        if (!signupDrafts.remove(token, draft)) {
            throw new ResponseStatusException(HttpStatus.GONE, "Your signup expired. Please start again.");
        }
        pendingSignups.put(draft.phoneNumber(), pending);

        return new DemoCodeResponse("Use this demo code to verify your phone number.", DEMO_CODE);
    }

    public SignupStartResponse startSignup(SignupStartRequest request) {
        requireDemoMode();
        var verificationToken = request.emailVerificationToken();
        var verifiedEmail = verificationToken == null ? null : verifiedEmails.get(verificationToken);
        if (verifiedEmail == null || verifiedEmail.expiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Verify your email address before continuing.");
        }
        var email = normalizeEmail(request.email());
        if (!verifiedEmail.email().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Verify the email address used for this signup.");
        }
        var phone = normalizePhone(request.phoneNumber());
        if (!E164.matcher(phone).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use an international WhatsApp number, such as +919876543210.");
        }
        if (profileRepository.findByPhoneNumber(phone).isPresent()
                || profileRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That phone number or email already has an account. Sign in instead.");
        }
        var name = requiredField(request.name(), "Enter a name between 2 and 80 characters.", 80);
        if (name.length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a name between 2 and 80 characters.");
        }
        var gender = request.gender() == null ? "" : request.gender().trim().toUpperCase(Locale.ROOT);
        if (!gender.equals("MAN") && !gender.equals("WOMAN")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose man or woman to continue.");
        }
        var dateOfBirth = request.dateOfBirth();
        if (dateOfBirth == null || dateOfBirth.isAfter(LocalDate.now().minusYears(18))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must be at least 18 years old to sign up.");
        }
        var relationshipStatus = request.relationshipStatus() == null
                ? "" : request.relationshipStatus().trim().toUpperCase(Locale.ROOT);
        if (!RELATIONSHIP_STATUSES.contains(relationshipStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a relationship status to continue.");
        }
        var city = requiredField(request.city(), "Enter your current city.", 100);
        var purposes = validateSelections(request.purposes(), PURPOSES, "Choose at least one reason for joining.");
        if (!verifiedEmails.remove(verificationToken, verifiedEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Verify your email address before continuing.");
        }
        signupDrafts.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(Instant.now()));
        var signupToken = UUID.randomUUID().toString();
        var accountId = generateAccountId();
        signupDrafts.put(signupToken, new SignupDraft(
                email, phone, name, gender, dateOfBirth, relationshipStatus, city,
                String.join(",", purposes), accountId, Instant.now().plus(Duration.ofMinutes(30))));
        return new SignupStartResponse(signupToken, accountId);
    }

    public DemoCodeResponse sendEmailCode(EmailCodeRequest request) {
        requireDemoMode();
        var email = normalizeEmail(request.email());
        if (!EMAIL.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid email address");
        }
        if (profileRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That email already has an account. Sign in instead.");
        }
        var now = Instant.now();
        verifiedEmails.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
        emailChallenges.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
        emailChallenges.put(email, new EmailChallenge(now.plus(Duration.ofMinutes(10)), 0));
        return new DemoCodeResponse("Use this demo code to verify your email address.", DEMO_CODE);
    }

    public EmailVerificationResponse verifyEmailCode(VerifyEmailRequest request) {
        requireDemoMode();
        var email = normalizeEmail(request.email());
        if (!EMAIL.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid email address");
        }
        var challenge = emailChallenges.get(email);
        if (challenge == null || challenge.expiresAt().isBefore(Instant.now())) {
            emailChallenges.remove(email);
            throw new ResponseStatusException(HttpStatus.GONE, "Email verification expired. Request a new code.");
        }
        if (!DEMO_CODE.equals(request.code())) {
            var attempts = challenge.attempts() + 1;
            if (attempts >= 5) {
                emailChallenges.remove(email);
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many incorrect codes. Request a new code.");
            }
            emailChallenges.put(email, new EmailChallenge(challenge.expiresAt(), attempts));
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The email verification code is incorrect.");
        }
        emailChallenges.remove(email);
        var token = UUID.randomUUID().toString();
        verifiedEmails.put(token, new VerifiedEmail(email, Instant.now().plus(Duration.ofMinutes(15))));
        return new EmailVerificationResponse("Email verified.", token);
    }

    public DemoCodeResponse sendAccountIdRecoveryCode(EmailCodeRequest request) {
        requireDemoMode();
        var email = normalizeEmail(request.email());
        if (!EMAIL.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid email address");
        }
        accountIdRecoveryChallenges.entrySet()
                .removeIf(entry -> entry.getValue().expiresAt().isBefore(Instant.now()));
        accountIdRecoveryChallenges.put(email, new EmailChallenge(Instant.now().plus(Duration.ofMinutes(10)), 0));
        return new DemoCodeResponse(
                "If an account exists for this email, use the code to recover its Kahani ID.",
                DEMO_CODE);
    }

    @Transactional(readOnly = true)
    public AccountIdRecoveryResponse recoverAccountId(VerifyEmailRequest request) {
        requireDemoMode();
        var email = normalizeEmail(request.email());
        if (!EMAIL.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid email address");
        }
        var challenge = accountIdRecoveryChallenges.get(email);
        if (challenge == null || challenge.expiresAt().isBefore(Instant.now())) {
            accountIdRecoveryChallenges.remove(email);
            throw new ResponseStatusException(HttpStatus.GONE, "Recovery code expired. Request a new code.");
        }
        if (!DEMO_CODE.equals(request.code())) {
            var attempts = challenge.attempts() + 1;
            if (attempts >= 5) {
                accountIdRecoveryChallenges.remove(email);
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many incorrect codes. Request a new code.");
            }
            accountIdRecoveryChallenges.put(email, new EmailChallenge(challenge.expiresAt(), attempts));
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The recovery code is incorrect.");
        }
        if (!accountIdRecoveryChallenges.remove(email, challenge)) {
            throw new ResponseStatusException(HttpStatus.GONE, "Recovery code expired. Request a new code.");
        }
        var accountId = profileRepository.findByEmailIgnoreCase(email)
                .map(Profile::getAccountId)
                .orElse(null);
        return new AccountIdRecoveryResponse(
                "If an account exists for this email, its Kahani ID is shown below.",
                accountId);
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
                "A story from me",
                signup.city(),
                signup.story(),
                signup.interests(),
                signup.gender(),
                signup.dateOfBirth(),
                signup.relationshipStatus(),
                signup.whatHereFor(),
                signup.occupation(),
                signup.nativeCity(),
                signup.languages(),
                signup.interestNotes(),
                signup.valuesNote(),
                phone,
                signup.instagramHandle(),
                "PENDING",
                false,
                false,
                0,
                false);
        profile.setCredentials(
                signup.accountId(),
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
        return login(request, session, servletRequest, false);
    }

    @Transactional(readOnly = true)
    public AccountSummary adminLogin(LoginRequest request, HttpSession session, HttpServletRequest servletRequest) {
        return login(request, session, servletRequest, true);
    }

    private AccountSummary login(
            LoginRequest request,
            HttpSession session,
            HttpServletRequest servletRequest,
            boolean adminLogin) {
        var identifier = request.identifier() == null ? "" : request.identifier().trim();
        var password = request.password() == null ? "" : request.password();
        var profile = identifier.contains("@")
                ? profileRepository.findByEmailIgnoreCase(normalizeEmail(identifier))
                : profileRepository.findByAccountIdIgnoreCase(identifier);
        var candidate = profile.orElse(null);
        if (candidate == null
                || candidate.getPasswordHash() == null
                || candidate.isAdmin() != adminLogin
                || !passwordEncoder.matches(password, candidate.getPasswordHash())) {
            var message = adminLogin
                    ? "Admin email or password is incorrect"
                    : "Email/Kahani ID or password is incorrect";
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
        }
        if (candidate.isSuspended()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account is suspended. Contact Kahani support.");
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
            var profile = profileRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account no longer exists"));
            if (profile.isSuspended()) {
                session.invalidate();
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account is suspended. Contact Kahani support.");
            }
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
        var card = ProfileCard.from(profile, 0, List.of());
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

    private String normalizeInstagramUrl(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            var uri = URI.create(value.trim());
            var host = uri.getHost();
            if (host == null || !(host.equalsIgnoreCase("instagram.com")
                    || host.equalsIgnoreCase("www.instagram.com"))) {
                return "";
            }
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                return "";
            }
            var path = uri.getPath();
            if (path == null) {
                return "";
            }
            var profilePath = path.replaceAll("^/+|/+$", "");
            if (profilePath.contains("/")) {
                return "";
            }
            var handle = profilePath;
            return normalizeInstagramHandle(handle);
        } catch (IllegalArgumentException ex) {
            return "";
        }
    }

    private List<String> validateSelections(List<String> values, Set<String> allowed, String message) {
        if (values == null || values.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        var selected = new java.util.LinkedHashSet<String>();
        for (var value : values) {
            var normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
            if (!allowed.contains(normalized)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
            }
            selected.add(normalized);
        }
        if (selected.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return List.copyOf(selected);
    }

    private String requiredField(String value, String message, int maxLength) {
        var clean = value == null ? "" : value.trim();
        if (clean.isBlank() || clean.length() > maxLength) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return clean;
    }

    private int wordCount(String text) {
        return text.isBlank() ? 0 : text.trim().split("\\s+").length;
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
            LocalDate dateOfBirth,
            String relationshipStatus,
            String city,
            String whatHereFor,
            String accountId,
            String occupation,
            String nativeCity,
            String languages,
            String story,
            String interests,
            String interestNotes,
            String valuesNote,
            String instagramHandle) {
    }

    private record SignupDraft(
            String email,
            String phoneNumber,
            String name,
            String gender,
            LocalDate dateOfBirth,
            String relationshipStatus,
            String city,
            String purposes,
            String accountId,
            Instant expiresAt) {
    }

    private record EmailChallenge(Instant expiresAt, int attempts) {
    }

    private record VerifiedEmail(String email, Instant expiresAt) {
    }
}
