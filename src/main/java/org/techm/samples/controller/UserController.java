package org.techm.samples.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.techm.samples.dto.AuthRequest;
import org.techm.samples.entity.Role;
import org.techm.samples.entity.User;
import org.techm.samples.exception.DuplicateUserException;
import org.techm.samples.service.auth.JwtService;
import org.techm.samples.service.auth.UserInfoService;

import java.util.Map;

@Controller
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private UserInfoService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/registerPage")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/registerUser")
    public String registerUser(@ModelAttribute("user") User user, @RequestParam("confirmPassword") String confirmPassword, Model model) {
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Password and Confirm Password do not match.");
            return "register";
        }
        try {
            user.setRole(Role.CUSTOMER);
            service.addUser(user);
            return "redirect:/auth/loginPage?registration_success";
        } catch (DuplicateUserException e) {
            model.addAttribute("errorMessage", "A user with email " + user.getEmail() + " already exists.");
            return "register";
        }
    }

    @GetMapping("/loginPage")
    public String showLoginPage(Model model) {
        model.addAttribute("authRequest", new AuthRequest());
        return "login";
    }

    // This endpoint now handles the login and returns a JWT in the response body.
    @PostMapping("/loginUser")
    @ResponseBody // This annotation is crucial for returning a JSON response
    public ResponseEntity<?> loginUser(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
            if (authentication.isAuthenticated()) {
                String token = jwtService.generateToken(authRequest.getUsername());
                // Return the token in a JSON object
                return ResponseEntity.ok(Map.of("token", token));
            } else {
                // This case is unlikely if authenticate() doesn't throw an exception, but included for completeness
                return ResponseEntity.status(401).body(Map.of("error", "Invalid Credentials"));
            }
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }
    }
}
