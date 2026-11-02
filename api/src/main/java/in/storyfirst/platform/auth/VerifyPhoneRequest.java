package in.storyfirst.platform.auth;

public record VerifyPhoneRequest(String phoneNumber, String code) {
}
