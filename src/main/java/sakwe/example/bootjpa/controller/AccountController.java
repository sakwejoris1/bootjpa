package sakwe.example.bootjpa.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import sakwe.example.bootjpa.repository.UserAccountRepo;
import sakwe.example.bootjpa.service.AccountService;

@Controller
public class AccountController {

    private final AccountService accountService;
    private final UserAccountRepo userAccountRepo;

    public AccountController(AccountService accountService, UserAccountRepo userAccountRepo) {
        this.accountService = accountService;
        this.userAccountRepo = userAccountRepo;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String createAccount(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {
        String normalizedUsername = username.trim();
        if (normalizedUsername.isEmpty() || normalizedUsername.length() > 50) {
            model.addAttribute("error", "Username must be between 1 and 50 characters.");
            model.addAttribute("username", normalizedUsername);
            return "register";
        }
        if (password.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters.");
            model.addAttribute("username", normalizedUsername);
            return "register";
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            model.addAttribute("error", "Password must be 72 bytes or fewer.");
            model.addAttribute("username", normalizedUsername);
            return "register";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("username", normalizedUsername);
            return "register";
        }
        if (userAccountRepo.existsByUsername(normalizedUsername)) {
            model.addAttribute("error", "That username is already taken.");
            model.addAttribute("username", normalizedUsername);
            return "register";
        }

        accountService.register(normalizedUsername, password);
        redirectAttributes.addFlashAttribute("message", "Account created. Please log in.");
        return "redirect:/login";
    }
}
