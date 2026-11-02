package in.storyfirst.platform.auth;

public record PhoneCodeRequest(
        String phoneNumber,
        String email,
        String password,
        String name,
        String gender,
        String headline,
        String city,
        String story,
        String interests,
        String instagramHandle) {
}
