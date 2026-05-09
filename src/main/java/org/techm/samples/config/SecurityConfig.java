package org.techm.samples.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.techm.samples.filter.JwtAuthFilter;
import org.techm.samples.service.auth.CustomOidcUserService;
import org.techm.samples.service.auth.OAuth2LoginSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Autowired
    public SecurityConfig(
            @Lazy JwtAuthFilter jwtAuthFilter,
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            CustomOidcUserService customOidcUserService,
            OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler // Injected the new handler
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.customOidcUserService = customOidcUserService;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
    }

    @Bean
    @Lazy
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(cs -> cs.disable())
                // 1. Set session management to STATELESS
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 2. Permit all authentication-related endpoints and public views
                        .requestMatchers(
                                "/auth/**",
                                "/oauth2/**",
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/",
                                "/home",
                                "/product/**",
                                "/products/**",
                                "/category/**",
                                "/chatbot/**",
                                "/api/reviews/product/**"
                        ).permitAll()
                        .requestMatchers("/wishlist/**", "/admin/**", "/products/wishlist/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/reviews/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/reviews/**").authenticated()
                        // 3. Optional: permit any request that falls through (or restrict to known)
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint("/auth/loginPage"))
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // 4. Configure OAuth2 to use the custom success handler
                .oauth2Login(oauth -> oauth
                        .loginPage("/auth/loginPage")
                        .userInfoEndpoint(u -> u.oidcUserService(customOidcUserService))
                        .successHandler(oAuth2LoginSuccessHandler) // This will handle JWT creation
                )
                // 5. Update logout to clear the JWT cookie
                .logout(lo -> lo
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/auth/loginPage")
                        .deleteCookies("jwt-token") // Important: clear the cookie
                        .permitAll()
                );

        // 6. Removed the .formLogin() configuration
        return http.build();
    }

    @Bean
    @Lazy
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider prov = new DaoAuthenticationProvider();
        prov.setUserDetailsService(userDetailsService);
        prov.setPasswordEncoder(passwordEncoder);
        return prov;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

}
