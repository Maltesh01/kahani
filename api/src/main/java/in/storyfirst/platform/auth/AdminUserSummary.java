package in.storyfirst.platform.auth;

public record AdminUserSummary(
        Long profileId,
        String name,
        String accountId,
        String email,
        String phoneNumber,
        String gender,
        String city,
        String verificationStatus,
        boolean suspended,
        boolean subscriptionActive,
        int creditsBalance) {
}
