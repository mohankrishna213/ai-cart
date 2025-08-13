package org.techm.samples.service.auth;

import java.util.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.*;
import org.springframework.stereotype.Service;
import org.techm.samples.entity.Role;
import org.techm.samples.entity.User;
import org.techm.samples.repository.UserInfoRepository;

import jakarta.transaction.Transactional;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final UserInfoRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public CustomOidcUserService(UserInfoRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder=passwordEncoder;
    }

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest)
            throws OAuth2AuthenticationException {

        // 1) Delegate to Spring’s default OidcUserService
        OidcUser oidcUser = super.loadUser(userRequest);

        // 2) Lookup/create user & map Role → GrantedAuthority
        String email = oidcUser.getAttribute("email");
        String name  = oidcUser.getAttribute("name");

        User user = userRepo.findByEmail(email)
            .orElseGet(() -> {
                User u = new User();
                u.setEmail(email);
                u.setUsername(name);
                u.setPassword(passwordEncoder.encode("customer@"+name));
                u.setRole(Role.CUSTOMER);
                return userRepo.saveAndFlush(u);
            });

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole().name()));
        authorities.addAll(oidcUser.getAuthorities());

        // 3) Re‐wrap into a new DefaultOidcUser so claims & ID token persist
        return new DefaultOidcUser(
            authorities,
            userRequest.getIdToken(),
            oidcUser.getUserInfo()
        );
    }
}
