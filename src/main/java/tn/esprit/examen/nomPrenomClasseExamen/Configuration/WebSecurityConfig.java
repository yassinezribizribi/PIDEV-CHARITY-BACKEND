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
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.AuthTokenFilter;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.CustomAccessDeniedHandler;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.CustomAuthenticationSuccessHandler;
import tn.esprit.examen.nomPrenomClasseExamen.services.SubscriberDetailsServiceImpl;

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
    private final SubscriberDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
                .authorizeHttpRequests(auth -> auth
                        // Public WebSocket and static resources
                        .requestMatchers("/ws/**", "/topic/**", "/assets/**", "/queue/**", "/app/**", "/user/**").permitAll()

                        // Auth routes
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**", "/error").permitAll()

                        // Translation & Report
                        .requestMatchers("/api/translate", "/report/**").permitAll()

                        // Public testimonials
                        .requestMatchers(HttpMethod.GET,
                                "/api/testimonials/all",
                                "/api/testimonials/images/**",
                                "/api/testimonials/media/**",
                                "/api/testimonials/search/**").permitAll()

                        // Public healthcare reports
                        .requestMatchers(HttpMethod.GET,
                                "/api/healthcare/export",
                                "/api/healthcare/report-url").permitAll()

                        // Public associations & donations
                        .requestMatchers(HttpMethod.POST, "/api/associations", "/api/associations/list").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/donations/**",
                                "/api/donations/getall",
                                "/api/donations/sorted-active",
                                "/api/donations/get/**",
                                "/api/donations/find/**",
                                "/api/paiments/cagnotte/**",
                                "/api/paiments/cagnotte/suggestions/**",
                                "/api/paiments/spin/**",
                                "/api/dons/get-dons/nonauth/**",
                                "/api/dons/receipt/**").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/paiments/create-payment-intent/**",
                                "/api/dons/**",
                                "/api/dons/update-dons/nonauth/**").permitAll()
                        .requestMatchers(HttpMethod.PUT,
                                "/api/dons/update-dons/nonauth/**").permitAll()

                        // Secured associations
                        .requestMatchers(HttpMethod.POST, "/api/associations/*/partners/*").hasAnyRole("ASSOCIATION_MEMBER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/associations/*/partners").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/associations/*/partners/*").hasAnyRole("ASSOCIATION_MEMBER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/associations/*/verify").hasRole("ADMIN")

                        // Protected files
                        .requestMatchers("/protected/files/**").authenticated()

                        // All other routes
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
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
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public HttpFirewall allowUrlEncodedDoubleSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedDoubleSlash(true);
        firewall.setAllowBackSlash(true);
        firewall.setAllowUrlEncodedPercent(true);
        return firewall;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:4200",
                "https://datastudio.google.com",
                "https://lookerstudio.googleusercontent.com"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
