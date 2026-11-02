package in.storyfirst.platform.billing;

import in.storyfirst.platform.auth.AuthService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class CreditPurchaseController {
    private final AuthService authService;
    private final CreditPurchaseService creditPurchaseService;

    public CreditPurchaseController(AuthService authService, CreditPurchaseService creditPurchaseService) {
        this.authService = authService;
        this.creditPurchaseService = creditPurchaseService;
    }

    @PostMapping("/credit-requests")
    public CreditPurchaseCase requestPurchase(
            @RequestBody CreditPurchaseRequest request,
            HttpSession session) {
        return creditPurchaseService.requestPurchase(authService.currentProfileId(session), request);
    }

    @GetMapping("/credit-requests")
    public List<CreditPurchaseCase> myPurchases(HttpSession session) {
        return creditPurchaseService.getMyPurchases(authService.currentProfileId(session));
    }
}
