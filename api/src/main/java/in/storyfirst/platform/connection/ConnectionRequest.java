package in.storyfirst.platform.connection;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Entity
@Table(name = "connection_requests", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"requesterId", "recipientId"})
})
public class ConnectionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long requesterId;
    private Long recipientId;
    private String status;
    private LocalDateTime createdAt;

    @Column(name = "intro_note", length = 500)
    private String introNote;

    protected ConnectionRequest() {
    }

    public ConnectionRequest(Long requesterId, Long recipientId, String introNote) {
        this.requesterId = requesterId;
        this.recipientId = recipientId;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
        this.introNote = introNote;
    }

    public Long getId() {
        return id;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getIntroNote() {
        return introNote;
    }

    public void updateIntroNote(String introNote) {
        this.introNote = introNote;
    }

    public void accept() {
        this.status = "ACCEPTED";
    }
}
