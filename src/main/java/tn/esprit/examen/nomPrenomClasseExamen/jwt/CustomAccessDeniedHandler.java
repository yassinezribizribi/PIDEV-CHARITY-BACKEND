package tn.esprit.examen.nomPrenomClasseExamen.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.Logger;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    // Utilisation de java.util.logging.Logger
    private static final Logger logger = Logger.getLogger(CustomAccessDeniedHandler.class.getName());

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpStatus.FORBIDDEN.value());

        // Log de l'erreur avec java.util.logging.Logger
        logger.severe("🔴 Accès refusé pour la requête : " + request.getRequestURI());
        logger.severe("Détails de l'exception : " + accessDeniedException.getMessage());

        // Message d'erreur personnalisé
        String message = "Access denied: You don't have sufficient privileges to access this resource.";

        // Convertit l'erreur en format JSON
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(message);

        // Écrit la réponse JSON pour le client
        response.getWriter().write(json);
    }
}