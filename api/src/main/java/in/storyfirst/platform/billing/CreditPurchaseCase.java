package in.storyfirst.platform.billing;

import java.time.LocalDateTime;

public record CreditPurchaseCase(
        Long id,
        Long profileId,
        String accountId,
        String name,
        String packageCode,
        int amountRupees,
        int credits,
        String paymentReference,
        String status,
        LocalDateTime createdAt) {
}
