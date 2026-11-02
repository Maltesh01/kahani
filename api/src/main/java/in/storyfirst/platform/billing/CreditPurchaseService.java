package in.storyfirst.platform.billing;

import in.storyfirst.platform.profile.Profile;
import in.storyfirst.platform.profile.ProfileRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CreditPurchaseService {
    private final CreditPurchaseRepository purchaseRepository;
    private final ProfileRepository profileRepository;
    private final CreditTransactionRepository transactionRepository;

    public CreditPurchaseService(
            CreditPurchaseRepository purchaseRepository,
            ProfileRepository profileRepository,
            CreditTransactionRepository transactionRepository) {
        this.purchaseRepository = purchaseRepository;
        this.profileRepository = profileRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public CreditPurchaseCase requestPurchase(Long profileId, CreditPurchaseRequest request) {
        var profile = findProfile(profileId);
        if (!"MAN".equals(profile.getGender())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Credit purchases are currently available to men's accounts.");
        }
        var packageCode = request.packageCode() == null
                ? "" : request.packageCode().trim().toUpperCase(Locale.ROOT);
        int amount;
        int credits;
        switch (packageCode) {
            case "FIVE_CREDITS" -> {
                amount = 249;
                credits = 5;
            }
            case "TWENTY_CREDITS" -> {
                amount = 899;
                credits = 20;
            }
            case "CUSTOM" -> {
                credits = request.customCredits() == null ? 0 : request.customCredits();
                if (credits < 1 || credits > 10000) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose between 1 and 10,000 custom credits.");
                }
                amount = Math.multiplyExact(credits, 80);
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a valid credit package.");
        }
        var reference = request.paymentReference() == null ? "" : request.paymentReference().trim();
        if (reference.length() < 6 || reference.length() > 120 || reference.chars().anyMatch(Character::isWhitespace)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid UPI/payment reference (6–120 characters, no spaces).");
        }
        if (purchaseRepository.existsByPaymentReference(reference)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That payment reference has already been submitted.");
        }
        var purchase = purchaseRepository.save(
                new CreditPurchase(profileId, packageCode, amount, credits, reference));
        return toCase(purchase, profile);
    }

    @Transactional(readOnly = true)
    public List<CreditPurchaseCase> getMyPurchases(Long profileId) {
        findProfile(profileId);
        return purchaseRepository.findByProfileIdOrderByCreatedAtDesc(profileId).stream()
                .map(purchase -> toCase(purchase, findProfile(purchase.getProfileId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CreditPurchaseCase> getPendingPurchases() {
        return purchaseRepository.findByStatusOrderByCreatedAtAsc("PENDING").stream()
                .map(purchase -> toCase(purchase, findProfile(purchase.getProfileId())))
                .toList();
    }

    @Transactional
    public void review(Long purchaseId, boolean approved) {
        var purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment request not found."));
        if (!"PENDING".equals(purchase.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This payment request has already been reviewed.");
        }
        Profile profile = findProfile(purchase.getProfileId());
        if (approved) {
            profile.addCredits(purchase.getCredits());
            transactionRepository.save(new CreditTransaction(
                    profile.getId(),
                    null,
                    purchase.getCredits(),
                    profile.getCreditsBalance(),
                    "ADMIN_APPROVED_PAYMENT:" + purchase.getId()));
        }
        purchase.review(approved);
    }

    private Profile findProfile(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found."));
    }

    private CreditPurchaseCase toCase(CreditPurchase purchase, Profile profile) {
        return new CreditPurchaseCase(
                purchase.getId(),
                profile.getId(),
                profile.getAccountId(),
                profile.getName(),
                purchase.getPackageCode(),
                purchase.getAmountRupees(),
                purchase.getCredits(),
                purchase.getPaymentReference(),
                purchase.getStatus(),
                purchase.getCreatedAt());
    }
}
