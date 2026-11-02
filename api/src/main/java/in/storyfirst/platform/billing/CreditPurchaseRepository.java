package in.storyfirst.platform.billing;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditPurchaseRepository extends JpaRepository<CreditPurchase, Long> {
    List<CreditPurchase> findByStatusOrderByCreatedAtAsc(String status);

    List<CreditPurchase> findByProfileIdOrderByCreatedAtDesc(Long profileId);

    boolean existsByPaymentReference(String paymentReference);
}
