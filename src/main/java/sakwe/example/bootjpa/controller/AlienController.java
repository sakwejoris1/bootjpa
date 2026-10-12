package sakwe.example.bootjpa.controller;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import sakwe.example.bootjpa.model.Alien;
import sakwe.example.bootjpa.repository.AlienRepo;

// Connects browser requests to the alien pages and database operations.
@Controller
public class AlienController {

    private final AlienRepo repo;

    public AlienController(AlienRepo repo) {
        this.repo = repo;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("aliens", repo.findAll());
        model.addAttribute("alien", new Alien());
        return "home";
    }

    @GetMapping("/AddAlien")
    public String addAlienGet() {
        return "redirect:/";
    }

    @PostMapping("/AddAlien")
    public String addAlien(
            @Valid @ModelAttribute("alien") Alien alien,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (hasDuplicateId(alien, bindingResult)) {
            bindingResult.rejectValue("aid", "duplicate", "That alien ID is already in use.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("aliens", repo.findAll());
            return "home";
        }

        repo.save(alien);
        redirectAttributes.addFlashAttribute("message", "Alien added successfully.");
        return "redirect:/";
    }

    @GetMapping("/aliens/{aid}/edit")
    public String editAlien(@PathVariable Integer aid, Model model, RedirectAttributes redirectAttributes) {
        Alien alien = repo.findById(aid).orElse(null);
        if (alien == null) {
            redirectAttributes.addFlashAttribute("error", "Alien not found.");
            return "redirect:/";
        }

        model.addAttribute("alien", alien);
        model.addAttribute("editing", true);
        model.addAttribute("formAction", "/aliens/" + aid + "/edit");
        return "alien-form";
    }

    @PostMapping("/aliens/{aid}/edit")
    public String updateAlien(
            @PathVariable Integer aid,
            @Valid @ModelAttribute("alien") Alien alien,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (!repo.existsById(aid)) {
            redirectAttributes.addFlashAttribute("error", "Alien not found.");
            return "redirect:/";
        }

        if (!aid.equals(alien.getAid())) {
            bindingResult.rejectValue("aid", "mismatch", "The alien ID cannot be changed.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("editing", true);
            model.addAttribute("formAction", "/aliens/" + aid + "/edit");
            return "alien-form";
        }

        repo.save(alien);
        redirectAttributes.addFlashAttribute("message", "Alien updated successfully.");
        return "redirect:/";
    }

    @PostMapping("/aliens/{aid}/delete")
    public String deleteAlien(@PathVariable Integer aid, RedirectAttributes redirectAttributes) {
        if (!repo.existsById(aid)) {
            redirectAttributes.addFlashAttribute("error", "Alien not found.");
            return "redirect:/";
        }

        repo.deleteById(aid);
        redirectAttributes.addFlashAttribute("message", "Alien deleted successfully.");
        return "redirect:/";
    }

    private boolean hasDuplicateId(Alien alien, BindingResult bindingResult) {
        Integer aid = alien.getAid();
        return aid != null && aid > 0
                && !bindingResult.hasFieldErrors("aid")
                && repo.existsById(aid);
    }
}