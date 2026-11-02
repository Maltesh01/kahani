package in.storyfirst.platform.auth;

import in.storyfirst.platform.billing.CreditPurchaseCase;
import in.storyfirst.platform.billing.CreditPurchaseService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class AdminOperationsController {
    private final AuthService authService;
    private final AdminOperationsService adminOperationsService;
    private final CreditPurchaseService creditPurchaseService;

    public AdminOperationsController(
            AuthService authService,
            AdminOperationsService adminOperationsService,
            CreditPurchaseService creditPurchaseService) {
        this.authService = authService;
        this.adminOperationsService = adminOperationsService;
        this.creditPurchaseService = creditPurchaseService;
    }

    @GetMapping("/users")
    public List<AdminUserSummary> users(HttpSession session) {
        requireAdmin(session);
        return adminOperationsService.getUsers();
    }

    @PatchMapping("/users/{profileId}/suspension")
    public AdminUserSummary setSuspension(
            @PathVariable Long profileId,
            @RequestBody AccountSuspensionDecision decision,
            HttpSession session) {
        requireAdmin(session);
        if (decision == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Suspension decision is required.");
        }
        return adminOperationsService.setSuspended(profileId, decision.suspended());
    }

    @GetMapping("/analytics")
    public AdminAnalyticsSummary analytics(HttpSession session) {
        requireAdmin(session);
        return adminOperationsService.getAnalytics();
    }

    @GetMapping("/payments")
    public List<CreditPurchaseCase> payments(HttpSession session) {
        requireAdmin(session);
        return creditPurchaseService.getAllPurchases();
    }

    private void requireAdmin(HttpSession session) {
        if (!authService.isAdmin(session)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }
}
