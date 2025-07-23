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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(@Lazy JwtAuthFilter jwtAuthFilter,
                          UserDetailsService userDetailsService,
                          PasswordEncoder passwordEncoder) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/auth/generateToken",
                    "/auth/registerPage",
                    "/auth/registerUser",
                    "/auth/loginPage",
                    "/auth/loginUser",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/products",
                    "/products/",
                    "/products/list",
                    "/products/search",
                    "/products/category/**",
                    "/products/{id}",
                    "/api/products",
                    "/api/products/search",
                    "/api/products/{id}",
                    "/api/categories"

                ).permitAll()

                // Admin-only endpoints
                .requestMatchers(
                    "/products/admin",
                    "/products/admin/new",
                    "/products/admin/{id}/edit",
                    "/products/admin/{id}",
                    "/products/admin/{id}/delete",
                    "/api/products/admin/**",
                    "/api/categories/admin",
                    "/api/categories/admin/{id}"
                ).hasAuthority("ADMIN")

                // Customer-only endpoints
                .requestMatchers("/auth/user/**").hasAuthority("CUSTOMER")

                // Review endpoints
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/reviews/product/{productId}").authenticated()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/reviews/product/{productId}").hasAuthority("CUSTOMER")
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/reviews/{reviewId}").hasAnyAuthority("ADMIN", "CUSTOMER")

                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
