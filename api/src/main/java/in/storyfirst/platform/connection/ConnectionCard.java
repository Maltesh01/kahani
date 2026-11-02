package in.storyfirst.platform.connection;

import in.storyfirst.platform.profile.ProfileCard;
import java.time.LocalDateTime;

public record ConnectionCard(
        Long requestId,
        String status,
        String direction,
        LocalDateTime createdAt,
        ProfileCard profile) {
}
