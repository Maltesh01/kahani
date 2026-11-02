package in.storyfirst.platform.profile;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public List<ProfileCard> discover(String search, Long profileId) {
        var current = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demo profile not found"));
        if (!"APPROVED".equals(current.getVerificationStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your account must be approved before you can discover stories");
        }
        var currentInterests = interestSet(current);
        var query = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);

        return profileRepository.findByIdNot(profileId).stream()
                .filter(profile -> !profile.isAdmin())
                .filter(profile -> "APPROVED".equals(profile.getVerificationStatus()))
                .filter(profile -> query.isEmpty() || matches(profile, query))
                .map(profile -> {
                    var shared = interestSet(profile).stream()
                            .filter(currentInterests::contains)
                            .sorted()
                            .toList();
                    var union = currentInterests.size() + interestSet(profile).size() - shared.size();
                    var score = union == 0 ? 0 : (int) Math.round(shared.size() * 100.0 / union);
                    return toCard(profile, score, shared);
                })
                .sorted((first, second) -> Integer.compare(second.matchPercent(), first.matchPercent()))
                .toList();
    }

    private boolean matches(Profile profile, String query) {
        return (profile.getName() + " " + profile.getHeadline() + " " + profile.getCity() + " "
                + profile.getStory() + " " + profile.getInterests()).toLowerCase(Locale.ROOT).contains(query);
    }

    private Set<String> interestSet(Profile profile) {
        return Arrays.stream(profile.getInterests().split(","))
                .map(String::trim)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .filter(value -> !value.isBlank())
                .collect(Collectors.toSet());
    }

    private ProfileCard toCard(Profile profile, int matchPercent, List<String> shared) {
        var interests = Arrays.stream(profile.getInterests().split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
        return new ProfileCard(
                profile.getId(),
                profile.getName(),
                profile.getHeadline(),
                profile.getCity(),
                profile.getStory(),
                interests,
                matchPercent,
                shared);
    }
}
