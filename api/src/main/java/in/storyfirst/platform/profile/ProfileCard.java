package in.storyfirst.platform.profile;

import java.util.List;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;

public record ProfileCard(
        Long id,
        String name,
        String headline,
        String city,
        String gender,
        Integer age,
        String relationshipStatus,
        List<String> whatHereFor,
        String occupation,
        String nativeCity,
        List<String> languages,
        String story,
        List<String> interests,
        List<String> interestNotes,
        String valuesNote,
        int matchPercent,
        List<String> sharedInterests) {

    public static ProfileCard from(Profile profile, int matchPercent, List<String> sharedInterests) {
        return new ProfileCard(
                profile.getId(),
                profile.getName(),
                profile.getHeadline(),
                profile.getCity(),
                profile.getGender(),
                profile.getDateOfBirth() == null ? null : Period.between(profile.getDateOfBirth(), LocalDate.now()).getYears(),
                profile.getRelationshipStatus(),
                split(profile.getWhatHereFor()),
                profile.getOccupation(),
                profile.getNativeCity(),
                split(profile.getLanguages()),
                profile.getStory(),
                split(profile.getInterests()),
                lines(profile.getInterestNotes()),
                profile.getValuesNote(),
                matchPercent,
                sharedInterests);
    }

    private static List<String> split(String value) {
        return value == null ? List.of() : Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private static List<String> lines(String value) {
        return value == null ? List.of() : value.lines()
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }
}
