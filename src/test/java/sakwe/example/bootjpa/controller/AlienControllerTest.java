package sakwe.example.bootjpa.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import sakwe.example.bootjpa.model.Alien;
import sakwe.example.bootjpa.repository.AlienRepo;

// These tests check the main browser actions and their database results.
@SpringBootTest
@AutoConfigureMockMvc
class AlienControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlienRepo repo;

    @BeforeEach
    void clearAliens() {
        repo.deleteAll();
    }

    @Test
    void showsHomePageAndAlienList() throws Exception {
        repo.save(alien(12, "Zorg", "Warp drive"));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("Zorg")))
                .andExpect(content().string(containsString("Warp drive")));
    }

    @Test
    void redirectsOldAddUrlToHome() throws Exception {
        mockMvc.perform(get("/AddAlien")
                        .param("aid", "7")
                        .param("aname", "Ignored")
                        .param("tech", "Ignored"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertThat(repo.count()).isZero();
    }

    @Test
    void addsValidAlien() throws Exception {
        mockMvc.perform(post("/AddAlien")
                        .param("aid", "42")
                        .param("aname", "Zorg")
                        .param("tech", "Warp drive"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        Alien savedAlien = repo.findById(42).orElseThrow();
        assertThat(savedAlien.getAname()).isEqualTo("Zorg");
        assertThat(savedAlien.getTech()).isEqualTo("Warp drive");
    }

    @Test
    void opensEditFormWithCurrentAlienDetails() throws Exception {
        repo.save(alien(8, "Zorg", "Warp drive"));

        mockMvc.perform(get("/aliens/8/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("alien-form"))
                .andExpect(content().string(containsString("value=\"Zorg\"")))
                .andExpect(content().string(containsString("value=\"Warp drive\"")));
    }

    @Test
    void rejectsInvalidAlienWithoutSaving() throws Exception {
        mockMvc.perform(post("/AddAlien")
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
        mockMvc.perform(post("/AddAlien")
                        .param("aname", "Zorg")
                        .param("tech", "Warp drive"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("Alien ID is required.")));

        assertThat(repo.count()).isZero();
    }

    @Test
    void rejectsDuplicateAlienIdInsteadOfOverwriting() throws Exception {
        repo.save(alien(7, "Existing", "Old tech"));

        mockMvc.perform(post("/AddAlien")
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
        repo.save(alien(9, "Before", "Old tech"));

        mockMvc.perform(post("/aliens/9/edit")
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
        repo.save(alien(9, "Before", "Old tech"));

        mockMvc.perform(post("/aliens/9/edit")
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
        repo.save(alien(15, "Delete me", "Unknown"));

        mockMvc.perform(post("/aliens/15/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertThat(repo.existsById(15)).isFalse();
    }

    private Alien alien(int aid, String name, String tech) {
        Alien alien = new Alien();
        alien.setAid(aid);
        alien.setAname(name);
        alien.setTech(tech);
        return alien;
    }
}
