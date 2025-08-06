package org.techm.samples.service.auth;

import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.techm.samples.entity.Role;
import org.techm.samples.entity.User;
import org.techm.samples.repository.UserInfoRepository;

import jakarta.transaction.Transactional;

@Service
public class CustomOAuth2UserService
    implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserInfoRepository userRepo;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    public CustomOAuth2UserService(UserInfoRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        // 1) Fetch Google attributes
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        String email = oauth2User.getAttribute("email");
        String name  = oauth2User.getAttribute("name");

        // 2) Lookup or create & flush new user
        User user = userRepo.findByEmail(email)
            .orElseGet(() -> {
                User u = new User();
                u.setUsername(name);
                u.setEmail(email);
                u.setPassword("OAUTH2_USER");
                u.setRole(Role.CUSTOMER);
                // saveAndFlush ensures immediate write
                return userRepo.saveAndFlush(u);
            });

        // 3) Map Role → Authority and wrap attributes
        GrantedAuthority authority =
            new SimpleGrantedAuthority(user.getRole().name());

        String userNameAttr =
            userRequest.getClientRegistration()
                       .getProviderDetails()
                       .getUserInfoEndpoint()
                       .getUserNameAttributeName();

        return new DefaultOAuth2User(
            Collections.singleton(authority),
            oauth2User.getAttributes(),
            userNameAttr
        );
    }
}
