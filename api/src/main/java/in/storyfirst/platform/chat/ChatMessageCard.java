package in.storyfirst.platform.chat;

import java.time.LocalDateTime;

public record ChatMessageCard(Long id, Long senderId, String body, LocalDateTime createdAt) {
}
