package in.storyfirst.platform.auth;

public record VerificationCase(
        Long profileId,
        String name,
        String gender,
        String headline,
        String city,
        String story,
        String instagramHandle,
        boolean voiceRecordingSubmitted) {
}
