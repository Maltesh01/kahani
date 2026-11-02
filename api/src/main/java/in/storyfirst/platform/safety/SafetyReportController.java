package in.storyfirst.platform.safety;

import in.storyfirst.platform.auth.AuthService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class SafetyReportController {
    private final AuthService authService;
    private final SafetyReportService reportService;

    public SafetyReportController(AuthService authService, SafetyReportService reportService) {
        this.authService = authService;
        this.reportService = reportService;
    }

    @PostMapping("/safety/reports")
    public SafetyReportCard submit(@RequestBody SafetyReportRequest request, HttpSession session) {
        return reportService.submit(authService.currentProfileId(session), request);
    }

    @GetMapping("/admin/reports")
    public List<SafetyReportCard> reports(HttpSession session) {
        requireAdmin(session);
        return reportService.getReports();
    }

    @PatchMapping("/admin/reports/{reportId}")
    public SafetyReportCard review(
            @PathVariable Long reportId,
            @RequestBody SafetyReportDecision decision,
            HttpSession session) {
        requireAdmin(session);
        return reportService.review(reportId, decision == null ? null : decision.status());
    }

    private void requireAdmin(HttpSession session) {
        if (!authService.isAdmin(session)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }
}
