package in.storyfirst.platform.profile;

import java.util.List;

public record ProfileCard(
        Long id,
        String name,
        String headline,
        String city,
        String story,
        List<String> interests,
        int matchPercent,
        List<String> sharedInterests) {
}
