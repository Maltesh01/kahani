package in.storyfirst.platform.billing;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditPurchaseRepository extends JpaRepository<CreditPurchase, Long> {
    List<CreditPurchase> findByStatusOrderByCreatedAtAsc(String status);

    List<CreditPurchase> findAllByOrderByCreatedAtDesc();

    List<CreditPurchase> findByProfileIdOrderByCreatedAtDesc(Long profileId);

    boolean existsByPaymentReference(String paymentReference);

    long countByStatus(String status);

    @org.springframework.data.jpa.repository.Query(
            "select coalesce(sum(p.amountRupees), 0) from CreditPurchase p where p.status = 'APPROVED'")
    long sumApprovedAmountRupees();
}
