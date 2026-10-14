package sakwe.example.bootjpa.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;

import sakwe.example.bootjpa.model.Alien;
import sakwe.example.bootjpa.model.UserAccount;
import sakwe.example.bootjpa.repository.AlienRepo;
import sakwe.example.bootjpa.repository.UserAccountRepo;

// These tests check the main browser actions and their database results.
@SpringBootTest
@AutoConfigureMockMvc
class AlienControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlienRepo repo;

    @Autowired
    private UserAccountRepo userAccountRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserAccount owner;

    @BeforeEach
    void clearData() {
        repo.deleteAll();
        userAccountRepo.deleteAll();
        owner = account("test-user");
        userAccountRepo.save(owner);
    }

    @Test
    void showsHomePageAndAlienList() throws Exception {
        repo.save(alien(12, "Zorg", "Warp drive", owner));

        mockMvc.perform(get("/").with(user("test-user")))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("User ID")))
                .andExpect(content().string(containsString(">" + owner.getId() + "</strong>")))
                .andExpect(content().string(containsString("Zorg")))
                .andExpect(content().string(containsString("Warp drive")));
    }

    @Test
    void redirectsOldAddUrlToHome() throws Exception {
        mockMvc.perform(get("/AddAlien").with(user("test-user"))
                        .param("aid", "7")
                        .param("aname", "Ignored")
                        .param("tech", "Ignored"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertThat(repo.count()).isZero();
    }

    @Test
    void addsValidAlien() throws Exception {
        mockMvc.perform(post("/AddAlien").with(user("test-user")).with(csrf())
                        .param("aid", "42")
                        .param("aname", "Zorg")
                        .param("tech", "Warp drive"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        Alien savedAlien = repo.findById(42).orElseThrow();
        assertThat(savedAlien.getAname()).isEqualTo("Zorg");
        assertThat(savedAlien.getTech()).isEqualTo("Warp drive");
        assertThat(savedAlien.getOwner().getId()).isEqualTo(owner.getId());

        mockMvc.perform(get("/").with(user("test-user")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Zorg")))
                .andExpect(content().string(containsString("Warp drive")));
    }

    @Test
    void opensEditFormWithCurrentAlienDetails() throws Exception {
        repo.save(alien(8, "Zorg", "Warp drive", owner));

        mockMvc.perform(get("/aliens/8/edit").with(user("test-user")))
                .andExpect(status().isOk())
                .andExpect(view().name("alien-form"))
                .andExpect(content().string(containsString("value=\"Zorg\"")))
                .andExpect(content().string(containsString("value=\"Warp drive\"")));
    }

    @Test
    void rejectsInvalidAlienWithoutSaving() throws Exception {
        mockMvc.perform(post("/AddAlien").with(user("test-user")).with(csrf())
                        .param("aid", "0")
                        .param("aname", " ")
                        .param("tech", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("Alien ID must be greater than zero.")))
                .andExpect(content().string(containsString("Alien name is required.")))
                .andExpect(content().string(containsString("Alien technology is required.")));

        assertThat(repo.count()).isZero();
    }

    @Test
    void rejectsMissingAlienIdWithoutSaving() throws Exception {
        mockMvc.perform(post("/AddAlien").with(user("test-user")).with(csrf())
                        .param("aname", "Zorg")
                        .param("tech", "Warp drive"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("Alien ID is required.")));

        assertThat(repo.count()).isZero();
    }

    @Test
    void rejectsDuplicateAlienIdInsteadOfOverwriting() throws Exception {
        repo.save(alien(7, "Existing", "Old tech", owner));

        mockMvc.perform(post("/AddAlien").with(user("test-user")).with(csrf())
                        .param("aid", "7")
                        .param("aname", "Replacement")
                        .param("tech", "New tech"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("That alien ID is already in use.")));

        assertThat(repo.findById(7).orElseThrow().getAname()).isEqualTo("Existing");
    }

    @Test
    void editsAlienDetails() throws Exception {
        repo.save(alien(9, "Before", "Old tech", owner));

        mockMvc.perform(post("/aliens/9/edit").with(user("test-user")).with(csrf())
                        .param("aid", "9")
                        .param("aname", "After")
                        .param("tech", "New tech"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        Alien updatedAlien = repo.findById(9).orElseThrow();
        assertThat(updatedAlien.getAname()).isEqualTo("After");
        assertThat(updatedAlien.getTech()).isEqualTo("New tech");
    }

    @Test
    void preventsChangingAlienIdWhileEditing() throws Exception {
        repo.save(alien(9, "Before", "Old tech", owner));

        mockMvc.perform(post("/aliens/9/edit").with(user("test-user")).with(csrf())
                        .param("aid", "10")
                        .param("aname", "After")
                        .param("tech", "New tech"))
                .andExpect(status().isOk())
                .andExpect(view().name("alien-form"))
                .andExpect(content().string(containsString("The alien ID cannot be changed.")));

        assertThat(repo.findById(9).orElseThrow().getAname()).isEqualTo("Before");
        assertThat(repo.existsById(10)).isFalse();
    }

    @Test
    void deletesAlien() throws Exception {
        repo.save(alien(15, "Delete me", "Unknown", owner));

        mockMvc.perform(post("/aliens/15/delete").with(user("test-user")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertThat(repo.existsById(15)).isFalse();
    }

    @Test
    void redirectsGuestsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void usersCannotSeeOrChangeAnotherUsersAliens() throws Exception {
        UserAccount otherUser = userAccountRepo.save(account("other-user"));
        repo.save(alien(22, "Private alien", "Secret tech", otherUser));

        mockMvc.perform(get("/").with(user("test-user")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Private alien"))));

        mockMvc.perform(post("/aliens/22/delete").with(user("test-user")).with(csrf()))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/aliens/22/edit").with(user("test-user")).with(csrf())
                        .param("aid", "22")
                        .param("aname", "Changed by attacker")
                        .param("tech", "Changed"))
                .andExpect(status().is3xxRedirection());

        assertThat(repo.existsById(22)).isTrue();
        assertThat(repo.findById(22).orElseThrow().getAname()).isEqualTo("Private alien");
    }

    @Test
    void registrationStoresHashedPasswordAndLoginCreatesSession() throws Exception {
        mockMvc.perform(get("/register").with(anonymous()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));

        mockMvc.perform(post("/register").with(anonymous()).with(csrf())
                        .param("username", "new-user")
                        .param("password", "safe-password")
                        .param("confirmPassword", "safe-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        UserAccount registered = userAccountRepo.findByUsername("new-user").orElseThrow();
        assertThat(registered.getId()).isNotNull();
        assertThat(registered.getPassword()).isNotEqualTo("safe-password");
        assertThat(passwordEncoder.matches("safe-password", registered.getPassword())).isTrue();

        MvcResult login = mockMvc.perform(post("/login").with(anonymous()).with(csrf())
                        .param("username", "new-user")
                        .param("password", "safe-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("new-user")))
                .andExpect(content().string(containsString("User ID")));
    }

    private UserAccount account(String username) {
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode("test-password"));
        return account;
    }

    private Alien alien(int aid, String name, String tech, UserAccount alienOwner) {
        Alien alien = new Alien();
        alien.setAid(aid);
        alien.setAname(name);
        alien.setTech(tech);
        alien.setOwner(alienOwner);
        return alien;
    }
}
