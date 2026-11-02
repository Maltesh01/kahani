package in.storyfirst.platform.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ProfileServiceTest {
    private ProfileRepository profileRepository;
    private ProfileService service;

    @BeforeEach
    void setUp() {
        profileRepository = mock(ProfileRepository.class);
        service = new ProfileService(profileRepository);
    }

    @Test
    void verifiedMembersCanReadOtherVerifiedMembersStoriesRegardlessOfGender() {
        var current = profile(1L, "CURRENT", "APPROVED", false);
        var verifiedWoman = profile(2L, "Verified woman", "APPROVED", false);
        var verifiedMan = profile(3L, "Verified man", "APPROVED", false);
        var pending = profile(4L, "Pending member", "PENDING", false);
        var admin = profile(5L, "Admin", "APPROVED", true);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(current));
        when(profileRepository.findByIdNot(1L)).thenReturn(List.of(verifiedWoman, verifiedMan, pending, admin));

        var stories = service.discover("", 1L);

        assertEquals(List.of("Verified woman", "Verified man"), stories.stream().map(ProfileCard::name).toList());
        assertEquals("Story by Verified woman", stories.get(0).story());
        assertEquals("Story by Verified man", stories.get(1).story());
        assertEquals(List.of("LOVE", "FRIENDSHIP"), stories.get(0).whatHereFor());
        assertEquals(List.of("Hindi", "Marathi"), stories.get(0).languages());
        assertEquals(List.of("Books: I love getting lost in a good story."), stories.get(0).interestNotes());
        assertEquals("Honesty and kindness matter most.", stories.get(0).valuesNote());
    }

    @Test
    void unverifiedMembersCannotBrowseDiscoverStories() {
        var current = profile(1L, "CURRENT", "PENDING", false);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(current));

        var error = assertThrows(ResponseStatusException.class, () -> service.discover("", 1L));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    private Profile profile(Long id, String name, String verificationStatus, boolean admin) {
        var profile = mock(Profile.class);
        when(profile.getId()).thenReturn(id);
        when(profile.getName()).thenReturn(name);
        when(profile.getHeadline()).thenReturn("A little about me");
        when(profile.getCity()).thenReturn("Pune");
        when(profile.getGender()).thenReturn("WOMAN");
        when(profile.getDateOfBirth()).thenReturn(LocalDate.of(1998, 1, 1));
        when(profile.getRelationshipStatus()).thenReturn("SINGLE");
        when(profile.getWhatHereFor()).thenReturn("LOVE,FRIENDSHIP");
        when(profile.getOccupation()).thenReturn("Designer");
        when(profile.getNativeCity()).thenReturn("Nashik");
        when(profile.getLanguages()).thenReturn("Hindi,Marathi");
        when(profile.getStory()).thenReturn("Story by " + name);
        when(profile.getInterests()).thenReturn("Books,Music");
        when(profile.getInterestNotes()).thenReturn("Books: I love getting lost in a good story.");
        when(profile.getValuesNote()).thenReturn("Honesty and kindness matter most.");
        when(profile.getVerificationStatus()).thenReturn(verificationStatus);
        when(profile.isAdmin()).thenReturn(admin);
        return profile;
    }
}
