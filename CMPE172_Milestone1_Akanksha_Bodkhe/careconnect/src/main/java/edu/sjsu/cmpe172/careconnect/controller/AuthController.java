package edu.sjsu.cmpe172.careconnect.controller;

import edu.sjsu.cmpe172.careconnect.dto.LoginForm;
import edu.sjsu.cmpe172.careconnect.model.UserAccount;
import edu.sjsu.cmpe172.careconnect.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String showLogin(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @Valid LoginForm form,
            BindingResult bindingResult,
            HttpSession session,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        try {
            UserAccount user = authService.authenticate(
                    form.getEmail(),
                    form.getPassword()
            );

            // Server-side session
            session.setAttribute("user", user);

            return "redirect:/app/slots";

        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", exception.getMessage());
            return "login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
