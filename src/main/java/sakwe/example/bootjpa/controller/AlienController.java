package sakwe.example.bootjpa.controller;

import javax.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import sakwe.example.bootjpa.model.Alien;
import sakwe.example.bootjpa.model.UserAccount;
import sakwe.example.bootjpa.repository.AlienRepo;
import sakwe.example.bootjpa.repository.UserAccountRepo;

// Connects browser requests to the alien pages and database operations.
@Controller
public class AlienController {

    private final AlienRepo repo;
    private final UserAccountRepo userAccountRepo;

    public AlienController(AlienRepo repo, UserAccountRepo userAccountRepo) {
        this.repo = repo;
        this.userAccountRepo = userAccountRepo;
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserDetails principal, Model model) {
        UserAccount owner = currentAccount(principal);
        model.addAttribute("username", owner.getUsername());
        model.addAttribute("userId", owner.getId());
        model.addAttribute("aliens", repo.findByOwnerOrderByAidAsc(owner));
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
            @AuthenticationPrincipal UserDetails principal,
            Model model,
            RedirectAttributes redirectAttributes) {
        UserAccount owner = currentAccount(principal);
        if (hasDuplicateId(alien, bindingResult)) {
            bindingResult.rejectValue("aid", "duplicate", "That alien ID is already in use.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("username", owner.getUsername());
            model.addAttribute("userId", owner.getId());
            model.addAttribute("aliens", repo.findByOwnerOrderByAidAsc(owner));
            return "home";
        }

        alien.setOwner(owner);
        repo.saveAndFlush(alien);
        redirectAttributes.addFlashAttribute("message", "Alien added successfully.");
        return "redirect:/";
    }

    @GetMapping("/aliens/{aid}/edit")
    public String editAlien(
            @PathVariable Integer aid,
            @AuthenticationPrincipal UserDetails principal,
            Model model,
            RedirectAttributes redirectAttributes) {
        Alien alien = repo.findByAidAndOwner(aid, currentAccount(principal)).orElse(null);
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
            @AuthenticationPrincipal UserDetails principal,
            Model model,
            RedirectAttributes redirectAttributes) {
        UserAccount owner = currentAccount(principal);
        Alien existingAlien = repo.findByAidAndOwner(aid, owner).orElse(null);
        if (existingAlien == null) {
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

        existingAlien.setAname(alien.getAname());
        existingAlien.setTech(alien.getTech());
        repo.save(existingAlien);
        redirectAttributes.addFlashAttribute("message", "Alien updated successfully.");
        return "redirect:/";
    }

    @PostMapping("/aliens/{aid}/delete")
    public String deleteAlien(
            @PathVariable Integer aid,
            @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirectAttributes) {
        Alien alien = repo.findByAidAndOwner(aid, currentAccount(principal)).orElse(null);
        if (alien == null) {
            redirectAttributes.addFlashAttribute("error", "Alien not found.");
            return "redirect:/";
        }

        repo.delete(alien);
        redirectAttributes.addFlashAttribute("message", "Alien deleted successfully.");
        return "redirect:/";
    }

    private boolean hasDuplicateId(Alien alien, BindingResult bindingResult) {
        Integer aid = alien.getAid();
        return aid != null && aid > 0
                && !bindingResult.hasFieldErrors("aid")
                && repo.existsById(aid);
    }

    private UserAccount currentAccount(UserDetails principal) {
        return userAccountRepo.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated account no longer exists."));
    }
}