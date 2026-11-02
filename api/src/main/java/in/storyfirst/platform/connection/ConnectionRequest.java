package in.storyfirst.platform.connection;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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

    protected ConnectionRequest() {
    }

    public ConnectionRequest(Long requesterId, Long recipientId) {
        this.requesterId = requesterId;
        this.recipientId = recipientId;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
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

    public void accept() {
        this.status = "ACCEPTED";
    }
}
