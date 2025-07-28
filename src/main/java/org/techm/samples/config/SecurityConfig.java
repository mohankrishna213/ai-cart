package org.techm.samples.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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

          // 1. URI Authorization by Role
          .authorizeHttpRequests(auth -> auth

            // Public (login, register, token gen, static assets)
            .requestMatchers(
              "/auth/generateToken",
              "/auth/registerPage",
              "/auth/registerUser",
              "/auth/loginPage",
              "/auth/loginUser",
              "/css/**",
              "/js/**",
              "/images/**"
            ).permitAll()

            // Admin UI (Thymeleaf) and Admin APIs
            .requestMatchers("/products/admin/**").hasAuthority("ADMIN")
            .requestMatchers("/api/products/admin/**", "/api/categories/admin/**")
              .hasAuthority("ADMIN")

            // Customer UI ─ only logged-in CUSTOMERS can browse/search/detail/wishlist
            .requestMatchers(
              "/", "/home",
              "/category/**",
              "/product/**",
              "/products",
              "/products/list",
              "/products/search",
              "/products/price-range",
              "/products/top-rated",
              "/wishlist"
            ).hasAuthority("CUSTOMER")

            // API: Reviews (GET any authenticated, POST by CUSTOMER, DELETE by ADMIN or CUSTOMER)
            .requestMatchers(HttpMethod.GET,    "/api/reviews/product/**")
              .authenticated()
            .requestMatchers(HttpMethod.POST,   "/api/reviews/product/**")
              .hasAuthority("CUSTOMER")
            .requestMatchers(HttpMethod.DELETE, "/api/reviews/**")
              .hasAnyAuthority("ADMIN", "CUSTOMER")

            // Everything else requires authentication
            .anyRequest().authenticated()
          )

          // 2. Session management: allow sessions for form-login
          .sessionManagement(sess -> 
            sess.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
          )

          // 3. JWT filter for API calls
          .authenticationProvider(authenticationProvider())
          .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

          // 4. Form-based login for the UI
          .formLogin(login -> login
              .loginPage("/auth/loginPage")
              .loginProcessingUrl("/auth/loginUser")
              .defaultSuccessUrl("/", true)
              .permitAll()
          )

          // 5. Logout
          .logout(logout -> logout
              .logoutUrl("/logout")
              .logoutSuccessUrl("/auth/loginPage")
              .permitAll()
          );

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
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
