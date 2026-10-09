package sakwe.example.bootjpa.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
    void savesValidAlienAndDisplaysIt() throws Exception {
        mockMvc.perform(post("/AddAlien")
                        .param("aid", "42")
                        .param("aname", "Zorg")
                        .param("tech", "Warp drive"))
                .andExpect(status().isOk())
                .andExpect(view().name("displayAlien"))
                .andExpect(content().string(containsString("42")))
                .andExpect(content().string(containsString("Zorg")));

        Alien savedAlien = repo.findById(42).orElseThrow();
        assertThat(savedAlien.getAname()).isEqualTo("Zorg");
        assertThat(savedAlien.getTech()).isEqualTo("Warp drive");
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
    void rejectsMissingAlienIdWithoutThrowing() throws Exception {
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
        Alien existingAlien = new Alien();
        existingAlien.setAid(7);
        existingAlien.setAname("Existing");
        existingAlien.setTech("Old tech");
        repo.save(existingAlien);

        mockMvc.perform(post("/AddAlien")
                        .param("aid", "7")
                        .param("aname", "Replacement")
                        .param("tech", "New tech"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("That alien ID is already in use.")));

        Alien unchangedAlien = repo.findById(7).orElseThrow();
        assertThat(unchangedAlien.getAname()).isEqualTo("Existing");
        assertThat(unchangedAlien.getTech()).isEqualTo("Old tech");
    }
}
