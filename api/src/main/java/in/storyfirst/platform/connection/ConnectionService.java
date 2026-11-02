package in.storyfirst.platform.connection;

import in.storyfirst.platform.profile.Profile;
import in.storyfirst.platform.profile.ProfileCard;
import in.storyfirst.platform.profile.ProfileRepository;
import in.storyfirst.platform.billing.CreditTransaction;
import in.storyfirst.platform.billing.CreditTransactionRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConnectionService {
    private final ConnectionRequestRepository requestRepository;
    private final ProfileRepository profileRepository;
    private final CreditTransactionRepository creditTransactionRepository;

    public ConnectionService(
            ConnectionRequestRepository requestRepository,
            ProfileRepository profileRepository,
            CreditTransactionRepository creditTransactionRepository) {
        this.requestRepository = requestRepository;
        this.profileRepository = profileRepository;
        this.creditTransactionRepository = creditTransactionRepository;
    }

    @Transactional(readOnly = true)
    public List<ConnectionCard> getConnections(Long currentProfileId) {
        var current = profileRepository.findById(currentProfileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        requireEligible(current);
        return requestRepository
                .findByRequesterIdOrRecipientIdOrderByCreatedAtDesc(currentProfileId, currentProfileId)
                .stream()
                .map(request -> toCard(request, currentProfileId))
                .toList();
    }

    @Transactional
    public ConnectionCard requestConnection(Long currentProfileId, Long profileId) {
        if (profileId.equals(currentProfileId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot connect with your own profile");
        }

        var current = profileRepository.findById(currentProfileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        var target = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        requireEligible(current);
        requireEligible(target);

        var outgoing = requestRepository.findByRequesterIdAndRecipientId(currentProfileId, profileId);
        if (outgoing.isPresent()) {
            return toCard(outgoing.get(), currentProfileId);
        }

        var incoming = requestRepository.findByRequesterIdAndRecipientId(profileId, currentProfileId);
        if (incoming.isPresent()) {
            if ("ACCEPTED".equals(incoming.get().getStatus())) {
                return toCard(incoming.get(), currentProfileId);
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This person has already sent you a request");
        }

        if ("MAN".equals(current.getGender())) {
            if (!current.isSubscriptionActive()) {
                throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "An active subscription is required to send connection requests");
            }
            if (current.getCreditsBalance() < 1) {
                throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "You need 1 credit to send a connection request");
            }
        }

        var request = requestRepository.save(new ConnectionRequest(currentProfileId, profileId));
        if ("MAN".equals(current.getGender())) {
            current.spendCredit();
            creditTransactionRepository.save(new CreditTransaction(
                    currentProfileId,
                    request.getId(),
                    -1,
                    current.getCreditsBalance(),
                    "CONNECTION_REQUEST"));
        }
        return toCard(request, currentProfileId);
    }

    @Transactional
    public ConnectionCard accept(Long currentProfileId, Long requestId) {
        var request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Connection request not found"));
        if (!request.getRecipientId().equals(currentProfileId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only accept requests sent to you");
        }
        if (!"PENDING".equals(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This request is no longer pending");
        }
        var current = profileRepository.findById(currentProfileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        var requester = profileRepository.findById(request.getRequesterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        requireEligible(current);
        requireEligible(requester);
        request.accept();
        return toCard(request, currentProfileId);
    }

    private void requireEligible(Profile profile) {
        if (!"APPROVED".equals(profile.getVerificationStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This account must be approved by the Kahani admin team before connecting");
        }
    }

    private ConnectionCard toCard(ConnectionRequest request, Long currentProfileId) {
        var otherProfileId = request.getRequesterId().equals(currentProfileId)
                ? request.getRecipientId()
                : request.getRequesterId();
        var profile = profileRepository.findById(otherProfileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        var direction = "ACCEPTED".equals(request.getStatus())
                ? "CONNECTED"
                : request.getRequesterId().equals(currentProfileId) ? "OUTGOING" : "INCOMING";
        return new ConnectionCard(request.getId(), request.getStatus(), direction, request.getCreatedAt(), toProfileCard(profile));
    }

    private ProfileCard toProfileCard(Profile profile) {
        return new ProfileCard(
                profile.getId(),
                profile.getName(),
                profile.getHeadline(),
                profile.getCity(),
                profile.getStory(),
                List.of(profile.getInterests().split(",")),
                0,
                List.of());
    }
}
