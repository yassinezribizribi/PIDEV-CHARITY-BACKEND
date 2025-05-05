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
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
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
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable()) // 🔥 Corrigé ici : disable() pour autoriser iframe
                )
                .authorizeHttpRequests(auth -> auth
                        // 📂 Public endpoints
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/translate").permitAll()

                        // 📂 Public Testimonials
                        .requestMatchers(HttpMethod.GET, "/api/testimonials/all").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/testimonials/images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/testimonials/media/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/testimonials/search/**").permitAll()

                        // 📂 Healthcare (public report access)
                        .requestMatchers(HttpMethod.GET, "/api/healthcare/export").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/healthcare/report-url").permitAll()
                        .requestMatchers(HttpMethod.GET, "/report/**").permitAll() // 🔥 PDFs accessibles sans token

                        // 🔒 Secure Testimonials
                        .requestMatchers(HttpMethod.POST, "/api/testimonials/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/testimonials/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/testimonials/**").authenticated()

                        // 🔒 Associations & Donations secured
                        .requestMatchers(HttpMethod.POST, "/api/associations/*/partners/*").hasAnyRole("ASSOCIATION_MEMBER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/associations/*/partners").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/associations/*/partners/*").hasAnyRole("ASSOCIATION_MEMBER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/associations/*/verify").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/associations").permitAll()

                        // 📂 Public Donations
                        .requestMatchers(HttpMethod.GET,
                                "/api/donations/**",
                                "/api/donations/getall",
                                "/api/donations/get/**",
                                "/api/donations/find/{donationType}").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/dons/**",
                                "/api/paiments/create-payment-intent/**").permitAll()

                        // 🔒 All Other Requests
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
    public HttpFirewall allowUrlEncodedDoubleSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedDoubleSlash(true);
        firewall.setAllowBackSlash(true);
        firewall.setAllowUrlEncodedPercent(true);
        return firewall;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(HttpFirewall firewall) {
        return (web) -> web.httpFirewall(firewall);
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
