package in.storyfirst.platform.auth;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "voice_verification_recordings", uniqueConstraints = {
        @UniqueConstraint(columnNames = "profileId")
})
public class VoiceVerificationRecording {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long profileId;
    private String contentType;

    @Lob
    private byte[] recording;

    protected VoiceVerificationRecording() {
    }

    public VoiceVerificationRecording(Long profileId, String contentType, byte[] recording) {
        this.profileId = profileId;
        this.contentType = contentType;
        this.recording = recording;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getRecording() {
        return recording;
    }

    public void replace(String contentType, byte[] recording) {
        this.contentType = contentType;
        this.recording = recording;
    }
}
