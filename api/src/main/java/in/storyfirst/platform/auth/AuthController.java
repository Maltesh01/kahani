package in.storyfirst.platform.auth;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AccountSummary login(
            @RequestBody LoginRequest request,
            HttpSession session,
            HttpServletRequest servletRequest) {
        return authService.login(request, session, servletRequest);
    }

    @PostMapping("/send-code")
    public DemoCodeResponse sendCode(@RequestBody PhoneCodeRequest request) {
        return authService.sendCode(request);
    }

    @PostMapping("/verify")
    public AccountSummary verify(
            @RequestBody VerifyPhoneRequest request,
            HttpSession session,
            HttpServletRequest servletRequest) {
        return authService.verify(request, session, servletRequest);
    }

    @GetMapping("/me")
    public AccountSummary current(HttpSession session) {
        return authService.current(session);
    }

    @PostMapping("/logout")
    public void logout(HttpSession session) {
        authService.logout(session);
    }
}
