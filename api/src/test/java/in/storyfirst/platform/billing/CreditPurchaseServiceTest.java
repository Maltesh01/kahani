package in.storyfirst.platform.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import in.storyfirst.platform.profile.Profile;
import in.storyfirst.platform.profile.ProfileRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class CreditPurchaseServiceTest {
    private CreditPurchaseRepository purchaseRepository;
    private ProfileRepository profileRepository;
    private CreditTransactionRepository transactionRepository;
    private CreditPurchaseService service;

    @BeforeEach
    void setUp() {
        purchaseRepository = mock(CreditPurchaseRepository.class);
        profileRepository = mock(ProfileRepository.class);
        transactionRepository = mock(CreditTransactionRepository.class);
        service = new CreditPurchaseService(purchaseRepository, profileRepository, transactionRepository);
        when(purchaseRepository.save(any(CreditPurchase.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsFixedFiveCreditPurchaseAtExpectedPrice() {
        mockProfile("MAN");
        var result = service.requestPurchase(12L, new CreditPurchaseRequest("FIVE_CREDITS", null, "UPI123456"));

        assertEquals(249, result.amountRupees());
        assertEquals(5, result.credits());
        assertEquals("PENDING", result.status());
    }

    @Test
    void pricesCustomCreditsAtEightyRupeesEach() {
        mockProfile("MAN");
        var result = service.requestPurchase(12L, new CreditPurchaseRequest("CUSTOM", 3, "UPI123457"));

        assertEquals(240, result.amountRupees());
        assertEquals(3, result.credits());
    }

    @Test
    void refusesCreditPurchasesForWomen() {
        mockProfile("WOMAN");

        var error = assertThrows(ResponseStatusException.class, () ->
                service.requestPurchase(12L, new CreditPurchaseRequest("FIVE_CREDITS", null, "UPI123458")));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void refusesDuplicatePaymentReferences() {
        mockProfile("MAN");
        when(purchaseRepository.existsByPaymentReference("UPI123459")).thenReturn(true);

        var error = assertThrows(ResponseStatusException.class, () ->
                service.requestPurchase(12L, new CreditPurchaseRequest("FIVE_CREDITS", null, "UPI123459")));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }

    @Test
    void approvingPurchaseAddsCreditsAndRecordsTransaction() {
        var purchase = mock(CreditPurchase.class);
        var profile = new Profile(
                "Test Member", "Designer", "Pune", "A profile story.", "Books", "MAN",
                null, null, "+919876543210", "test_member", "APPROVED",
                true, false, 2, false);
        profile.setCredentials("KANI-TEST12345", "test@example.com", "encoded-password");
        when(purchaseRepository.findById(41L)).thenReturn(Optional.of(purchase));
        when(purchase.getStatus()).thenReturn("PENDING");
        when(purchase.getProfileId()).thenReturn(12L);
        when(purchase.getCredits()).thenReturn(5);
        when(purchase.getId()).thenReturn(41L);
        when(profileRepository.findById(12L)).thenReturn(Optional.of(profile));

        var result = service.review(41L, true);

        assertEquals(41L, result.purchaseId());
        assertEquals("KANI-TEST12345", result.accountId());
        assertEquals(5, result.creditsAdded());
        assertEquals(7, result.creditBalance());
        assertTrue(result.subscriptionActive());
        assertEquals(7, profile.getCreditsBalance());
        assertTrue(profile.isSubscriptionActive());
        verify(transactionRepository).save(any(CreditTransaction.class));
        verify(purchase).review(true);
    }

    @Test
    void preventsReviewingPurchaseTwice() {
        var purchase = mock(CreditPurchase.class);
        when(purchaseRepository.findById(41L)).thenReturn(Optional.of(purchase));
        when(purchase.getStatus()).thenReturn("APPROVED");

        var error = assertThrows(ResponseStatusException.class, () -> service.review(41L, true));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }

    private Profile mockProfile(String gender) {
        var profile = mock(Profile.class);
        when(profile.getId()).thenReturn(12L);
        when(profile.getName()).thenReturn("Test Member");
        when(profile.getAccountId()).thenReturn("KANI-TEST12345");
        when(profile.getGender()).thenReturn(gender);
        when(profile.getCreditsBalance()).thenReturn(5);
        when(profileRepository.findById(12L)).thenReturn(Optional.of(profile));
        return profile;
    }
}
