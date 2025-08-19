package org.techm.samples.service.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String email = oidcUser.getAttribute("email");

        // Generate the JWT for the OIDC user
        String token = jwtService.generateToken(email);

        // Create a secure, HttpOnly cookie to store the JWT
        Cookie cookie = new Cookie("jwt-token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure()); // Use 'true' in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // Set cookie expiry to 1 hour

        // Add the cookie to the response
        response.addCookie(cookie);

        // Redirect the user to the home page
        response.sendRedirect("/");
    }
}
