package sakwe.example.bootjpa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sakwe.example.bootjpa.model.UserAccount;

public interface UserAccountRepo extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);
}
