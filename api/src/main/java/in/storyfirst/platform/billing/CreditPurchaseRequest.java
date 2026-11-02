package in.storyfirst.platform.billing;

public record CreditPurchaseRequest(String packageCode, Integer customCredits, String paymentReference) {
}
