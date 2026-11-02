package in.storyfirst.platform.billing;

public record CreditPurchaseReviewResult(
        Long purchaseId,
        String accountId,
        int creditsAdded,
        int creditBalance,
        boolean subscriptionActive) {
}
