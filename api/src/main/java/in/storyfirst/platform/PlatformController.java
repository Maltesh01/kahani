package in.storyfirst.platform;

import in.storyfirst.platform.auth.AccountSummary;
import in.storyfirst.platform.auth.AuthService;
import in.storyfirst.platform.chat.ChatMessageCard;
import in.storyfirst.platform.chat.ChatMessageRequest;
import in.storyfirst.platform.chat.ConversationService;
import in.storyfirst.platform.connection.ConnectionCard;
import in.storyfirst.platform.connection.InstagramShareCard;
import in.storyfirst.platform.connection.ConnectionService;
import in.storyfirst.platform.profile.ProfileCard;
import in.storyfirst.platform.profile.ProfileService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PlatformController {
    private final ProfileService profileService;
    private final ConnectionService connectionService;
    private final AuthService authService;
    private final ConversationService conversationService;

    public PlatformController(
            ProfileService profileService,
            ConnectionService connectionService,
            AuthService authService,
            ConversationService conversationService) {
        this.profileService = profileService;
        this.connectionService = connectionService;
        this.authService = authService;
        this.conversationService = conversationService;
    }

    @GetMapping("/profiles/me")
    public AccountSummary getCurrentProfile(HttpSession session) {
        return authService.current(session);
    }

    @GetMapping("/profiles/discover")
    public List<ProfileCard> discover(@RequestParam(defaultValue = "") String search, HttpSession session) {
        return profileService.discover(search, authService.currentProfileId(session));
    }

    @GetMapping("/connections")
    public List<ConnectionCard> getConnections(HttpSession session) {
        return connectionService.getConnections(authService.currentProfileId(session));
    }

    @PostMapping("/connections/{profileId}")
    public ConnectionCard requestConnection(@PathVariable Long profileId, HttpSession session) {
        return connectionService.requestConnection(authService.currentProfileId(session), profileId);
    }

    @PatchMapping("/connections/{requestId}/accept")
    public ConnectionCard acceptConnection(@PathVariable Long requestId, HttpSession session) {
        return connectionService.accept(authService.currentProfileId(session), requestId);
    }

    @GetMapping("/connections/{requestId}/messages")
    public List<ChatMessageCard> getMessages(@PathVariable Long requestId, HttpSession session) {
        return conversationService.getMessages(authService.currentProfileId(session), requestId);
    }

    @PostMapping("/connections/{requestId}/messages")
    public ChatMessageCard sendMessage(
            @PathVariable Long requestId,
            @RequestBody ChatMessageRequest request,
            HttpSession session) {
        return conversationService.sendMessage(authService.currentProfileId(session), requestId, request);
    }

    @GetMapping("/connections/{requestId}/instagram-shares")
    public List<InstagramShareCard> getInstagramShares(@PathVariable Long requestId, HttpSession session) {
        return conversationService.getInstagramShares(authService.currentProfileId(session), requestId);
    }

    @PostMapping("/connections/{requestId}/instagram-share")
    public List<InstagramShareCard> shareInstagram(@PathVariable Long requestId, HttpSession session) {
        return conversationService.shareInstagram(authService.currentProfileId(session), requestId);
    }

    @DeleteMapping("/connections/{requestId}/instagram-share")
    public List<InstagramShareCard> hideInstagram(@PathVariable Long requestId, HttpSession session) {
        return conversationService.hideInstagram(authService.currentProfileId(session), requestId);
    }
}
