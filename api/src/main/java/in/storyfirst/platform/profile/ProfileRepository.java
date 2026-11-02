package in.storyfirst.platform.profile;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    List<Profile> findByIdNot(Long id);

    java.util.Optional<Profile> findByPhoneNumber(String phoneNumber);

    java.util.Optional<Profile> findByEmailIgnoreCase(String email);

    java.util.Optional<Profile> findByAccountIdIgnoreCase(String accountId);

    boolean existsByAccountId(String accountId);

    java.util.List<Profile> findByVerificationStatusOrderByIdAsc(String verificationStatus);
}
