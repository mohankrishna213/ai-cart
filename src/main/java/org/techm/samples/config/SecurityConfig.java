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

       
          .authorizeHttpRequests(auth -> auth

            .requestMatchers(
              "/auth/generateToken",
              "/auth/registerPage",
              "/auth/registerUser",
              "/auth/loginPage",
              "/auth/loginUser",
              "/css/**",
              "/js/**",
              "/images/**",
              "/api/users/**"
            ).permitAll()

            
            .requestMatchers("/products/admin/**").hasAuthority("ADMIN")
            .requestMatchers("/api/products/admin/**", "/api/categories/admin/**")
              .hasAuthority("ADMIN")

           
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
            ).hasAnyAuthority("ADMIN", "CUSTOMER")

            
            .requestMatchers(HttpMethod.GET,    "/api/reviews/product/**")
              .authenticated()
            .requestMatchers(HttpMethod.POST,   "/api/reviews/product/**")
              .hasAuthority("CUSTOMER")
            .requestMatchers(HttpMethod.DELETE, "/api/reviews/**")
              .hasAnyAuthority("ADMIN", "CUSTOMER")

           
            .anyRequest().authenticated()
          )

          
          .sessionManagement(sess -> 
            sess.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
          )

          
          .authenticationProvider(authenticationProvider())
          .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

          
          .formLogin(login -> login
              .loginPage("/auth/loginPage")
              .loginProcessingUrl("/auth/loginUser")
              .defaultSuccessUrl("/", true)
              .permitAll()
          )

         
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
