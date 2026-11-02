package in.storyfirst.platform.connection;

import java.time.LocalDateTime;

public record InstagramShareCard(Long senderId, String handle, LocalDateTime sharedAt) {
}
