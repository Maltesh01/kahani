package in.storyfirst.platform.safety;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SafetyReportRepository extends JpaRepository<SafetyReport, Long> {
    List<SafetyReport> findAllByOrderByCreatedAtDesc();

    long countByStatus(String status);
}
