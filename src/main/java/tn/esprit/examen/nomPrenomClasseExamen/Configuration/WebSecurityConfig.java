package tn.esprit.examen.nomPrenomClasseExamen.Configuration;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import tn.esprit.examen.nomPrenomClasseExamen.jwt.AuthTokenFilter;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.CustomAccessDeniedHandler;
import tn.esprit.examen.nomPrenomClasseExamen.services.SubscriberDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private final AuthTokenFilter authTokenFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final SubscriberDetailsServiceImpl userDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

    public WebSecurityConfig(AuthTokenFilter authTokenFilter,
                             CustomAccessDeniedHandler customAccessDeniedHandler,
                             SubscriberDetailsServiceImpl userDetailsService) {
        this.authTokenFilter = authTokenFilter;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.disable()) // Désactive CORS si tu n’en as pas besoin
                .csrf(csrf -> csrf.disable()) // Désactive CSRF (utile pour API REST)
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers("/api/auth/signup", "/api/auth/signin", "/generate/**", "/reset-password/**").permitAll()
                            .anyRequest().authenticated(); // Toute autre requête nécessite une authentification
                })
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            logger.error("🔴 Accès refusé : URI = {}, Message = {}", request.getRequestURI(), accessDeniedException.getMessage());
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                        })
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class) // Ajouter le filtre JWT
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
}