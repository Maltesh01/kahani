package in.storyfirst.platform.billing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_purchases")
public class CreditPurchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long profileId;
    private String packageCode;
    private int amountRupees;
    private int credits;

    @Column(unique = true, nullable = false, length = 120)
    private String paymentReference;

    @Column(nullable = false, length = 20)
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    @Version
    private long version;

    protected CreditPurchase() {
    }

    public CreditPurchase(
            Long profileId,
            String packageCode,
            int amountRupees,
            int credits,
            String paymentReference) {
        this.profileId = profileId;
        this.packageCode = packageCode;
        this.amountRupees = amountRupees;
        this.credits = credits;
        this.paymentReference = paymentReference;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getProfileId() {
        return profileId;
    }

    public String getPackageCode() {
        return packageCode;
    }

    public int getAmountRupees() {
        return amountRupees;
    }

    public int getCredits() {
        return credits;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void review(boolean approved) {
        status = approved ? "APPROVED" : "REJECTED";
        reviewedAt = LocalDateTime.now();
    }
}
