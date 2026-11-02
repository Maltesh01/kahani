package in.storyfirst.platform.connection;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectionRequestRepository extends JpaRepository<ConnectionRequest, Long> {
    Optional<ConnectionRequest> findByRequesterIdAndRecipientId(Long requesterId, Long recipientId);

    List<ConnectionRequest> findByRequesterIdOrRecipientIdOrderByCreatedAtDesc(Long requesterId, Long recipientId);
}
