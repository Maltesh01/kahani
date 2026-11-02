package in.storyfirst.platform.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "profiles")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String headline;
    private String city;

    @Column(length = 20000, columnDefinition = "TEXT")
    private String story;

    @Column(length = 2000)
    private String interests;

    @Column(unique = true)
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String accountId;

    private String passwordHash;
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "relationship_status", length = 40)
    private String relationshipStatus;

    @Column(name = "what_here_for", length = 500)
    private String whatHereFor;

    @Column(length = 120)
    private String occupation;

    @Column(name = "native_city", length = 100)
    private String nativeCity;

    @Column(length = 500)
    private String languages;

    @Column(name = "interest_notes", columnDefinition = "TEXT")
    private String interestNotes;

    @Column(name = "values_note", columnDefinition = "TEXT")
    private String valuesNote;

    private String instagramHandle;
    private String verificationStatus;
    private boolean instagramVerified;
    private boolean subscriptionActive;
    private boolean suspended;
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
            LocalDate dateOfBirth,
            String relationshipStatus,
            String phoneNumber,
            String instagramHandle,
            String verificationStatus,
            boolean instagramVerified,
            boolean subscriptionActive,
            int creditsBalance,
            boolean admin) {
        this(name, headline, city, story, interests, gender, dateOfBirth, relationshipStatus,
                null, null, null, null, null, null, phoneNumber, instagramHandle,
                verificationStatus, instagramVerified, subscriptionActive, creditsBalance, admin);
    }

    public Profile(
            String name,
            String headline,
            String city,
            String story,
            String interests,
            String gender,
            LocalDate dateOfBirth,
            String relationshipStatus,
            String whatHereFor,
            String occupation,
            String nativeCity,
            String languages,
            String interestNotes,
            String valuesNote,
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
        this.dateOfBirth = dateOfBirth;
        this.relationshipStatus = relationshipStatus;
        this.whatHereFor = whatHereFor;
        this.occupation = occupation;
        this.nativeCity = nativeCity;
        this.languages = languages;
        this.interestNotes = interestNotes;
        this.valuesNote = valuesNote;
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getRelationshipStatus() {
        return relationshipStatus;
    }

    public String getWhatHereFor() {
        return whatHereFor;
    }

    public String getOccupation() {
        return occupation;
    }

    public String getNativeCity() {
        return nativeCity;
    }

    public String getLanguages() {
        return languages;
    }

    public String getInterestNotes() {
        return interestNotes;
    }

    public String getValuesNote() {
        return valuesNote;
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

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public int getCreditsBalance() {
        return creditsBalance;
    }

    public void addCredits(int credits) {
        if (credits <= 0) {
            throw new IllegalArgumentException("Credits must be positive");
        }
        creditsBalance = Math.addExact(creditsBalance, credits);
        subscriptionActive = true;
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
