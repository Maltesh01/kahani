package in.storyfirst.platform.safety;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "safety_reports")
public class SafetyReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reporterProfileId;

    @Column(nullable = false)
    private Long reportedProfileId;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(nullable = false, length = 1000)
    private String details;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;

    protected SafetyReport() {
    }

    public SafetyReport(Long reporterProfileId, Long reportedProfileId, String category, String details) {
        this.reporterProfileId = reporterProfileId;
        this.reportedProfileId = reportedProfileId;
        this.category = category;
        this.details = details;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getReporterProfileId() {
        return reporterProfileId;
    }

    public Long getReportedProfileId() {
        return reportedProfileId;
    }

    public String getCategory() {
        return category;
    }

    public String getDetails() {
        return details;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void review(String status) {
        this.status = status;
        this.reviewedAt = LocalDateTime.now();
    }
}
