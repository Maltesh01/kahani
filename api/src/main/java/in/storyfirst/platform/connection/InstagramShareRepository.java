package in.storyfirst.platform.connection;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstagramShareRepository extends JpaRepository<InstagramShare, Long> {
    List<InstagramShare> findByConnectionRequestIdOrderBySharedAtAsc(Long connectionRequestId);

    Optional<InstagramShare> findByConnectionRequestIdAndSenderId(Long connectionRequestId, Long senderId);

    void deleteByConnectionRequestIdAndSenderId(Long connectionRequestId, Long senderId);
}
