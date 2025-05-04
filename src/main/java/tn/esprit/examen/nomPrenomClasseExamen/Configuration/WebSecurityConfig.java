package tn.esprit.examen.nomPrenomClasseExamen.Configuration;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.AuthTokenFilter;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.CustomAccessDeniedHandler;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.CustomAuthenticationSuccessHandler;
import tn.esprit.examen.nomPrenomClasseExamen.services.SubscriberDetailsServiceImpl;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class WebSecurityConfig {

    private final AuthTokenFilter authTokenFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final SubscriberDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/ws/**").permitAll() // Allow WebSocket connections
                        .requestMatchers("/topic/**").permitAll()
                        .requestMatchers("/assets/**").permitAll()
                        .requestMatchers("/queue/**").permitAll()
                        .requestMatchers("/app/**").permitAll()
                        .requestMatchers("/user/**").permitAll()
                        .requestMatchers("/protected/files/**").authenticated()
                        .requestMatchers("/error").permitAll()
                        // Add these specific rules for partnership endpoints
                        .requestMatchers(HttpMethod.POST, "/api/associations/*/partners/*").hasAnyRole("ASSOCIATION_MEMBER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/associations/*/partners").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/associations/*/partners/*").hasAnyRole("ASSOCIATION_MEMBER", "ADMIN")
                        // Donation endpoints
                        .requestMatchers(HttpMethod.GET,
                                "/api/donations/**",
                                "/api/donations/getall",
                                "/api/donations/sorted-active",
                                "/api/donations/get/**",
                                "/api/donations/find/{donationType}",
                                "/api/paiments/cagnotte/**",
                                "/api/paiments/cagnotte/suggestions/",
                                "/api/paiments/spin/**",
                                "/api/dons/receipt/",
                                "/api/dons/receipt/**",

                                "/api/dons/get-dons/nonauth/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/dons/**",
                                "/api/paiments/create-payment-intent/**",
                                "/api/dons/update-dons/nonauth/**"

                        ).permitAll()
                        .requestMatchers(HttpMethod.PUT,

                                "/api/dons/update-dons/nonauth/**"

                        ).permitAll()



                        // Other existing rules
                        .requestMatchers(HttpMethod.PUT, "/api/associations/*/verify").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/associations").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/associations/list").permitAll()


                        .anyRequest().authenticated()
                )
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // CORS Configuration
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList("http://localhost:4200")); // Allow your frontend origin
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS","PATCH")); // Allow necessary methods
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept")); // Allow necessary headers
        config.setAllowCredentials(true); // Allow credentials (e.g., cookies, authorization headers)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // Apply to all endpoints
        return source;
    }

}
