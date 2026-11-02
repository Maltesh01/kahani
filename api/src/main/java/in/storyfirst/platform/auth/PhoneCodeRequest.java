package in.storyfirst.platform.auth;

import java.util.List;
import java.util.Map;

public record PhoneCodeRequest(
        String signupToken,
        String password,
        String occupation,
        String nativeCity,
        List<String> languages,
        List<String> interests,
        Map<String, String> interestNotes,
        String valuesNote,
        String story,
        String instagramUrl) {
}
