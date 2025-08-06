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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.techm.samples.filter.JwtAuthFilter;
import org.techm.samples.repository.UserInfoRepository;
import org.techm.samples.config.OAuth2LoginSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final UserInfoRepository userRepository;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService;
    private final ClientRegistrationRepository clientRegRepo;

    @Autowired
    public SecurityConfig(
        @Lazy JwtAuthFilter jwtAuthFilter,
        UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder,
        UserInfoRepository userRepository,
        OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService,
        ClientRegistrationRepository clientRegRepo
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.customOAuth2UserService = customOAuth2UserService;
        this.clientRegRepo = clientRegRepo;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
          .csrf(cs -> cs.disable())
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

        	            .requestMatchers("/login/oauth2/**").permitAll()
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
        	            ).authenticated()

        	            
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
          .formLogin(fl -> fl
            .loginPage("/auth/loginPage")
            .loginProcessingUrl("/auth/loginUser")
            .defaultSuccessUrl("/", true)
            .permitAll()
          )
          .oauth2Login(oauth -> oauth
            .loginPage("/auth/loginPage")
            // Force Google to show account selector every time
            .authorizationEndpoint(endpoint -> endpoint
              .authorizationRequestResolver(
                customAuthRequestResolver(clientRegRepo)
              )
            )
            .userInfoEndpoint(u -> u
              .userService(customOAuth2UserService)
            )
            .successHandler(new OAuth2LoginSuccessHandler(userRepository))
          )
          .logout(lo -> lo
            .logoutUrl("/logout")
            .logoutSuccessUrl("/auth/loginPage")
            .permitAll()
          );

        return http.build();
    }

    @Bean
    public OAuth2AuthorizationRequestResolver customAuthRequestResolver(
            ClientRegistrationRepository repo) {
        DefaultOAuth2AuthorizationRequestResolver resolver =
            new DefaultOAuth2AuthorizationRequestResolver(
                repo, "/oauth2/authorization"
            );
        resolver.setAuthorizationRequestCustomizer(customizer ->
            customizer.additionalParameters(params ->
                params.put("prompt", "select_account")
            )
        );
        return resolver;
    }

    @Bean
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
