package in.storyfirst.platform.auth;

import in.storyfirst.platform.profile.ProfileCard;

public record AccountSummary(
        ProfileCard profile,
        String gender,
        String accountStatus,
        String verificationStatus,
        String instagramHandle,
        boolean instagramVerified,
        boolean voiceRecordingSubmitted,
        boolean subscriptionActive,
        int creditsBalance,
        String phoneNumber,
        String accountId,
        boolean admin) {
}
