package in.storyfirst.platform.auth;

public record VerificationCase(
        Long profileId,
        String name,
        String gender,
        String headline,
        String city,
        String story,
        String instagramHandle,
        boolean voiceRecordingSubmitted,
        String accountId,
        String relationshipStatus,
        String dateOfBirth,
        String whatHereFor,
        String occupation,
        String nativeCity,
        String languages,
        String interests,
        String interestNotes,
        String valuesNote) {
}
