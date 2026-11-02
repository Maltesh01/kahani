package in.storyfirst.platform.auth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoiceVerificationRecordingRepository extends JpaRepository<VoiceVerificationRecording, Long> {
    Optional<VoiceVerificationRecording> findByProfileId(Long profileId);

    void deleteByProfileId(Long profileId);
}
