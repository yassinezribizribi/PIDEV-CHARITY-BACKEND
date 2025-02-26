package tn.esprit.examen.nomPrenomClasseExamen.Configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Remplacer 'allowedOrigins("*")' par 'allowedOriginPatterns' pour permettre les credentials.
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200") // ✅ Utilisation de allowedOriginPatterns pour spécifier les origines autorisées
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
