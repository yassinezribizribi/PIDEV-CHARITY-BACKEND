package tn.esprit.examen.nomPrenomClasseExamen.Configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(
                        "http://localhost:4200",
                        "https://datastudio.google.com",
                        "https://lookerstudio.googleusercontent.com"
                )
                .allowedMethods("*")               // Autorise toutes les méthodes HTTP
                .allowedHeaders("*")              // Autorise tous les headers
                .exposedHeaders("Authorization")  // Expose le header Authorization dans les réponses
                .allowCredentials(true);          // Autorise les cookies/headers sécurisés
    }
}
