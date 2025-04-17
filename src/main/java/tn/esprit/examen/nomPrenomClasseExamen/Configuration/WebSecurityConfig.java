package tn.esprit.examen.nomPrenomClasseExamen.Configuration;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class WebSecurityConfig {

    private final AuthTokenFilter authTokenFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public Endpoints
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Testimonial media/images
                        .requestMatchers("/api/testimonials/images/**").permitAll()
                        .requestMatchers("/api/testimonials/media/**").permitAll()

                        // Testimonial restricted endpoints
                        .requestMatchers(HttpMethod.GET, "/api/testimonials/all").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/testimonials").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/testimonials/media").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/testimonials/{id}/like").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/testimonials/{id}").authenticated()

                        // Association and Donation
                        .requestMatchers(HttpMethod.POST, "/api/associations/*/partners/*").hasAnyRole("ASSOCIATION_MEMBER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/associations/*/partners").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/associations/*/partners/*").hasAnyRole("ASSOCIATION_MEMBER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/associations/*/verify").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/associations").permitAll()

                        // Donations
                        .requestMatchers(HttpMethod.GET,
                                "/api/donations/**",
                                "/api/donations/getall",
                                "/api/donations/get/**",
                                "/api/donations/find/{donationType}").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/dons/**",
                                "/api/paiments/create-payment-intent/**").permitAll()

                        // Catch-all: authenticated required
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
