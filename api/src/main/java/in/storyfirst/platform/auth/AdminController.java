package in.storyfirst.platform.auth;

import in.storyfirst.platform.billing.CreditPurchaseCase;
import in.storyfirst.platform.billing.CreditPurchaseDecision;
import in.storyfirst.platform.billing.CreditPurchaseService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class AdminController {
    private final AuthService authService;
    private final VerificationService verificationService;
    private final CreditPurchaseService creditPurchaseService;

    public AdminController(
            AuthService authService,
            VerificationService verificationService,
            CreditPurchaseService creditPurchaseService) {
        this.authService = authService;
        this.verificationService = verificationService;
        this.creditPurchaseService = creditPurchaseService;
    }

    @GetMapping("/admin/verification-queue")
    public List<VerificationCase> queue(HttpSession session) {
        requireAdmin(session);
        return verificationService.getQueue();
    }

    @GetMapping("/admin/payment-queue")
    public List<CreditPurchaseCase> paymentQueue(HttpSession session) {
        requireAdmin(session);
        return creditPurchaseService.getPendingPurchases();
    }

    @PatchMapping("/admin/payments/{purchaseId}")
    public void reviewPayment(
            @PathVariable Long purchaseId,
            @RequestBody CreditPurchaseDecision decision,
            HttpSession session) {
        requireAdmin(session);
        creditPurchaseService.review(purchaseId, decision.approved());
    }

    @GetMapping("/admin/profiles/{profileId}/voice")
    public ResponseEntity<byte[]> voiceRecording(@PathVariable Long profileId, HttpSession session) {
        requireAdmin(session);
        var recording = verificationService.getVoiceRecording(profileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.parseMediaType(recording.contentType()))
                .body(recording.recording());
    }

    @PatchMapping("/admin/profiles/{profileId}/verification")
    public void review(
            @PathVariable Long profileId,
            @RequestBody VerificationDecision decision,
            HttpSession session) {
        requireAdmin(session);
        verificationService.review(profileId, decision.approved());
    }

    @PostMapping("/profiles/me/voice-verification")
    public void submitVoice(
            @RequestParam("recording") MultipartFile recording,
            HttpSession session) {
        verificationService.submitVoiceRecording(authService.currentProfileId(session), recording);
    }

    private void requireAdmin(HttpSession session) {
        if (!authService.isAdmin(session)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }
}
