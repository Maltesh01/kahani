package in.storyfirst.platform.billing;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_transactions")
public class CreditTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long profileId;
    private Long connectionRequestId;
    private int amount;
    private int balanceAfter;
    private String reason;
    private LocalDateTime createdAt;

    protected CreditTransaction() {
    }

    public CreditTransaction(Long profileId, Long connectionRequestId, int amount, int balanceAfter, String reason) {
        this.profileId = profileId;
        this.connectionRequestId = connectionRequestId;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }
}
