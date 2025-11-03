package com.airline.controller;

import com.airline.dto.LoginRequest;
import com.airline.dto.RegisterRequest;
import com.airline.entity.User;
import com.airline.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;

@Controller
@RequestMapping
public class AuthController {

    @Autowired
    private UserService userService;

    // Login Page
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String redirect,
                           Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        model.addAttribute("redirect", redirect != null ? redirect : "/");
        return "auth/login";
    }

    // Login Submit
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginRequest request,
                       BindingResult result,
                       @RequestParam(required = false) String redirect,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "auth/login";
        }

        Optional<User> userOpt = userService.login(request.getUsername(), request.getPassword());
        
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invalid username or password");
            return "redirect:/login" + (redirect != null ? "?redirect=" + redirect : "");
        }

        User user = userOpt.get();
        session.setAttribute("user", user);
        session.setAttribute("userId", user.getId());
        session.setAttribute("userRole", user.getRole().name());

        // Redirect based on role
        if (user.isAdmin()) {
            return "redirect:/admin/dashboard";
        }

        return "redirect:" + (redirect != null ? redirect : "/");
    }

    // Register Page
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    // Register Submit
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest request,
                          BindingResult result,
                          RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(User.UserRole.USER)
                .build();

            userService.register(user);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}

