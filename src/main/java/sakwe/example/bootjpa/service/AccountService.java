package sakwe.example.bootjpa.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import sakwe.example.bootjpa.model.UserAccount;
import sakwe.example.bootjpa.repository.UserAccountRepo;

@Service
public class AccountService {

    private final UserAccountRepo userAccountRepo;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserAccountRepo userAccountRepo, PasswordEncoder passwordEncoder) {
        this.userAccountRepo = userAccountRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public UserAccount register(String username, String rawPassword) {
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(rawPassword));
        return userAccountRepo.save(account);
    }
}
