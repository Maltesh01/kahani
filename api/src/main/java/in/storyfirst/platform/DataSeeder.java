package in.storyfirst.platform;

import in.storyfirst.platform.connection.ConnectionRequest;
import in.storyfirst.platform.connection.ConnectionRequestRepository;
import in.storyfirst.platform.profile.Profile;
import in.storyfirst.platform.profile.ProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@org.springframework.context.annotation.Profile("demo")
public class DataSeeder {
    @Bean
    CommandLineRunner seedDemoData(
            ProfileRepository profileRepository,
            ConnectionRequestRepository requestRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            if (profileRepository.count() > 0) {
                return;
            }

            var demo = seed(profileRepository, "Alex Morgan", "Product designer", "Bengaluru, India",
                    "I once spent a whole afternoon helping a stranger find the right colors for a community mural. I left with paint on my shoes and a reminder that creative work feels better when it brings people together.",
                    "Design,Photography,Running,Books", "MAN", "+15550000001", "alex.morgan.demo", true, 2, false, passwordEncoder);
            var maya = seed(profileRepository, "Maya Chen", "Creative director & weekend trail runner", "Bengaluru, India",
                    "The best day I had this year started with a wrong turn on a trail. A group of strangers shared snacks, stories, and the longer way home. I still think about how quickly a small kindness can make a place feel familiar.",
                    "Design,Running,Photography,Travel", "WOMAN", "+15550000002", "maya.chen.demo", false, 0, false, passwordEncoder);
            var arjun = seed(profileRepository, "Arjun Rao", "Engineer making climate tech more human", "Bengaluru, India",
                    "My grandfather taught me to repair things before replacing them. These days I bring that same patience to building climate tools, and I love hearing how other people imagine a kinder future for their cities.",
                    "Climate,Technology,Cycling,Books", "MAN", "+15550000003", "arjun.rao.demo", false, 0, false, passwordEncoder);
            var priya = seed(profileRepository, "Priya Nair", "Founder, maker, and curious cook", "Mysuru, India",
                    "A recipe from my mother has no measurements, just stories about who taught her and what was growing that season. I am building a small company now, and try to bring that same care to the things we make.",
                    "Startups,Design,Cooking,Travel", "WOMAN", "+15550000004", "priya.nair.demo", false, 0, false, passwordEncoder);
            var noah = seed(profileRepository, "Noah Williams", "Photographer finding wonder in ordinary days", "Bengaluru, India",
                    "I used to think a good photograph needed a perfect view. A neighbor showed me the light on our ordinary staircase at 6 a.m., and now I look for the small details people usually hurry past.",
                    "Photography,Travel,Running,Music", "MAN", "+15550000005", "noah.williams.demo", false, 0, false, passwordEncoder);
            var sara = seed(profileRepository, "Sara Patel", "Community builder in the arts", "Hyderabad, India",
                    "My first art gathering was four people around a kitchen table. We made a mess, listened to each other, and planned the next one before the tea was cold. I still believe belonging starts small.",
                    "Art,Books,Community,Photography", "WOMAN", "+15550000006", "sara.patel.demo", false, 0, false, passwordEncoder);
            seed(profileRepository, "Leo Martin", "Urban gardener and optimistic amateur", "Bengaluru, India",
                    "A tomato plant survived a summer I was sure would finish it. Looking after that tiny balcony garden taught me more about patience and asking for help than any productivity book ever did.",
                    "Gardening,Climate,Cooking,Cycling", "MAN", "+15550000007", "leo.martin.demo", false, 0, false, passwordEncoder);
            seed(profileRepository, "Aisha Khan", "Writer exploring tech and culture", "Chennai, India",
                    "I started writing letters to myself when I moved to a new city. They became a way to notice what I missed, what surprised me, and the strangers who slowly turned into familiar faces.",
                    "Writing,Technology,Books,Music", "WOMAN", "+15550000008", "aisha.khan.demo", false, 0, false, passwordEncoder);
            seed(profileRepository, "Kahani Admin", "Community verification team", "Private",
                    "Admin account for reviewing demo verification submissions.", "",
                    "MAN", "+15550000009", "kahani.admin.demo", false, 0, true, passwordEncoder);

            requestRepository.save(new ConnectionRequest(maya.getId(), demo.getId(), "Your story about design and running caught my eye."));
            requestRepository.save(new ConnectionRequest(arjun.getId(), demo.getId(), "I would love to hear more about your climate work."));
            requestRepository.save(new ConnectionRequest(demo.getId(), priya.getId(), "Your story made me smile. How did you get into cooking?"));
            var accepted = new ConnectionRequest(noah.getId(), demo.getId(), "I loved your photography story. What do you enjoy capturing most?");
            accepted.accept();
            requestRepository.save(accepted);
        };
    }

    private Profile seed(
            ProfileRepository repository,
            String name,
            String headline,
            String city,
            String story,
            String interests,
            String gender,
            String phoneNumber,
            String instagramHandle,
            boolean subscriptionActive,
            int credits,
            boolean admin,
            PasswordEncoder passwordEncoder) {
        var profile = new Profile(
                name,
                headline,
                city,
                story,
                interests,
                gender,
                null,
                null,
                phoneNumber,
                instagramHandle,
                "APPROVED",
                true,
                subscriptionActive,
                credits,
                admin);
        var accountSuffix = phoneNumber.substring(phoneNumber.length() - 4);
        var emailPrefix = instagramHandle.replaceAll("[^A-Za-z0-9]", "");
        profile.setCredentials(
                "KAHANI-DEMO-" + accountSuffix,
                emailPrefix + "@kahani.demo",
                passwordEncoder.encode("KahaniDemo123!"));
        return repository.save(profile);
    }
}
