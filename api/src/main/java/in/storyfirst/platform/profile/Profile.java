package in.storyfirst.platform.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "profiles")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String headline;
    private String city;

    @Column(length = 2000)
    private String story;

    @Column(length = 1000)
    private String interests;

    @Column(unique = true)
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String accountId;

    private String passwordHash;
    private String gender;
    private String instagramHandle;
    private String verificationStatus;
    private boolean instagramVerified;
    private boolean subscriptionActive;
    private int creditsBalance;
    private boolean admin;

    private boolean voiceRecordingSubmitted;

    protected Profile() {
    }

    public Profile(
            String name,
            String headline,
            String city,
            String story,
            String interests,
            String gender,
            String phoneNumber,
            String instagramHandle,
            String verificationStatus,
            boolean instagramVerified,
            boolean subscriptionActive,
            int creditsBalance,
            boolean admin) {
        this.name = name;
        this.headline = headline;
        this.city = city;
        this.story = story;
        this.interests = interests;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.instagramHandle = instagramHandle;
        this.verificationStatus = verificationStatus;
        this.instagramVerified = instagramVerified;
        this.subscriptionActive = subscriptionActive;
        this.creditsBalance = creditsBalance;
        this.admin = admin;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHeadline() {
        return headline;
    }

    public String getCity() {
        return city;
    }

    public String getStory() {
        return story;
    }

    public String getInterests() {
        return interests;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setCredentials(String accountId, String email, String passwordHash) {
        this.accountId = accountId;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public String getGender() {
        return gender;
    }

    public String getInstagramHandle() {
        return instagramHandle;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public boolean isInstagramVerified() {
        return instagramVerified;
    }

    public boolean isSubscriptionActive() {
        return subscriptionActive;
    }

    public int getCreditsBalance() {
        return creditsBalance;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isVoiceRecordingSubmitted() {
        return voiceRecordingSubmitted;
    }

    public void markVoiceRecordingSubmitted() {
        this.voiceRecordingSubmitted = true;
        if (!"APPROVED".equals(verificationStatus)) {
            this.verificationStatus = "PENDING";
        }
    }

    public void reviewVerification(boolean approved) {
        this.verificationStatus = approved ? "APPROVED" : "REJECTED";
        this.instagramVerified = approved;
        this.voiceRecordingSubmitted = false;
    }

    public void spendCredit() {
        if (creditsBalance < 1) {
            throw new IllegalStateException("Insufficient credits");
        }
        creditsBalance--;
    }
}
