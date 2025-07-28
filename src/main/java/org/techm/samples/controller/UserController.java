package org.techm.samples.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.techm.samples.dto.AuthRequest;
import org.techm.samples.entity.Role;
import org.techm.samples.entity.User;
import org.techm.samples.exception.DuplicateUserException;
import org.techm.samples.service.auth.JwtService;
import org.techm.samples.service.auth.UserInfoService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private UserInfoService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    

    // Thymeleaf: Show registration page
    @GetMapping("/registerPage")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register"; // maps to register.html
    }

    // Thymeleaf: Handle registration form
    @PostMapping("/registerUser")
    public String registerUser(@ModelAttribute("user") User user, @RequestParam("confirmPassword") String confirmPassword, Model model) {
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Password and Confirm Password do not match.");
            return "register";
        }
        try {
            user.setRole(Role.CUSTOMER);
            service.addUser(user);
            return "redirect:/auth/loginPage";
        } catch (DuplicateUserException e) {
            model.addAttribute("errorMessage", "A user with email " + user.getEmail() + " already exists.");
            return "register";
        }
    }

    // Thymeleaf: Show login page
    @GetMapping("/loginPage")
    public String showLoginPage(Model model) {
        model.addAttribute("authRequest", new AuthRequest());
        return "login"; // maps to login.html
    }

    // Thymeleaf: Handle login form
    @PostMapping("/loginUser")
    public String loginUser(@ModelAttribute("authRequest") AuthRequest authRequest, Model model, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );
        if (authentication.isAuthenticated()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String remoteUser = (auth != null && auth.isAuthenticated()) ? auth.getName() : null;
            model.addAttribute("remoteUser", remoteUser);
            return "redirect:/";
        } else {
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }
    }
}
