package in.storyfirst.platform.connection;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "instagram_shares", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"connectionRequestId", "senderId"})
})
public class InstagramShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long connectionRequestId;
    private Long senderId;
    private String handle;
    private LocalDateTime sharedAt;

    protected InstagramShare() {
    }

    public InstagramShare(Long connectionRequestId, Long senderId, String handle) {
        this.connectionRequestId = connectionRequestId;
        this.senderId = senderId;
        this.handle = handle;
        this.sharedAt = LocalDateTime.now();
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getHandle() {
        return handle;
    }

    public LocalDateTime getSharedAt() {
        return sharedAt;
    }

    public void updateHandle(String handle) {
        this.handle = handle;
        this.sharedAt = LocalDateTime.now();
    }
}
