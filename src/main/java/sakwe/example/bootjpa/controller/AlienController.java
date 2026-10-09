package sakwe.example.bootjpa.controller;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import sakwe.example.bootjpa.model.Alien;
import sakwe.example.bootjpa.repository.AlienRepo;

@Controller
public class AlienController {

    private final AlienRepo repo;

    public AlienController(AlienRepo repo) {
        this.repo = repo;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("alien", new Alien());
        return "home";
    }

    @PostMapping("/AddAlien")
    public String addAlien(
            @Valid @ModelAttribute("alien") Alien alien,
            BindingResult bindingResult) {
        if (hasDuplicateId(alien, bindingResult)) {
            bindingResult.rejectValue("aid", "duplicate", "That alien ID is already in use.");
        }

        if (bindingResult.hasErrors()) {
            return "home";
        }

        repo.save(alien);
        return "displayAlien";
    }

    private boolean hasDuplicateId(Alien alien, BindingResult bindingResult) {
        Integer aid = alien.getAid();
        return aid != null && aid > 0
                && !bindingResult.hasFieldErrors("aid")
                && repo.existsById(aid);
    }
}