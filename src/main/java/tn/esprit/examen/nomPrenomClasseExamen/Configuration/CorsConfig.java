package tn.esprit.examen.nomPrenomClasseExamen.Configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class
CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200") // Remplacez ceci par l'URL de votre frontend
                .allowedMethods("*") // Autorisez toutes les méthodes HTTP
                .allowedHeaders("*") // Allow all headers

                .allowCredentials(true); // Autorise les credentials (cookies, autorisation)
    }
}

