package org.techm.samples.config;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.techm.samples.entity.Role;
import org.techm.samples.entity.User;
import org.techm.samples.repository.UserInfoRepository;

public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserInfoRepository userRepo;

    public OAuth2LoginSuccessHandler(UserInfoRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {

        if (authentication instanceof OAuth2AuthenticationToken) {
            var oauthToken = (OAuth2AuthenticationToken) authentication;
            var oauthUser  = oauthToken.getPrincipal();

            String email = oauthUser.getAttribute("email");
            String name  = oauthUser.getAttribute("name");

            // Ensure registration if it didn't happen in the service
            userRepo.findByEmail(email).orElseGet(() -> {
                User u = new User();
                u.setUsername(name);
                u.setEmail(email);
                u.setPassword("OAUTH2_USER");
                u.setRole(Role.CUSTOMER);
                return userRepo.save(u);
            });
        }

        // Always redirect to home
        response.sendRedirect("/");
    }
}
