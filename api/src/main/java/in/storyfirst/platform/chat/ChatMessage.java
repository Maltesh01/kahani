package in.storyfirst.platform.chat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long connectionRequestId;
    private Long senderId;

    @Column(length = 2000)
    private String body;
    private LocalDateTime createdAt;

    protected ChatMessage() {
    }

    public ChatMessage(Long connectionRequestId, Long senderId, String body) {
        this.connectionRequestId = connectionRequestId;
        this.senderId = senderId;
        this.body = body;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
