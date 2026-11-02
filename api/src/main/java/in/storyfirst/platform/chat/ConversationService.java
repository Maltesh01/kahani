package in.storyfirst.platform.chat;

import in.storyfirst.platform.connection.ConnectionRequestRepository;
import in.storyfirst.platform.connection.InstagramShare;
import in.storyfirst.platform.connection.InstagramShareCard;
import in.storyfirst.platform.connection.InstagramShareRepository;
import in.storyfirst.platform.profile.ProfileRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConversationService {
    private final ConnectionRequestRepository connectionRepository;
    private final ChatMessageRepository messageRepository;
    private final InstagramShareRepository instagramShareRepository;
    private final ProfileRepository profileRepository;

    public ConversationService(
            ConnectionRequestRepository connectionRepository,
            ChatMessageRepository messageRepository,
            InstagramShareRepository instagramShareRepository,
            ProfileRepository profileRepository) {
        this.connectionRepository = connectionRepository;
        this.messageRepository = messageRepository;
        this.instagramShareRepository = instagramShareRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageCard> getMessages(Long profileId, Long requestId) {
        requireConnectedParticipant(profileId, requestId);
        return messageRepository.findByConnectionRequestIdOrderByCreatedAtAsc(requestId).stream()
                .map(message -> new ChatMessageCard(
                        message.getId(),
                        message.getSenderId(),
                        message.getBody(),
                        message.getCreatedAt()))
                .toList();
    }

    @Transactional
    public ChatMessageCard sendMessage(Long profileId, Long requestId, ChatMessageRequest request) {
        requireConnectedParticipant(profileId, requestId);
        var body = request.body() == null ? "" : request.body().trim();
        if (body.isEmpty() || body.length() > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Messages must be between 1 and 2,000 characters");
        }
        var message = messageRepository.save(new ChatMessage(requestId, profileId, body));
        return new ChatMessageCard(message.getId(), message.getSenderId(), message.getBody(), message.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<InstagramShareCard> getInstagramShares(Long profileId, Long requestId) {
        requireConnectedParticipant(profileId, requestId);
        return instagramShareRepository.findByConnectionRequestIdOrderBySharedAtAsc(requestId).stream()
                .map(share -> new InstagramShareCard(share.getSenderId(), share.getHandle(), share.getSharedAt()))
                .toList();
    }

    @Transactional
    public List<InstagramShareCard> shareInstagram(Long profileId, Long requestId) {
        requireConnectedParticipant(profileId, requestId);
        var profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        var handle = profile.getInstagramHandle();
        if (handle == null || handle.isBlank() || !profile.isInstagramVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your Instagram account must be verified before you can share it");
        }
        var existing = instagramShareRepository.findByConnectionRequestIdAndSenderId(requestId, profileId);
        if (existing.isPresent()) {
            existing.get().updateHandle(handle);
        } else {
            instagramShareRepository.save(new InstagramShare(requestId, profileId, handle));
        }
        return getInstagramShares(profileId, requestId);
    }

    @Transactional
    public List<InstagramShareCard> hideInstagram(Long profileId, Long requestId) {
        requireConnectedParticipant(profileId, requestId);
        instagramShareRepository.deleteByConnectionRequestIdAndSenderId(requestId, profileId);
        return getInstagramShares(profileId, requestId);
    }

    private void requireConnectedParticipant(Long profileId, Long requestId) {
        var connection = connectionRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Connection not found"));
        if (!connection.getRequesterId().equals(profileId) && !connection.getRecipientId().equals(profileId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This conversation is not yours");
        }
        if (!"ACCEPTED".equals(connection.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chat and Instagram sharing unlock after both people connect");
        }
        var requester = profileRepository.findById(connection.getRequesterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        var recipient = profileRepository.findById(connection.getRecipientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        if (!"APPROVED".equals(requester.getVerificationStatus())
                || !"APPROVED".equals(recipient.getVerificationStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin verification is required to use chat");
        }
    }
}
