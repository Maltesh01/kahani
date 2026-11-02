package in.storyfirst.platform.auth;

import in.storyfirst.platform.profile.Profile;
import in.storyfirst.platform.profile.ProfileRepository;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VerificationService {
    private static final long MAX_VOICE_BYTES = 5 * 1024 * 1024;
    private final ProfileRepository profileRepository;
    private final VoiceVerificationRecordingRepository recordingRepository;

    public VerificationService(
            ProfileRepository profileRepository,
            VoiceVerificationRecordingRepository recordingRepository) {
        this.profileRepository = profileRepository;
        this.recordingRepository = recordingRepository;
    }

    @Transactional(readOnly = true)
    public List<VerificationCase> getQueue() {
        return profileRepository.findByVerificationStatusOrderByIdAsc("PENDING").stream()
                .filter(profile -> !profile.isAdmin())
                .map(profile -> new VerificationCase(
                        profile.getId(),
                        profile.getName(),
                        profile.getGender(),
                        profile.getHeadline(),
                        profile.getCity(),
                        profile.getStory(),
                        profile.getInstagramHandle(),
                        profile.isVoiceRecordingSubmitted()))
                .toList();
    }

    @Transactional
    public void submitVoiceRecording(Long profileId, MultipartFile recording) {
        if (recording == null || recording.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose an audio recording first");
        }
        if (recording.getSize() > MAX_VOICE_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Voice recordings must be 5 MB or smaller");
        }
        var contentType = recording.getContentType();
        if (contentType == null || !contentType.startsWith("audio/")) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Upload an audio recording");
        }
        var profile = findProfile(profileId);
        if ("APPROVED".equals(profile.getVerificationStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This account is already verified");
        }
        try {
            var bytes = recording.getBytes();
            recordingRepository.findByProfileId(profileId)
                    .ifPresentOrElse(
                            existing -> existing.replace(contentType, bytes),
                            () -> recordingRepository.save(new VoiceVerificationRecording(profileId, contentType, bytes)));
            profile.markVoiceRecordingSubmitted();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read the voice recording", exception);
        }
    }

    @Transactional(readOnly = true)
    public VoiceRecordingData getVoiceRecording(Long profileId) {
        return recordingRepository.findByProfileId(profileId)
                .map(recording -> new VoiceRecordingData(recording.getContentType(), recording.getRecording()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No voice recording was submitted"));
    }

    @Transactional
    public void review(Long profileId, boolean approved) {
        var profile = findProfile(profileId);
        if (!"PENDING".equals(profile.getVerificationStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This verification request is no longer pending");
        }
        profile.reviewVerification(approved);
        recordingRepository.deleteByProfileId(profileId);
    }

    private Profile findProfile(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }
}
